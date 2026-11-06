package com.top_logic.ai.chat.api.messages;

/**
 * A file the user attached to the message being written, as the composer shows it before the
 * message is sent.
 *
 * <p>
 * The file itself stays on the server until the message is sent; the browser knows it by
 * {@link #getId()}, which is what it names to take the file off the message again.
 * </p>
 */
public class DraftAttachmentDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.DraftAttachmentDto} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.DraftAttachmentDto create() {
		return new com.top_logic.ai.chat.api.messages.DraftAttachmentDto();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.DraftAttachmentDto} type in JSON format. */
	public static final String DRAFT_ATTACHMENT_DTO__TYPE = "DraftAttachmentDto";

	/** @see #getId() */
	public static final String ID__PROP = "id";

	/** @see #getFileName() */
	public static final String FILE_NAME__PROP = "fileName";

	/** @see #getMimeType() */
	public static final String MIME_TYPE__PROP = "mimeType";

	/** @see #getSize() */
	public static final String SIZE__PROP = "size";

	private String _id = "";

	private String _fileName = "";

	private String _mimeType = "";

	private long _size = 0L;

	/**
	 * Creates a {@link DraftAttachmentDto} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.DraftAttachmentDto#create()
	 */
	protected DraftAttachmentDto() {
		super();
	}

	/**
	 * The server's identifier of the attachment, new for every upload.
	 */
	public final String getId() {
		return _id;
	}

	/**
	 * @see #getId()
	 */
	public com.top_logic.ai.chat.api.messages.DraftAttachmentDto setId(String value) {
		internalSetId(value);
		return this;
	}

	/** Internal setter for {@link #getId()} without chain call utility. */
	protected final void internalSetId(String value) {
		_id = value;
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
	public com.top_logic.ai.chat.api.messages.DraftAttachmentDto setFileName(String value) {
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
	public com.top_logic.ai.chat.api.messages.DraftAttachmentDto setMimeType(String value) {
		internalSetMimeType(value);
		return this;
	}

	/** Internal setter for {@link #getMimeType()} without chain call utility. */
	protected final void internalSetMimeType(String value) {
		_mimeType = value;
	}

	/**
	 * File size in bytes.
	 */
	public final long getSize() {
		return _size;
	}

	/**
	 * @see #getSize()
	 */
	public com.top_logic.ai.chat.api.messages.DraftAttachmentDto setSize(long value) {
		internalSetSize(value);
		return this;
	}

	/** Internal setter for {@link #getSize()} without chain call utility. */
	protected final void internalSetSize(long value) {
		_size = value;
	}

	@Override
	public String jsonType() {
		return DRAFT_ATTACHMENT_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			ID__PROP, 
			FILE_NAME__PROP, 
			MIME_TYPE__PROP, 
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
			case ID__PROP: return getId();
			case FILE_NAME__PROP: return getFileName();
			case MIME_TYPE__PROP: return getMimeType();
			case SIZE__PROP: return getSize();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ID__PROP: internalSetId((String) value); break;
			case FILE_NAME__PROP: internalSetFileName((String) value); break;
			case MIME_TYPE__PROP: internalSetMimeType((String) value); break;
			case SIZE__PROP: internalSetSize((long) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.DraftAttachmentDto readDraftAttachmentDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.DraftAttachmentDto result = new com.top_logic.ai.chat.api.messages.DraftAttachmentDto();
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
		out.name(FILE_NAME__PROP);
		out.value(getFileName());
		out.name(MIME_TYPE__PROP);
		out.value(getMimeType());
		out.name(SIZE__PROP);
		out.value(getSize());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ID__PROP: setId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case FILE_NAME__PROP: setFileName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case MIME_TYPE__PROP: setMimeType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case SIZE__PROP: setSize(in.nextLong()); break;
			default: super.readField(in, field);
		}
	}

}
