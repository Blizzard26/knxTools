package org.openhab.support.knx2openhab.velocity;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.app.event.MethodExceptionEventHandler;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;
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
        props.put(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, this.templatePath.getAbsolutePath());
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

        List<String> templates = getTemplates(this.templatePath);
        context.put("templates", templates);

        t.merge(context, writer);

    }

    private List<String> getTemplates(final File templatePath)
    {
        List<String> templates;
        try
        {
            templates = Files.walk(templatePath.toPath()).filter(p -> !Files.isDirectory(p))
                    .map(p -> p.getFileName().toString()).filter(p -> p.substring(p.lastIndexOf('.')).equals(".vm"))
                    .collect(Collectors.toList());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return templates;
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
