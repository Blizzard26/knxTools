package org.knx;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Unmarshaller.Listener;

import org.knx.xml.BaseClass;
import org.knx.xml.KNX;
import org.knx.xml.KnxApplicationProgramT;
import org.knx.xml.KnxManufacturerDataT;
import org.knx.xml.KnxManufacturerDataT.KnxManufacturer;
import org.knx.xml.KnxManufacturerDataT.KnxManufacturer.KnxApplicationPrograms;
import org.knx.xml.KnxManufacturerDataT.KnxManufacturer.KnxBaggages;
import org.knx.xml.KnxManufacturerDataT.KnxManufacturer.KnxCatalog;
import org.knx.xml.KnxManufacturerDataT.KnxManufacturer.KnxHardware;
import org.knx.xml.KnxProjectT;
import org.knx.xml.KnxProjectT.KnxInstallations;
import org.knx.xml.KnxProjectT.KnxInstallations.KnxInstallation;
import org.slf4j.LoggerFactory;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.util.FileUtils;

public class ETSLoader {
	
	
	protected final org.slf4j.Logger LOG = LoggerFactory.getLogger(this.getClass());

	private static final String TEMP_FILE_PREFIX = "knx-project";
	private static final String ZIP_EXTENSION = "zip";

	private static final String MASTER_DATA_FILE = "knx_master.xml";

	private static final String MANUFACTURER_DIR_PREFIX = "M-";
	private static final String BAGGAGES_FILE = "Baggages.xml";
	private static final String HARDWARE_FILE = "Hardware.xml";
	private static final String CATALOG_FILE = "Catalog.xml";

	private static final String PROJECT_ZIP_FILE_PREFIX = "P-";
	private static final String MAIN_PROJECT_FILE = "project.xml";

	public KNX load(File file, Optional<String> password) throws ETSLoaderException {
		ZipFile projectZipFile = new ZipFile(file);

		LookupIdResolver idResolver = new LookupIdResolver();

		LOG.debug("Loading Master Data");

		KNX knx = loadMasterData(projectZipFile, idResolver);

		LOG.debug("Loading Manufacturer Data");

		KnxManufacturerDataT manufacturerData = new KnxManufacturerDataT();
		loadManufacturerData(projectZipFile, manufacturerData, idResolver);
		reconnect(manufacturerData, knx, KNX::setManufacturerData, idResolver);

		LOG.debug("Loading Projects");

		loadProjects(password, projectZipFile, knx, idResolver);

		return knx;
	}

	private <T extends BaseClass, P extends BaseClass> void reconnect(T element, P newParent,
			BiConsumer<P, T> adder, LookupIdResolver idResolver) {
		if (element.getParent() != null) {
			idResolver.purgeSuperHierarchy(element.getParent());
		}
		adder.accept(newParent, element);
		element.setParent(newParent);
	}

	/**
	 * Load manufacturer data from Zip file
	 * 
	 * @param projectZipFile
	 * @param manufacturerData 
	 * @param idLookupMap
	 * @return
	 */
	protected void loadManufacturerData(ZipFile projectZipFile, KnxManufacturerDataT manufacturerData, LookupIdResolver idResolver) {

		try {
			// Group files in Zip by manufacturer directory name
			Map<String, List<FileHeader>> directories = projectZipFile.getFileHeaders().stream()
					.filter(h -> h.getFileName().indexOf('/') > 0)
					.filter(h -> h.getFileName().startsWith(MANUFACTURER_DIR_PREFIX))
					.collect(Collectors.groupingBy(h -> h.getFileName().substring(0, h.getFileName().indexOf('/'))));

			// Load manufacturers
			directories.values().stream().forEach(d -> loadManufacturer(projectZipFile, d, manufacturerData, idResolver));
		} catch (ZipException e) {
			throw new ETSLoaderException(e);
		}
	}

