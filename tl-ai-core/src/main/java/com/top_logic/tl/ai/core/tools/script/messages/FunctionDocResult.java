package com.top_logic.tl.ai.core.tools.script.messages;

/**
 * Result message containing detailed documentation for a TLScript function.
 */
public class FunctionDocResult extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.script.messages.FunctionDocResult} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.script.messages.FunctionDocResult create() {
		return new com.top_logic.tl.ai.core.tools.script.messages.FunctionDocResult();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.script.messages.FunctionDocResult} type in JSON format. */
	public static final String FUNCTION_DOC_RESULT__TYPE = "FunctionDocResult";

	/** @see #getDocumentation() */
	public static final String DOCUMENTATION__PROP = "documentation";

	/** @see #getParameters() */
	public static final String PARAMETERS__PROP = "parameters";

	/** @see #getErrorMessage() */
	public static final String ERROR_MESSAGE__PROP = "errorMessage";

	private String _documentation = "";

	private final java.util.List<String> _parameters = new java.util.ArrayList<>();

	private String _errorMessage = "";

	/**
	 * Creates a {@link FunctionDocResult} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.script.messages.FunctionDocResult#create()
	 */
	protected FunctionDocResult() {
		super();
	}

	/**
	 * The function documentation in Markdown format (when available).
	 */
	public final String getDocumentation() {
		return _documentation;
	}

	/**
	 * @see #getDocumentation()
	 */
	public com.top_logic.tl.ai.core.tools.script.messages.FunctionDocResult setDocumentation(String value) {
		internalSetDocumentation(value);
		return this;
	}

	/** Internal setter for {@link #getDocumentation()} without chain call utility. */
	protected final void internalSetDocumentation(String value) {
		_documentation = value;
	}

	/**
	 * List of parameter names for the function.
	 * Contains "..." for varargs functions.
	 */
	public final java.util.List<String> getParameters() {
		return _parameters;
	}

	/**
	 * @see #getParameters()
	 */
	public com.top_logic.tl.ai.core.tools.script.messages.FunctionDocResult setParameters(java.util.List<? extends String> value) {
		internalSetParameters(value);
		return this;
	}

	/** Internal setter for {@link #getParameters()} without chain call utility. */
	protected final void internalSetParameters(java.util.List<? extends String> value) {
		_parameters.clear();
		_parameters.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getParameters()} list.
	 */
	public com.top_logic.tl.ai.core.tools.script.messages.FunctionDocResult addParameter(String value) {
		internalAddParameter(value);
		return this;
	}

	/** Implementation of {@link #addParameter(String)} without chain call utility. */
	protected final void internalAddParameter(String value) {
		_parameters.add(value);
	}

	/**
	 * Removes a value from the {@link #getParameters()} list.
	 */
	public final void removeParameter(String value) {
		_parameters.remove(value);
	}

	/**
	 * Error message if documentation retrieval failed.
	 */
	public final String getErrorMessage() {
		return _errorMessage;
	}

	/**
	 * @see #getErrorMessage()
	 */
	public com.top_logic.tl.ai.core.tools.script.messages.FunctionDocResult setErrorMessage(String value) {
		internalSetErrorMessage(value);
		return this;
	}

	/** Internal setter for {@link #getErrorMessage()} without chain call utility. */
	protected final void internalSetErrorMessage(String value) {
		_errorMessage = value;
	}

	@Override
	public String jsonType() {
		return FUNCTION_DOC_RESULT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			DOCUMENTATION__PROP, 
			PARAMETERS__PROP, 
			ERROR_MESSAGE__PROP);
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
			case DOCUMENTATION__PROP: return getDocumentation();
			case PARAMETERS__PROP: return getParameters();
			case ERROR_MESSAGE__PROP: return getErrorMessage();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case DOCUMENTATION__PROP: internalSetDocumentation((String) value); break;
			case PARAMETERS__PROP: internalSetParameters(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
			case ERROR_MESSAGE__PROP: internalSetErrorMessage((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.script.messages.FunctionDocResult readFunctionDocResult(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.script.messages.FunctionDocResult result = new com.top_logic.tl.ai.core.tools.script.messages.FunctionDocResult();
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
		out.name(DOCUMENTATION__PROP);
		out.value(getDocumentation());
		out.name(PARAMETERS__PROP);
		out.beginArray();
		for (String x : getParameters()) {
			out.value(x);
		}
		out.endArray();
		out.name(ERROR_MESSAGE__PROP);
		out.value(getErrorMessage());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case DOCUMENTATION__PROP: setDocumentation(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case PARAMETERS__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setParameters(newValue);
			}
			break;
			case ERROR_MESSAGE__PROP: setErrorMessage(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
