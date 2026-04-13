package com.top_logic.ai.service.tools.layout.messages;

/**
 * Response containing a list of available layout templates.
 */
public class LayoutTemplateListResponse extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.service.tools.layout.messages.LayoutTemplateListResponse} instance.
	 */
	public static com.top_logic.ai.service.tools.layout.messages.LayoutTemplateListResponse create() {
		return new com.top_logic.ai.service.tools.layout.messages.LayoutTemplateListResponse();
	}

	/** Identifier for the {@link com.top_logic.ai.service.tools.layout.messages.LayoutTemplateListResponse} type in JSON format. */
	public static final String LAYOUT_TEMPLATE_LIST_RESPONSE__TYPE = "LayoutTemplateListResponse";

	/** @see #getTemplates() */
	public static final String TEMPLATES__PROP = "templates";

	private final java.util.List<com.top_logic.ai.service.tools.layout.messages.LayoutTemplateInfo> _templates = new java.util.ArrayList<>();

	/**
	 * Creates a {@link LayoutTemplateListResponse} instance.
	 *
	 * @see com.top_logic.ai.service.tools.layout.messages.LayoutTemplateListResponse#create()
	 */
	protected LayoutTemplateListResponse() {
		super();
	}

	/**
	 * Array of layout template information.
	 */
	public final java.util.List<com.top_logic.ai.service.tools.layout.messages.LayoutTemplateInfo> getTemplates() {
		return _templates;
	}

	/**
	 * @see #getTemplates()
	 */
	public com.top_logic.ai.service.tools.layout.messages.LayoutTemplateListResponse setTemplates(java.util.List<? extends com.top_logic.ai.service.tools.layout.messages.LayoutTemplateInfo> value) {
		internalSetTemplates(value);
		return this;
	}

	/** Internal setter for {@link #getTemplates()} without chain call utility. */
	protected final void internalSetTemplates(java.util.List<? extends com.top_logic.ai.service.tools.layout.messages.LayoutTemplateInfo> value) {
		if (value == null) throw new IllegalArgumentException("Property 'templates' cannot be null.");
		_templates.clear();
		_templates.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getTemplates()} list.
	 */
	public com.top_logic.ai.service.tools.layout.messages.LayoutTemplateListResponse addTemplate(com.top_logic.ai.service.tools.layout.messages.LayoutTemplateInfo value) {
		internalAddTemplate(value);
		return this;
	}

	/** Implementation of {@link #addTemplate(com.top_logic.ai.service.tools.layout.messages.LayoutTemplateInfo)} without chain call utility. */
	protected final void internalAddTemplate(com.top_logic.ai.service.tools.layout.messages.LayoutTemplateInfo value) {
		_templates.add(value);
	}

	/**
	 * Removes a value from the {@link #getTemplates()} list.
	 */
	public final void removeTemplate(com.top_logic.ai.service.tools.layout.messages.LayoutTemplateInfo value) {
		_templates.remove(value);
	}

	@Override
	public String jsonType() {
		return LAYOUT_TEMPLATE_LIST_RESPONSE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TEMPLATES__PROP);
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
			case TEMPLATES__PROP: return getTemplates();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TEMPLATES__PROP: internalSetTemplates(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.ai.service.tools.layout.messages.LayoutTemplateInfo.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.service.tools.layout.messages.LayoutTemplateListResponse readLayoutTemplateListResponse(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.service.tools.layout.messages.LayoutTemplateListResponse result = new com.top_logic.ai.service.tools.layout.messages.LayoutTemplateListResponse();
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
		out.name(TEMPLATES__PROP);
		out.beginArray();
		for (com.top_logic.ai.service.tools.layout.messages.LayoutTemplateInfo x : getTemplates()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TEMPLATES__PROP: {
				java.util.List<com.top_logic.ai.service.tools.layout.messages.LayoutTemplateInfo> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.ai.service.tools.layout.messages.LayoutTemplateInfo.readLayoutTemplateInfo(in));
				}
				in.endArray();
				setTemplates(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
