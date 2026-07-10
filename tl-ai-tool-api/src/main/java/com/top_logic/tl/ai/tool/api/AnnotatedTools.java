/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.tool.api;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonWriter;

import com.top_logic.tl.ai.tool.api.json.JsonTree;

/**
 * Scans an object for {@link ToolMethod}-annotated methods and produces {@link Tool} instances.
 *
 * <p>Scans the full class hierarchy (declared methods of the class and all superclasses),
 * so annotated methods in base classes are discovered.</p>
 *
 * <p>Parameters of type {@link ToolContext} are automatically injected during execution
 * and excluded from the JSON Schema sent to the LLM.</p>
 *
 * @see ToolMethod
 * @see P
 */
public final class AnnotatedTools {

	/**
	 * Scan the given object for {@link ToolMethod}-annotated methods.
	 *
	 * <p>Walks the full class hierarchy so that inherited methods are included.</p>
	 *
	 * @param provider the object whose methods to scan
	 * @return a list of {@link Tool} instances, one per annotated method
	 */
	public static List<Tool> from(Object provider) {
		List<Tool> tools = new ArrayList<>();
		Set<String> seen = new HashSet<>();
		Class<?> clazz = provider.getClass();
		while (clazz != null && clazz != Object.class) {
			for (Method method : clazz.getDeclaredMethods()) {
				ToolMethod annotation = method.getAnnotation(ToolMethod.class);
				if (annotation == null) {
					continue;
				}
				String key = method.getName() + ":" + List.of(method.getParameterTypes());
				if (!seen.add(key)) {
					continue;
				}
				method.setAccessible(true);
				tools.add(createTool(provider, method, annotation));
			}
			clazz = clazz.getSuperclass();
		}
		return tools;
	}

	private static Tool createTool(Object provider, Method method, ToolMethod annotation) {
		String toolName = annotation.name().isEmpty() ? method.getName() : annotation.name();
		String description = String.join("\n", annotation.description());
		ToolMode mode = annotation.mode();
		boolean concurrencySafe = annotation.concurrencySafe();
		int truncationHint = annotation.truncationHint();
		ToolSchema schema = generateSchema(toolName, description, method);

		return new ReflectiveTool(toolName, description, schema, mode, concurrencySafe,
			truncationHint, provider, method);
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
					if (isInjectedParam(param)) {
						continue;
					}
					P p = param.getAnnotation(P.class);
					String paramName = param.getName();
					json.name(paramName);
					writeParamSchema(json, param.getType(), param.getParameterizedType(), p);
				}
				json.endObject();

				List<String> required = new ArrayList<>();
				for (Parameter param : params) {
					if (isInjectedParam(param)) {
						continue;
					}
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

	private static boolean isInjectedParam(Parameter param) {
		return ToolExecutionContext.class.isAssignableFrom(param.getType());
	}

	private static void writeParamSchema(JsonWriter json, Class<?> type, Type genericType, P p) throws IOException {
		json.beginObject();
		writeTypeSchema(json, type, genericType);
		if (p != null && !p.value().isEmpty()) {
			json.name("description").value(p.value());
		}
		json.endObject();
	}

	/**
	 * Emits ONLY the {@code type} (and {@code items} / {@code properties} / {@code enum}) keys —
	 * no description, no opening/closing braces.
	 */
	private static void writeTypeSchema(JsonWriter json, Class<?> type, Type genericType) throws IOException {
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
			json.name("items");
			json.beginObject();
			Class<?> elementType = resolveListElementType(genericType);
			Type elementGenericType = resolveListElementGenericType(genericType);
			if (elementType != null) {
				writeTypeSchema(json, elementType, elementGenericType);
			} else {
				json.name("type").value("string");
			}
			json.endObject();
		} else if (type.isRecord()) {
			writeRecordSchema(json, type);
		} else {
			// Fallback for non-record POJOs: an open object schema. The LLM may struggle
			// without per-field info, but at least it knows to emit an object, not a string.
			json.name("type").value("object");
			json.name("additionalProperties").value(true);
		}
	}

	private static void writeRecordSchema(JsonWriter json, Class<?> type) throws IOException {
		json.name("type").value("object");
		json.name("properties");
		json.beginObject();
		for (RecordComponent rc : type.getRecordComponents()) {
			json.name(rc.getName());
			json.beginObject();
			writeTypeSchema(json, rc.getType(), rc.getGenericType());
			json.endObject();
		}
		json.endObject();
		// additionalProperties:false keeps the schema strict; LLMs respect it.
		json.name("additionalProperties").value(false);
	}

	private static Class<?> resolveListElementType(Type listGenericType) {
		if (listGenericType instanceof ParameterizedType pt) {
			Type arg = pt.getActualTypeArguments()[0];
			if (arg instanceof Class<?> c) {
				return c;
			}
			if (arg instanceof ParameterizedType apt && apt.getRawType() instanceof Class<?> rc) {
				return rc;
			}
		}
		return null;
	}

	private static Type resolveListElementGenericType(Type listGenericType) {
		if (listGenericType instanceof ParameterizedType pt) {
			return pt.getActualTypeArguments()[0];
		}
		return null;
	}

	// --- Argument coercion ---

