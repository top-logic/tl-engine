package com.top_logic.tl.ai.core.tools.model.messages;

/**
 * Response for enumeration creation/update operations.
 */
public class EnumResponse extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.model.messages.EnumResponse} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.model.messages.EnumResponse create() {
		return new com.top_logic.tl.ai.core.tools.model.messages.EnumResponse();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.model.messages.EnumResponse} type in JSON format. */
	public static final String ENUM_RESPONSE__TYPE = "EnumResponse";

	/** @see #isSuccess() */
	public static final String SUCCESS__PROP = "success";

	/** @see #getName() */
	public static final String NAME__PROP = "name";

	/** @see #getTid() */
	public static final String TID__PROP = "tid";

	/** @see #isCreated() */
	public static final String CREATED__PROP = "created";

	private boolean _success = false;

	private String _name = "";

	private String _tid = "";

	private boolean _created = false;

	/**
	 * Creates a {@link EnumResponse} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.model.messages.EnumResponse#create()
	 */
	protected EnumResponse() {
		super();
	}

	/**
	 * Whether the operation succeeded.
	 */
	public final boolean isSuccess() {
		return _success;
	}

	/**
	 * @see #isSuccess()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.EnumResponse setSuccess(boolean value) {
		internalSetSuccess(value);
		return this;
	}

	/** Internal setter for {@link #isSuccess()} without chain call utility. */
	protected final void internalSetSuccess(boolean value) {
		_success = value;
	}

	/**
	 * Enumeration name.
	 */
	public final String getName() {
		return _name;
	}

	/**
	 * @see #getName()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.EnumResponse setName(String value) {
		internalSetName(value);
		return this;
	}

	/** Internal setter for {@link #getName()} without chain call utility. */
	protected final void internalSetName(String value) {
		_name = value;
	}

	/**
	 * Enumeration TID.
	 */
	public final String getTid() {
		return _tid;
	}

	/**
	 * @see #getTid()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.EnumResponse setTid(String value) {
		internalSetTid(value);
		return this;
	}

	/** Internal setter for {@link #getTid()} without chain call utility. */
	protected final void internalSetTid(String value) {
		_tid = value;
	}

	/**
	 * Whether the enumeration was newly created.
	 */
	public final boolean isCreated() {
		return _created;
	}

	/**
	 * @see #isCreated()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.EnumResponse setCreated(boolean value) {
		internalSetCreated(value);
		return this;
	}

	/** Internal setter for {@link #isCreated()} without chain call utility. */
	protected final void internalSetCreated(boolean value) {
		_created = value;
	}

	@Override
	public String jsonType() {
		return ENUM_RESPONSE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			SUCCESS__PROP, 
			NAME__PROP, 
			TID__PROP, 
			CREATED__PROP);
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
			case SUCCESS__PROP: return isSuccess();
			case NAME__PROP: return getName();
			case TID__PROP: return getTid();
			case CREATED__PROP: return isCreated();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case SUCCESS__PROP: internalSetSuccess((boolean) value); break;
			case NAME__PROP: internalSetName((String) value); break;
			case TID__PROP: internalSetTid((String) value); break;
			case CREATED__PROP: internalSetCreated((boolean) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.model.messages.EnumResponse readEnumResponse(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.model.messages.EnumResponse result = new com.top_logic.tl.ai.core.tools.model.messages.EnumResponse();
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
		out.name(SUCCESS__PROP);
		out.value(isSuccess());
		out.name(NAME__PROP);
		out.value(getName());
		out.name(TID__PROP);
		out.value(getTid());
		out.name(CREATED__PROP);
		out.value(isCreated());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case SUCCESS__PROP: setSuccess(in.nextBoolean()); break;
			case NAME__PROP: setName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TID__PROP: setTid(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CREATED__PROP: setCreated(in.nextBoolean()); break;
			default: super.readField(in, field);
		}
	}

}