	protected void loadManufacturer(ZipFile projectZipFile, List<FileHeader> directory,
			KnxManufacturerDataT manufacturerData, LookupIdResolver idResolver) {
		KnxManufacturer manufacturer = new KnxManufacturer();

		// Extract manufacturer ID from directory name
		String fileName = directory.get(0).getFileName();
		String manufacturerId = fileName.substring(0, fileName.indexOf('/'));
		manufacturer.setRefId(manufacturerId);

		directory.stream().filter(d -> d.getFileName().endsWith(BAGGAGES_FILE))
				.findAny().ifPresent(h -> loadBaggage(projectZipFile, h, manufacturer, idResolver));

		KnxApplicationPrograms knxApplicationPrograms = new KnxApplicationPrograms();
		directory.stream()
				.filter(d -> d.getFileName().startsWith(MANUFACTURER_DIR_PREFIX, d.getFileName().indexOf('/') + 1))
				.forEach(d -> loadApplicationProgram(projectZipFile, d, knxApplicationPrograms, idResolver));
		reconnect(knxApplicationPrograms, manufacturer, KnxManufacturer::setApplicationPrograms, idResolver);
		

		directory.stream().filter(d -> d.getFileName().endsWith(HARDWARE_FILE))
				.findAny().ifPresent(h -> loadHardware(projectZipFile, h, manufacturer, idResolver));
		
		directory.stream().filter(d -> d.getFileName().endsWith(CATALOG_FILE))
				.findAny().ifPresent(h -> loadCatalog(projectZipFile, h, manufacturer, idResolver));

		reconnect(manufacturer, manufacturerData, (p, e) -> p.getManufacturer().add(e), idResolver);
	}

	protected void loadBaggage(ZipFile zipFile, FileHeader fileHeader, KnxManufacturer manufacturer, LookupIdResolver idResolver) {
		try (InputStream stream = getInputStreamFromZip(zipFile, fileHeader)) {
			KNX knx = loadKnxFromInputStream(stream, idResolver, null);
			KnxBaggages baggages = knx.getManufacturerData().getManufacturer().get(0).getBaggages();
			reconnect(baggages, manufacturer, KnxManufacturer::setBaggages, idResolver);
		} catch (IOException e) {
			throw new ETSLoaderException(e);
		}
	}

	protected void loadApplicationProgram(ZipFile zipFile, FileHeader fileHeader,
			KnxApplicationPrograms knxApplicationPrograms, LookupIdResolver idResolver) {
		try (InputStream stream = getInputStreamFromZip(zipFile, fileHeader)) {
			KNX knx = loadKnxFromInputStream(stream, idResolver, null);
			KnxApplicationProgramT knxApplicationProgramT = knx.getManufacturerData().getManufacturer().get(0).getApplicationPrograms().getApplicationProgram()
					.get(0);
			
			reconnect(knxApplicationProgramT, knxApplicationPrograms, (p, e) -> p.getApplicationProgram().add(e), idResolver);
		} catch (IOException e) {
			throw new ETSLoaderException(e);
		}
	}

	protected void loadHardware(ZipFile zipFile, FileHeader fileHeader, KnxManufacturer manufacturer, LookupIdResolver idResolver) {
		try (InputStream stream = getInputStreamFromZip(zipFile, fileHeader)) {
			KNX knx = loadKnxFromInputStream(stream, idResolver, null);
			KnxHardware hardware = knx.getManufacturerData().getManufacturer().get(0).getHardware();
			reconnect(hardware, manufacturer, KnxManufacturer::setHardware, idResolver);
		} catch (IOException e) {
			throw new ETSLoaderException(e);
		}
	}

	protected void loadCatalog(ZipFile zipFile, FileHeader fileHeader, KnxManufacturer manufacturer, LookupIdResolver idResolver) {
		try (InputStream stream = getInputStreamFromZip(zipFile, fileHeader)) {
			KNX knx = loadKnxFromInputStream(stream, idResolver, null);
			KnxCatalog catalog = knx.getManufacturerData().getManufacturer().get(0).getCatalog();
			reconnect(catalog, manufacturer, KnxManufacturer::setCatalog, idResolver);
		} catch (IOException e) {
			throw new ETSLoaderException(e);
		}
	}

	protected void loadProjects(Optional<String> password, ZipFile projectZipFile,
			KNX knx, LookupIdResolver idResolver) {

		try {
			projectZipFile.getFileHeaders().stream().filter(this::isProjectFile)
					.forEach(h -> loadProjectFile(projectZipFile, h, password, knx,  idResolver));
		} catch (ZipException e) {
			throw new ETSLoaderException(e);
		}

	}