	/**
	 * Coerce a JSON-parsed value to the target Java parameter type.
	 *
	 * @throws ToolArgumentCoercionException with a clean, LLM-facing message on type mismatch.
	 *         Always thrown before the tool method is invoked — safe to treat as synthetic
	 *         (no side effects) by callers deciding whether to roll back a transaction.
	 */
	static Object coerceArg(Object value, Class<?> targetType, Type genericType, String paramName) {
		if (value == null) {
			if (targetType.isPrimitive()) {
				throw new ToolArgumentCoercionException(
					"Parameter '" + paramName + "' is required (type " + targetType.getSimpleName() + "), got null.");
			}
			return null;
		}

		try {
			return doCoerce(value, targetType, genericType);
		} catch (IllegalArgumentException e) {
			throw new ToolArgumentCoercionException(
				"Parameter '" + paramName + "': expected " + targetType.getSimpleName()
					+ ", got '" + value + "' (" + value.getClass().getSimpleName() + ").",
				e);
		}
	}

	private static Object doCoerce(Object value, Class<?> targetType, Type genericType) {
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

		if (List.class.isAssignableFrom(targetType) && value instanceof List<?> list) {
			return coerceList(list, genericType);
		}

		if (targetType.isRecord() && value instanceof Map<?, ?> map) {
			return coerceRecord(map, targetType);
		}

		// LLMs sometimes stringify complex args as JSON text instead of emitting a real object.
		if (value instanceof String s) {
			String trimmed = s.trim();
			if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
				Object parsed = parseJsonTree(s);
				return doCoerce(parsed, targetType, genericType);
			}
		}

		return value;
	}

	private static List<Object> coerceList(List<?> list, Type genericType) {
		Class<?> elementType = resolveListElementType(genericType);
		Type elementGenericType = resolveListElementGenericType(genericType);
		if (elementType == null || elementType == String.class) {
			return new ArrayList<>(list);
		}
		List<Object> result = new ArrayList<>(list.size());
		for (Object item : list) {
			result.add(doCoerce(item, elementType, elementGenericType));
		}
		return result;
	}

	private static Object coerceRecord(Map<?, ?> map, Class<?> recordType) {
		RecordComponent[] components = recordType.getRecordComponents();
		Object[] args = new Object[components.length];
		for (int i = 0; i < components.length; i++) {
			RecordComponent rc = components[i];
			Object raw = map.get(rc.getName());
			if (raw == null && !map.containsKey(rc.getName())) {
				// Field omitted by the LLM — use a sensible default instead of failing.
				args[i] = primitiveDefault(rc.getType());
			} else {
				args[i] = coerceArg(raw, rc.getType(), rc.getGenericType(), rc.getName());
			}
		}
		try {
			Class<?>[] paramTypes = new Class<?>[components.length];
			for (int i = 0; i < components.length; i++) {
				paramTypes[i] = components[i].getType();
			}
			return recordType.getDeclaredConstructor(paramTypes).newInstance(args);
		} catch (ReflectiveOperationException e) {
			throw new ToolArgumentCoercionException(
				"Failed to construct record " + recordType.getSimpleName() + " from " + map, e);
		}
	}

	private static Object primitiveDefault(Class<?> type) {
		if (type == boolean.class) return Boolean.FALSE;
		if (type == int.class) return Integer.valueOf(0);
		if (type == long.class) return Long.valueOf(0L);
		if (type == double.class) return Double.valueOf(0.0);
		if (type == float.class) return Float.valueOf(0.0f);
		return null;
	}

	// --- msgbuf-based JSON string → Map/List tree ---

	private static Object parseJsonTree(String json) {
		try {
			return JsonTree.parseValue(json);
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to parse JSON string: " + e.getMessage(), e);
		}
	}

	// --- ReflectiveTool ---

	/**
	 * A {@link Tool} backed by a reflective method invocation.
	 */
	private static final class ReflectiveTool implements Tool {

		private final String _name;

		private final String _description;

		private final ToolSchema _schema;

		private final ToolMode _mode;

		private final boolean _concurrencySafe;

		private final int _truncationHint;

		private final Object _provider;

		private final Method _method;

		ReflectiveTool(String name, String description, ToolSchema schema,
				ToolMode mode, boolean concurrencySafe, int truncationHint,
				Object provider, Method method) {
			_name = name;
			_description = description;
			_schema = schema;
			_mode = mode;
			_concurrencySafe = concurrencySafe;
			_truncationHint = truncationHint;
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
		public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
			Parameter[] params = _method.getParameters();
			Object[] invokeArgs = new Object[params.length];

			for (int i = 0; i < params.length; i++) {
				if (isInjectedParam(params[i])) {
					invokeArgs[i] = context;
				} else {
					String paramName = params[i].getName();
					Object rawValue = args.get(paramName);
					invokeArgs[i] = coerceArg(rawValue, params[i].getType(),
						params[i].getParameterizedType(), paramName);
				}
			}

			try {
				Object result = _method.invoke(_provider, invokeArgs);
				if (_method.getReturnType() == void.class) {
					return new ToolResult("Success", null, _truncationHint);
				}
				if (result == null) {
					return new ToolResult("null", null, _truncationHint);
				}
				return new ToolResult(result.toString(), null, _truncationHint);
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
