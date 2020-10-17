package org.openhab.support.knx2openhab;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Collectors;

import org.knx.ETSLoader;
import org.knx.xml.KNX;
import org.knx.xml.KnxProjectT;
import org.knx.xml.KnxProjectT.KnxInstallations.KnxInstallation;
import org.openhab.support.knx2openhab.model.KNXThing;
import org.openhab.support.knx2openhab.velocity.VelocityProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Main
{

    protected Logger LOG = LoggerFactory.getLogger(this.getClass());
    private final Params params;
    private final Config configuration;

    public static void main(final String[] args) throws IOException
    {
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

        Main main = new Main(params);
        main.process();
    }

    public Main(final Params params)
    {
        this.params = params;
        this.configuration = loadConfiguration(params.getConfigDir());
    }

    public void process() throws IOException
    {

        ETSLoader loader = new ETSLoader();

        File knxProjectFile = params.getProjectFile();

        System.out.println("======================================");
        System.out.println("Loading ETS file " + knxProjectFile.getAbsolutePath());
        System.out.println("======================================");

        KNX knx = loader.load(knxProjectFile, params.getPassword());

        this.LOG.info(
                "Projects: " + knx.getProject().stream().map(KnxProjectT::getId).collect(Collectors.joining(", ")));

        KnxProjectT knxProject = getProjectById(knx, params.getProjectId());

        this.LOG.info("Installations on project " + knxProject.getId() + ": "
                + knxProject.getInstallations().getInstallation().stream().map(KnxInstallation::getInstallationId)
                        .map(String::valueOf).collect(Collectors.joining(", ")));

        KnxInstallation knxInstallation = getInstallationById(knxProject, params.getInstallationId());

        processInstallation(knx, knxInstallation);
    }

    private Config loadConfiguration(final File configDir)
    {
        File configFile = new File(configDir, "conf.json");
        ObjectMapper mapper = new ObjectMapper();

        try
        {
            Config config = mapper.readValue(configFile, Config.class);
            return config;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

    }

    private KnxInstallation getInstallationById(final KnxProjectT knxProject, final Optional<Integer> installationId)
    {
        KnxInstallation knxInstallation;
        if (installationId.isPresent())
        {
            knxInstallation = knxProject.getInstallations().getInstallation().stream()
                    .filter(i -> i.getInstallationId() == installationId.get()).findAny()
                    .orElseThrow(() -> new IllegalArgumentException("No installation with id " + installationId.get()
                            + " found on project " + knxProject.getId()));
        }
        else
        {
            knxInstallation = knxProject.getInstallations().getInstallation().get(0);
        }
        return knxInstallation;
    }

    private KnxProjectT getProjectById(final KNX knx, final Optional<String> projectId)
    {
        KnxProjectT knxProject;
        if (projectId.isPresent())
        {
            knxProject = knx.getProject().stream().filter(p -> p.getId().equals(projectId.get())).findAny().orElseThrow(
                    () -> new IllegalArgumentException("No project with id " + projectId.get() + " found!"));
        }
        else
        {
            knxProject = knx.getProject().get(0);
        }
        return knxProject;
    }

    public void processInstallation(final KNX knx, final KnxInstallation knxInstallation) throws IOException
    {
        System.out.println("======================================");
        System.out.println("Extracting things");
        System.out.println("======================================");

        File thingsConfigFile = new File(configuration.configFile);

        ThingExtractor thingExtractor = new ThingExtractor(knxInstallation, thingsConfigFile);
        List<KNXThing> things = thingExtractor.getThings();

        for (Entry<String, File> e : params.getTemplates().entrySet())
        {
            processTemplate(knx, knxInstallation, things, e.getKey(), e.getValue());
        }
    }

    public void processTemplate(final KNX knx, final KnxInstallation knxInstallation, final List<KNXThing> things,
            final String templateFile, final File outputFile) throws IOException
    {
        System.out.println("======================================");
        System.out.println("Processing " + templateFile + " to " + outputFile.getName());
        System.out.println("======================================");

        File parentFile = outputFile.getParentFile();
        if (parentFile != null && !parentFile.exists())
        {
            parentFile.mkdirs();
        }

        VelocityProcessor processor = new VelocityProcessor(new File(configuration.templateDir), templateFile);
        try (Writer writer = Files.newBufferedWriter(outputFile.toPath(), StandardCharsets.UTF_8))
        {
            processor.process(knx, knxInstallation, things, writer, configuration.env);
        }
    }

}
