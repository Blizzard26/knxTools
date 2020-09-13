package org.knx;

import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.knx.xml.BaseClass;
import org.knx.xml.KnxDeviceInstanceT;
import org.knx.xml.KnxModuleDefT;
import org.knx.xml.KnxModuleInstanceT;
import org.knx.xml.KnxModuleT;
import org.knx.xml.KnxProjectT;
import org.knx.xml.KnxProjectT.KnxInstallations.KnxInstallation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.sun.xml.bind.IDResolver;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

public class LookupIdResolver extends IDResolver
{

    protected Logger LOG = LoggerFactory.getLogger(this.getClass());

    public interface DynamicProxyResolution<T>
    {
        T resolveObject(BaseClass context);
    }

    protected final Map<String, Object> lookupMap = new HashMap<>();

    public LookupIdResolver()
    {
    }

    @Override
    public void bind(final String key, final Object value) throws SAXException
    {
        final Object oldValue = lookupMap.put(key, value);
        if (oldValue != null)
        {
            final String message = MessageFormat.format("duplicated key ''{0}'' => ''{1}'' - old: ''{2}''", key, value,
                    oldValue);
            throw new AssertionError(message);
        }
        LOG.trace("Registering key: " + key + " for " + value.getClass().getSimpleName());
    }

    @Override
    public Callable<?> resolve(final String key, @SuppressWarnings("rawtypes") final Class clazz) throws SAXException
    {

        return new Callable<Object>()
        {
            @SuppressWarnings("unchecked")
            @Override
            public Object call()
            {
                final Object value;
                if (lookupMap.containsKey(key))
                {
                    value = lookupMap.get(key);
                }
                else
                {
                    value = createDummyProxy(key, clazz);
                }

                return value;
            }
        };
    }

