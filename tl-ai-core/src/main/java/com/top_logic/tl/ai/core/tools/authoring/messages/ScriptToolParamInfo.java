package com.top_logic.tl.ai.core.tools.authoring.messages;

/**
 * One declared parameter of a TL-Script tool.
 */
public class ScriptToolParamInfo extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolParamInfo} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolParamInfo create() {
		return new com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolParamInfo();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolParamInfo} type in JSON format. */
	public static final String SCRIPT_TOOL_PARAM_INFO__TYPE = "ScriptToolParamInfo";

	/** @see #getName() */
	public static final String NAME__PROP = "name";

	/** @see #getType() */
	public static final String TYPE__PROP = "type";

	/** @see #getDescription() */
	public static final String DESCRIPTION__PROP = "description";

	/** @see #isMandatory() */
	public static final String MANDATORY__PROP = "mandatory";

	/** @see #isMultiple() */
	public static final String MULTIPLE__PROP = "multiple";

	private String _name = "";

	private String _type = "";

	private String _description = "";

	private boolean _mandatory = false;

	private boolean _multiple = false;

	/**
	 * Creates a {@link ScriptToolParamInfo} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolParamInfo#create()
	 */
	protected ScriptToolParamInfo() {
		super();
	}

	/**
	 * Parameter name (the binding name used as $name in the script body).
	 */
	public final String getName() {
		return _name;
	}

	/**
	 * @see #getName()
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolParamInfo setName(String value) {
		internalSetName(value);
		return this;
	}

	/** Internal setter for {@link #getName()} without chain call utility. */
	protected final void internalSetName(String value) {
		_name = value;
	}

	/**
	 * Qualified TL type, e.g. tl.core:String.
	 */
	public final String getType() {
		return _type;
	}

	/**
	 * @see #getType()
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolParamInfo setType(String value) {
		internalSetType(value);
		return this;
	}

	/** Internal setter for {@link #getType()} without chain call utility. */
	protected final void internalSetType(String value) {
		_type = value;
	}

	/**
	 * Parameter description.
	 */
	public final String getDescription() {
		return _description;
	}

	/**
	 * @see #getDescription()
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolParamInfo setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	/** Internal setter for {@link #getDescription()} without chain call utility. */
	protected final void internalSetDescription(String value) {
		_description = value;
	}

	/**
	 * Whether the parameter must be supplied.
	 */
	public final boolean isMandatory() {
		return _mandatory;
	}

	/**
	 * @see #isMandatory()
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolParamInfo setMandatory(boolean value) {
		internalSetMandatory(value);
		return this;
	}

	/** Internal setter for {@link #isMandatory()} without chain call utility. */
	protected final void internalSetMandatory(boolean value) {
		_mandatory = value;
	}

	/**
	 * Whether the parameter accepts a list of values.
	 */
	public final boolean isMultiple() {
		return _multiple;
	}

	/**
	 * @see #isMultiple()
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolParamInfo setMultiple(boolean value) {
		internalSetMultiple(value);
		return this;
	}

	/** Internal setter for {@link #isMultiple()} without chain call utility. */
	protected final void internalSetMultiple(boolean value) {
		_multiple = value;
	}

	@Override
	public String jsonType() {
		return SCRIPT_TOOL_PARAM_INFO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			NAME__PROP, 
			TYPE__PROP, 
			DESCRIPTION__PROP, 
			MANDATORY__PROP, 
			MULTIPLE__PROP);
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
			case NAME__PROP: return getName();
			case TYPE__PROP: return getType();
			case DESCRIPTION__PROP: return getDescription();
			case MANDATORY__PROP: return isMandatory();
			case MULTIPLE__PROP: return isMultiple();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case NAME__PROP: internalSetName((String) value); break;
			case TYPE__PROP: internalSetType((String) value); break;
			case DESCRIPTION__PROP: internalSetDescription((String) value); break;
			case MANDATORY__PROP: internalSetMandatory((boolean) value); break;
			case MULTIPLE__PROP: internalSetMultiple((boolean) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolParamInfo readScriptToolParamInfo(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolParamInfo result = new com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolParamInfo();
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
		out.name(NAME__PROP);
		out.value(getName());
		out.name(TYPE__PROP);
		out.value(getType());
		out.name(DESCRIPTION__PROP);
		out.value(getDescription());
		out.name(MANDATORY__PROP);
		out.value(isMandatory());
		out.name(MULTIPLE__PROP);
		out.value(isMultiple());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case NAME__PROP: setName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TYPE__PROP: setType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DESCRIPTION__PROP: setDescription(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case MANDATORY__PROP: setMandatory(in.nextBoolean()); break;
			case MULTIPLE__PROP: setMultiple(in.nextBoolean()); break;
			default: super.readField(in, field);
		}
	}

}