	protected KNX loadMasterData(ZipFile projectZipFile, LookupIdResolver idResolver) throws ETSLoaderException {
		try {
			FileHeader masterDataFileHeader = projectZipFile.getFileHeader(MASTER_DATA_FILE);
			try (InputStream masterDataFileStream = getInputStreamFromZip(projectZipFile, masterDataFileHeader)) {
				return loadKnxFromInputStream(masterDataFileStream, idResolver, null);
			}
		} catch (IOException e) {
			throw new ETSLoaderException(e);
		}
	}

	protected void loadProjectFile(ZipFile zipFile, FileHeader fileHeader, Optional<String> password,
			KNX knx, LookupIdResolver idResolver) throws ETSLoaderException {
		try (InputStream stream = getInputStreamFromZip(zipFile, fileHeader)) {
			Path tempFile = Files.createTempFile(TEMP_FILE_PREFIX, "." + ZIP_EXTENSION);
			tempFile.toFile().deleteOnExit();

			Files.copy(stream, tempFile, StandardCopyOption.REPLACE_EXISTING);
			ZipFile projectZipFile = new ZipFile(tempFile.toFile(), password.map(String::toCharArray).orElse(null));

			KnxInstallations installations = new KnxInstallations();
			projectZipFile.getFileHeaders().stream()
					.filter(h -> isInstallationFile(h)).sorted((o1, o2) -> o1.getFileName().compareTo(o2.getFileName()))
					.forEach(h -> loadInstallationFile(projectZipFile, h, installations, idResolver));
			

			FileHeader projectFileHeader = projectZipFile.getFileHeader(MAIN_PROJECT_FILE);
			KNX knxProject;
			try (ZipInputStream inputStream = getInputStreamFromZip(projectZipFile, projectFileHeader)) {
				knxProject = loadKnxFromInputStream(inputStream, idResolver, null);
			}
			KnxProjectT project = knxProject.getProject().get(0);
			reconnect(project, knx, (p, e) -> p.getProject().add(e), idResolver);

			reconnect(installations, project, KnxProjectT::setInstallations, idResolver);
		} catch (IOException e) {
			throw new ETSLoaderException(e);
		}
	}

	protected void loadInstallationFile(ZipFile zipFile, FileHeader fileHeader,
			KnxInstallations installations, LookupIdResolver idResolver) {
		try (InputStream stream = getInputStreamFromZip(zipFile, fileHeader)) {
			int installationId = Integer.parseInt(FileUtils.getFileNameWithoutExtension(fileHeader.getFileName()));
			KNX knxFile = loadKnxFromInputStream(stream, idResolver, (knx) -> knx.getProject().get(0).getInstallations()
					.getInstallation().get(0).setInstallationId(installationId));
			KnxInstallation installation = knxFile.getProject().get(0).getInstallations().getInstallation().get(0);
			reconnect(installation, installations, (p, e) -> p.getInstallation().add(e), idResolver);
		} catch (IOException e) {
			throw new ETSLoaderException(e);
		}
	}

	protected ZipInputStream getInputStreamFromZip(ZipFile zipFile, FileHeader fileHeader) throws IOException {
		return zipFile.getInputStream(fileHeader);
	}

	protected KNX loadKnxFromInputStream(InputStream inputStream, LookupIdResolver idResolver,
			Consumer<KNX> postProcessing) throws ETSLoaderException {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(KNX.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			jaxbUnmarshaller.setProperty(com.sun.xml.bind.IDResolver.class.getName(), idResolver);
			jaxbUnmarshaller.setListener(new Listener() {

				@Override
				public void afterUnmarshal(Object target, Object parent) {

					((BaseClass) target).setParent((BaseClass) parent);

					super.afterUnmarshal(target, parent);
				}

			});

			KNX knx = (KNX) jaxbUnmarshaller.unmarshal(inputStream);

			if (postProcessing != null) {
				postProcessing.accept(knx);
			}

			idResolver.resolveProxies(knx);

			return knx;
		} catch (JAXBException e) {
			throw new ETSLoaderException(e);
		}
	}

	protected boolean isInstallationFile(FileHeader h) {
		String fileName = FileUtils.getFileNameWithoutExtension(h.getFileName());
		try {
			Integer.parseInt(fileName);
			return true;
		} catch (@SuppressWarnings("unused") NumberFormatException e) {
			return false;
		}
	}

	protected boolean isProjectFile(FileHeader p) {
		return p.getFileName().startsWith(PROJECT_ZIP_FILE_PREFIX) && p.getFileName().endsWith("." + ZIP_EXTENSION);
	}

}
