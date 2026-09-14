package com.top_logic.ai.chat.api.messages;

/**
 * One source a {@code [n]} marker in an answer points to.
 *
 * Mirrors the persist {@code CitationDto}. The server drops any citation whose id or url did not
 * appear in this turn's tool results.
 */
public class CitationDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.CitationDto} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.CitationDto create() {
		return new com.top_logic.ai.chat.api.messages.CitationDto();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.CitationDto} type in JSON format. */
	public static final String CITATION_DTO__TYPE = "CitationDto";

	/** @see #getIndex() */
	public static final String INDEX__PROP = "index";

	/** @see #getToolName() */
	public static final String TOOL_NAME__PROP = "toolName";

	/** @see #getToolCallId() */
	public static final String TOOL_CALL_ID__PROP = "toolCallId";

	/** @see #getKind() */
	public static final String KIND__PROP = "kind";

	/** @see #getId() */
	public static final String ID__PROP = "id";

	/** @see #getLabel() */
	public static final String LABEL__PROP = "label";

	/** @see #getUrl() */
	public static final String URL__PROP = "url";

	private int _index = 0;

	private String _toolName = "";

	private String _toolCallId = "";

	private String _kind = "";

	private String _id = "";

	private String _label = "";

	private String _url = "";

	/**
	 * Creates a {@link CitationDto} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.CitationDto#create()
	 */
	protected CitationDto() {
		super();
	}

	/**
	 * 1-based marker index shown in the answer.
	 */
	public final int getIndex() {
		return _index;
	}

	/**
	 * @see #getIndex()
	 */
	public com.top_logic.ai.chat.api.messages.CitationDto setIndex(int value) {
		internalSetIndex(value);
		return this;
	}

	/** Internal setter for {@link #getIndex()} without chain call utility. */
	protected final void internalSetIndex(int value) {
		_index = value;
	}

	/**
	 * Tool that produced the cited id or URL.
	 */
	public final String getToolName() {
		return _toolName;
	}

	/**
	 * @see #getToolName()
	 */
	public com.top_logic.ai.chat.api.messages.CitationDto setToolName(String value) {
		internalSetToolName(value);
		return this;
	}

	/** Internal setter for {@link #getToolName()} without chain call utility. */
	protected final void internalSetToolName(String value) {
		_toolName = value;
	}

	/**
	 * Tool call id of that tool, when known.
	 */
	public final String getToolCallId() {
		return _toolCallId;
	}

	/**
	 * @see #getToolCallId()
	 */
	public com.top_logic.ai.chat.api.messages.CitationDto setToolCallId(String value) {
		internalSetToolCallId(value);
		return this;
	}

	/** Internal setter for {@link #getToolCallId()} without chain call utility. */
	protected final void internalSetToolCallId(String value) {
		_toolCallId = value;
	}

	/**
	 * Host kind, e.g. {@code web} or {@code invoice}.
	 */
	public final String getKind() {
		return _kind;
	}

	/**
	 * @see #getKind()
	 */
	public com.top_logic.ai.chat.api.messages.CitationDto setKind(String value) {
		internalSetKind(value);
		return this;
	}

	/** Internal setter for {@link #getKind()} without chain call utility. */
	protected final void internalSetKind(String value) {
		_kind = value;
	}

	/**
	 * Id the tool returned (a URL, a TID, …).
	 */
	public final String getId() {
		return _id;
	}

	/**
	 * @see #getId()
	 */
	public com.top_logic.ai.chat.api.messages.CitationDto setId(String value) {
		internalSetId(value);
		return this;
	}

	/** Internal setter for {@link #getId()} without chain call utility. */
	protected final void internalSetId(String value) {
		_id = value;
	}

	/**
	 * Human label for the chip popover.
	 */
	public final String getLabel() {
		return _label;
	}

	/**
	 * @see #getLabel()
	 */
	public com.top_logic.ai.chat.api.messages.CitationDto setLabel(String value) {
		internalSetLabel(value);
		return this;
	}

	/** Internal setter for {@link #getLabel()} without chain call utility. */
	protected final void internalSetLabel(String value) {
		_label = value;
	}

	/**
	 * Link for a web source; empty otherwise.
	 */
	public final String getUrl() {
		return _url;
	}

	/**
	 * @see #getUrl()
	 */
	public com.top_logic.ai.chat.api.messages.CitationDto setUrl(String value) {
		internalSetUrl(value);
		return this;
	}

	/** Internal setter for {@link #getUrl()} without chain call utility. */
	protected final void internalSetUrl(String value) {
		_url = value;
	}

	@Override
	public String jsonType() {
		return CITATION_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			INDEX__PROP, 
			TOOL_NAME__PROP, 
			TOOL_CALL_ID__PROP, 
			KIND__PROP, 
			ID__PROP, 
			LABEL__PROP, 
			URL__PROP);
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
			case INDEX__PROP: return getIndex();
			case TOOL_NAME__PROP: return getToolName();
			case TOOL_CALL_ID__PROP: return getToolCallId();
			case KIND__PROP: return getKind();
			case ID__PROP: return getId();
			case LABEL__PROP: return getLabel();
			case URL__PROP: return getUrl();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case INDEX__PROP: internalSetIndex((int) value); break;
			case TOOL_NAME__PROP: internalSetToolName((String) value); break;
			case TOOL_CALL_ID__PROP: internalSetToolCallId((String) value); break;
			case KIND__PROP: internalSetKind((String) value); break;
			case ID__PROP: internalSetId((String) value); break;
			case LABEL__PROP: internalSetLabel((String) value); break;
			case URL__PROP: internalSetUrl((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.CitationDto readCitationDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.CitationDto result = new com.top_logic.ai.chat.api.messages.CitationDto();
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
		out.name(INDEX__PROP);
		out.value(getIndex());
		out.name(TOOL_NAME__PROP);
		out.value(getToolName());
		out.name(TOOL_CALL_ID__PROP);
		out.value(getToolCallId());
		out.name(KIND__PROP);
		out.value(getKind());
		out.name(ID__PROP);
		out.value(getId());
		out.name(LABEL__PROP);
		out.value(getLabel());
		out.name(URL__PROP);
		out.value(getUrl());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case INDEX__PROP: setIndex(in.nextInt()); break;
			case TOOL_NAME__PROP: setToolName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TOOL_CALL_ID__PROP: setToolCallId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case KIND__PROP: setKind(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ID__PROP: setId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case LABEL__PROP: setLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case URL__PROP: setUrl(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
