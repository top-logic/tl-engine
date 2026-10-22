package com.top_logic.ai.chat.api.messages;

/**
 * One selectable option in a user interaction.
 */
public class InteractionOptionDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.InteractionOptionDto} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.InteractionOptionDto create() {
		return new com.top_logic.ai.chat.api.messages.InteractionOptionDto();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.InteractionOptionDto} type in JSON format. */
	public static final String INTERACTION_OPTION_DTO__TYPE = "InteractionOptionDto";

	/** @see #getLabel() */
	public static final String LABEL__PROP = "label";

	/** @see #getDescription() */
	public static final String DESCRIPTION__PROP = "description";

	private String _label = "";

	private String _description = "";

	/**
	 * Creates a {@link InteractionOptionDto} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.InteractionOptionDto#create()
	 */
	protected InteractionOptionDto() {
		super();
	}

	/**
	 * Display label for the option.
	 */
	public final String getLabel() {
		return _label;
	}

	/**
	 * @see #getLabel()
	 */
	public com.top_logic.ai.chat.api.messages.InteractionOptionDto setLabel(String value) {
		internalSetLabel(value);
		return this;
	}

	/** Internal setter for {@link #getLabel()} without chain call utility. */
	protected final void internalSetLabel(String value) {
		_label = value;
	}

	/**
	 * Optional description / tooltip text.
	 */
	public final String getDescription() {
		return _description;
	}

	/**
	 * @see #getDescription()
	 */
	public com.top_logic.ai.chat.api.messages.InteractionOptionDto setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	/** Internal setter for {@link #getDescription()} without chain call utility. */
	protected final void internalSetDescription(String value) {
		_description = value;
	}

	@Override
	public String jsonType() {
		return INTERACTION_OPTION_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
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
			case LABEL__PROP: return getLabel();
			case DESCRIPTION__PROP: return getDescription();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case LABEL__PROP: internalSetLabel((String) value); break;
			case DESCRIPTION__PROP: internalSetDescription((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.InteractionOptionDto readInteractionOptionDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.InteractionOptionDto result = new com.top_logic.ai.chat.api.messages.InteractionOptionDto();
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
		out.name(LABEL__PROP);
		out.value(getLabel());
		out.name(DESCRIPTION__PROP);
		out.value(getDescription());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case LABEL__PROP: setLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DESCRIPTION__PROP: setDescription(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
