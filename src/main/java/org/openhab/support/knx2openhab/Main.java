package org.openhab.support.knx2openhab;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import org.knx.KNX;
import org.knx.KnxProjectT.KnxInstallations.KnxInstallation;
import org.openhab.support.knx2openhab.etsLoader.ETSLoader;
import org.openhab.support.knx2openhab.model.Thing;

public class Main {

	protected Logger LOG = Logger.getLogger(this.getClass().getName());

	private static final File THINGS_FILE = new File("knx.things");
	private static final File ITEMS_FILE = new File("knx.items");

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
//		System.out.println("== Functions ============================");
//		List<KnxFunctionExt> functions = ETSSupport.getFunctions(knxInstallation);
//		functions.forEach(f -> System.out.println(f.getName() + " (" + f.getNumber() + ", "
//				+ ETSSupport.resolveType(knx.getMasterData(), f.getType()) + ")"));
//

//
//		System.out.println("\r\n\r\n== GroupAddresses ============================");
//		List<KnxGroupAddressExt> groupAddress = ETSSupport.getGroupAddresses(knxInstallation);
//		Map<String, KnxGroupAddressT> groupAddressId = groupAddress.stream()
//				.collect(Collectors.toMap(KnxGroupAddressExt::getId, g -> g));
//
//		groupAddress.forEach(g -> System.out.println(g.getAddressAsString() + " = " + g.getName()));
//
//		System.out.println("\r\n\r\n== GroupAddresses without Functions ============================");
//		functions.stream().flatMap(f -> f.getGroupAddressRef().stream())
//				.forEach(g -> groupAddressId.remove(g.getRefId()));
//		groupAddressId.values().forEach(g -> System.out.println(g.getName()));

		System.out.println("===================");
		System.out.println("Extracting things");
		System.out.println("===================");
		ThingExtractor thingExtractor = new ThingExtractor(knx, knxInstallation);
		List<Thing> things = thingExtractor.getThings();

		System.out.println("===================");
		System.out.println("Writing things");
		System.out.println("===================");

		ThingWriter thingWriter = new ThingWriter(knx, knxInstallation);
		try (Writer writer = new FileWriter(THINGS_FILE)) {
			thingWriter.write(things, writer);
		}
		
		ItemsWriter itemsWriter = new ItemsWriter(knx, knxInstallation);
		try (Writer writer = new FileWriter(ITEMS_FILE)) {
			itemsWriter.write(things, writer);
		}

	}

}
