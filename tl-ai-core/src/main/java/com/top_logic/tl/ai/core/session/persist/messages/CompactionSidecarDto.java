package com.top_logic.tl.ai.core.session.persist.messages;

/**
 * The replay sidecar of a compaction: the messages it produced, plus a fingerprint of the
 * canonical history they replaced.
 */
public class CompactionSidecarDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.session.persist.messages.CompactionSidecarDto} instance.
	 */
	public static com.top_logic.tl.ai.core.session.persist.messages.CompactionSidecarDto create() {
		return new com.top_logic.tl.ai.core.session.persist.messages.CompactionSidecarDto();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.session.persist.messages.CompactionSidecarDto} type in JSON format. */
	public static final String COMPACTION_SIDECAR_DTO__TYPE = "CompactionSidecarDto";

	/** @see #getFingerprintVersion() */
	public static final String FINGERPRINT_VERSION__PROP = "fingerprintVersion";

	/** @see #getFingerprint() */
	public static final String FINGERPRINT__PROP = "fingerprint";

	/** @see #getReplacementMessages() */
	public static final String REPLACEMENT_MESSAGES__PROP = "replacementMessages";

	private int _fingerprintVersion = 0;

	private String _fingerprint = "";

	private final java.util.List<com.top_logic.tl.ai.core.session.persist.messages.PersistedMessage> _replacementMessages = new java.util.ArrayList<>();

	/**
	 * Creates a {@link CompactionSidecarDto} instance.
	 *
	 * @see com.top_logic.tl.ai.core.session.persist.messages.CompactionSidecarDto#create()
	 */
	protected CompactionSidecarDto() {
		super();
	}

	/**
	 * Version of the fingerprint algorithm {@link #getFingerprint()} was computed with.
	 *
	 * <p>A sidecar whose version is not the reader's current one is rejected rather than
	 * migrated: it is a cache, and the fallback - replaying the full canonical history - is
	 * always available.</p>
	 */
	public final int getFingerprintVersion() {
		return _fingerprintVersion;
	}

	/**
	 * @see #getFingerprintVersion()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.CompactionSidecarDto setFingerprintVersion(int value) {
		internalSetFingerprintVersion(value);
		return this;
	}

	/** Internal setter for {@link #getFingerprintVersion()} without chain call utility. */
	protected final void internalSetFingerprintVersion(int value) {
		_fingerprintVersion = value;
	}

	/**
	 * Fingerprint of the message history this compaction replaced.
	 */
	public final String getFingerprint() {
		return _fingerprint;
	}

	/**
	 * @see #getFingerprint()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.CompactionSidecarDto setFingerprint(String value) {
		internalSetFingerprint(value);
		return this;
	}

	/** Internal setter for {@link #getFingerprint()} without chain call utility. */
	protected final void internalSetFingerprint(String value) {
		_fingerprint = value;
	}

	/**
	 * The messages this compaction put in place of that history.
	 */
	public final java.util.List<com.top_logic.tl.ai.core.session.persist.messages.PersistedMessage> getReplacementMessages() {
		return _replacementMessages;
	}

	/**
	 * @see #getReplacementMessages()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.CompactionSidecarDto setReplacementMessages(java.util.List<? extends com.top_logic.tl.ai.core.session.persist.messages.PersistedMessage> value) {
		internalSetReplacementMessages(value);
		return this;
	}

	/** Internal setter for {@link #getReplacementMessages()} without chain call utility. */
	protected final void internalSetReplacementMessages(java.util.List<? extends com.top_logic.tl.ai.core.session.persist.messages.PersistedMessage> value) {
		if (value == null) throw new IllegalArgumentException("Property 'replacementMessages' cannot be null.");
		_replacementMessages.clear();
		_replacementMessages.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getReplacementMessages()} list.
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.CompactionSidecarDto addReplacementMessage(com.top_logic.tl.ai.core.session.persist.messages.PersistedMessage value) {
		internalAddReplacementMessage(value);
		return this;
	}

	/** Implementation of {@link #addReplacementMessage(com.top_logic.tl.ai.core.session.persist.messages.PersistedMessage)} without chain call utility. */
	protected final void internalAddReplacementMessage(com.top_logic.tl.ai.core.session.persist.messages.PersistedMessage value) {
		_replacementMessages.add(value);
	}

	/**
	 * Removes a value from the {@link #getReplacementMessages()} list.
	 */
	public final void removeReplacementMessage(com.top_logic.tl.ai.core.session.persist.messages.PersistedMessage value) {
		_replacementMessages.remove(value);
	}

	@Override
	public String jsonType() {
		return COMPACTION_SIDECAR_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			FINGERPRINT_VERSION__PROP, 
			FINGERPRINT__PROP, 
			REPLACEMENT_MESSAGES__PROP);
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
			case FINGERPRINT_VERSION__PROP: return getFingerprintVersion();
			case FINGERPRINT__PROP: return getFingerprint();
			case REPLACEMENT_MESSAGES__PROP: return getReplacementMessages();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case FINGERPRINT_VERSION__PROP: internalSetFingerprintVersion((int) value); break;
			case FINGERPRINT__PROP: internalSetFingerprint((String) value); break;
			case REPLACEMENT_MESSAGES__PROP: internalSetReplacementMessages(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.tl.ai.core.session.persist.messages.PersistedMessage.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.session.persist.messages.CompactionSidecarDto readCompactionSidecarDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.session.persist.messages.CompactionSidecarDto result = new com.top_logic.tl.ai.core.session.persist.messages.CompactionSidecarDto();
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
		out.name(FINGERPRINT_VERSION__PROP);
		out.value(getFingerprintVersion());
		out.name(FINGERPRINT__PROP);
		out.value(getFingerprint());
		out.name(REPLACEMENT_MESSAGES__PROP);
		out.beginArray();
		for (com.top_logic.tl.ai.core.session.persist.messages.PersistedMessage x : getReplacementMessages()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case FINGERPRINT_VERSION__PROP: setFingerprintVersion(in.nextInt()); break;
			case FINGERPRINT__PROP: setFingerprint(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case REPLACEMENT_MESSAGES__PROP: {
				java.util.List<com.top_logic.tl.ai.core.session.persist.messages.PersistedMessage> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.tl.ai.core.session.persist.messages.PersistedMessage.readPersistedMessage(in));
				}
				in.endArray();
				setReplacementMessages(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
