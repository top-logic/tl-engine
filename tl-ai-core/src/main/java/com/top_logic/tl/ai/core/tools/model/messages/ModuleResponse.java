package com.top_logic.tl.ai.core.tools.model.messages;

/**
 * Response for module creation/update operations.
 */
public class ModuleResponse extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.model.messages.ModuleResponse} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.model.messages.ModuleResponse create() {
		return new com.top_logic.tl.ai.core.tools.model.messages.ModuleResponse();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.model.messages.ModuleResponse} type in JSON format. */
	public static final String MODULE_RESPONSE__TYPE = "ModuleResponse";

	/** @see #isSuccess() */
	public static final String SUCCESS__PROP = "success";

	/** @see #getName() */
	public static final String NAME__PROP = "name";

	/** @see #getTid() */
	public static final String TID__PROP = "tid";

	/** @see #getTypeCount() */
	public static final String TYPE_COUNT__PROP = "typeCount";

	/** @see #isCreated() */
	public static final String CREATED__PROP = "created";

	/** @see #getResultMessage() */
	public static final String RESULT_MESSAGE__PROP = "resultMessage";

	/** @see #getLabel() */
	public static final String LABEL__PROP = "label";

	/** @see #getDescription() */
	public static final String DESCRIPTION__PROP = "description";

	private boolean _success = false;

	private String _name = "";

	private String _tid = "";

	private long _typeCount = 0L;

	private boolean _created = false;

	private String _resultMessage = "";

	private String _label = "";

	private String _description = "";

	/**
	 * Creates a {@link ModuleResponse} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.model.messages.ModuleResponse#create()
	 */
	protected ModuleResponse() {
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
	public com.top_logic.tl.ai.core.tools.model.messages.ModuleResponse setSuccess(boolean value) {
		internalSetSuccess(value);
		return this;
	}

	/** Internal setter for {@link #isSuccess()} without chain call utility. */
	protected final void internalSetSuccess(boolean value) {
		_success = value;
	}

	/**
	 * Module name.
	 */
	public final String getName() {
		return _name;
	}

	/**
	 * @see #getName()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.ModuleResponse setName(String value) {
		internalSetName(value);
		return this;
	}

	/** Internal setter for {@link #getName()} without chain call utility. */
	protected final void internalSetName(String value) {
		_name = value;
	}

	/**
	 * Module TID.
	 */
	public final String getTid() {
		return _tid;
	}

	/**
	 * @see #getTid()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.ModuleResponse setTid(String value) {
		internalSetTid(value);
		return this;
	}

	/** Internal setter for {@link #getTid()} without chain call utility. */
	protected final void internalSetTid(String value) {
		_tid = value;
	}

	/**
	 * Number of types in the module.
	 */
	public final long getTypeCount() {
		return _typeCount;
	}

	/**
	 * @see #getTypeCount()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.ModuleResponse setTypeCount(long value) {
		internalSetTypeCount(value);
		return this;
	}

	/** Internal setter for {@link #getTypeCount()} without chain call utility. */
	protected final void internalSetTypeCount(long value) {
		_typeCount = value;
	}

	/**
	 * Whether the module was newly created.
	 */
	public final boolean isCreated() {
		return _created;
	}

	/**
	 * @see #isCreated()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.ModuleResponse setCreated(boolean value) {
		internalSetCreated(value);
		return this;
	}

	/** Internal setter for {@link #isCreated()} without chain call utility. */
	protected final void internalSetCreated(boolean value) {
		_created = value;
	}

	/**
	 * Human-readable result message.
	 */
	public final String getResultMessage() {
		return _resultMessage;
	}

	/**
	 * @see #getResultMessage()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.ModuleResponse setResultMessage(String value) {
		internalSetResultMessage(value);
		return this;
	}

	/** Internal setter for {@link #getResultMessage()} without chain call utility. */
	protected final void internalSetResultMessage(String value) {
		_resultMessage = value;
	}

	/**
	 * Localized label.
	 */
	public final String getLabel() {
		return _label;
	}

	/**
	 * @see #getLabel()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.ModuleResponse setLabel(String value) {
		internalSetLabel(value);
		return this;
	}

	/** Internal setter for {@link #getLabel()} without chain call utility. */
	protected final void internalSetLabel(String value) {
		_label = value;
	}

	/**
	 * Localized description.
	 */
	public final String getDescription() {
		return _description;
	}

	/**
	 * @see #getDescription()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.ModuleResponse setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	/** Internal setter for {@link #getDescription()} without chain call utility. */
	protected final void internalSetDescription(String value) {
		_description = value;
	}

	@Override
	public String jsonType() {
		return MODULE_RESPONSE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			SUCCESS__PROP, 
			NAME__PROP, 
			TID__PROP, 
			TYPE_COUNT__PROP, 
			CREATED__PROP, 
			RESULT_MESSAGE__PROP, 
			LABEL__PROP, 
			DESCRIPTION__PROP);
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
			case TYPE_COUNT__PROP: return getTypeCount();
			case CREATED__PROP: return isCreated();
			case RESULT_MESSAGE__PROP: return getResultMessage();
			case LABEL__PROP: return getLabel();
			case DESCRIPTION__PROP: return getDescription();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case SUCCESS__PROP: internalSetSuccess((boolean) value); break;
			case NAME__PROP: internalSetName((String) value); break;
			case TID__PROP: internalSetTid((String) value); break;
			case TYPE_COUNT__PROP: internalSetTypeCount((long) value); break;
			case CREATED__PROP: internalSetCreated((boolean) value); break;
			case RESULT_MESSAGE__PROP: internalSetResultMessage((String) value); break;
			case LABEL__PROP: internalSetLabel((String) value); break;
			case DESCRIPTION__PROP: internalSetDescription((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.model.messages.ModuleResponse readModuleResponse(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.model.messages.ModuleResponse result = new com.top_logic.tl.ai.core.tools.model.messages.ModuleResponse();
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
		out.name(TYPE_COUNT__PROP);
		out.value(getTypeCount());
		out.name(CREATED__PROP);
		out.value(isCreated());
		out.name(RESULT_MESSAGE__PROP);
		out.value(getResultMessage());
		out.name(LABEL__PROP);
		out.value(getLabel());
		out.name(DESCRIPTION__PROP);
		out.value(getDescription());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case SUCCESS__PROP: setSuccess(in.nextBoolean()); break;
			case NAME__PROP: setName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TID__PROP: setTid(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TYPE_COUNT__PROP: setTypeCount(in.nextLong()); break;
			case CREATED__PROP: setCreated(in.nextBoolean()); break;
			case RESULT_MESSAGE__PROP: setResultMessage(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case LABEL__PROP: setLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DESCRIPTION__PROP: setDescription(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
