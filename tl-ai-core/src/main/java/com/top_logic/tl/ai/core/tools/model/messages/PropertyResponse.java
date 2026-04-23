package com.top_logic.tl.ai.core.tools.model.messages;

/**
 * Response for property creation/update operations.
 */
public class PropertyResponse extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.model.messages.PropertyResponse} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.model.messages.PropertyResponse create() {
		return new com.top_logic.tl.ai.core.tools.model.messages.PropertyResponse();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.model.messages.PropertyResponse} type in JSON format. */
	public static final String PROPERTY_RESPONSE__TYPE = "PropertyResponse";

	/** @see #isSuccess() */
	public static final String SUCCESS__PROP = "success";

	/** @see #getName() */
	public static final String NAME__PROP = "name";

	/** @see #getTid() */
	public static final String TID__PROP = "tid";

	/** @see #getOwner() */
	public static final String OWNER__PROP = "owner";

	/** @see #getType() */
	public static final String TYPE__PROP = "type";

	/** @see #isMandatory() */
	public static final String MANDATORY__PROP = "mandatory";

	/** @see #isMultiple() */
	public static final String MULTIPLE__PROP = "multiple";

	/** @see #isOrdered() */
	public static final String ORDERED__PROP = "ordered";

	/** @see #isBag() */
	public static final String BAG__PROP = "bag";

	/** @see #isAbstract() */
	public static final String ABSTRACT__PROP = "abstract";

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

	private String _owner = "";

	private String _type = "";

	private boolean _mandatory = false;

	private boolean _multiple = false;

	private boolean _ordered = false;

	private boolean _bag = false;

	private boolean _abstract = false;

	private boolean _created = false;

	private String _resultMessage = "";

	private String _label = "";

	private String _description = "";

	/**
	 * Creates a {@link PropertyResponse} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.model.messages.PropertyResponse#create()
	 */
	protected PropertyResponse() {
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
	public com.top_logic.tl.ai.core.tools.model.messages.PropertyResponse setSuccess(boolean value) {
		internalSetSuccess(value);
		return this;
	}

	/** Internal setter for {@link #isSuccess()} without chain call utility. */
	protected final void internalSetSuccess(boolean value) {
		_success = value;
	}

	/**
	 * Property name.
	 */
	public final String getName() {
		return _name;
	}

	/**
	 * @see #getName()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.PropertyResponse setName(String value) {
		internalSetName(value);
		return this;
	}

	/** Internal setter for {@link #getName()} without chain call utility. */
	protected final void internalSetName(String value) {
		_name = value;
	}

	/**
	 * Property TID.
	 */
	public final String getTid() {
		return _tid;
	}

	/**
	 * @see #getTid()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.PropertyResponse setTid(String value) {
		internalSetTid(value);
		return this;
	}

	/** Internal setter for {@link #getTid()} without chain call utility. */
	protected final void internalSetTid(String value) {
		_tid = value;
	}

	/**
	 * Owner class (module:ClassName).
	 */
	public final String getOwner() {
		return _owner;
	}

	/**
	 * @see #getOwner()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.PropertyResponse setOwner(String value) {
		internalSetOwner(value);
		return this;
	}

	/** Internal setter for {@link #getOwner()} without chain call utility. */
	protected final void internalSetOwner(String value) {
		_owner = value;
	}

	/**
	 * Property type name.
	 */
	public final String getType() {
		return _type;
	}

	/**
	 * @see #getType()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.PropertyResponse setType(String value) {
		internalSetType(value);
		return this;
	}

	/** Internal setter for {@link #getType()} without chain call utility. */
	protected final void internalSetType(String value) {
		_type = value;
	}

	/**
	 * Whether mandatory.
	 */
	public final boolean isMandatory() {
		return _mandatory;
	}

	/**
	 * @see #isMandatory()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.PropertyResponse setMandatory(boolean value) {
		internalSetMandatory(value);
		return this;
	}

	/** Internal setter for {@link #isMandatory()} without chain call utility. */
	protected final void internalSetMandatory(boolean value) {
		_mandatory = value;
	}

	/**
	 * Whether multiple-valued.
	 */
	public final boolean isMultiple() {
		return _multiple;
	}

	/**
	 * @see #isMultiple()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.PropertyResponse setMultiple(boolean value) {
		internalSetMultiple(value);
		return this;
	}

	/** Internal setter for {@link #isMultiple()} without chain call utility. */
	protected final void internalSetMultiple(boolean value) {
		_multiple = value;
	}

	/**
	 * Whether ordered.
	 */
	public final boolean isOrdered() {
		return _ordered;
	}

	/**
	 * @see #isOrdered()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.PropertyResponse setOrdered(boolean value) {
		internalSetOrdered(value);
		return this;
	}

	/** Internal setter for {@link #isOrdered()} without chain call utility. */
	protected final void internalSetOrdered(boolean value) {
		_ordered = value;
	}

	/**
	 * Whether bag.
	 */
	public final boolean isBag() {
		return _bag;
	}

	/**
	 * @see #isBag()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.PropertyResponse setBag(boolean value) {
		internalSetBag(value);
		return this;
	}

	/** Internal setter for {@link #isBag()} without chain call utility. */
	protected final void internalSetBag(boolean value) {
		_bag = value;
	}

	/**
	 * Whether abstract.
	 */
	public final boolean isAbstract() {
		return _abstract;
	}

	/**
	 * @see #isAbstract()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.PropertyResponse setAbstract(boolean value) {
		internalSetAbstract(value);
		return this;
	}

	/** Internal setter for {@link #isAbstract()} without chain call utility. */
	protected final void internalSetAbstract(boolean value) {
		_abstract = value;
	}

	/**
	 * Whether the property was newly created.
	 */
	public final boolean isCreated() {
		return _created;
	}

	/**
	 * @see #isCreated()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.PropertyResponse setCreated(boolean value) {
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
	public com.top_logic.tl.ai.core.tools.model.messages.PropertyResponse setResultMessage(String value) {
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
	public com.top_logic.tl.ai.core.tools.model.messages.PropertyResponse setLabel(String value) {
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
	public com.top_logic.tl.ai.core.tools.model.messages.PropertyResponse setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	/** Internal setter for {@link #getDescription()} without chain call utility. */
	protected final void internalSetDescription(String value) {
		_description = value;
	}

	@Override
	public String jsonType() {
		return PROPERTY_RESPONSE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			SUCCESS__PROP, 
			NAME__PROP, 
			TID__PROP, 
			OWNER__PROP, 
			TYPE__PROP, 
			MANDATORY__PROP, 
			MULTIPLE__PROP, 
			ORDERED__PROP, 
			BAG__PROP, 
			ABSTRACT__PROP, 
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
			case OWNER__PROP: return getOwner();
			case TYPE__PROP: return getType();
			case MANDATORY__PROP: return isMandatory();
			case MULTIPLE__PROP: return isMultiple();
			case ORDERED__PROP: return isOrdered();
			case BAG__PROP: return isBag();
			case ABSTRACT__PROP: return isAbstract();
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
			case OWNER__PROP: internalSetOwner((String) value); break;
			case TYPE__PROP: internalSetType((String) value); break;
			case MANDATORY__PROP: internalSetMandatory((boolean) value); break;
			case MULTIPLE__PROP: internalSetMultiple((boolean) value); break;
			case ORDERED__PROP: internalSetOrdered((boolean) value); break;
			case BAG__PROP: internalSetBag((boolean) value); break;
			case ABSTRACT__PROP: internalSetAbstract((boolean) value); break;
			case CREATED__PROP: internalSetCreated((boolean) value); break;
			case RESULT_MESSAGE__PROP: internalSetResultMessage((String) value); break;
			case LABEL__PROP: internalSetLabel((String) value); break;
			case DESCRIPTION__PROP: internalSetDescription((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.model.messages.PropertyResponse readPropertyResponse(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.model.messages.PropertyResponse result = new com.top_logic.tl.ai.core.tools.model.messages.PropertyResponse();
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
		out.name(OWNER__PROP);
		out.value(getOwner());
		out.name(TYPE__PROP);
		out.value(getType());
		out.name(MANDATORY__PROP);
		out.value(isMandatory());
		out.name(MULTIPLE__PROP);
		out.value(isMultiple());
		out.name(ORDERED__PROP);
		out.value(isOrdered());
		out.name(BAG__PROP);
		out.value(isBag());
		out.name(ABSTRACT__PROP);
		out.value(isAbstract());
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
			case OWNER__PROP: setOwner(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TYPE__PROP: setType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case MANDATORY__PROP: setMandatory(in.nextBoolean()); break;
			case MULTIPLE__PROP: setMultiple(in.nextBoolean()); break;
			case ORDERED__PROP: setOrdered(in.nextBoolean()); break;
			case BAG__PROP: setBag(in.nextBoolean()); break;
			case ABSTRACT__PROP: setAbstract(in.nextBoolean()); break;
			case CREATED__PROP: setCreated(in.nextBoolean()); break;
			case RESULT_MESSAGE__PROP: setResultMessage(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case LABEL__PROP: setLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DESCRIPTION__PROP: setDescription(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
