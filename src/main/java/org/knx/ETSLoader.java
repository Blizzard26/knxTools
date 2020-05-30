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
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.knx.xml.KNX;
import org.knx.xml.KnxApplicationProgramT;
import org.knx.xml.KnxManufacturerDataT;
import org.knx.xml.KnxManufacturerDataT.KnxManufacturer;
import org.knx.xml.KnxManufacturerDataT.KnxManufacturer.KnxApplicationPrograms;
import org.knx.xml.KnxManufacturerDataT.KnxManufacturer.KnxCatalog;
import org.knx.xml.KnxManufacturerDataT.KnxManufacturer.KnxHardware;
import org.knx.xml.KnxProjectT;
import org.knx.xml.KnxProjectT.KnxInstallations;
import org.knx.xml.KnxProjectT.KnxInstallations.KnxInstallation;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.util.FileUtils;

public class ETSLoader {

	private static final String TEMP_FILE_PREFIX = "knx-project";
	private static final String ZIP_EXTENSION = "zip";

	private static final String MASTER_DATA_FILE = "knx_master.xml";

	private static final String HARDWARE_FILE = "Hardware.xml";
	private static final String MANUFACTURER_DIR_PREFIX = "M-";
	private static final String CATALOG_FILE = "Catalog.xml";

	private static final String PROJECT_ZIP_FILE_PREFIX = "P-";
	private static final String MAIN_PROJECT_FILE = "project.xml";

	public KNX load(File file, Optional<String> password) throws ETSLoaderException {
		return load(file, password, k -> k);
	}
	
	public <T> T load(File file, Optional<String> password, java.util.function.Function<KNX, T> mappingFunction) throws ETSLoaderException {
		ZipFile projectZipFile = new ZipFile(file);

		KNX knx = loadMasterData(projectZipFile);

		KnxManufacturerDataT manufacturerData = new KnxManufacturerDataT();
		List<KnxManufacturer> manufacturers = loadManufacturerData(projectZipFile);
		manufacturerData.getManufacturer().addAll(manufacturers);
		knx.setManufacturerData(manufacturerData);

		List<KnxProjectT> projects = loadProjects(password, projectZipFile);
		knx.getProject().addAll(projects);

		return mappingFunction.apply(knx);
	}

	/**
	 * Load manufacturer data from Zip file
	 * 
	 * @param projectZipFile
	 * @return
	 */
	protected List<KnxManufacturer> loadManufacturerData(ZipFile projectZipFile) {

		try {
			// Group files in Zip by manufacturer directory name
			Map<String, List<FileHeader>> directories = projectZipFile.getFileHeaders().stream()
					.filter(h -> h.getFileName().indexOf('/') > 0)
					.filter(h -> h.getFileName().startsWith(MANUFACTURER_DIR_PREFIX))
					.collect(Collectors.groupingBy(h -> h.getFileName().substring(0, h.getFileName().indexOf('/'))));

			// Load manufacturers
			return directories.values().stream().map(d -> loadManufacturer(projectZipFile, d))
					.collect(Collectors.toList());
		} catch (ZipException e) {
			throw new ETSLoaderException(e);
		}
	}

	protected KnxManufacturer loadManufacturer(ZipFile projectZipFile, List<FileHeader> directory) {
		KnxManufacturer manufacturer = new KnxManufacturer();

		// Extract manufacturer ID from directory name
		String fileName = directory.get(0).getFileName();
		String manufacturerId = fileName.substring(0, fileName.indexOf('/'));
		manufacturer.setRefId(manufacturerId);

		Optional<FileHeader> catalogFile = directory.stream().filter(d -> d.getFileName().endsWith(CATALOG_FILE))
				.findAny();
		Optional<KnxCatalog> catalog = catalogFile.map(h -> loadCatalog(projectZipFile, h));
		catalog.ifPresent(c -> manufacturer.setCatalog(c));

		Optional<FileHeader> hardwareFile = directory.stream().filter(d -> d.getFileName().endsWith(HARDWARE_FILE))
				.findAny();
		Optional<KnxHardware> hardware = hardwareFile.map(h -> loadHardware(projectZipFile, h));
		hardware.ifPresent(h -> manufacturer.setHardware(h));

		List<KnxApplicationProgramT> applicationProgramms = directory.stream()
				.filter(d -> d.getFileName().startsWith(MANUFACTURER_DIR_PREFIX, d.getFileName().indexOf('/') + 1))
				.map(d -> loadApplicationProgram(projectZipFile, d)).collect(Collectors.toList());

		manufacturer.setApplicationPrograms(new KnxApplicationPrograms());
		manufacturer.getApplicationPrograms().getApplicationProgram().addAll(applicationProgramms);

		return manufacturer;
	}

