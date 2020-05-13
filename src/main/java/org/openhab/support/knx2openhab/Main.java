package org.openhab.support.knx2openhab;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import org.knx.KNX;
import org.knx.KnxProjectT.KnxInstallations.KnxInstallation;
import org.openhab.support.knx2openhab.etsLoader.ETSLoader;
import org.openhab.support.knx2openhab.model.KNXThing;
import org.openhab.support.knx2openhab.velocity.VelocityProcessor;

public class Main {

	protected Logger LOG = Logger.getLogger(this.getClass().getName());

	private static final File THINGS_FILE = new File("out/knx.things");
	private static final File ITEMS_FILE = new File("out/knx.items");
	private static final File SITEMAP_FILE = new File("out/knx.sitemap");

	private File configDir;



	public static void main(String[] args) throws IOException, JAXBException {
		
		// --project Angerweg12.knxproj --configDir conf/ --template things.vm --out out/knx.things --template items.vm --out out/knx.items
		
		Queue<String> paramArgs = new ArrayBlockingQueue<>(args.length);
		paramArgs.addAll(Arrays.asList(args));
		
		Params params = Params.parseParams(paramArgs);
		if (params == null)
			return;
		
		
		
		Main main = new Main(params.getConfigDir());
		
		main.process(params.getProjectFile(), params.getPassword(), params.getTemplates());
	}
	
	public Main(File configDir) {
		this.configDir = configDir;
	}

	public void process(File knxProjectFile, Optional<String> projectPassword, Map<String, File> templates) throws IOException {
		ETSLoader loader = new ETSLoader();

		System.out.println("===================");
		System.out.println("Loading ETS file " + knxProjectFile.getAbsolutePath());
		System.out.println("===================");

		KNX knx = loader.load(knxProjectFile, projectPassword);

		KnxInstallation knxInstallation = knx.getProject().get(0).getInstallations().getInstallation().get(0);

		processInstallation(knx, knxInstallation, templates, configDir);
	}

	public void processInstallation(KNX knx, KnxInstallation knxInstallation, Map<String, File> templates, File configDir) throws IOException {

		System.out.println("===================");
		System.out.println("Extracting things");
		System.out.println("===================");
		ThingExtractor thingExtractor = new ThingExtractor(knx, knxInstallation, new File(configDir, "things.json"));
		List<KNXThing> things = thingExtractor.getThings();

		for (Entry<String, File> e : templates.entrySet()) {
			processTemplate(things, e.getKey(), e.getValue());
		}
	}

	public void processTemplate(List<KNXThing> things, String templateFile, File outputFile) throws IOException {
		System.out.println("===================");
		System.out.println("Processing " + templateFile + " to " + outputFile.getName());
		System.out.println("===================");
		
		File parentFile = outputFile.getParentFile();
		if (parentFile != null && !parentFile.exists())
		{
			parentFile.mkdirs();
		}
		
		VelocityProcessor processor = new VelocityProcessor(new File("templates"), templateFile);
		try (Writer writer = Files.newBufferedWriter(outputFile.toPath(), StandardCharsets.UTF_8)) {
			processor.process(things, writer);
		}
	}

}
