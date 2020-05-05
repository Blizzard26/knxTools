package org.openhab.support.knx2openhab.velocity;

import java.io.File;
import java.io.Writer;
import java.util.Collection;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.ToolManager;
import org.openhab.support.knx2openhab.model.Thing;

public class VelocityProcessor {

	private File template;

	public VelocityProcessor(File template)
	{
		this.template = template;
	}
	
	public void process(Collection<Thing> things, Writer writer)
	{
		VelocityEngine velocityEngine = new VelocityEngine();
		velocityEngine.init();
		
		ToolManager manager = new ToolManager(false, true);
		manager.setVelocityEngine(velocityEngine);
		manager.configure("/org/apache/velocity/tools/generic/tools.xml");
		
		    
		Template t = velocityEngine.getTemplate(template.getName(), "UTF-8");
		Context context = manager.createContext();
		
		context.put("things", things);		     
		
		t.merge( context, writer );
		
	}
	
}