    @SuppressWarnings("unchecked")
    public <T> T createDummyProxy(final String key, final Class<T> clazz)
    {

        Enhancer e = new Enhancer();
        e.setClassLoader(clazz.getClassLoader());
        e.setSuperclass(BaseClass.class.isAssignableFrom(clazz) ? clazz : BaseClass.class);
        e.setInterfaces(new Class[] { DynamicProxyResolution.class });

        e.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
            if (method.getName().equals("resolveObject"))
            {
                BaseClass context = (BaseClass) args[0];
                return resolveObject(key, context);
            }
            else
                return proxy.invokeSuper(obj, args);
        });
        T myProxy = (T) e.create();
        return myProxy;
    }

    @SuppressWarnings("unchecked")
    private <T> T getParent(final BaseClass context, final Class<T> clazz)
    {
        BaseClass p = context;

        while (p != null)
        {
            if (clazz.isAssignableFrom(p.getClass()))
                return (T) p;
            p = p.getParent();
        }
        return null;
    }

    protected Object resolveObject(final String key, final BaseClass context)
    {
        final String extendedKey;
        // Group Address
        if (key.startsWith("GA-"))
        {
            extendedKey = getGroupAddressKey(key, context);
        }
        // ComObjects
        else if (key.startsWith("O-"))
        {
            extendedKey = getComObjectKey(key, context);
        }
        // Channel
        else if (key.startsWith("CH-"))
        {
            extendedKey = getChannelKey(key, context);
        }
        // Module
        else if (key.startsWith("MD-"))
        {
            extendedKey = getModulKey(key, context);
        }
        else if (key.startsWith("M-") && key.contains("_MD-"))
        {
            String tempKey = key.substring(key.indexOf("_MD-") + 1);
            extendedKey = getModulKey(tempKey, context);
        }
        else
        {
            extendedKey = key;
        }

        if (lookupMap.containsKey(extendedKey))
            return lookupMap.get(extendedKey);
        else
        {
            LOG.error("Unable to resolve id: " + key);
            return null;
            // throw new AssertionError("Unable to resolve id: " + key);
        }
    }

    private String getModulKey(final String key, final BaseClass context)
    {
        KnxDeviceInstanceT deviceInstance = getParent(context, KnxDeviceInstanceT.class);

        String extendedKey;
        if (key.contains("_MI-"))
        {
            String moduleInstanceKey = key.substring(0, key.indexOf('_', key.indexOf("_MI-") + 4));

            KnxModuleInstanceT moduleInstance = (KnxModuleInstanceT) lookupMap.get(moduleInstanceKey);
            KnxModuleT module = moduleInstance.getModule();
            // ModuleInstance might not be resolved at this point as it comes later in the XML
            // Hence, we need to resolve it first.
            if (module instanceof DynamicProxyResolution)
            {
                resolveProxies(moduleInstance);
                module = moduleInstance.getModule();
            }

            KnxModuleDefT refId = (KnxModuleDefT) module.getRefId();

            String subkey = key.substring(key.indexOf("_MI-") + 4);
            subkey = subkey.substring(subkey.indexOf('_') + 1);
            extendedKey = refId.getId() + "_" + subkey;
        }
        else
        {
            extendedKey = deviceInstance.getHardware2Program().getApplicationProgramRef().get(0).getApplicationProgram()
                    .getId() + "_" + key;

        }

        return extendedKey;
    }

    private String getChannelKey(final String key, final BaseClass context)
    {
        KnxDeviceInstanceT deviceInstance = getParent(context, KnxDeviceInstanceT.class);
        final String extendedKey = deviceInstance.getHardware2Program().getApplicationProgramRef().get(0)
                .getApplicationProgram().getId() + "_" + key;
        return extendedKey;
    }

    private String getComObjectKey(final String key, final BaseClass context)
    {
        KnxDeviceInstanceT deviceInstance = getParent(context, KnxDeviceInstanceT.class);
        final String extendedKey = deviceInstance.getHardware2Program().getApplicationProgramRef().get(0)
                .getApplicationProgram().getId() + "_" + key;
        return extendedKey;
    }

    private String getGroupAddressKey(final String key, final BaseClass context)
    {
        KnxInstallation installation = getParent(context, KnxInstallation.class);
        KnxProjectT project = getParent(installation, KnxProjectT.class);
        final String extendedKey = project.getId() + "-" + installation.getInstallationId() + "_" + key;
        return extendedKey;
    }

    public void resolveProxies(final BaseClass object)
    {
        Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        try
        {
            internalResolveProxies(object, visited);
        }
        catch (IllegalArgumentException | IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void internalResolveProxies(final Object obj, final Set<Object> visited)
            throws IllegalArgumentException, IllegalAccessException
    {
        if (obj == null)
            return;

        if (obj.getClass().getPackage() == null || !obj.getClass().getPackage().getName().startsWith("org.knx.xml"))
            return;

        if (visited.contains(obj))
            return;
        visited.add(obj);

        for (Field field : obj.getClass().getDeclaredFields())
        {
            if (!field.isAccessible())
            {
                field.setAccessible(true);
            }
            Object value = field.get(obj);
            if (value instanceof DynamicProxyResolution)
            {
                value = ((DynamicProxyResolution<?>) value).resolveObject((BaseClass) obj);
                field.set(obj, value);
            }

            if (value instanceof List)
            {
                ListIterator iter = ((List) value).listIterator();
                for (; iter.hasNext();)
                {
                    Object v = iter.next();
                    if (v instanceof DynamicProxyResolution)
                    {
                        v = ((DynamicProxyResolution) v).resolveObject((BaseClass) obj);
                        iter.set(v);
                    }
                    internalResolveProxies(v, visited);
                }
            }
            else
            {
                internalResolveProxies(value, visited);
            }
        }
    }

    public void purgeSuperHierarchy(final BaseClass base)
    {
        BaseClass p = base;
        while (p != null)
        {
            lookupMap.values().remove(p);
            p = p.getParent();
        }
    }

}
