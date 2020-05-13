package org.openhab.support.knx2openhab;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Queue;

public class Params {

	private File projectFile;
	private File configDirFile;
	private Map<String, File> templates = new LinkedHashMap<>();

	// --project Angerweg12.knxproj --configDir conf/ --template things.vm --out
	// out/knx.things --template items.vm --out out/knx.items
	public static Params parseParams(Queue<String> args) {
		try {
			return internalParseParams(args);
		} catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
			printUsage();
			return null;
		} catch (NoSuchElementException e) {
			printUsage();
			return null;
		}
	}

	public static Params internalParseParams(Queue<String> args) {
		Params params = new Params();

		String param = args.remove();
		if (!param.equalsIgnoreCase("--project")) {
			throw new IllegalArgumentException("Project-File must be provided");
		}

		String projectFile = args.remove();
		if (projectFile.startsWith("--")) {
			throw new IllegalArgumentException("Project-File cannot start with '--'");
		}

		File file = new File(projectFile);
		if (!file.exists() || !file.isFile()) {
			throw new IllegalArgumentException("Project-File " + file + " does not exist");
		}
		params.setProjectFile(file);

		while (!args.isEmpty()) {
			param = args.remove();
			if (param.equalsIgnoreCase("--configDir")) {
				String configDir = args.remove();

				if (configDir.startsWith("--")) {
					throw new IllegalArgumentException("Config-Dir cannot start with '--'");
				}

				File configDirFile = new File(configDir);
				if (!configDirFile.exists() || !configDirFile.isDirectory()) {
					throw new IllegalArgumentException("Config-Dir " + configDirFile + " does not exist");
				}

				params.setConfigDir(configDirFile);
			} else if (param.equalsIgnoreCase("--template")) {
				String template = args.remove();
				if (template.startsWith("--")) {
					throw new IllegalArgumentException("Template cannot start with '--'");
				}
				param = args.remove();
				if (!param.equalsIgnoreCase("--out")) {
					throw new IllegalArgumentException("Missing --out argument");
				}
				String output = args.remove();
				if (output.startsWith("--")) {
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

	private void addTemplate(String template, File output) {
		templates.put(template, output);

	}

	private void setConfigDir(File configDirFile) {
		this.configDirFile = configDirFile;
	}

	private void setProjectFile(File file) {
		this.projectFile = file;
	}

	private static void printUsage() {
		System.out.println("Usage:\r\n"
				+ "--project projectFile.knxproj --configDir conf/ --template template.vm --out outputfile");

	}

	public File getProjectFile() {
		return projectFile;
	}

	public File getConfigDir() {
		return configDirFile;
	}

	public Map<String, File> getTemplates() {
		return templates;
	}

	public Optional<String> getPassword() {
		// TODO Auto-generated method stub
		return Optional.of("2G8d4yu!");
	}

}
