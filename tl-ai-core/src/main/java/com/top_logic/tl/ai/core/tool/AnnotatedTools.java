/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tool;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonWriter;

import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.provider.ToolSchema;

/**
 * Scans an object for {@link ToolMethod}-annotated methods and produces {@link Tool} instances.
 *
 * @see ToolMethod
 * @see P
 */
public final class AnnotatedTools {

	/**
	 * Scan the given object for {@link ToolMethod}-annotated methods.
	 *
	 * @param provider the object whose methods to scan
	 * @return a list of {@link Tool} instances, one per annotated method
	 */
	public static List<Tool> from(Object provider) {
		List<Tool> tools = new ArrayList<>();
		for (Method method : provider.getClass().getDeclaredMethods()) {
			ToolMethod annotation = method.getAnnotation(ToolMethod.class);
			if (annotation == null) {
				continue;
			}
			method.setAccessible(true);
			tools.add(createTool(provider, method, annotation));
		}
		return tools;
	}

	private static Tool createTool(Object provider, Method method, ToolMethod annotation) {
		String toolName = annotation.name().isEmpty() ? method.getName() : annotation.name();
		String description = String.join("\n", annotation.value());
		ToolMode mode = annotation.mode();
		boolean concurrencySafe = annotation.concurrencySafe();
		ToolSchema schema = generateSchema(toolName, description, method);

		return new ReflectiveTool(toolName, description, schema, mode, concurrencySafe,
			provider, method);
	}

	private static ToolSchema generateSchema(String toolName, String description, Method method) {
		String inputSchemaJson = buildInputSchemaJson(method);
		return new ToolSchema(toolName, description, inputSchemaJson);
	}

	private static String buildInputSchemaJson(Method method) {
		Parameter[] params = method.getParameters();
		try {
			StringW buffer = new StringW();
			try (JsonWriter json = new JsonWriter(buffer)) {
				json.beginObject();
				json.name("type").value("object");

				json.name("properties");
				json.beginObject();
				for (Parameter param : params) {
					P p = param.getAnnotation(P.class);
					String paramName = param.getName();
					json.name(paramName);
					writeParamSchema(json, param.getType(), p);
				}
				json.endObject();

				List<String> required = new ArrayList<>();
				for (Parameter param : params) {
					P p = param.getAnnotation(P.class);
					if (p == null || p.required()) {
						required.add(param.getName());
					}
				}
				if (!required.isEmpty()) {
					json.name("required");
					json.beginArray();
					for (String name : required) {
						json.value(name);
					}
					json.endArray();
				}

				json.endObject();
			}
			return buffer.toString();
		} catch (IOException e) {
			throw new IllegalStateException("Failed to generate JSON Schema", e);
		}
	}

	private static void writeParamSchema(JsonWriter json, Class<?> type, P p) throws IOException {
		json.beginObject();

		if (type == String.class) {
			json.name("type").value("string");
		} else if (type == int.class || type == Integer.class
				|| type == long.class || type == Long.class) {
			json.name("type").value("integer");
		} else if (type == double.class || type == Double.class
				|| type == float.class || type == Float.class) {
			json.name("type").value("number");
		} else if (type == boolean.class || type == Boolean.class) {
			json.name("type").value("boolean");
		} else if (type.isEnum()) {
			json.name("type").value("string");
			json.name("enum");
			json.beginArray();
			for (Object constant : type.getEnumConstants()) {
				json.value(((Enum<?>) constant).name());
			}
			json.endArray();
		} else if (List.class.isAssignableFrom(type)) {
			json.name("type").value("array");
		} else {
			json.name("type").value("string");
		}

		if (p != null && !p.value().isEmpty()) {
			json.name("description").value(p.value());
		}

		json.endObject();
	}

	/**
	 * Coerce a JSON-parsed value to the target Java parameter type.
	 */
	static Object coerceArg(Object value, Class<?> targetType) {
		if (value == null) {
			if (targetType.isPrimitive()) {
				throw new IllegalArgumentException(
					"Cannot pass null for primitive parameter of type " + targetType.getName());
			}
			return null;
		}

		if (targetType == String.class) {
			return value.toString();
		}

		if (targetType == int.class || targetType == Integer.class) {
			if (value instanceof Number n) {
				return n.intValue();
			}
			return Integer.parseInt(value.toString());
		}

		if (targetType == long.class || targetType == Long.class) {
			if (value instanceof Number n) {
				return n.longValue();
			}
			return Long.parseLong(value.toString());
		}

		if (targetType == double.class || targetType == Double.class) {
			if (value instanceof Number n) {
				return n.doubleValue();
			}
			return Double.parseDouble(value.toString());
		}

		if (targetType == float.class || targetType == Float.class) {
			if (value instanceof Number n) {
				return n.floatValue();
			}
			return Float.parseFloat(value.toString());
		}

		if (targetType == boolean.class || targetType == Boolean.class) {
			if (value instanceof Boolean b) {
				return b;
			}
			return Boolean.parseBoolean(value.toString());
		}

		if (targetType.isEnum()) {
			@SuppressWarnings({"unchecked", "rawtypes"})
			Object enumValue = Enum.valueOf((Class<Enum>) targetType, value.toString());
			return enumValue;
		}

		if (List.class.isAssignableFrom(targetType)) {
			return value;
		}

		return value;
	}

	/**
	 * A {@link Tool} backed by a reflective method invocation.
	 */
	private static final class ReflectiveTool implements Tool {

		private final String _name;

		private final String _description;

		private final ToolSchema _schema;

		private final ToolMode _mode;

		private final boolean _concurrencySafe;

		private final Object _provider;

		private final Method _method;

		ReflectiveTool(String name, String description, ToolSchema schema,
				ToolMode mode, boolean concurrencySafe,
				Object provider, Method method) {
			_name = name;
			_description = description;
			_schema = schema;
			_mode = mode;
			_concurrencySafe = concurrencySafe;
			_provider = provider;
			_method = method;
		}

		@Override
		public String name() {
			return _name;
		}

		@Override
		public String description() {
			return _description;
		}

		@Override
		public ToolSchema schema() {
			return _schema;
		}

		@Override
		public ToolMode mode() {
			return _mode;
		}

		@Override
		public boolean isConcurrencySafe() {
			return _concurrencySafe;
		}

		@Override
		public String execute(Map<String, Object> args, ToolContext context) {
			Parameter[] params = _method.getParameters();
			Object[] invokeArgs = new Object[params.length];

			for (int i = 0; i < params.length; i++) {
				String paramName = params[i].getName();
				Object rawValue = args.get(paramName);
				invokeArgs[i] = coerceArg(rawValue, params[i].getType());
			}

			try {
				Object result = _method.invoke(_provider, invokeArgs);
				if (_method.getReturnType() == void.class) {
					return "Success";
				}
				if (result == null) {
					return "null";
				}
				return result.toString();
			} catch (InvocationTargetException e) {
				Throwable cause = e.getCause();
				if (cause instanceof RuntimeException re) {
					throw re;
				}
				throw new RuntimeException(cause.getMessage(), cause);
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Cannot invoke tool method: " + _method.getName(), e);
			}
		}
	}

	private AnnotatedTools() {
		// static utility
	}
}
