package org.openhab.support.knx2openhab.velocity;

import java.io.File;
import java.io.Writer;
import java.util.Collection;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.app.event.MethodExceptionEventHandler;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.ToolManager;
import org.apache.velocity.util.introspection.Info;
import org.knx.xml.KNX;
import org.knx.xml.KnxProjectT.KnxInstallations.KnxInstallation;
import org.openhab.support.knx2openhab.model.KNXThing;
import org.openhab.support.knx2openhab.model.ModelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VelocityProcessor
{
    protected static final Logger LOG = LoggerFactory.getLogger(VelocityProcessor.class);

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
        props.put("event_handler.method_exception.class", ExceptionHandler.class.getName());
        velocityEngine.init(props);

        ToolManager manager = new ToolManager(false, true);
        manager.setVelocityEngine(velocityEngine);
        manager.configure("/org/apache/velocity/tools/generic/tools.xml");

        Template t = velocityEngine.getTemplate(this.template, "UTF-8");
        Context context = manager.createContext();

        context.put("things", things);
        context.put("knx", knx);
        context.put("installation", knxInstallation);
        context.put("modelUtil", ModelUtil.class);
        context.put("tools", VelocityTools.class);

        t.merge(context, writer);

    }

    public static class ExceptionHandler implements MethodExceptionEventHandler
    {

        @Override
        public Object methodException(final Context context, @SuppressWarnings("rawtypes") final Class claz,
                final String method, final Exception e, final Info info)
        {
            LOG.error("Exception during template evaluation", e);
            throw new RuntimeException(e);
        }

    }

}
