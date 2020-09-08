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
    }

    @Override
    public Callable<?> resolve(final String key, final Class clazz) throws SAXException
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

    protected KnxDeviceInstanceT getDeviceInstance(final BaseClass context)
    {
        return getParent(context, KnxDeviceInstanceT.class);
    }

    protected KnxInstallation getInstallation(final BaseClass context)
    {
        return getParent(context, KnxInstallation.class);
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
        if (key.startsWith("GA-"))
        {
            extendedKey = getGroupAddressKey(key, context);
        }
        else if (key.startsWith("O-"))
        {
            extendedKey = getComObjectKey(key, context);
        }
        else if (key.startsWith("CH-"))
        {
            extendedKey = getChannelKey(key, context);
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

    private String getChannelKey(final String key, final BaseClass context)
    {
        KnxDeviceInstanceT deviceInstance = getDeviceInstance(context);
        final String extendedKey = deviceInstance.getHardware2Program().getApplicationProgramRef().get(0)
                .getApplicationProgram().getId() + "_" + key;
        return extendedKey;
    }

    private String getComObjectKey(final String key, final BaseClass context)
    {
        KnxDeviceInstanceT deviceInstance = getDeviceInstance(context);
        final String extendedKey = deviceInstance.getHardware2Program().getApplicationProgramRef().get(0)
                .getApplicationProgram().getId() + "_" + key;
        return extendedKey;
    }

    private String getGroupAddressKey(final String key, final BaseClass context)
    {
        KnxInstallation installation = getInstallation(context);
        final String extendedKey = ((KnxProjectT) installation.getParent().getParent()).getId() + "-"
                + installation.getInstallationId() + "_" + key;
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
