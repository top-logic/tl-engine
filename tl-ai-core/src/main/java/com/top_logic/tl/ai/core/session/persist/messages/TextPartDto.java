package com.top_logic.tl.ai.core.session.persist.messages;

/**
 * Text content part.
 */
public class TextPartDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.session.persist.messages.TextPartDto} instance.
	 */
	public static com.top_logic.tl.ai.core.session.persist.messages.TextPartDto create() {
		return new com.top_logic.tl.ai.core.session.persist.messages.TextPartDto();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.session.persist.messages.TextPartDto} type in JSON format. */
	public static final String TEXT_PART_DTO__TYPE = "TextPartDto";

	/** @see #getText() */
	public static final String TEXT__PROP = "text";

	/** @see #getCitations() */
	public static final String CITATIONS__PROP = "citations";

	private String _text = "";

	private final java.util.List<com.top_logic.tl.ai.core.session.persist.messages.CitationDto> _citations = new java.util.ArrayList<>();

	/**
	 * Creates a {@link TextPartDto} instance.
	 *
	 * @see com.top_logic.tl.ai.core.session.persist.messages.TextPartDto#create()
	 */
	protected TextPartDto() {
		super();
	}

	/**
	 * The text content.
	 */
	public final String getText() {
		return _text;
	}

	/**
	 * @see #getText()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.TextPartDto setText(String value) {
		internalSetText(value);
		return this;
	}

	/** Internal setter for {@link #getText()} without chain call utility. */
	protected final void internalSetText(String value) {
		_text = value;
	}

	/**
	 * Citations the validator kept for this text; empty when the turn cited nothing.
	 */
	public final java.util.List<com.top_logic.tl.ai.core.session.persist.messages.CitationDto> getCitations() {
		return _citations;
	}

	/**
	 * @see #getCitations()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.TextPartDto setCitations(java.util.List<? extends com.top_logic.tl.ai.core.session.persist.messages.CitationDto> value) {
		internalSetCitations(value);
		return this;
	}

	/** Internal setter for {@link #getCitations()} without chain call utility. */
	protected final void internalSetCitations(java.util.List<? extends com.top_logic.tl.ai.core.session.persist.messages.CitationDto> value) {
		if (value == null) throw new IllegalArgumentException("Property 'citations' cannot be null.");
		_citations.clear();
		_citations.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getCitations()} list.
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.TextPartDto addCitation(com.top_logic.tl.ai.core.session.persist.messages.CitationDto value) {
		internalAddCitation(value);
		return this;
	}

	/** Implementation of {@link #addCitation(com.top_logic.tl.ai.core.session.persist.messages.CitationDto)} without chain call utility. */
	protected final void internalAddCitation(com.top_logic.tl.ai.core.session.persist.messages.CitationDto value) {
		_citations.add(value);
	}

	/**
	 * Removes a value from the {@link #getCitations()} list.
	 */
	public final void removeCitation(com.top_logic.tl.ai.core.session.persist.messages.CitationDto value) {
		_citations.remove(value);
	}

	@Override
	public String jsonType() {
		return TEXT_PART_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TEXT__PROP, 
			CITATIONS__PROP);
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
			case TEXT__PROP: return getText();
			case CITATIONS__PROP: return getCitations();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TEXT__PROP: internalSetText((String) value); break;
			case CITATIONS__PROP: internalSetCitations(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.tl.ai.core.session.persist.messages.CitationDto.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.session.persist.messages.TextPartDto readTextPartDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.session.persist.messages.TextPartDto result = new com.top_logic.tl.ai.core.session.persist.messages.TextPartDto();
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
		out.name(TEXT__PROP);
		out.value(getText());
		out.name(CITATIONS__PROP);
		out.beginArray();
		for (com.top_logic.tl.ai.core.session.persist.messages.CitationDto x : getCitations()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TEXT__PROP: setText(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CITATIONS__PROP: {
				java.util.List<com.top_logic.tl.ai.core.session.persist.messages.CitationDto> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.tl.ai.core.session.persist.messages.CitationDto.readCitationDto(in));
				}
				in.endArray();
				setCitations(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
