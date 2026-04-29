package com.top_logic.ai.chat.api.messages;

/**
 * SSE event for completed assistant message with server-rendered HTML.
 */
public class MessageCompleteEvent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.MessageCompleteEvent} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.MessageCompleteEvent create() {
		return new com.top_logic.ai.chat.api.messages.MessageCompleteEvent();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.MessageCompleteEvent} type in JSON format. */
	public static final String MESSAGE_COMPLETE_EVENT__TYPE = "MessageCompleteEvent";

	/** @see #getContentHtml() */
	public static final String CONTENT_HTML__PROP = "contentHtml";

	/** @see #getContentText() */
	public static final String CONTENT_TEXT__PROP = "contentText";

	private String _contentHtml = "";

	private String _contentText = "";

	/**
	 * Creates a {@link MessageCompleteEvent} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.MessageCompleteEvent#create()
	 */
	protected MessageCompleteEvent() {
		super();
	}

	/**
	 * Server-rendered markdown-to-HTML content.
	 */
	public final String getContentHtml() {
		return _contentHtml;
	}

	/**
	 * @see #getContentHtml()
	 */
	public com.top_logic.ai.chat.api.messages.MessageCompleteEvent setContentHtml(String value) {
		internalSetContentHtml(value);
		return this;
	}

	/** Internal setter for {@link #getContentHtml()} without chain call utility. */
	protected final void internalSetContentHtml(String value) {
		_contentHtml = value;
	}

	/**
	 * Raw markdown text for persistence.
	 */
	public final String getContentText() {
		return _contentText;
	}

	/**
	 * @see #getContentText()
	 */
	public com.top_logic.ai.chat.api.messages.MessageCompleteEvent setContentText(String value) {
		internalSetContentText(value);
		return this;
	}

	/** Internal setter for {@link #getContentText()} without chain call utility. */
	protected final void internalSetContentText(String value) {
		_contentText = value;
	}

	@Override
	public String jsonType() {
		return MESSAGE_COMPLETE_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			CONTENT_HTML__PROP, 
			CONTENT_TEXT__PROP);
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
			case CONTENT_HTML__PROP: return getContentHtml();
			case CONTENT_TEXT__PROP: return getContentText();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case CONTENT_HTML__PROP: internalSetContentHtml((String) value); break;
			case CONTENT_TEXT__PROP: internalSetContentText((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.MessageCompleteEvent readMessageCompleteEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.MessageCompleteEvent result = new com.top_logic.ai.chat.api.messages.MessageCompleteEvent();
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
		out.name(CONTENT_HTML__PROP);
		out.value(getContentHtml());
		out.name(CONTENT_TEXT__PROP);
		out.value(getContentText());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case CONTENT_HTML__PROP: setContentHtml(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CONTENT_TEXT__PROP: setContentText(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
