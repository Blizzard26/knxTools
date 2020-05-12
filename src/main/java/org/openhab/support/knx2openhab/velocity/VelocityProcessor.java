package org.openhab.support.knx2openhab.velocity;

import java.io.File;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.collections.map.HashedMap;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.ToolManager;
import org.openhab.support.knx2openhab.model.KNXThing;

public class VelocityProcessor {

	private String template;
	private File templatePath;

	public VelocityProcessor(File templatePath, String template) {
		this.templatePath = templatePath;
		this.template = template;
	}

	public void process(Collection<KNXThing> things, Writer writer) {
		VelocityEngine velocityEngine = new VelocityEngine();
		Properties props = new Properties();
		props.put("resource.loader.file.path", templatePath.getAbsolutePath());
		velocityEngine.init(props);

		ToolManager manager = new ToolManager(false, true);
		manager.setVelocityEngine(velocityEngine);
		manager.configure("/org/apache/velocity/tools/generic/tools.xml");

		Template t = velocityEngine.getTemplate(template, "UTF-8");
		Context context = manager.createContext();

		context.put("things", things);

		t.merge(context, writer);

	}

}
