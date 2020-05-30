package org.openhab.support.knx2openhab;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.knx.KNX;
import org.knx.KnxProjectT;
import org.knx.KnxProjectT.KnxInstallations.KnxInstallation;
import org.openhab.support.knx2openhab.etsLoader.ETSLoader;
import org.openhab.support.knx2openhab.model.KNXThing;
import org.openhab.support.knx2openhab.velocity.VelocityProcessor;

public class Main {

	protected Logger LOG = Logger.getLogger(this.getClass().getName());


	public static void main(String[] args) throws IOException {
		if (args.length == 0)
		{
			Params.printUsage();
			return;
		}
		
		Queue<String> paramArgs = new ArrayBlockingQueue<>(args.length);
		paramArgs.addAll(Arrays.asList(args));

		Params params = Params.parseParams(paramArgs);
		if (params == null)
			return;

		Main main = new Main();
		main.process(params);
	}

	public Main() {
	}

	public void process(Params params) throws IOException {
		ETSLoader loader = new ETSLoader();

		File knxProjectFile = params.getProjectFile();

		System.out.println("======================================");
		System.out.println("Loading ETS file " + knxProjectFile.getAbsolutePath());
		System.out.println("======================================");

		KNX knx = loader.load(knxProjectFile, params.getPassword());
		
		System.out.print("Projects: ");
		System.out.println(knx.getProject().stream().map(p -> p.getId()).collect(Collectors.joining(", ")));
		
		KnxProjectT knxProject = getProjectById(knx, params.getProjectId());
		
		System.out.print("Installations on project " + knxProject.getId() + ": ");
		System.out.println(knxProject.getInstallations().getInstallation().stream().map(i -> i.getInstallationId()).map(String::valueOf).collect(Collectors.joining(", ")));

		KnxInstallation knxInstallation = getInstallationById(knxProject, params.getInstallationId());
		
		File thingsConfigFile = new File(params.getConfigDir(), "things.json");

		processInstallation(knx, knxInstallation, params.getTemplates(), thingsConfigFile);
	}

	private KnxInstallation getInstallationById(KnxProjectT knxProject, Optional<Integer> installationId) {
		KnxInstallation knxInstallation;
		if (installationId.isPresent()) {
			knxInstallation = knxProject.getInstallations().getInstallation().stream()
					.filter(i -> i.getInstallationId() == installationId.get()).findAny()
					.orElseThrow(() -> new IllegalArgumentException("No installation with id "
							+ installationId.get() + " found on project " + knxProject.getId()));
		} else {
			knxInstallation = knxProject.getInstallations().getInstallation().get(0);
		}
		return knxInstallation;
	}

	private KnxProjectT getProjectById(KNX knx, Optional<String> projectId) {
		KnxProjectT knxProject;
		if (projectId.isPresent()) {
			knxProject = knx.getProject().stream().filter(p -> {
				
				return p.getId().equals(projectId.get());
			}).findAny()
					.orElseThrow(() -> new IllegalArgumentException(
							"No project with id " + projectId.get() + " found!"));
		} else {
			knxProject = knx.getProject().get(0);
		}
		return knxProject;
	}

	public void processInstallation(KNX knx, KnxInstallation knxInstallation, Map<String, File> templates,
			File thingsConfigFile) throws IOException {

		System.out.println("======================================");
		System.out.println("Extracting things");
		System.out.println("======================================");
		
		ThingExtractor thingExtractor = new ThingExtractor(knx, knxInstallation, thingsConfigFile);
		List<KNXThing> things = thingExtractor.getThings();

		for (Entry<String, File> e : templates.entrySet()) {
			processTemplate(things, e.getKey(), e.getValue());
		}
	}

	public void processTemplate(List<KNXThing> things, String templateFile, File outputFile) throws IOException {
		System.out.println("======================================");
		System.out.println("Processing " + templateFile + " to " + outputFile.getName());
		System.out.println("======================================");

		File parentFile = outputFile.getParentFile();
		if (parentFile != null && !parentFile.exists()) {
			parentFile.mkdirs();
		}

		VelocityProcessor processor = new VelocityProcessor(new File("templates"), templateFile);
		try (Writer writer = Files.newBufferedWriter(outputFile.toPath(), StandardCharsets.UTF_8)) {
			processor.process(things, writer);
		}
	}

}
