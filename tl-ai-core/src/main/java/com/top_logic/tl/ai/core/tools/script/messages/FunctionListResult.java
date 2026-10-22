package com.top_logic.tl.ai.core.tools.script.messages;

/**
 * Result message containing a list of available TLScript function names.
 */
public class FunctionListResult extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.script.messages.FunctionListResult} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.script.messages.FunctionListResult create() {
		return new com.top_logic.tl.ai.core.tools.script.messages.FunctionListResult();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.script.messages.FunctionListResult} type in JSON format. */
	public static final String FUNCTION_LIST_RESULT__TYPE = "FunctionListResult";

	/** @see #getFunctions() */
	public static final String FUNCTIONS__PROP = "functions";

	private final java.util.List<String> _functions = new java.util.ArrayList<>();

	/**
	 * Creates a {@link FunctionListResult} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.script.messages.FunctionListResult#create()
	 */
	protected FunctionListResult() {
		super();
	}

	/**
	 * List of function names that can be used in TLScript expressions.
	 */
	public final java.util.List<String> getFunctions() {
		return _functions;
	}

	/**
	 * @see #getFunctions()
	 */
	public com.top_logic.tl.ai.core.tools.script.messages.FunctionListResult setFunctions(java.util.List<? extends String> value) {
		internalSetFunctions(value);
		return this;
	}

	/** Internal setter for {@link #getFunctions()} without chain call utility. */
	protected final void internalSetFunctions(java.util.List<? extends String> value) {
		_functions.clear();
		_functions.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getFunctions()} list.
	 */
	public com.top_logic.tl.ai.core.tools.script.messages.FunctionListResult addFunction(String value) {
		internalAddFunction(value);
		return this;
	}

	/** Implementation of {@link #addFunction(String)} without chain call utility. */
	protected final void internalAddFunction(String value) {
		_functions.add(value);
	}

	/**
	 * Removes a value from the {@link #getFunctions()} list.
	 */
	public final void removeFunction(String value) {
		_functions.remove(value);
	}

	@Override
	public String jsonType() {
		return FUNCTION_LIST_RESULT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			FUNCTIONS__PROP);
		PROPERTIES = java.util.Collections.unmodifiableList(local);
	}

	static final java.util.Set<String> TRANSIENT_PROPERTIES;
	static {
		java.util.HashSet<String> tmp = new java.util.HashSet<>();
		tmp.addAll(java.util.Arrays.asList(
				));
		TRANSIENT_PROPERTIES = java.util.Collections.unmodifiableSet(tmp);
	}

	@Override
	public java.util.List<String> properties() {
		return PROPERTIES;
	}

	@Override
	public java.util.Set<String> transientProperties() {
		return TRANSIENT_PROPERTIES;
	}

	@Override
	public Object get(String field) {
		switch (field) {
			case FUNCTIONS__PROP: return getFunctions();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case FUNCTIONS__PROP: internalSetFunctions(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.script.messages.FunctionListResult readFunctionListResult(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.script.messages.FunctionListResult result = new com.top_logic.tl.ai.core.tools.script.messages.FunctionListResult();
		result.readContent(in);
		return result;
	}

	@Override
	public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		writeContent(out);
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(FUNCTIONS__PROP);
		out.beginArray();
		for (String x : getFunctions()) {
			out.value(x);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case FUNCTIONS__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setFunctions(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
