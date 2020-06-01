package org.openhab.support.knx2openhab;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Queue;

public class Params
{

    private File projectFile;
    private File configDirFile;
    private final Map<String, File> templates = new LinkedHashMap<>();
    private Optional<String> password = Optional.empty();
    private Optional<String> projectId = Optional.empty();
    private Optional<Integer> installationId = Optional.empty();

    // --project Angerweg12.knxproj --password pass --projectId 0 --installationId 0
    // --configDir conf/ --template things.vm --out
    // out/knx.things --template items.vm --out out/knx.items
    public static Params parseParams(final Queue<String> args)
    {
        try
        {
            return internalParseParams(args);
        }
        catch (IllegalArgumentException e)
        {
            System.out.println(e.getMessage());
            printUsage();
            return null;
        }
        catch (@SuppressWarnings("unused") NoSuchElementException e)
        {
            printUsage();
            return null;
        }
    }

    public static Params internalParseParams(final Queue<String> args)
    {
        Params params = new Params();

        String param = args.remove();
        if (!param.equalsIgnoreCase("--project"))
        {
            throw new IllegalArgumentException("Project-File must be provided");
        }

        String projectFile = args.remove();
        if (projectFile.startsWith("--"))
        {
            throw new IllegalArgumentException("Project-File cannot start with '--'");
        }

        File file = new File(projectFile);
        if (!file.exists() || !file.isFile())
        {
            throw new IllegalArgumentException("Project-File " + file + " does not exist");
        }
        params.setProjectFile(file);

        while (!args.isEmpty())
        {
            param = args.remove();
            if (param.equalsIgnoreCase("--password"))
            {
                String password = args.remove();

                params.setPassword(password);
            }
            else if (param.equalsIgnoreCase("--projectId"))
            {
                String projectId = args.remove();

                params.setProjectId(projectId);
            }
            else if (param.equalsIgnoreCase("--installationId"))
            {
                String installationIdString = args.remove();

                int installationId;
                try
                {
                    installationId = Integer.parseInt(installationIdString);
                }
                catch (@SuppressWarnings("unused") NumberFormatException e)
                {
                    throw new IllegalArgumentException(
                            "InstallationId '" + installationIdString + "' is not a valid number");
                }

                params.setInstallationId(installationId);
            }
            else if (param.equalsIgnoreCase("--configDir"))
            {
                String configDir = args.remove();

                if (configDir.startsWith("--"))
                {
                    throw new IllegalArgumentException("Config-Dir cannot start with '--'");
                }

                File configDirFile = new File(configDir);
                if (!configDirFile.exists() || !configDirFile.isDirectory())
                {
                    throw new IllegalArgumentException("Config-Dir " + configDirFile + " does not exist");
                }

                params.setConfigDir(configDirFile);
            }
            else if (param.equalsIgnoreCase("--template"))
            {
                String template = args.remove();
                if (template.startsWith("--"))
                {
                    throw new IllegalArgumentException("Template cannot start with '--'");
                }
                param = args.remove();
                if (!param.equalsIgnoreCase("--out"))
                {
                    throw new IllegalArgumentException("Missing --out argument");
                }
                String output = args.remove();
                if (output.startsWith("--"))
                {
                    throw new IllegalArgumentException("Output-File cannot start with '--'");
                }
                params.addTemplate(template, new File(output));
            }
            else
            {
                throw new IllegalArgumentException("Unkown parameter: " + param);
            }
        }

        return params;
    }

    private void setInstallationId(final Integer installationId)
    {
        this.installationId = Optional.of(installationId);
    }

    private void setProjectId(final String projectId)
    {
        this.projectId = Optional.of(projectId);
    }

    private void setPassword(final String password)
    {
        this.password = Optional.of(password);
    }

    private void addTemplate(final String template, final File output)
    {
        this.templates.put(template, output);

    }

    private void setConfigDir(final File configDirFile)
    {
        this.configDirFile = configDirFile;
    }

    private void setProjectFile(final File file)
    {
        this.projectFile = file;
    }

    public static void printUsage()
    {
        System.out.println("Usage:\r\n"
                + "--project projectFile.knxproj [--password pass --projectId 0 --installationId 0] --configDir conf/ --template template.vm --out outputfile");

    }

    public File getProjectFile()
    {
        return this.projectFile;
    }

    public File getConfigDir()
    {
        return this.configDirFile;
    }

    public Map<String, File> getTemplates()
    {
        return this.templates;
    }

    public Optional<String> getPassword()
    {
        return this.password;
    }

    public Optional<String> getProjectId()
    {
        return this.projectId;
    }

    public Optional<Integer> getInstallationId()
    {
        return this.installationId;
    }

}
