package com.top_logic.tl.ai.core.tools.model.messages;

/**
 * Information about a module in the application model.
 */
public class ModuleInfo extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.model.messages.ModuleInfo} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.model.messages.ModuleInfo create() {
		return new com.top_logic.tl.ai.core.tools.model.messages.ModuleInfo();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.model.messages.ModuleInfo} type in JSON format. */
	public static final String MODULE_INFO__TYPE = "ModuleInfo";

	/** @see #getName() */
	public static final String NAME__PROP = "name";

	/** @see #getTypeCount() */
	public static final String TYPE_COUNT__PROP = "typeCount";

	/** @see #getLabel() */
	public static final String LABEL__PROP = "label";

	/** @see #getDescription() */
	public static final String DESCRIPTION__PROP = "description";

	/** @see #isFrameworkModule() */
	public static final String FRAMEWORK_MODULE__PROP = "frameworkModule";

	private String _name = "";

	private long _typeCount = 0L;

	private String _label = "";

	private String _description = "";

	private boolean _frameworkModule = false;

	/**
	 * Creates a {@link ModuleInfo} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.model.messages.ModuleInfo#create()
	 */
	protected ModuleInfo() {
		super();
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
	public com.top_logic.tl.ai.core.tools.model.messages.ModuleInfo setName(String value) {
		internalSetName(value);
		return this;
	}

	/** Internal setter for {@link #getName()} without chain call utility. */
	protected final void internalSetName(String value) {
		_name = value;
	}

	/**
	 * Number of types in this module (excluding associations).
	 */
	public final long getTypeCount() {
		return _typeCount;
	}

	/**
	 * @see #getTypeCount()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.ModuleInfo setTypeCount(long value) {
		internalSetTypeCount(value);
		return this;
	}

	/** Internal setter for {@link #getTypeCount()} without chain call utility. */
	protected final void internalSetTypeCount(long value) {
		_typeCount = value;
	}

	/**
	 * Localized label for the module.
	 */
	public final String getLabel() {
		return _label;
	}

	/**
	 * @see #getLabel()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.ModuleInfo setLabel(String value) {
		internalSetLabel(value);
		return this;
	}

	/** Internal setter for {@link #getLabel()} without chain call utility. */
	protected final void internalSetLabel(String value) {
		_label = value;
	}

	/**
	 * Localized description of the module.
	 */
	public final String getDescription() {
		return _description;
	}

	/**
	 * @see #getDescription()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.ModuleInfo setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	/** Internal setter for {@link #getDescription()} without chain call utility. */
	protected final void internalSetDescription(String value) {
		_description = value;
	}

	/**
	 * Whether this is a framework/platform module (true) or an application domain module (false).
	 * Framework modules include tl.*, tl5.*, and infrastructure modules (<code>orgStructure</code>, <code>mandatorStructure</code>, etc.).
	 */
	public final boolean isFrameworkModule() {
		return _frameworkModule;
	}

	/**
	 * @see #isFrameworkModule()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.ModuleInfo setFrameworkModule(boolean value) {
		internalSetFrameworkModule(value);
		return this;
	}

	/** Internal setter for {@link #isFrameworkModule()} without chain call utility. */
	protected final void internalSetFrameworkModule(boolean value) {
		_frameworkModule = value;
	}

	@Override
	public String jsonType() {
		return MODULE_INFO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			NAME__PROP, 
			TYPE_COUNT__PROP, 
			LABEL__PROP, 
			DESCRIPTION__PROP, 
			FRAMEWORK_MODULE__PROP);
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
			case TYPE_COUNT__PROP: return getTypeCount();
			case LABEL__PROP: return getLabel();
			case DESCRIPTION__PROP: return getDescription();
			case FRAMEWORK_MODULE__PROP: return isFrameworkModule();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case NAME__PROP: internalSetName((String) value); break;
			case TYPE_COUNT__PROP: internalSetTypeCount((long) value); break;
			case LABEL__PROP: internalSetLabel((String) value); break;
			case DESCRIPTION__PROP: internalSetDescription((String) value); break;
			case FRAMEWORK_MODULE__PROP: internalSetFrameworkModule((boolean) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.model.messages.ModuleInfo readModuleInfo(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.model.messages.ModuleInfo result = new com.top_logic.tl.ai.core.tools.model.messages.ModuleInfo();
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
		out.name(TYPE_COUNT__PROP);
		out.value(getTypeCount());
		out.name(LABEL__PROP);
		out.value(getLabel());
		out.name(DESCRIPTION__PROP);
		out.value(getDescription());
		out.name(FRAMEWORK_MODULE__PROP);
		out.value(isFrameworkModule());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case NAME__PROP: setName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TYPE_COUNT__PROP: setTypeCount(in.nextLong()); break;
			case LABEL__PROP: setLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DESCRIPTION__PROP: setDescription(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case FRAMEWORK_MODULE__PROP: setFrameworkModule(in.nextBoolean()); break;
			default: super.readField(in, field);
		}
	}

}
