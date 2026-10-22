package com.top_logic.tl.ai.core.tools.model.messages;

/**
 * Response for lock status check operations.
 */
public class LockStatusResponse extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.model.messages.LockStatusResponse} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.model.messages.LockStatusResponse create() {
		return new com.top_logic.tl.ai.core.tools.model.messages.LockStatusResponse();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.model.messages.LockStatusResponse} type in JSON format. */
	public static final String LOCK_STATUS_RESPONSE__TYPE = "LockStatusResponse";

	/** @see #isLocked() */
	public static final String LOCKED__PROP = "locked";

	/** @see #getResource() */
	public static final String RESOURCE__PROP = "resource";

	/** @see #getHolder() */
	public static final String HOLDER__PROP = "holder";

	/** @see #getStatusText() */
	public static final String STATUS_TEXT__PROP = "statusText";

	private boolean _locked = false;

	private String _resource = "";

	private String _holder = "";

	private String _statusText = "";

	/**
	 * Creates a {@link LockStatusResponse} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.model.messages.LockStatusResponse#create()
	 */
	protected LockStatusResponse() {
		super();
	}

	/**
	 * Whether the resource is currently locked.
	 */
	public final boolean isLocked() {
		return _locked;
	}

	/**
	 * @see #isLocked()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.LockStatusResponse setLocked(boolean value) {
		internalSetLocked(value);
		return this;
	}

	/** Internal setter for {@link #isLocked()} without chain call utility. */
	protected final void internalSetLocked(boolean value) {
		_locked = value;
	}

	/**
	 * Qualified name of the checked resource.
	 */
	public final String getResource() {
		return _resource;
	}

	/**
	 * @see #getResource()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.LockStatusResponse setResource(String value) {
		internalSetResource(value);
		return this;
	}

	/** Internal setter for {@link #getResource()} without chain call utility. */
	protected final void internalSetResource(String value) {
		_resource = value;
	}

	/**
	 * Name of the lock holder (only set when locked).
	 */
	public final String getHolder() {
		return _holder;
	}

	/**
	 * @see #getHolder()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.LockStatusResponse setHolder(String value) {
		internalSetHolder(value);
		return this;
	}

	/** Internal setter for {@link #getHolder()} without chain call utility. */
	protected final void internalSetHolder(String value) {
		_holder = value;
	}

	/**
	 * Informational status text (only set when not locked).
	 */
	public final String getStatusText() {
		return _statusText;
	}

	/**
	 * @see #getStatusText()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.LockStatusResponse setStatusText(String value) {
		internalSetStatusText(value);
		return this;
	}

	/** Internal setter for {@link #getStatusText()} without chain call utility. */
	protected final void internalSetStatusText(String value) {
		_statusText = value;
	}

	@Override
	public String jsonType() {
		return LOCK_STATUS_RESPONSE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			LOCKED__PROP, 
			RESOURCE__PROP, 
			HOLDER__PROP, 
			STATUS_TEXT__PROP);
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
			case LOCKED__PROP: return isLocked();
			case RESOURCE__PROP: return getResource();
			case HOLDER__PROP: return getHolder();
			case STATUS_TEXT__PROP: return getStatusText();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case LOCKED__PROP: internalSetLocked((boolean) value); break;
			case RESOURCE__PROP: internalSetResource((String) value); break;
			case HOLDER__PROP: internalSetHolder((String) value); break;
			case STATUS_TEXT__PROP: internalSetStatusText((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.model.messages.LockStatusResponse readLockStatusResponse(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.model.messages.LockStatusResponse result = new com.top_logic.tl.ai.core.tools.model.messages.LockStatusResponse();
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
		out.name(LOCKED__PROP);
		out.value(isLocked());
		out.name(RESOURCE__PROP);
		out.value(getResource());
		out.name(HOLDER__PROP);
		out.value(getHolder());
		out.name(STATUS_TEXT__PROP);
		out.value(getStatusText());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case LOCKED__PROP: setLocked(in.nextBoolean()); break;
			case RESOURCE__PROP: setResource(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case HOLDER__PROP: setHolder(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case STATUS_TEXT__PROP: setStatusText(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
