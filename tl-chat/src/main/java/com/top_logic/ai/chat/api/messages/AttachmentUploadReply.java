package com.top_logic.ai.chat.api.messages;

/**
 * Reply to an upload of files to the message being written in a {@code ChatControl}.
 */
public class AttachmentUploadReply extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.AttachmentUploadReply} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.AttachmentUploadReply create() {
		return new com.top_logic.ai.chat.api.messages.AttachmentUploadReply();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.AttachmentUploadReply} type in JSON format. */
	public static final String ATTACHMENT_UPLOAD_REPLY__TYPE = "AttachmentUploadReply";

	/** @see #getReason() */
	public static final String REASON__PROP = "reason";

	private final java.util.List<String> _reason = new java.util.ArrayList<>();

	/**
	 * Creates a {@link AttachmentUploadReply} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.AttachmentUploadReply#create()
	 */
	protected AttachmentUploadReply() {
		super();
	}

	/**
	 * Why files were not attached, one sentence in the user's language each.
	 */
	public final java.util.List<String> getReason() {
		return _reason;
	}

	/**
	 * @see #getReason()
	 */
	public com.top_logic.ai.chat.api.messages.AttachmentUploadReply setReason(java.util.List<? extends String> value) {
		internalSetReason(value);
		return this;
	}

	/** Internal setter for {@link #getReason()} without chain call utility. */
	protected final void internalSetReason(java.util.List<? extends String> value) {
		_reason.clear();
		_reason.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getReason()} list.
	 */
	public com.top_logic.ai.chat.api.messages.AttachmentUploadReply addReason(String value) {
		internalAddReason(value);
		return this;
	}

	/** Implementation of {@link #addReason(String)} without chain call utility. */
	protected final void internalAddReason(String value) {
		_reason.add(value);
	}

	/**
	 * Removes a value from the {@link #getReason()} list.
	 */
	public final void removeReason(String value) {
		_reason.remove(value);
	}

	@Override
	public String jsonType() {
		return ATTACHMENT_UPLOAD_REPLY__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			REASON__PROP);
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
			case REASON__PROP: return getReason();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case REASON__PROP: internalSetReason(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.AttachmentUploadReply readAttachmentUploadReply(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.AttachmentUploadReply result = new com.top_logic.ai.chat.api.messages.AttachmentUploadReply();
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
		out.name(REASON__PROP);
		out.beginArray();
		for (String x : getReason()) {
			out.value(x);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case REASON__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setReason(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
