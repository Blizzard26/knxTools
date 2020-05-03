package org.openhab.support.knx2openhab.etsLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.Optional;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.knx.KNX;
import org.knx.KnxProjectT;
import org.knx.KnxProjectT.KnxInstallations;
import org.knx.KnxProjectT.KnxInstallations.KnxInstallation;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.util.FileUtils;

public class ETSLoader {

	private static final String MASTER_DATA_FILE = "knx_master.xml";
	private static final String PROJECT_ZIP_FILE_PREFIX = "P-";
	private static final String MAIN_PROJECT_FILE = "project.xml";

	private static final String TEMP_FILE_PREFIX = "knx-project";
	private static final String ZIP_EXTENSION = "zip";

	public KNX load(File file, Optional<String> password) throws ETSLoaderException {
		ZipFile projectZipFile = new ZipFile(file);

		KNX knx = loadMasterData(projectZipFile);

		try {
			projectZipFile.getFileHeaders().stream().filter(this::isProjectFile).map(h -> {
				try {
					return projectZipFile.getInputStream(h);
				} catch (IOException e) {
					throw new ETSLoaderException(e);
				}
			}).map(i -> loadProjectFile(i, password)).forEach(p -> knx.getProject().add(p));
		} catch (ZipException e) {
			throw new ETSLoaderException(e);
		}

		return knx;
	}

	private boolean isProjectFile(FileHeader p) {
		return p.getFileName().startsWith(PROJECT_ZIP_FILE_PREFIX) && p.getFileName().endsWith("." + ZIP_EXTENSION);
	}

	protected KNX loadMasterData(ZipFile projectZipFile) throws ETSLoaderException {
		try {
			FileHeader masterDataFileHeader = projectZipFile.getFileHeader(MASTER_DATA_FILE);
			ZipInputStream masterDataFileStream = projectZipFile.getInputStream(masterDataFileHeader);

			return loadKnxFromInputStream(masterDataFileStream);
		} catch (IOException e) {
			throw new ETSLoaderException(e);
		}
	}

	protected KNX loadKnxFromInputStream(InputStream masterDataFileStream) throws ETSLoaderException {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(KNX.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

			return (KNX) jaxbUnmarshaller.unmarshal(masterDataFileStream);
		} catch (JAXBException e) {
			throw new ETSLoaderException(e);
		}
	}

	protected KnxProjectT loadProjectFile(InputStream stream, Optional<String> password) throws ETSLoaderException {
		try {
			Path tempFile = Files.createTempFile(TEMP_FILE_PREFIX, "." + ZIP_EXTENSION);
			tempFile.toFile().deleteOnExit();

			Files.copy(stream, tempFile, StandardCopyOption.REPLACE_EXISTING);
			ZipFile projectZipFile = new ZipFile(tempFile.toFile(), password.map(String::toCharArray).orElse(null));

			FileHeader projectFileHeader = projectZipFile.getFileHeader(MAIN_PROJECT_FILE);
			ZipInputStream inputStream = projectZipFile.getInputStream(projectFileHeader);

			KNX knxProject = loadKnxFromInputStream(inputStream);
			KnxProjectT knxProjectT = knxProject.getProject().get(0);

			projectZipFile.getFileHeaders().stream().filter(h -> isInstallationFile(h))
					.sorted(new Comparator<FileHeader>() {
						public int compare(FileHeader o1, FileHeader o2) {
							return o1.getFileName().compareTo(o2.getFileName());
						}
					}).map(h -> {
						try {
							return projectZipFile.getInputStream(h);
						} catch (IOException e) {
							throw new ETSLoaderException(e);
						}
					}).map(i -> {
						return loadInstallationFile(i);
					}).forEach(i -> {
						KnxInstallations installations = knxProjectT.getInstallations();

						if (installations == null) {
							installations = new KnxInstallations();
							knxProjectT.setInstallations(installations);
						}

						installations.getInstallation().add(i);
					});

			return knxProjectT;
		} catch (IOException e) {
			throw new ETSLoaderException(e);
		}
	}

	private KnxInstallation loadInstallationFile(InputStream stream) throws ETSLoaderException {
		KNX knxFile = loadKnxFromInputStream(stream);
		return knxFile.getProject().get(0).getInstallations().getInstallation().get(0);
	}

	private boolean isInstallationFile(FileHeader h) {
		String fileName = FileUtils.getFileNameWithoutExtension(h.getFileName());
		try {
			Integer.parseInt(fileName);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

}
