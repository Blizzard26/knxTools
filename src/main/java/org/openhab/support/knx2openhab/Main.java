package org.openhab.support.knx2openhab;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import org.knx.KNX;
import org.knx.KnxProjectT.KnxInstallations.KnxInstallation;
import org.openhab.support.knx2openhab.etsLoader.ETSLoader;
import org.openhab.support.knx2openhab.model.KNXThing;
import org.openhab.support.knx2openhab.velocity.VelocityProcessor;

public class Main {


	protected Logger LOG = Logger.getLogger(this.getClass().getName());

	private static final File THINGS_FILE = new File("knx.things");
	private static final File ITEMS_FILE = new File("knx.items");
	private static final File SITEMAP_FILE = new File("knx.sitemap");

	public static void main(String[] args) throws IOException, JAXBException {
		File file = new File("Angerweg12.knxproj");
		Optional<String> password = Optional.of("2G8d4yu!");

		Main main = new Main();
		main.process(file, password);
	}

	private void process(File file, Optional<String> password) throws IOException {
		ETSLoader loader = new ETSLoader();

		System.out.println("===================");
		System.out.println("Loading ETS file " + file.getAbsolutePath());
		System.out.println("===================");

		KNX knx = loader.load(file, password);

		KnxInstallation knxInstallation = knx.getProject().get(0).getInstallations().getInstallation().get(0);

		processInstallation(knx, knxInstallation);
	}

	private void processInstallation(KNX knx, KnxInstallation knxInstallation) throws IOException {

		System.out.println("===================");
		System.out.println("Extracting things");
		System.out.println("===================");
		ThingExtractor thingExtractor = new ThingExtractor(knx, knxInstallation);
		List<KNXThing> things = thingExtractor.getThings();

		
		System.out.println("===================");
		System.out.println("Writing things");
		System.out.println("===================");
		VelocityProcessor processor = new VelocityProcessor(new File("things.vm"));
		try (Writer writer = Files.newBufferedWriter(THINGS_FILE.toPath(), StandardCharsets.UTF_8)) {
			processor.process(things, writer);
		}
		
		System.out.println("===================");
		System.out.println("Writing items");
		System.out.println("===================");
		
		processor = new VelocityProcessor(new File("items.vm"));
		try (Writer writer = Files.newBufferedWriter(ITEMS_FILE.toPath(), StandardCharsets.UTF_8)) {
			processor.process(things, writer);
		}
		
		System.out.println("===================");
		System.out.println("Writing sitemap");
		System.out.println("===================");
		
		
		processor = new VelocityProcessor(new File("sitemap.vm"));
		try (Writer writer = Files.newBufferedWriter(SITEMAP_FILE.toPath(), StandardCharsets.UTF_8)) {
			processor.process(things, writer);
		}

	}

}
