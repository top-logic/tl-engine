package com.top_logic.tl.ai.core.tools.model.messages;

/**
 * A concrete implementation option for a polymorphic configuration property.
 */
public class ImplementationOption extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.model.messages.ImplementationOption} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.model.messages.ImplementationOption create() {
		return new com.top_logic.tl.ai.core.tools.model.messages.ImplementationOption();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.model.messages.ImplementationOption} type in JSON format. */
	public static final String IMPLEMENTATION_OPTION__TYPE = "ImplementationOption";

	/** @see #getTagName() */
	public static final String TAG_NAME__PROP = "tagName";

	/** @see #getConfigInterface() */
	public static final String CONFIG_INTERFACE__PROP = "configInterface";

	/** @see #getDescription() */
	public static final String DESCRIPTION__PROP = "description";

	private String _tagName = "";

	private String _configInterface = "";

	private String _description = "";

	/**
	 * Creates a {@link ImplementationOption} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.model.messages.ImplementationOption#create()
	 */
	protected ImplementationOption() {
		super();
	}

	/**
	 * XML tag name from {@code @TagName} annotation.
	 */
	public final String getTagName() {
		return _tagName;
	}

	/**
	 * @see #getTagName()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.ImplementationOption setTagName(String value) {
		internalSetTagName(value);
		return this;
	}

	/** Internal setter for {@link #getTagName()} without chain call utility. */
	protected final void internalSetTagName(String value) {
		_tagName = value;
	}

	/**
	 * Fully qualified configuration interface class name.
	 */
	public final String getConfigInterface() {
		return _configInterface;
	}

	/**
	 * @see #getConfigInterface()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.ImplementationOption setConfigInterface(String value) {
		internalSetConfigInterface(value);
		return this;
	}

	/** Internal setter for {@link #getConfigInterface()} without chain call utility. */
	protected final void internalSetConfigInterface(String value) {
		_configInterface = value;
	}

	/**
	 * Human-readable description from i18n tooltip resources.
	 */
	public final String getDescription() {
		return _description;
	}

	/**
	 * @see #getDescription()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.ImplementationOption setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	/** Internal setter for {@link #getDescription()} without chain call utility. */
	protected final void internalSetDescription(String value) {
		_description = value;
	}

	@Override
	public String jsonType() {
		return IMPLEMENTATION_OPTION__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TAG_NAME__PROP, 
			CONFIG_INTERFACE__PROP, 
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
			case TAG_NAME__PROP: return getTagName();
			case CONFIG_INTERFACE__PROP: return getConfigInterface();
			case DESCRIPTION__PROP: return getDescription();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TAG_NAME__PROP: internalSetTagName((String) value); break;
			case CONFIG_INTERFACE__PROP: internalSetConfigInterface((String) value); break;
			case DESCRIPTION__PROP: internalSetDescription((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.model.messages.ImplementationOption readImplementationOption(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.model.messages.ImplementationOption result = new com.top_logic.tl.ai.core.tools.model.messages.ImplementationOption();
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
		out.name(TAG_NAME__PROP);
		out.value(getTagName());
		out.name(CONFIG_INTERFACE__PROP);
		out.value(getConfigInterface());
		out.name(DESCRIPTION__PROP);
		out.value(getDescription());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TAG_NAME__PROP: setTagName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CONFIG_INTERFACE__PROP: setConfigInterface(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DESCRIPTION__PROP: setDescription(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
