package com.top_logic.ai.chat.api.messages;

/**
 * Which files a message of a conversation may carry, as the answerer of the conversation states them
 * in its {@link com.top_logic.ai.chat.backend.AttachmentPolicy}.
 *
 * <p>
 * The browser offers and accepts only these files; the server checks them again on upload and send.
 * </p>
 */
public class AttachmentPolicyDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.AttachmentPolicyDto} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.AttachmentPolicyDto create() {
		return new com.top_logic.ai.chat.api.messages.AttachmentPolicyDto();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.AttachmentPolicyDto} type in JSON format. */
	public static final String ATTACHMENT_POLICY_DTO__TYPE = "AttachmentPolicyDto";

	/** @see #getAccept() */
	public static final String ACCEPT__PROP = "accept";

	/** @see #getTypes() */
	public static final String TYPES__PROP = "types";

	/** @see #getMaxFiles() */
	public static final String MAX_FILES__PROP = "maxFiles";

	/** @see #getMaxFileSize() */
	public static final String MAX_FILE_SIZE__PROP = "maxFileSize";

	/** @see #getMaxMessageSize() */
	public static final String MAX_MESSAGE_SIZE__PROP = "maxMessageSize";

	private String _accept = "";

	private final java.util.List<String> _types = new java.util.ArrayList<>();

	private int _maxFiles = 0;

	private long _maxFileSize = 0L;

	private long _maxMessageSize = 0L;

	/**
	 * Creates a {@link AttachmentPolicyDto} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.AttachmentPolicyDto#create()
	 */
	protected AttachmentPolicyDto() {
		super();
	}

	/**
	 * The value of the <code>accept</code> attribute of the file input, empty when every file type
	 * is allowed.
	 */
	public final String getAccept() {
		return _accept;
	}

	/**
	 * @see #getAccept()
	 */
	public com.top_logic.ai.chat.api.messages.AttachmentPolicyDto setAccept(String value) {
		internalSetAccept(value);
		return this;
	}

	/** Internal setter for {@link #getAccept()} without chain call utility. */
	protected final void internalSetAccept(String value) {
		_accept = value;
	}

	/**
	 * The allowed file types one by one: MIME types like <code>text/csv</code>, MIME patterns like
	 * <code>text/*</code> and file patterns like <code>*.zip</code>. Empty, or holding
	 * <code>*&#47;*</code> or <code>*.*</code>, when every type is allowed.
	 */
	public final java.util.List<String> getTypes() {
		return _types;
	}

	/**
	 * @see #getTypes()
	 */
	public com.top_logic.ai.chat.api.messages.AttachmentPolicyDto setTypes(java.util.List<? extends String> value) {
		internalSetTypes(value);
		return this;
	}

	/** Internal setter for {@link #getTypes()} without chain call utility. */
	protected final void internalSetTypes(java.util.List<? extends String> value) {
		_types.clear();
		_types.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getTypes()} list.
	 */
	public com.top_logic.ai.chat.api.messages.AttachmentPolicyDto addType(String value) {
		internalAddType(value);
		return this;
	}

	/** Implementation of {@link #addType(String)} without chain call utility. */
	protected final void internalAddType(String value) {
		_types.add(value);
	}

	/**
	 * Removes a value from the {@link #getTypes()} list.
	 */
	public final void removeType(String value) {
		_types.remove(value);
	}

	/**
	 * How many files one message may carry; <code>0</code> when it may carry none.
	 */
	public final int getMaxFiles() {
		return _maxFiles;
	}

	/**
	 * @see #getMaxFiles()
	 */
	public com.top_logic.ai.chat.api.messages.AttachmentPolicyDto setMaxFiles(int value) {
		internalSetMaxFiles(value);
		return this;
	}

	/** Internal setter for {@link #getMaxFiles()} without chain call utility. */
	protected final void internalSetMaxFiles(int value) {
		_maxFiles = value;
	}

	/**
	 * The largest file a message may carry, in bytes; <code>0</code> for no limit.
	 */
	public final long getMaxFileSize() {
		return _maxFileSize;
	}

	/**
	 * @see #getMaxFileSize()
	 */
	public com.top_logic.ai.chat.api.messages.AttachmentPolicyDto setMaxFileSize(long value) {
		internalSetMaxFileSize(value);
		return this;
	}

	/** Internal setter for {@link #getMaxFileSize()} without chain call utility. */
	protected final void internalSetMaxFileSize(long value) {
		_maxFileSize = value;
	}

	/**
	 * The largest sum of the sizes of all files of one message, in bytes; <code>0</code> for no limit.
	 */
	public final long getMaxMessageSize() {
		return _maxMessageSize;
	}

	/**
	 * @see #getMaxMessageSize()
	 */
	public com.top_logic.ai.chat.api.messages.AttachmentPolicyDto setMaxMessageSize(long value) {
		internalSetMaxMessageSize(value);
		return this;
	}

	/** Internal setter for {@link #getMaxMessageSize()} without chain call utility. */
	protected final void internalSetMaxMessageSize(long value) {
		_maxMessageSize = value;
	}

	@Override
	public String jsonType() {
		return ATTACHMENT_POLICY_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			ACCEPT__PROP, 
			TYPES__PROP, 
			MAX_FILES__PROP, 
			MAX_FILE_SIZE__PROP, 
			MAX_MESSAGE_SIZE__PROP);
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
			case ACCEPT__PROP: return getAccept();
			case TYPES__PROP: return getTypes();
			case MAX_FILES__PROP: return getMaxFiles();
			case MAX_FILE_SIZE__PROP: return getMaxFileSize();
			case MAX_MESSAGE_SIZE__PROP: return getMaxMessageSize();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ACCEPT__PROP: internalSetAccept((String) value); break;
			case TYPES__PROP: internalSetTypes(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
			case MAX_FILES__PROP: internalSetMaxFiles((int) value); break;
			case MAX_FILE_SIZE__PROP: internalSetMaxFileSize((long) value); break;
			case MAX_MESSAGE_SIZE__PROP: internalSetMaxMessageSize((long) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.AttachmentPolicyDto readAttachmentPolicyDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.AttachmentPolicyDto result = new com.top_logic.ai.chat.api.messages.AttachmentPolicyDto();
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
		out.name(ACCEPT__PROP);
		out.value(getAccept());
		out.name(TYPES__PROP);
		out.beginArray();
		for (String x : getTypes()) {
			out.value(x);
		}
		out.endArray();
		out.name(MAX_FILES__PROP);
		out.value(getMaxFiles());
		out.name(MAX_FILE_SIZE__PROP);
		out.value(getMaxFileSize());
		out.name(MAX_MESSAGE_SIZE__PROP);
		out.value(getMaxMessageSize());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ACCEPT__PROP: setAccept(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TYPES__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setTypes(newValue);
			}
			break;
			case MAX_FILES__PROP: setMaxFiles(in.nextInt()); break;
			case MAX_FILE_SIZE__PROP: setMaxFileSize(in.nextLong()); break;
			case MAX_MESSAGE_SIZE__PROP: setMaxMessageSize(in.nextLong()); break;
			default: super.readField(in, field);
		}
	}

}
