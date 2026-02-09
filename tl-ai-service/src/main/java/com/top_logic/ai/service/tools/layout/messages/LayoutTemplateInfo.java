package com.top_logic.ai.service.tools.layout.messages;

/**
 * Information about a layout template definition.
 */
public class LayoutTemplateInfo extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.service.tools.layout.messages.LayoutTemplateInfo} instance.
	 */
	public static com.top_logic.ai.service.tools.layout.messages.LayoutTemplateInfo create() {
		return new com.top_logic.ai.service.tools.layout.messages.LayoutTemplateInfo();
	}

	/** Identifier for the {@link com.top_logic.ai.service.tools.layout.messages.LayoutTemplateInfo} type in JSON format. */
	public static final String LAYOUT_TEMPLATE_INFO__TYPE = "LayoutTemplateInfo";

	/** @see #getName() */
	public static final String NAME__PROP = "name";

	/** @see #getLabel() */
	public static final String LABEL__PROP = "label";

	/** @see #getGroups() */
	public static final String GROUPS__PROP = "groups";

	private String _name = "";

	private String _label = "";

	private final java.util.List<String> _groups = new java.util.ArrayList<>();

	/**
	 * Creates a {@link LayoutTemplateInfo} instance.
	 *
	 * @see com.top_logic.ai.service.tools.layout.messages.LayoutTemplateInfo#create()
	 */
	protected LayoutTemplateInfo() {
		super();
	}

	/**
	 * Template name (definition file identifier).
	 */
	public final String getName() {
		return _name;
	}

	/**
	 * @see #getName()
	 */
	public com.top_logic.ai.service.tools.layout.messages.LayoutTemplateInfo setName(String value) {
		internalSetName(value);
		return this;
	}

	/** Internal setter for {@link #getName()} without chain call utility. */
	protected final void internalSetName(String value) {
		_name = value;
	}

	/**
	 * Localized label for the template.
	 */
	public final String getLabel() {
		return _label;
	}

	/**
	 * @see #getLabel()
	 */
	public com.top_logic.ai.service.tools.layout.messages.LayoutTemplateInfo setLabel(String value) {
		internalSetLabel(value);
		return this;
	}

	/** Internal setter for {@link #getLabel()} without chain call utility. */
	protected final void internalSetLabel(String value) {
		_label = value;
	}

	/**
	 * Groups this template belongs to.
	 */
	public final java.util.List<String> getGroups() {
		return _groups;
	}

	/**
	 * @see #getGroups()
	 */
	public com.top_logic.ai.service.tools.layout.messages.LayoutTemplateInfo setGroups(java.util.List<? extends String> value) {
		internalSetGroups(value);
		return this;
	}

	/** Internal setter for {@link #getGroups()} without chain call utility. */
	protected final void internalSetGroups(java.util.List<? extends String> value) {
		_groups.clear();
		_groups.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getGroups()} list.
	 */
	public com.top_logic.ai.service.tools.layout.messages.LayoutTemplateInfo addGroup(String value) {
		internalAddGroup(value);
		return this;
	}

	/** Implementation of {@link #addGroup(String)} without chain call utility. */
	protected final void internalAddGroup(String value) {
		_groups.add(value);
	}

	/**
	 * Removes a value from the {@link #getGroups()} list.
	 */
	public final void removeGroup(String value) {
		_groups.remove(value);
	}

	@Override
	public String jsonType() {
		return LAYOUT_TEMPLATE_INFO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			NAME__PROP, 
			LABEL__PROP, 
			GROUPS__PROP);
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
			case LABEL__PROP: return getLabel();
			case GROUPS__PROP: return getGroups();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case NAME__PROP: internalSetName((String) value); break;
			case LABEL__PROP: internalSetLabel((String) value); break;
			case GROUPS__PROP: internalSetGroups(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.service.tools.layout.messages.LayoutTemplateInfo readLayoutTemplateInfo(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.service.tools.layout.messages.LayoutTemplateInfo result = new com.top_logic.ai.service.tools.layout.messages.LayoutTemplateInfo();
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
		out.name(LABEL__PROP);
		out.value(getLabel());
		out.name(GROUPS__PROP);
		out.beginArray();
		for (String x : getGroups()) {
			out.value(x);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case NAME__PROP: setName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case LABEL__PROP: setLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case GROUPS__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setGroups(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
