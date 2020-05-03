package org.openhab.support.knx2openhab;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternFormatter {

	private static final Pattern VARIABLE_PATTERN = Pattern
			.compile("\\$(\\w+)(\\[(\\w+)\\])?(\\.\\w+(\\[(\\w+)\\])?)*");
	
	private static final Pattern PROPERTY_PATTERN = Pattern
			.compile("(\\w+)(\\[(\\w+)\\])?((\\.\\w+(\\[(\\w+)\\])?)*)");

	private static final Pattern IF_PATTERN = Pattern.compile("#if \\( (.+?) \\)\r\n((.+?))#end\r\n",
			Pattern.MULTILINE | Pattern.DOTALL);

	private static final Pattern ESCAPE_PATTERN = Pattern.compile("#escape\\((.+?)\\)");
	private static final Pattern LOOKUP_PREFIX_PATTERN = Pattern.compile("#lookupAndReplacePrefix\\((.+?), (.+?)\\)");

	private static final Logger LOG = Logger.getLogger(PatternFormatter.class.getName());

	private static Map<Class<?>, Map<String, Method>> clazzMethods = new HashMap<>();

	public static String format(String pattern, Map<String, Object> context) {

		String result = pattern;
		result = processIfStatements(result, context);
		result = replaceVariables(result, context);
		result = processLookupAndReplace(result, context);
		result = processEscape(result);
		return result.replace("\\_", "_");
	}

	private static String processLookupAndReplace(String pattern, Map<String, Object> env) {
		Matcher matcher = LOOKUP_PREFIX_PATTERN.matcher(pattern);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String param = matcher.group(1);
			matcher.appendReplacement(sb, param.replace('/', '_'));
		}
		matcher.appendTail(sb);
		String result = sb.toString();
		return result;
	}

	private static String processEscape(String pattern) {
		Matcher matcher = ESCAPE_PATTERN.matcher(pattern);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String param = matcher.group(1);
			matcher.appendReplacement(sb, param.replace('/', '_'));
		}
		matcher.appendTail(sb);
		String result = sb.toString();
		return result;
	}

	private static String processIfStatements(String pattern, Map<String, Object> context) {

		Matcher matcher = IF_PATTERN.matcher(pattern);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String variable = matcher.group(1);
			String body = matcher.group(2);
			Object property = getProperty(context, variable.substring(1));

			if (property != null) {
				matcher.appendReplacement(sb, Matcher.quoteReplacement(body));
			} else {
				matcher.appendReplacement(sb, "");
			}

			// Object property = getProperty(knxActionGroup, matcher);

			//
		}
		matcher.appendTail(sb);
		String result = sb.toString();
		return result;

	}

	protected static String replaceVariables(String pattern, Map<String, Object> context) {
		Matcher matcher = VARIABLE_PATTERN.matcher(pattern);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			Object property = getProperty(context, matcher.group().substring(1));

			if (property == null) {
				LOG.severe("Unkown variable " + matcher.group());
			}

			matcher.appendReplacement(sb, property != null ? property.toString() : "UKNOWN");
		}
		matcher.appendTail(sb);
		String result = sb.toString();
		return result;
	}

	private static Object getProperty(Object context, String propertyName) {
		Matcher matcher = PROPERTY_PATTERN.matcher(propertyName);

		if (!matcher.find())
			return null;

		Object property = null;
		
//		System.out.println(matcher.group());
//		
//		StringBuilder builder = new StringBuilder();
//		String sep = "{";
//		for (int i = 0; i < matcher.groupCount(); i++)
//		{
//			builder.append(sep);
//			builder.append(matcher.group(i));
//			sep = "; ";
//		}
//		builder.append("}");
//		System.out.println(builder);
		
		

		String group = matcher.group(1);
		property = resolveProperty(group, context);

		if (property == null) {
			return null;
		}

		String mapAccess = matcher.group(3);
		if (mapAccess != null) {
			property = getMapElement(property, mapAccess);
		}

		if (property == null) {
			return null;
		}

		String subElement = matcher.group(4);
		if (subElement != null && subElement.length() > 0) {
			if (!subElement.startsWith("."))
				throw new IllegalStateException();
			
			subElement = subElement.substring(1);
			
			property = getProperty(property, subElement);
		}

		return property;
	}

	private static Object getMapElement(Object property, String mapAccess) {
		if (property instanceof Map) {
			Map<?, ?> map = (Map<?, ?>) property;
			return map.get(mapAccess);
		} else if (property instanceof Collection) {
			Collection<?> collection = (Collection<?>) property;
			for (Object o : collection) {
				try {
					Method getKey = o.getClass().getMethod("getKey");
					Object keyValue = getKey.invoke(o);

					if (Objects.equals(keyValue, mapAccess)) {
						return o;
					}

				} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			throw new IllegalArgumentException();
		}
		return null;
	}

	private static <R, T> R resolveProperty(String propertyName, T object) {
		Objects.requireNonNull(object);
		
		if (object instanceof Map<?, ?>)
		{
			return (R) ((Map<String, ?>) object).get(propertyName);
		}

		try {
			Map<String, Method> methods = getMethods(object.getClass());

			// System.out.println(object.getClass() + " > " + methods);

			Method method = methods.get(propertyName.toLowerCase());

			if (method != null)
				return (R) method.invoke(object);
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	private static <T> Map<String, Method> getMethods(Class<T> clazz) {

		return clazzMethods.computeIfAbsent(clazz, (c) -> {
			Map<String, Method> methods = new HashMap<>();
			for (Method m : c.getMethods()) {
				String methodName = m.getName();
				if (methodName.startsWith("get"))
					methods.put(methodName.substring(3).toLowerCase(), m);

				if (methodName.startsWith("is"))
					methods.put(methodName.substring(2).toLowerCase(), m);
			}
			return methods;
		});
	}

}
