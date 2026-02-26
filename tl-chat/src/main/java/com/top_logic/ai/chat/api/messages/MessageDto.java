package com.top_logic.ai.chat.api.messages;

/**
 * One message in the chat.
 */
public class MessageDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.MessageDto} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.MessageDto create() {
		return new com.top_logic.ai.chat.api.messages.MessageDto();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.MessageDto} type in JSON format. */
	public static final String MESSAGE_DTO__TYPE = "MessageDto";

	/** @see #getId() */
	public static final String ID__PROP = "id";

	/** @see #getRole() */
	public static final String ROLE__PROP = "role";

	/** @see #getUserName() */
	public static final String USER_NAME__PROP = "userName";

	/** @see #getContent() */
	public static final String CONTENT__PROP = "content";

	/** @see #getContentHtml() */
	public static final String CONTENT_HTML__PROP = "contentHtml";

	/** @see #getTime() */
	public static final String TIME__PROP = "time";

	/** @see #getAttachments() */
	public static final String ATTACHMENTS__PROP = "attachments";

	private String _id = "";

	private String _role = "";

	private String _userName = "";

	private String _content = "";

	private String _contentHtml = "";

	private String _time = "";

	private final java.util.List<com.top_logic.ai.chat.api.messages.AttachmentDto> _attachments = new java.util.ArrayList<>();

	/**
	 * Creates a {@link MessageDto} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.MessageDto#create()
	 */
	protected MessageDto() {
		super();
	}

	/**
	 * Message ID.
	 */
	public final String getId() {
		return _id;
	}

	/**
	 * @see #getId()
	 */
	public com.top_logic.ai.chat.api.messages.MessageDto setId(String value) {
		internalSetId(value);
		return this;
	}

	/** Internal setter for {@link #getId()} without chain call utility. */
	protected final void internalSetId(String value) {
		_id = value;
	}

	/**
	 * "user", "agent", or "system".
	 */
	public final String getRole() {
		return _role;
	}

	/**
	 * @see #getRole()
	 */
	public com.top_logic.ai.chat.api.messages.MessageDto setRole(String value) {
		internalSetRole(value);
		return this;
	}

	/** Internal setter for {@link #getRole()} without chain call utility. */
	protected final void internalSetRole(String value) {
		_role = value;
	}

	/**
	 * Display name of the user who sent this message.
	 */
	public final String getUserName() {
		return _userName;
	}

	/**
	 * @see #getUserName()
	 */
	public com.top_logic.ai.chat.api.messages.MessageDto setUserName(String value) {
		internalSetUserName(value);
		return this;
	}

	/** Internal setter for {@link #getUserName()} without chain call utility. */
	protected final void internalSetUserName(String value) {
		_userName = value;
	}

	/**
	 * Message text content.
	 */
	public final String getContent() {
		return _content;
	}

	/**
	 * @see #getContent()
	 */
	public com.top_logic.ai.chat.api.messages.MessageDto setContent(String value) {
		internalSetContent(value);
		return this;
	}

	/** Internal setter for {@link #getContent()} without chain call utility. */
	protected final void internalSetContent(String value) {
		_content = value;
	}

	/**
	 * Pre-rendered HTML for the message content (agent messages only; null for user/system).
	 */
	public final String getContentHtml() {
		return _contentHtml;
	}

	/**
	 * @see #getContentHtml()
	 */
	public com.top_logic.ai.chat.api.messages.MessageDto setContentHtml(String value) {
		internalSetContentHtml(value);
		return this;
	}

	/** Internal setter for {@link #getContentHtml()} without chain call utility. */
	protected final void internalSetContentHtml(String value) {
		_contentHtml = value;
	}

	/**
	 * Formatted timestamp string.
	 */
	public final String getTime() {
		return _time;
	}

	/**
	 * @see #getTime()
	 */
	public com.top_logic.ai.chat.api.messages.MessageDto setTime(String value) {
		internalSetTime(value);
		return this;
	}

	/** Internal setter for {@link #getTime()} without chain call utility. */
	protected final void internalSetTime(String value) {
		_time = value;
	}

	/**
	 * Attachment metadata (file name, type, size). Binary data is NOT persisted.
	 */
	public final java.util.List<com.top_logic.ai.chat.api.messages.AttachmentDto> getAttachments() {
		return _attachments;
	}

	/**
	 * @see #getAttachments()
	 */
	public com.top_logic.ai.chat.api.messages.MessageDto setAttachments(java.util.List<? extends com.top_logic.ai.chat.api.messages.AttachmentDto> value) {
		internalSetAttachments(value);
		return this;
	}

	/** Internal setter for {@link #getAttachments()} without chain call utility. */
	protected final void internalSetAttachments(java.util.List<? extends com.top_logic.ai.chat.api.messages.AttachmentDto> value) {
		if (value == null) throw new IllegalArgumentException("Property 'attachments' cannot be null.");
		_attachments.clear();
		_attachments.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getAttachments()} list.
	 */
	public com.top_logic.ai.chat.api.messages.MessageDto addAttachment(com.top_logic.ai.chat.api.messages.AttachmentDto value) {
		internalAddAttachment(value);
		return this;
	}

	/** Implementation of {@link #addAttachment(com.top_logic.ai.chat.api.messages.AttachmentDto)} without chain call utility. */
	protected final void internalAddAttachment(com.top_logic.ai.chat.api.messages.AttachmentDto value) {
		_attachments.add(value);
	}

	/**
	 * Removes a value from the {@link #getAttachments()} list.
	 */
	public final void removeAttachment(com.top_logic.ai.chat.api.messages.AttachmentDto value) {
		_attachments.remove(value);
	}

	@Override
	public String jsonType() {
		return MESSAGE_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			ID__PROP, 
			ROLE__PROP, 
			USER_NAME__PROP, 
			CONTENT__PROP, 
			CONTENT_HTML__PROP, 
			TIME__PROP, 
			ATTACHMENTS__PROP);
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
			case ID__PROP: return getId();
			case ROLE__PROP: return getRole();
			case USER_NAME__PROP: return getUserName();
			case CONTENT__PROP: return getContent();
			case CONTENT_HTML__PROP: return getContentHtml();
			case TIME__PROP: return getTime();
			case ATTACHMENTS__PROP: return getAttachments();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ID__PROP: internalSetId((String) value); break;
			case ROLE__PROP: internalSetRole((String) value); break;
			case USER_NAME__PROP: internalSetUserName((String) value); break;
			case CONTENT__PROP: internalSetContent((String) value); break;
			case CONTENT_HTML__PROP: internalSetContentHtml((String) value); break;
			case TIME__PROP: internalSetTime((String) value); break;
			case ATTACHMENTS__PROP: internalSetAttachments(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.ai.chat.api.messages.AttachmentDto.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.MessageDto readMessageDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.MessageDto result = new com.top_logic.ai.chat.api.messages.MessageDto();
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
		out.name(ID__PROP);
		out.value(getId());
		out.name(ROLE__PROP);
		out.value(getRole());
		out.name(USER_NAME__PROP);
		out.value(getUserName());
		out.name(CONTENT__PROP);
		out.value(getContent());
		out.name(CONTENT_HTML__PROP);
		out.value(getContentHtml());
		out.name(TIME__PROP);
		out.value(getTime());
		out.name(ATTACHMENTS__PROP);
		out.beginArray();
		for (com.top_logic.ai.chat.api.messages.AttachmentDto x : getAttachments()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ID__PROP: setId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ROLE__PROP: setRole(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case USER_NAME__PROP: setUserName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CONTENT__PROP: setContent(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CONTENT_HTML__PROP: setContentHtml(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TIME__PROP: setTime(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ATTACHMENTS__PROP: {
				java.util.List<com.top_logic.ai.chat.api.messages.AttachmentDto> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.ai.chat.api.messages.AttachmentDto.readAttachmentDto(in));
				}
				in.endArray();
				setAttachments(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
