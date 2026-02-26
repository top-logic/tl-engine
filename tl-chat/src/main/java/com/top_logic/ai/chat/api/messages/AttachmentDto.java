package com.top_logic.ai.chat.api.messages;

/**
 * File attachment metadata and optional binary data.
 */
public class AttachmentDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.AttachmentDto} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.AttachmentDto create() {
		return new com.top_logic.ai.chat.api.messages.AttachmentDto();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.AttachmentDto} type in JSON format. */
	public static final String ATTACHMENT_DTO__TYPE = "AttachmentDto";

	/** @see #getFileName() */
	public static final String FILE_NAME__PROP = "fileName";

	/** @see #getMimeType() */
	public static final String MIME_TYPE__PROP = "mimeType";

	/** @see #getBase64Data() */
	public static final String BASE_64_DATA__PROP = "base64Data";

	/** @see #getSize() */
	public static final String SIZE__PROP = "size";

	private String _fileName = "";

	private String _mimeType = "";

	private String _base64Data = "";

	private int _size = 0;

	/**
	 * Creates a {@link AttachmentDto} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.AttachmentDto#create()
	 */
	protected AttachmentDto() {
		super();
	}

	/**
	 * Original file name.
	 */
	public final String getFileName() {
		return _fileName;
	}

	/**
	 * @see #getFileName()
	 */
	public com.top_logic.ai.chat.api.messages.AttachmentDto setFileName(String value) {
		internalSetFileName(value);
		return this;
	}

	/** Internal setter for {@link #getFileName()} without chain call utility. */
	protected final void internalSetFileName(String value) {
		_fileName = value;
	}

	/**
	 * MIME type (e.g. "image/png", "application/pdf").
	 */
	public final String getMimeType() {
		return _mimeType;
	}

	/**
	 * @see #getMimeType()
	 */
	public com.top_logic.ai.chat.api.messages.AttachmentDto setMimeType(String value) {
		internalSetMimeType(value);
		return this;
	}

	/** Internal setter for {@link #getMimeType()} without chain call utility. */
	protected final void internalSetMimeType(String value) {
		_mimeType = value;
	}

	/**
	 * Base64-encoded file content. Present in ChatSendRequest (upload); null in persisted MessageDto.
	 */
	public final String getBase64Data() {
		return _base64Data;
	}

	/**
	 * @see #getBase64Data()
	 */
	public com.top_logic.ai.chat.api.messages.AttachmentDto setBase64Data(String value) {
		internalSetBase64Data(value);
		return this;
	}

	/** Internal setter for {@link #getBase64Data()} without chain call utility. */
	protected final void internalSetBase64Data(String value) {
		_base64Data = value;
	}

	/**
	 * File size in bytes.
	 */
	public final int getSize() {
		return _size;
	}

	/**
	 * @see #getSize()
	 */
	public com.top_logic.ai.chat.api.messages.AttachmentDto setSize(int value) {
		internalSetSize(value);
		return this;
	}

	/** Internal setter for {@link #getSize()} without chain call utility. */
	protected final void internalSetSize(int value) {
		_size = value;
	}

	@Override
	public String jsonType() {
		return ATTACHMENT_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			FILE_NAME__PROP, 
			MIME_TYPE__PROP, 
			BASE_64_DATA__PROP, 
			SIZE__PROP);
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
			case FILE_NAME__PROP: return getFileName();
			case MIME_TYPE__PROP: return getMimeType();
			case BASE_64_DATA__PROP: return getBase64Data();
			case SIZE__PROP: return getSize();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case FILE_NAME__PROP: internalSetFileName((String) value); break;
			case MIME_TYPE__PROP: internalSetMimeType((String) value); break;
			case BASE_64_DATA__PROP: internalSetBase64Data((String) value); break;
			case SIZE__PROP: internalSetSize((int) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.AttachmentDto readAttachmentDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.AttachmentDto result = new com.top_logic.ai.chat.api.messages.AttachmentDto();
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
		out.name(FILE_NAME__PROP);
		out.value(getFileName());
		out.name(MIME_TYPE__PROP);
		out.value(getMimeType());
		out.name(BASE_64_DATA__PROP);
		out.value(getBase64Data());
		out.name(SIZE__PROP);
		out.value(getSize());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case FILE_NAME__PROP: setFileName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case MIME_TYPE__PROP: setMimeType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case BASE_64_DATA__PROP: setBase64Data(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case SIZE__PROP: setSize(in.nextInt()); break;
			default: super.readField(in, field);
		}
	}

}
