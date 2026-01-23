package com.top_logic.ai.service.tools.help.messages;

/**
 * Result message containing the Model Explorer's text response.
 */
public class ModelExplorerResponse extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.service.tools.help.messages.ModelExplorerResponse} instance.
	 */
	public static com.top_logic.ai.service.tools.help.messages.ModelExplorerResponse create() {
		return new com.top_logic.ai.service.tools.help.messages.ModelExplorerResponse();
	}

	/** Identifier for the {@link com.top_logic.ai.service.tools.help.messages.ModelExplorerResponse} type in JSON format. */
	public static final String MODEL_EXPLORER_RESPONSE__TYPE = "ModelExplorerResponse";

	/** @see #getResult() */
	public static final String RESULT__PROP = "result";

	private String _result = "";

	/**
	 * Creates a {@link ModelExplorerResponse} instance.
	 *
	 * @see com.top_logic.ai.service.tools.help.messages.ModelExplorerResponse#create()
	 */
	protected ModelExplorerResponse() {
		super();
	}

	/**
	 * The exploration result as formatted text with bullet points,
	 * optional next step suggestions, and source citations.
	 */
	public final String getResult() {
		return _result;
	}

	/**
	 * @see #getResult()
	 */
	public com.top_logic.ai.service.tools.help.messages.ModelExplorerResponse setResult(String value) {
		internalSetResult(value);
		return this;
	}

	/** Internal setter for {@link #getResult()} without chain call utility. */
	protected final void internalSetResult(String value) {
		_result = value;
	}

	@Override
	public String jsonType() {
		return MODEL_EXPLORER_RESPONSE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			RESULT__PROP);
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
			case RESULT__PROP: return getResult();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case RESULT__PROP: internalSetResult((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.service.tools.help.messages.ModelExplorerResponse readModelExplorerResponse(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.service.tools.help.messages.ModelExplorerResponse result = new com.top_logic.ai.service.tools.help.messages.ModelExplorerResponse();
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
		out.name(RESULT__PROP);
		out.value(getResult());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case RESULT__PROP: setResult(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
