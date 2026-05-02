package com.top_logic.tl.ai.core.provider.openai.messages;

/**
 * Basic response fields.
 */
public class ResponseInfo extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.provider.openai.messages.ResponseInfo} instance.
	 */
	public static com.top_logic.tl.ai.core.provider.openai.messages.ResponseInfo create() {
		return new com.top_logic.tl.ai.core.provider.openai.messages.ResponseInfo();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.provider.openai.messages.ResponseInfo} type in JSON format. */
	public static final String RESPONSE_INFO__TYPE = "ResponseInfo";

	/** @see #getId() */
	public static final String ID__PROP = "id";

	/** @see #getModel() */
	public static final String MODEL__PROP = "model";

	/** @see #getStatus() */
	public static final String STATUS__PROP = "status";

	/** @see #getUsage() */
	public static final String USAGE__PROP = "usage";

	/** @see #getError() */
	public static final String ERROR__PROP = "error";

	private String _id = "";

	private String _model = "";

	private String _status = "";

	private Object _usage = null;

	private Object _error = null;

	/**
	 * Creates a {@link ResponseInfo} instance.
	 *
	 * @see com.top_logic.tl.ai.core.provider.openai.messages.ResponseInfo#create()
	 */
	protected ResponseInfo() {
		super();
	}

	public final String getId() {
		return _id;
	}

	/**
	 * @see #getId()
	 */
	public com.top_logic.tl.ai.core.provider.openai.messages.ResponseInfo setId(String value) {
		internalSetId(value);
		return this;
	}

	/** Internal setter for {@link #getId()} without chain call utility. */
	protected final void internalSetId(String value) {
		_id = value;
	}

	public final String getModel() {
		return _model;
	}

	/**
	 * @see #getModel()
	 */
	public com.top_logic.tl.ai.core.provider.openai.messages.ResponseInfo setModel(String value) {
		internalSetModel(value);
		return this;
	}

	/** Internal setter for {@link #getModel()} without chain call utility. */
	protected final void internalSetModel(String value) {
		_model = value;
	}

	public final String getStatus() {
		return _status;
	}

	/**
	 * @see #getStatus()
	 */
	public com.top_logic.tl.ai.core.provider.openai.messages.ResponseInfo setStatus(String value) {
		internalSetStatus(value);
		return this;
	}

	/** Internal setter for {@link #getStatus()} without chain call utility. */
	protected final void internalSetStatus(String value) {
		_status = value;
	}

	public final Object getUsage() {
		return _usage;
	}

	/**
	 * @see #getUsage()
	 */
	public com.top_logic.tl.ai.core.provider.openai.messages.ResponseInfo setUsage(Object value) {
		internalSetUsage(value);
		return this;
	}

	/** Internal setter for {@link #getUsage()} without chain call utility. */
	protected final void internalSetUsage(Object value) {
		_usage = value;
	}

	/**
	 * Checks, whether {@link #getUsage()} has a value.
	 */
	public final boolean hasUsage() {
		return _usage != null;
	}

	public final Object getError() {
		return _error;
	}

	/**
	 * @see #getError()
	 */
	public com.top_logic.tl.ai.core.provider.openai.messages.ResponseInfo setError(Object value) {
		internalSetError(value);
		return this;
	}

	/** Internal setter for {@link #getError()} without chain call utility. */
	protected final void internalSetError(Object value) {
		_error = value;
	}

	/**
	 * Checks, whether {@link #getError()} has a value.
	 */
	public final boolean hasError() {
		return _error != null;
	}

	@Override
	public String jsonType() {
		return RESPONSE_INFO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			ID__PROP, 
			MODEL__PROP, 
			STATUS__PROP, 
			USAGE__PROP, 
			ERROR__PROP);
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
			case ID__PROP: return getId();
			case MODEL__PROP: return getModel();
			case STATUS__PROP: return getStatus();
			case USAGE__PROP: return getUsage();
			case ERROR__PROP: return getError();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ID__PROP: internalSetId((String) value); break;
			case MODEL__PROP: internalSetModel((String) value); break;
			case STATUS__PROP: internalSetStatus((String) value); break;
			case USAGE__PROP: internalSetUsage((Object) value); break;
			case ERROR__PROP: internalSetError((Object) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.provider.openai.messages.ResponseInfo readResponseInfo(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.provider.openai.messages.ResponseInfo result = new com.top_logic.tl.ai.core.provider.openai.messages.ResponseInfo();
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
		out.name(ID__PROP);
		out.value(getId());
		out.name(MODEL__PROP);
		out.value(getModel());
		out.name(STATUS__PROP);
		out.value(getStatus());
		if (hasUsage()) {
			out.name(USAGE__PROP);
			de.haumacher.msgbuf.json.JsonUtil.writeJsonValue(out, getUsage());
		}
		if (hasError()) {
			out.name(ERROR__PROP);
			de.haumacher.msgbuf.json.JsonUtil.writeJsonValue(out, getError());
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ID__PROP: setId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case MODEL__PROP: setModel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case STATUS__PROP: setStatus(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case USAGE__PROP: setUsage(de.haumacher.msgbuf.json.JsonUtil.nextJsonValue(in)); break;
			case ERROR__PROP: setError(de.haumacher.msgbuf.json.JsonUtil.nextJsonValue(in)); break;
			default: super.readField(in, field);
		}
	}

}
