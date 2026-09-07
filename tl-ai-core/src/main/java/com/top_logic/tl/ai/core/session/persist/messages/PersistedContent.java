package com.top_logic.tl.ai.core.session.persist.messages;

/**
 * One piece of message content.
 */
public class PersistedContent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.session.persist.messages.PersistedContent} instance.
	 */
	public static com.top_logic.tl.ai.core.session.persist.messages.PersistedContent create() {
		return new com.top_logic.tl.ai.core.session.persist.messages.PersistedContent();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.session.persist.messages.PersistedContent} type in JSON format. */
	public static final String PERSISTED_CONTENT__TYPE = "PersistedContent";

	/** @see #getType() */
	public static final String TYPE__PROP = "type";

	/** @see #getText() */
	public static final String TEXT__PROP = "text";

	/** @see #getContinuityToken() */
	public static final String CONTINUITY_TOKEN__PROP = "continuityToken";

	/** @see #getToolCallId() */
	public static final String TOOL_CALL_ID__PROP = "toolCallId";

	/** @see #getToolName() */
	public static final String TOOL_NAME__PROP = "toolName";

	/** @see #getArguments() */
	public static final String ARGUMENTS__PROP = "arguments";

	/** @see #getUrl() */
	public static final String URL__PROP = "url";

	/** @see #getData() */
	public static final String DATA__PROP = "data";

	/** @see #getMediaType() */
	public static final String MEDIA_TYPE__PROP = "mediaType";

	private String _type = "";

	private String _text = "";

	private String _continuityToken = "";

	private String _toolCallId = "";

	private String _toolName = "";

	private String _arguments = "";

	private String _url = "";

	private String _data = "";

	private String _mediaType = "";

	/**
	 * Creates a {@link PersistedContent} instance.
	 *
	 * @see com.top_logic.tl.ai.core.session.persist.messages.PersistedContent#create()
	 */
	protected PersistedContent() {
		super();
	}

	/**
	 * Discriminator for the content type.
	 */
	public final String getType() {
		return _type;
	}

	/**
	 * @see #getType()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedContent setType(String value) {
		internalSetType(value);
		return this;
	}

	/** Internal setter for {@link #getType()} without chain call utility. */
	protected final void internalSetType(String value) {
		_type = value;
	}

	/**
	 * The text, for text and thinking content.
	 */
	public final String getText() {
		return _text;
	}

	/**
	 * @see #getText()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedContent setText(String value) {
		internalSetText(value);
		return this;
	}

	/** Internal setter for {@link #getText()} without chain call utility. */
	protected final void internalSetText(String value) {
		_text = value;
	}

	/**
	 * Opaque provider token for multi-turn continuity, for thinking content.
	 */
	public final String getContinuityToken() {
		return _continuityToken;
	}

	/**
	 * @see #getContinuityToken()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedContent setContinuityToken(String value) {
		internalSetContinuityToken(value);
		return this;
	}

	/** Internal setter for {@link #getContinuityToken()} without chain call utility. */
	protected final void internalSetContinuityToken(String value) {
		_continuityToken = value;
	}

	/**
	 * ID of the tool call, for tool call content.
	 */
	public final String getToolCallId() {
		return _toolCallId;
	}

	/**
	 * @see #getToolCallId()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedContent setToolCallId(String value) {
		internalSetToolCallId(value);
		return this;
	}

	/** Internal setter for {@link #getToolCallId()} without chain call utility. */
	protected final void internalSetToolCallId(String value) {
		_toolCallId = value;
	}

	/**
	 * Name of the called tool, for tool call content.
	 */
	public final String getToolName() {
		return _toolName;
	}

	/**
	 * @see #getToolName()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedContent setToolName(String value) {
		internalSetToolName(value);
		return this;
	}

	/** Internal setter for {@link #getToolName()} without chain call utility. */
	protected final void internalSetToolName(String value) {
		_toolName = value;
	}

	/**
	 * JSON-encoded arguments, for tool call content.
	 */
	public final String getArguments() {
		return _arguments;
	}

	/**
	 * @see #getArguments()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedContent setArguments(String value) {
		internalSetArguments(value);
		return this;
	}

	/** Internal setter for {@link #getArguments()} without chain call utility. */
	protected final void internalSetArguments(String value) {
		_arguments = value;
	}

	/**
	 * The image URL, for image content.
	 */
	public final String getUrl() {
		return _url;
	}

	/**
	 * @see #getUrl()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedContent setUrl(String value) {
		internalSetUrl(value);
		return this;
	}

	/** Internal setter for {@link #getUrl()} without chain call utility. */
	protected final void internalSetUrl(String value) {
		_url = value;
	}

	/**
	 * The base64-encoded document, for PDF content.
	 */
	public final String getData() {
		return _data;
	}

	/**
	 * @see #getData()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedContent setData(String value) {
		internalSetData(value);
		return this;
	}

	/** Internal setter for {@link #getData()} without chain call utility. */
	protected final void internalSetData(String value) {
		_data = value;
	}

	/**
	 * The media type, for image and PDF content.
	 */
	public final String getMediaType() {
		return _mediaType;
	}

	/**
	 * @see #getMediaType()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedContent setMediaType(String value) {
		internalSetMediaType(value);
		return this;
	}

	/** Internal setter for {@link #getMediaType()} without chain call utility. */
	protected final void internalSetMediaType(String value) {
		_mediaType = value;
	}

	@Override
	public String jsonType() {
		return PERSISTED_CONTENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TYPE__PROP, 
			TEXT__PROP, 
			CONTINUITY_TOKEN__PROP, 
			TOOL_CALL_ID__PROP, 
			TOOL_NAME__PROP, 
			ARGUMENTS__PROP, 
			URL__PROP, 
			DATA__PROP, 
			MEDIA_TYPE__PROP);
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
			case TYPE__PROP: return getType();
			case TEXT__PROP: return getText();
			case CONTINUITY_TOKEN__PROP: return getContinuityToken();
			case TOOL_CALL_ID__PROP: return getToolCallId();
			case TOOL_NAME__PROP: return getToolName();
			case ARGUMENTS__PROP: return getArguments();
			case URL__PROP: return getUrl();
			case DATA__PROP: return getData();
			case MEDIA_TYPE__PROP: return getMediaType();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TYPE__PROP: internalSetType((String) value); break;
			case TEXT__PROP: internalSetText((String) value); break;
			case CONTINUITY_TOKEN__PROP: internalSetContinuityToken((String) value); break;
			case TOOL_CALL_ID__PROP: internalSetToolCallId((String) value); break;
			case TOOL_NAME__PROP: internalSetToolName((String) value); break;
			case ARGUMENTS__PROP: internalSetArguments((String) value); break;
			case URL__PROP: internalSetUrl((String) value); break;
			case DATA__PROP: internalSetData((String) value); break;
			case MEDIA_TYPE__PROP: internalSetMediaType((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.session.persist.messages.PersistedContent readPersistedContent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.session.persist.messages.PersistedContent result = new com.top_logic.tl.ai.core.session.persist.messages.PersistedContent();
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
		out.name(TYPE__PROP);
		out.value(getType());
		out.name(TEXT__PROP);
		out.value(getText());
		out.name(CONTINUITY_TOKEN__PROP);
		out.value(getContinuityToken());
		out.name(TOOL_CALL_ID__PROP);
		out.value(getToolCallId());
		out.name(TOOL_NAME__PROP);
		out.value(getToolName());
		out.name(ARGUMENTS__PROP);
		out.value(getArguments());
		out.name(URL__PROP);
		out.value(getUrl());
		out.name(DATA__PROP);
		out.value(getData());
		out.name(MEDIA_TYPE__PROP);
		out.value(getMediaType());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TYPE__PROP: setType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TEXT__PROP: setText(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CONTINUITY_TOKEN__PROP: setContinuityToken(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TOOL_CALL_ID__PROP: setToolCallId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TOOL_NAME__PROP: setToolName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ARGUMENTS__PROP: setArguments(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case URL__PROP: setUrl(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DATA__PROP: setData(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case MEDIA_TYPE__PROP: setMediaType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
