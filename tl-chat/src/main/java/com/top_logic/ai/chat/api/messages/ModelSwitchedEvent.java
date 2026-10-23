package com.top_logic.ai.chat.api.messages;

/**
 * SSE event for a run that continues on another model.
 */
public class ModelSwitchedEvent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.ModelSwitchedEvent} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.ModelSwitchedEvent create() {
		return new com.top_logic.ai.chat.api.messages.ModelSwitchedEvent();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.ModelSwitchedEvent} type in JSON format. */
	public static final String MODEL_SWITCHED_EVENT__TYPE = "ModelSwitchedEvent";

	/** @see #getFromModel() */
	public static final String FROM_MODEL__PROP = "fromModel";

	/** @see #getToModel() */
	public static final String TO_MODEL__PROP = "toModel";

	/** @see #getReason() */
	public static final String REASON__PROP = "reason";

	private String _fromModel = "";

	private String _toModel = "";

	private String _reason = "";

	/**
	 * Creates a {@link ModelSwitchedEvent} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.ModelSwitchedEvent#create()
	 */
	protected ModelSwitchedEvent() {
		super();
	}

	/**
	 * The model the run was using, qualified by its provider where one was named.
	 */
	public final String getFromModel() {
		return _fromModel;
	}

	/**
	 * @see #getFromModel()
	 */
	public com.top_logic.ai.chat.api.messages.ModelSwitchedEvent setFromModel(String value) {
		internalSetFromModel(value);
		return this;
	}

	/** Internal setter for {@link #getFromModel()} without chain call utility. */
	protected final void internalSetFromModel(String value) {
		_fromModel = value;
	}

	/**
	 * The model the run continues with, qualified the same way.
	 */
	public final String getToModel() {
		return _toModel;
	}

	/**
	 * @see #getToModel()
	 */
	public com.top_logic.ai.chat.api.messages.ModelSwitchedEvent setToModel(String value) {
		internalSetToModel(value);
		return this;
	}

	/** Internal setter for {@link #getToModel()} without chain call utility. */
	protected final void internalSetToModel(String value) {
		_toModel = value;
	}

	/**
	 * Why the previous model was left, in one line.
	 */
	public final String getReason() {
		return _reason;
	}

	/**
	 * @see #getReason()
	 */
	public com.top_logic.ai.chat.api.messages.ModelSwitchedEvent setReason(String value) {
		internalSetReason(value);
		return this;
	}

	/** Internal setter for {@link #getReason()} without chain call utility. */
	protected final void internalSetReason(String value) {
		_reason = value;
	}

	@Override
	public String jsonType() {
		return MODEL_SWITCHED_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			FROM_MODEL__PROP, 
			TO_MODEL__PROP, 
			REASON__PROP);
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
			case FROM_MODEL__PROP: return getFromModel();
			case TO_MODEL__PROP: return getToModel();
			case REASON__PROP: return getReason();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case FROM_MODEL__PROP: internalSetFromModel((String) value); break;
			case TO_MODEL__PROP: internalSetToModel((String) value); break;
			case REASON__PROP: internalSetReason((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.ModelSwitchedEvent readModelSwitchedEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.ModelSwitchedEvent result = new com.top_logic.ai.chat.api.messages.ModelSwitchedEvent();
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
		out.name(FROM_MODEL__PROP);
		out.value(getFromModel());
		out.name(TO_MODEL__PROP);
		out.value(getToModel());
		out.name(REASON__PROP);
		out.value(getReason());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case FROM_MODEL__PROP: setFromModel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TO_MODEL__PROP: setToModel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case REASON__PROP: setReason(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
