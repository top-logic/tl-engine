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

import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.tool.api.json.JsonTree;

/**
 * Scans an object for {@link ToolMethod}-annotated methods and produces {@link Tool} instances.
 *
 * <p>Scans the full class hierarchy (declared methods of the class and all superclasses),
 * so annotated methods in base classes are discovered.</p>
 *
 * <p>Parameters of type {@link ToolExecutionContext} are automatically injected during execution
 * and excluded from the JSON Schema sent to the LLM.</p>
 *
 * <p>
 * A parameter of a model type ({@link TLObject} or a subtype) is declared to the model as the
 * {@code "TypeName:id"} string a tool result reports, and resolved back to the object before the
 * method is invoked - through {@link ToolExecutionContext#resolveObject(String)}, which runs the
 * read check of the run's principal. A Java tool therefore resolves an identifier exactly as a
 * configured script tool does, and cannot forget the check (Gitea #381). An identifier that names
 * nothing, or names something the principal may not read, fails as a
 * {@link ToolArgumentCoercionException}: the method never ran, so the turn is not rolled back.
 * </p>
 *
 * @see ToolMethod
 * @see P
 */
public final class AnnotatedTools {

	/**
	 * What a parameter of a model type is told to the model, appended to whatever the tool's author
	 * wrote about it.
	 */
	private static final String OBJECT_ID_HINT =
		"The object's identifier, in the form \"TypeName:id\" a tool result reports it in.";

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
			truncationHint, annotation.group(), annotation.marking(), provider, method);
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
		String description = paramDescription(type, genericType, p);
		if (!description.isEmpty()) {
			json.name("description").value(description);
		}
		json.endObject();
	}

	/**
	 * The description of one parameter: what its author wrote, plus - for a parameter that carries an
	 * object - what the string is supposed to be. A model told only "the invoice" sends a label.
	 */
	private static String paramDescription(Class<?> type, Type genericType, P p) {
		String authored = p == null ? "" : p.value();
		if (!carriesObjectId(type, genericType)) {
			return authored;
		}
		return authored.isEmpty() ? OBJECT_ID_HINT : authored + " " + OBJECT_ID_HINT;
	}

	/** Whether the parameter is a model object, or a list of them. */
	private static boolean carriesObjectId(Class<?> type, Type genericType) {
		if (TLObject.class.isAssignableFrom(type)) {
			return true;
		}
		if (List.class.isAssignableFrom(type)) {
			Class<?> elementType = resolveListElementType(genericType);
			return elementType != null && TLObject.class.isAssignableFrom(elementType);
		}
		return false;
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
		} else if (TLObject.class.isAssignableFrom(type)) {
			// An object travels as its identifier: the model reads one in a tool result and names it
			// again here. The framework resolves it back before the tool method is invoked.
			json.name("type").value("string");
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
	 * @param context
	 *        The call being served, which resolves an identifier of a model-typed parameter into the
	 *        object - under the read rights of the run's principal. May be {@code null} only where no
	 *        parameter carries an object.
	 * @throws ToolArgumentCoercionException with a clean, LLM-facing message on type mismatch.
	 *         Always thrown before the tool method is invoked — safe to treat as synthetic
	 *         (no side effects) by callers deciding whether to roll back a transaction.
	 */
	static Object coerceArg(Object value, Class<?> targetType, Type genericType, String paramName,
			ToolExecutionContext context) {
		if (value == null) {
			if (targetType.isPrimitive()) {
				throw new ToolArgumentCoercionException(
					"Parameter '" + paramName + "' is required (type " + targetType.getSimpleName() + "), got null.");
			}
			return null;
		}

		try {
			return doCoerce(value, targetType, genericType, context);
		} catch (ToolArgumentCoercionException e) {
			// Already phrased for the model - a resolution that failed says why, and restating it as
			// a type mismatch would replace the reason with a guess.
			throw e;
		} catch (IllegalArgumentException e) {
			throw new ToolArgumentCoercionException(
				"Parameter '" + paramName + "': expected " + targetType.getSimpleName()
					+ ", got '" + value + "' (" + value.getClass().getSimpleName() + ").",
				e);
		}
	}

	private static Object doCoerce(Object value, Class<?> targetType, Type genericType,
			ToolExecutionContext context) {
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

		if (TLObject.class.isAssignableFrom(targetType)) {
			return resolveObject(value, targetType, context);
		}

		if (List.class.isAssignableFrom(targetType) && value instanceof List<?> list) {
			return coerceList(list, genericType, context);
		}

		if (targetType.isRecord() && value instanceof Map<?, ?> map) {
			return coerceRecord(map, targetType, context);
		}

		// LLMs sometimes stringify complex args as JSON text instead of emitting a real object.
		if (value instanceof String s) {
			String trimmed = s.trim();
			if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
				Object parsed = parseJsonTree(s);
				return doCoerce(parsed, targetType, genericType, context);
			}
		}

		return value;
	}

	/**
	 * Turns the identifier the model wrote into the object the parameter declares.
	 *
	 * <p>
	 * The read check lives in {@link ToolExecutionContext#resolveObject(String)}, so this only
	 * translates the two ways it can say no into the exception that tells the caller the tool never
	 * ran: an object of the wrong type is as unusable to the method as one that does not exist.
	 * </p>
	 */
	private static Object resolveObject(Object value, Class<?> targetType, ToolExecutionContext context) {
		if (targetType.isInstance(value)) {
			// Already the object: a caller that holds one - TL-Script's aiCallTool, a test - passes it
			// rather than writing its identifier out for this method to read back. Nothing to check
			// either: whoever produced the object read it with their own rights.
			return value;
		}
		if (context == null) {
			throw new IllegalStateException("A tool parameter of type " + targetType.getSimpleName()
				+ " is resolved under the read rights of the run's principal, and this call names no"
				+ " context to ask. Execute the tool with the context of an agent run.");
		}
		String id = String.valueOf(value);
		Object resolved;
		try {
			resolved = context.resolveObject(id);
		} catch (IllegalArgumentException e) {
			throw new ToolArgumentCoercionException(e.getMessage(), e);
		}
		if (!targetType.isInstance(resolved)) {
			throw new ToolArgumentCoercionException("'" + id + "' is not a "
				+ targetType.getSimpleName() + ".");
		}
		return resolved;
	}

	private static List<Object> coerceList(List<?> list, Type genericType, ToolExecutionContext context) {
		Class<?> elementType = resolveListElementType(genericType);
		Type elementGenericType = resolveListElementGenericType(genericType);
		if (elementType == null || elementType == String.class) {
			return new ArrayList<>(list);
		}
		List<Object> result = new ArrayList<>(list.size());
		for (Object item : list) {
			result.add(doCoerce(item, elementType, elementGenericType, context));
		}
		return result;
	}

	private static Object coerceRecord(Map<?, ?> map, Class<?> recordType, ToolExecutionContext context) {
		RecordComponent[] components = recordType.getRecordComponents();
		Object[] args = new Object[components.length];
		for (int i = 0; i < components.length; i++) {
			RecordComponent rc = components[i];
			Object raw = map.get(rc.getName());
			if (raw == null && !map.containsKey(rc.getName())) {
				// Field omitted by the LLM — use a sensible default instead of failing.
				args[i] = primitiveDefault(rc.getType());
			} else {
				args[i] = coerceArg(raw, rc.getType(), rc.getGenericType(), rc.getName(), context);
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

		private final String _group;

		private final AnswerMarking _marking;

		private final Object _provider;

		private final Method _method;

		ReflectiveTool(String name, String description, ToolSchema schema,
				ToolMode mode, boolean concurrencySafe, int truncationHint, String group,
				AnswerMarking marking, Object provider, Method method) {
			_name = name;
			_description = description;
			_schema = schema;
			_mode = mode;
			_concurrencySafe = concurrencySafe;
			_truncationHint = truncationHint;
			_group = group;
			_marking = marking;
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
		public String group() {
			return _group;
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
						params[i].getParameterizedType(), paramName, context);
				}
			}

			try {
				Object result = _method.invoke(_provider, invokeArgs);
				if (_method.getReturnType() == void.class) {
					// The framework's own word for "it worked", not an answer of the tool's.
					return new ToolResult("Success", null, _truncationHint);
				}
				if (result == null) {
					return new ToolResult("null", null, _truncationHint);
				}
				if (result instanceof ToolResult toolResult) {
					// A tool that returns the structured result speaks for itself: keep its summary
					// and content, and only fill in the truncation hint it did not specify.
					ToolResult hinted = toolResult.truncationHint() < 0
						? new ToolResult(toolResult.text(), toolResult.summary(), _truncationHint,
							toolResult.content())
						: toolResult;
					return marked(hinted);
				}
				return marked(new ToolResult(result.toString(), null, _truncationHint));
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

		/**
		 * The answer as the model is to read it: marked as untrusted content unless this tool says
		 * somebody else does that, or does not need to.
		 *
		 * <p>
		 * <b>The whole answer, not the successful half of it.</b> A reflective tool hands back a
		 * string; nothing here can tell a disclosed record from the tool's own sentence about a call
		 * that failed, and a rule that held only for some answers would be a rule the model cannot
		 * use. A tool that must draw that line draws it itself, with {@link AnswerMarking#TOOL}.
		 * </p>
		 *
		 * <p>
		 * The content parts are marked too, and not only {@link ToolResult#text()}: a multimodal
		 * result is read by the model <em>through</em> its parts, so marking the text alone would mark
		 * the one rendering nobody sees. Images and documents pass through untouched - they are not
		 * text and cannot carry a tag.
		 * </p>
		 */
		private ToolResult marked(ToolResult result) {
			if (_marking != AnswerMarking.FRAMEWORK) {
				return result;
			}
			String text = mark(result.text());
			List<ToolContent> content = result.content();
			if (text.equals(result.text()) && content.isEmpty()) {
				return result;
			}
			List<ToolContent> markedContent = new ArrayList<>(content.size());
			for (ToolContent part : content) {
				markedContent.add(part instanceof ToolContent.Text textPart
					? new ToolContent.Text(mark(textPart.text()))
					: part);
			}
			return new ToolResult(text, result.summary(), result.truncationHint(), markedContent);
		}

		/**
		 * The given text marked, unless there is nothing to mark (an empty answer) or it is marked
		 * already.
		 */
		private String mark(String text) {
			return text.isBlank() || UntrustedEnvelope.isMarked(text) ? text
				: UntrustedEnvelope.MODEL_DATA.wrap(_name, text);
		}
	}

	private AnnotatedTools() {
		// static utility
	}
}