	protected KnxApplicationProgramT loadApplicationProgram(ZipFile zipFile, FileHeader fileHeader) {
		try (InputStream stream = getInputStreamFromZip(zipFile, fileHeader)) {
			KNX knx = loadKnxFromInputStream(stream);
			return knx.getManufacturerData().getManufacturer().get(0).getApplicationPrograms().getApplicationProgram()
					.get(0);
		} catch (IOException e) {
			throw new ETSLoaderException(e);
		}
	}

	protected KnxHardware loadHardware(ZipFile zipFile, FileHeader fileHeader) {
		try (InputStream stream = getInputStreamFromZip(zipFile, fileHeader)) {
			KNX knx = loadKnxFromInputStream(stream);
			return knx.getManufacturerData().getManufacturer().get(0).getHardware();
		} catch (IOException e) {
			throw new ETSLoaderException(e);
		}
	}

	protected KnxCatalog loadCatalog(ZipFile zipFile, FileHeader fileHeader) {
		try (InputStream stream = getInputStreamFromZip(zipFile, fileHeader)) {
			KNX knx = loadKnxFromInputStream(stream);
			return knx.getManufacturerData().getManufacturer().get(0).getCatalog();
		} catch (IOException e) {
			throw new ETSLoaderException(e);
		}
	}

	protected List<KnxProjectT> loadProjects(Optional<String> password, ZipFile projectZipFile) {
		
		try {
			List<KnxProjectT> projects = projectZipFile.getFileHeaders().stream().filter(this::isProjectFile)
					.map(h -> loadProjectFile(projectZipFile, h, password)).collect(Collectors.toList());
			return projects;
		} catch (ZipException e) {
			throw new ETSLoaderException(e);
		}
		
	}

	protected KNX loadMasterData(ZipFile projectZipFile) throws ETSLoaderException {
		try {
			FileHeader masterDataFileHeader = projectZipFile.getFileHeader(MASTER_DATA_FILE);
			try (InputStream masterDataFileStream = getInputStreamFromZip(projectZipFile, masterDataFileHeader)) {
				return loadKnxFromInputStream(masterDataFileStream);
			}
		} catch (IOException e) {
			throw new ETSLoaderException(e);
		}
	}

	protected KnxProjectT loadProjectFile(ZipFile zipFile, FileHeader fileHeader, Optional<String> password)
			throws ETSLoaderException {
		try (InputStream stream = getInputStreamFromZip(zipFile, fileHeader)) {
			Path tempFile = Files.createTempFile(TEMP_FILE_PREFIX, "." + ZIP_EXTENSION);
			tempFile.toFile().deleteOnExit();

			Files.copy(stream, tempFile, StandardCopyOption.REPLACE_EXISTING);
			ZipFile projectZipFile = new ZipFile(tempFile.toFile(), password.map(String::toCharArray).orElse(null));

			FileHeader projectFileHeader = projectZipFile.getFileHeader(MAIN_PROJECT_FILE);
			KNX knxProject;
			try (ZipInputStream inputStream = getInputStreamFromZip(projectZipFile, projectFileHeader)) {
				knxProject = loadKnxFromInputStream(inputStream);
			}
			KnxProjectT knxProjectT = knxProject.getProject().get(0);
			KnxInstallations installations = new KnxInstallations();

			List<KnxInstallation> knxInstallations = projectZipFile.getFileHeaders().stream()
					.filter(h -> isInstallationFile(h)).sorted((o1, o2) -> o1.getFileName().compareTo(o2.getFileName()))
					.map(h -> loadInstallationFile(projectZipFile, h)).collect(Collectors.toList());

			installations.getInstallation().addAll(knxInstallations);
			knxProjectT.setInstallations(installations);

			return knxProjectT;
		} catch (IOException e) {
			throw new ETSLoaderException(e);
		}
	}

	protected KnxInstallation loadInstallationFile(ZipFile zipFile, FileHeader fileHeader) {
		try (InputStream stream = getInputStreamFromZip(zipFile, fileHeader)) {
			KNX knxFile = loadKnxFromInputStream(stream);
			String installationId = FileUtils.getFileNameWithoutExtension(fileHeader.getFileName());
			KnxInstallation installation = knxFile.getProject().get(0).getInstallations().getInstallation().get(0);
			installation.setInstallationId(Integer.valueOf(installationId));
			return installation;
		} catch (IOException e) {
			throw new ETSLoaderException(e);
		}
	}

	protected ZipInputStream getInputStreamFromZip(ZipFile zipFile, FileHeader fileHeader) throws IOException {
			return zipFile.getInputStream(fileHeader);
	}

	protected KNX loadKnxFromInputStream(InputStream inputStream) throws ETSLoaderException {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(KNX.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			return (KNX) jaxbUnmarshaller.unmarshal(inputStream);
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
