package com.top_logic.ai.service.tools.model.messages;

/**
 * Information about a blocker preventing deletion.
 */
public class DeletionBlocker extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.service.tools.model.messages.DeletionBlocker} instance.
	 */
	public static com.top_logic.ai.service.tools.model.messages.DeletionBlocker create() {
		return new com.top_logic.ai.service.tools.model.messages.DeletionBlocker();
	}

	/** Identifier for the {@link com.top_logic.ai.service.tools.model.messages.DeletionBlocker} type in JSON format. */
	public static final String DELETION_BLOCKER__TYPE = "DeletionBlocker";

	/** @see #getReason() */
	public static final String REASON__PROP = "reason";

	/** @see #getDetails() */
	public static final String DETAILS__PROP = "details";

	private String _reason = "";

	private String _details = "";

	/**
	 * Creates a {@link DeletionBlocker} instance.
	 *
	 * @see com.top_logic.ai.service.tools.model.messages.DeletionBlocker#create()
	 */
	protected DeletionBlocker() {
		super();
	}

	/**
	 * Reason for blocking: HAS_INSTANCES, HAS_SPECIALIZATIONS, USED_BY_TYPEPART, DELETE_PROTECTED.
	 */
	public final String getReason() {
		return _reason;
	}

	/**
	 * @see #getReason()
	 */
	public com.top_logic.ai.service.tools.model.messages.DeletionBlocker setReason(String value) {
		internalSetReason(value);
		return this;
	}

	/** Internal setter for {@link #getReason()} without chain call utility. */
	protected final void internalSetReason(String value) {
		_reason = value;
	}

	/**
	 * Human-readable details about the blocker.
	 */
	public final String getDetails() {
		return _details;
	}

	/**
	 * @see #getDetails()
	 */
	public com.top_logic.ai.service.tools.model.messages.DeletionBlocker setDetails(String value) {
		internalSetDetails(value);
		return this;
	}

	/** Internal setter for {@link #getDetails()} without chain call utility. */
	protected final void internalSetDetails(String value) {
		_details = value;
	}

	@Override
	public String jsonType() {
		return DELETION_BLOCKER__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			REASON__PROP, 
			DETAILS__PROP);
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
			case DETAILS__PROP: return getDetails();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case REASON__PROP: internalSetReason((String) value); break;
			case DETAILS__PROP: internalSetDetails((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.service.tools.model.messages.DeletionBlocker readDeletionBlocker(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.service.tools.model.messages.DeletionBlocker result = new com.top_logic.ai.service.tools.model.messages.DeletionBlocker();
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
		out.value(getReason());
		out.name(DETAILS__PROP);
		out.value(getDetails());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case REASON__PROP: setReason(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DETAILS__PROP: setDetails(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
