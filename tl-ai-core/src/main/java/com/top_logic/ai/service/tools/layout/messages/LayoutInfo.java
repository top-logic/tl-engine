package com.top_logic.ai.service.tools.layout.messages;

/**
 * Information about a specific layout configuration.
 */
public class LayoutInfo extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.service.tools.layout.messages.LayoutInfo} instance.
	 */
	public static com.top_logic.ai.service.tools.layout.messages.LayoutInfo create() {
		return new com.top_logic.ai.service.tools.layout.messages.LayoutInfo();
	}

	/** Identifier for the {@link com.top_logic.ai.service.tools.layout.messages.LayoutInfo} type in JSON format. */
	public static final String LAYOUT_INFO__TYPE = "LayoutInfo";

	/** @see #getLayoutKey() */
	public static final String LAYOUT_KEY__PROP = "layoutKey";

	/** @see #isHasTemplate() */
	public static final String HAS_TEMPLATE__PROP = "hasTemplate";

	/** @see #getTemplateName() */
	public static final String TEMPLATE_NAME__PROP = "templateName";

	/** @see #getArguments() */
	public static final String ARGUMENTS__PROP = "arguments";

	/** @see #getComponentConfig() */
	public static final String COMPONENT_CONFIG__PROP = "componentConfig";

	/** @see #getTemplateSchemaUri() */
	public static final String TEMPLATE_SCHEMA_URI__PROP = "templateSchemaUri";

	private String _layoutKey = "";

	private boolean _hasTemplate = false;

	private String _templateName = "";

	private String _arguments = "";

	private String _componentConfig = "";

	private String _templateSchemaUri = "";

	/**
	 * Creates a {@link LayoutInfo} instance.
	 *
	 * @see com.top_logic.ai.service.tools.layout.messages.LayoutInfo#create()
	 */
	protected LayoutInfo() {
		super();
	}

	/**
	 * The layout key.
	 */
	public final String getLayoutKey() {
		return _layoutKey;
	}

	/**
	 * @see #getLayoutKey()
	 */
	public com.top_logic.ai.service.tools.layout.messages.LayoutInfo setLayoutKey(String value) {
		internalSetLayoutKey(value);
		return this;
	}

	/** Internal setter for {@link #getLayoutKey()} without chain call utility. */
	protected final void internalSetLayoutKey(String value) {
		_layoutKey = value;
	}

	/**
	 * Whether the layout is based on a template.
	 */
	public final boolean isHasTemplate() {
		return _hasTemplate;
	}

	/**
	 * @see #isHasTemplate()
	 */
	public com.top_logic.ai.service.tools.layout.messages.LayoutInfo setHasTemplate(boolean value) {
		internalSetHasTemplate(value);
		return this;
	}

	/** Internal setter for {@link #isHasTemplate()} without chain call utility. */
	protected final void internalSetHasTemplate(boolean value) {
		_hasTemplate = value;
	}

	/**
	 * The template name, if the layout is template-based.
	 */
	public final String getTemplateName() {
		return _templateName;
	}

	/**
	 * @see #getTemplateName()
	 */
	public com.top_logic.ai.service.tools.layout.messages.LayoutInfo setTemplateName(String value) {
		internalSetTemplateName(value);
		return this;
	}

	/** Internal setter for {@link #getTemplateName()} without chain call utility. */
	protected final void internalSetTemplateName(String value) {
		_templateName = value;
	}

	/**
	 * Template arguments as JSON string, if the layout is template-based.
	 */
	public final String getArguments() {
		return _arguments;
	}

	/**
	 * @see #getArguments()
	 */
	public com.top_logic.ai.service.tools.layout.messages.LayoutInfo setArguments(String value) {
		internalSetArguments(value);
		return this;
	}

	/** Internal setter for {@link #getArguments()} without chain call utility. */
	protected final void internalSetArguments(String value) {
		_arguments = value;
	}

	/**
	 * The component configuration as JSON, if the layout is not template-based.
	 */
	public final String getComponentConfig() {
		return _componentConfig;
	}

	/**
	 * @see #getComponentConfig()
	 */
	public com.top_logic.ai.service.tools.layout.messages.LayoutInfo setComponentConfig(String value) {
		internalSetComponentConfig(value);
		return this;
	}

	/** Internal setter for {@link #getComponentConfig()} without chain call utility. */
	protected final void internalSetComponentConfig(String value) {
		_componentConfig = value;
	}

	/**
	 * URI for retrieving the JSON Schema of the template parameters.
	 */
	public final String getTemplateSchemaUri() {
		return _templateSchemaUri;
	}

	/**
	 * @see #getTemplateSchemaUri()
	 */
	public com.top_logic.ai.service.tools.layout.messages.LayoutInfo setTemplateSchemaUri(String value) {
		internalSetTemplateSchemaUri(value);
		return this;
	}

	/** Internal setter for {@link #getTemplateSchemaUri()} without chain call utility. */
	protected final void internalSetTemplateSchemaUri(String value) {
		_templateSchemaUri = value;
	}

	@Override
	public String jsonType() {
		return LAYOUT_INFO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			LAYOUT_KEY__PROP, 
			HAS_TEMPLATE__PROP, 
			TEMPLATE_NAME__PROP, 
			ARGUMENTS__PROP, 
			COMPONENT_CONFIG__PROP, 
			TEMPLATE_SCHEMA_URI__PROP);
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
			case LAYOUT_KEY__PROP: return getLayoutKey();
			case HAS_TEMPLATE__PROP: return isHasTemplate();
			case TEMPLATE_NAME__PROP: return getTemplateName();
			case ARGUMENTS__PROP: return getArguments();
			case COMPONENT_CONFIG__PROP: return getComponentConfig();
			case TEMPLATE_SCHEMA_URI__PROP: return getTemplateSchemaUri();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case LAYOUT_KEY__PROP: internalSetLayoutKey((String) value); break;
			case HAS_TEMPLATE__PROP: internalSetHasTemplate((boolean) value); break;
			case TEMPLATE_NAME__PROP: internalSetTemplateName((String) value); break;
			case ARGUMENTS__PROP: internalSetArguments((String) value); break;
			case COMPONENT_CONFIG__PROP: internalSetComponentConfig((String) value); break;
			case TEMPLATE_SCHEMA_URI__PROP: internalSetTemplateSchemaUri((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.service.tools.layout.messages.LayoutInfo readLayoutInfo(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.service.tools.layout.messages.LayoutInfo result = new com.top_logic.ai.service.tools.layout.messages.LayoutInfo();
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
		out.name(LAYOUT_KEY__PROP);
		out.value(getLayoutKey());
		out.name(HAS_TEMPLATE__PROP);
		out.value(isHasTemplate());
		out.name(TEMPLATE_NAME__PROP);
		out.value(getTemplateName());
		out.name(ARGUMENTS__PROP);
		out.value(getArguments());
		out.name(COMPONENT_CONFIG__PROP);
		out.value(getComponentConfig());
		out.name(TEMPLATE_SCHEMA_URI__PROP);
		out.value(getTemplateSchemaUri());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case LAYOUT_KEY__PROP: setLayoutKey(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case HAS_TEMPLATE__PROP: setHasTemplate(in.nextBoolean()); break;
			case TEMPLATE_NAME__PROP: setTemplateName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ARGUMENTS__PROP: setArguments(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case COMPONENT_CONFIG__PROP: setComponentConfig(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TEMPLATE_SCHEMA_URI__PROP: setTemplateSchemaUri(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
