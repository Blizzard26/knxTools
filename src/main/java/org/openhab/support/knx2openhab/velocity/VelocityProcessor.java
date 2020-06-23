package org.openhab.support.knx2openhab.velocity;

import java.io.File;
import java.io.Writer;
import java.util.Collection;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.ToolManager;
import org.knx.xml.KNX;
import org.knx.xml.KnxProjectT.KnxInstallations.KnxInstallation;
import org.openhab.support.knx2openhab.model.KNXThing;

public class VelocityProcessor
{

    private final String template;
    private final File templatePath;

    public VelocityProcessor(final File templatePath, final String template)
    {
        this.templatePath = templatePath;
        this.template = template;
    }

    public void process(final KNX knx, final KnxInstallation knxInstallation, final Collection<KNXThing> things,
            final Writer writer)
    {
        VelocityEngine velocityEngine = new VelocityEngine();
        Properties props = new Properties();
        props.put("resource.loader.file.path", this.templatePath.getAbsolutePath());
        velocityEngine.init(props);

        ToolManager manager = new ToolManager(false, true);
        manager.setVelocityEngine(velocityEngine);
        manager.configure("/org/apache/velocity/tools/generic/tools.xml");

        Template t = velocityEngine.getTemplate(this.template, "UTF-8");
        Context context = manager.createContext();

        context.put("things", things);
        context.put("knx", knx);
        context.put("installation", knxInstallation);

        t.merge(context, writer);

    }

}
