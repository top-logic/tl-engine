package com.top_logic.ai.service.tools.model.messages;

/**
 * Type information for an instance.
 */
public class InstanceTypeInfo extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.service.tools.model.messages.InstanceTypeInfo} instance.
	 */
	public static com.top_logic.ai.service.tools.model.messages.InstanceTypeInfo create() {
		return new com.top_logic.ai.service.tools.model.messages.InstanceTypeInfo();
	}

	/** Identifier for the {@link com.top_logic.ai.service.tools.model.messages.InstanceTypeInfo} type in JSON format. */
	public static final String INSTANCE_TYPE_INFO__TYPE = "InstanceTypeInfo";

	/** @see #getName() */
	public static final String NAME__PROP = "name";

	/** @see #getModule() */
	public static final String MODULE__PROP = "module";

	/** @see #getModuleLabel() */
	public static final String MODULE_LABEL__PROP = "moduleLabel";

	/** @see #getLabel() */
	public static final String LABEL__PROP = "label";

	/** @see #getKind() */
	public static final String KIND__PROP = "kind";

	private String _name = "";

	private String _module = "";

	private String _moduleLabel = "";

	private String _label = "";

	private String _kind = "";

	/**
	 * Creates a {@link InstanceTypeInfo} instance.
	 *
	 * @see com.top_logic.ai.service.tools.model.messages.InstanceTypeInfo#create()
	 */
	protected InstanceTypeInfo() {
		super();
	}

	/**
	 * Type name.
	 */
	public final String getName() {
		return _name;
	}

	/**
	 * @see #getName()
	 */
	public com.top_logic.ai.service.tools.model.messages.InstanceTypeInfo setName(String value) {
		internalSetName(value);
		return this;
	}

	/** Internal setter for {@link #getName()} without chain call utility. */
	protected final void internalSetName(String value) {
		_name = value;
	}

	/**
	 * Module name.
	 */
	public final String getModule() {
		return _module;
	}

	/**
	 * @see #getModule()
	 */
	public com.top_logic.ai.service.tools.model.messages.InstanceTypeInfo setModule(String value) {
		internalSetModule(value);
		return this;
	}

	/** Internal setter for {@link #getModule()} without chain call utility. */
	protected final void internalSetModule(String value) {
		_module = value;
	}

	/**
	 * Module label.
	 */
	public final String getModuleLabel() {
		return _moduleLabel;
	}

	/**
	 * @see #getModuleLabel()
	 */
	public com.top_logic.ai.service.tools.model.messages.InstanceTypeInfo setModuleLabel(String value) {
		internalSetModuleLabel(value);
		return this;
	}

	/** Internal setter for {@link #getModuleLabel()} without chain call utility. */
	protected final void internalSetModuleLabel(String value) {
		_moduleLabel = value;
	}

	/**
	 * Type label.
	 */
	public final String getLabel() {
		return _label;
	}

	/**
	 * @see #getLabel()
	 */
	public com.top_logic.ai.service.tools.model.messages.InstanceTypeInfo setLabel(String value) {
		internalSetLabel(value);
		return this;
	}

	/** Internal setter for {@link #getLabel()} without chain call utility. */
	protected final void internalSetLabel(String value) {
		_label = value;
	}

	/**
	 * Model kind (class, enumeration, etc.).
	 */
	public final String getKind() {
		return _kind;
	}

	/**
	 * @see #getKind()
	 */
	public com.top_logic.ai.service.tools.model.messages.InstanceTypeInfo setKind(String value) {
		internalSetKind(value);
		return this;
	}

	/** Internal setter for {@link #getKind()} without chain call utility. */
	protected final void internalSetKind(String value) {
		_kind = value;
	}

	@Override
	public String jsonType() {
		return INSTANCE_TYPE_INFO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			NAME__PROP, 
			MODULE__PROP, 
			MODULE_LABEL__PROP, 
			LABEL__PROP, 
			KIND__PROP);
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
			case MODULE__PROP: return getModule();
			case MODULE_LABEL__PROP: return getModuleLabel();
			case LABEL__PROP: return getLabel();
			case KIND__PROP: return getKind();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case NAME__PROP: internalSetName((String) value); break;
			case MODULE__PROP: internalSetModule((String) value); break;
			case MODULE_LABEL__PROP: internalSetModuleLabel((String) value); break;
			case LABEL__PROP: internalSetLabel((String) value); break;
			case KIND__PROP: internalSetKind((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.service.tools.model.messages.InstanceTypeInfo readInstanceTypeInfo(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.service.tools.model.messages.InstanceTypeInfo result = new com.top_logic.ai.service.tools.model.messages.InstanceTypeInfo();
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
		out.name(MODULE__PROP);
		out.value(getModule());
		out.name(MODULE_LABEL__PROP);
		out.value(getModuleLabel());
		out.name(LABEL__PROP);
		out.value(getLabel());
		out.name(KIND__PROP);
		out.value(getKind());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case NAME__PROP: setName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case MODULE__PROP: setModule(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case MODULE_LABEL__PROP: setModuleLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case LABEL__PROP: setLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case KIND__PROP: setKind(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
