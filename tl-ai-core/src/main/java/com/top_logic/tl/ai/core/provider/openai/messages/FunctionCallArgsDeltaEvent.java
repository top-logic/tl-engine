package com.top_logic.tl.ai.core.provider.openai.messages;

/**
 * {@code response.function_call_arguments.delta}.
 */
public class FunctionCallArgsDeltaEvent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.provider.openai.messages.FunctionCallArgsDeltaEvent} instance.
	 */
	public static com.top_logic.tl.ai.core.provider.openai.messages.FunctionCallArgsDeltaEvent create() {
		return new com.top_logic.tl.ai.core.provider.openai.messages.FunctionCallArgsDeltaEvent();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.provider.openai.messages.FunctionCallArgsDeltaEvent} type in JSON format. */
	public static final String FUNCTION_CALL_ARGS_DELTA_EVENT__TYPE = "FunctionCallArgsDeltaEvent";

	/** @see #getType() */
	public static final String TYPE__PROP = "type";

	/** @see #getOutputIndex() */
	public static final String OUTPUT_INDEX__PROP = "output_index";

	/** @see #getDelta() */
	public static final String DELTA__PROP = "delta";

	private String _type = "";

	private int _outputIndex = 0;

	private String _delta = "";

	/**
	 * Creates a {@link FunctionCallArgsDeltaEvent} instance.
	 *
	 * @see com.top_logic.tl.ai.core.provider.openai.messages.FunctionCallArgsDeltaEvent#create()
	 */
	protected FunctionCallArgsDeltaEvent() {
		super();
	}

	public final String getType() {
		return _type;
	}

	/**
	 * @see #getType()
	 */
	public com.top_logic.tl.ai.core.provider.openai.messages.FunctionCallArgsDeltaEvent setType(String value) {
		internalSetType(value);
		return this;
	}

	/** Internal setter for {@link #getType()} without chain call utility. */
	protected final void internalSetType(String value) {
		_type = value;
	}

	public final int getOutputIndex() {
		return _outputIndex;
	}

	/**
	 * @see #getOutputIndex()
	 */
	public com.top_logic.tl.ai.core.provider.openai.messages.FunctionCallArgsDeltaEvent setOutputIndex(int value) {
		internalSetOutputIndex(value);
		return this;
	}

	/** Internal setter for {@link #getOutputIndex()} without chain call utility. */
	protected final void internalSetOutputIndex(int value) {
		_outputIndex = value;
	}

	public final String getDelta() {
		return _delta;
	}

	/**
	 * @see #getDelta()
	 */
	public com.top_logic.tl.ai.core.provider.openai.messages.FunctionCallArgsDeltaEvent setDelta(String value) {
		internalSetDelta(value);
		return this;
	}

	/** Internal setter for {@link #getDelta()} without chain call utility. */
	protected final void internalSetDelta(String value) {
		_delta = value;
	}

	@Override
	public String jsonType() {
		return FUNCTION_CALL_ARGS_DELTA_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TYPE__PROP, 
			OUTPUT_INDEX__PROP, 
			DELTA__PROP);
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
			case TYPE__PROP: return getType();
			case OUTPUT_INDEX__PROP: return getOutputIndex();
			case DELTA__PROP: return getDelta();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TYPE__PROP: internalSetType((String) value); break;
			case OUTPUT_INDEX__PROP: internalSetOutputIndex((int) value); break;
			case DELTA__PROP: internalSetDelta((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.provider.openai.messages.FunctionCallArgsDeltaEvent readFunctionCallArgsDeltaEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.provider.openai.messages.FunctionCallArgsDeltaEvent result = new com.top_logic.tl.ai.core.provider.openai.messages.FunctionCallArgsDeltaEvent();
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
		out.name(TYPE__PROP);
		out.value(getType());
		out.name(OUTPUT_INDEX__PROP);
		out.value(getOutputIndex());
		out.name(DELTA__PROP);
		out.value(getDelta());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TYPE__PROP: setType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case OUTPUT_INDEX__PROP: setOutputIndex(in.nextInt()); break;
			case DELTA__PROP: setDelta(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
