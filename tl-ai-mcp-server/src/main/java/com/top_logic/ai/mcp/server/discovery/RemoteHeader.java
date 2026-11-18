package com.top_logic.ai.mcp.server.discovery;

/**
 * A request header a client sends to a {@link ServerRemote}.
 */
public class RemoteHeader extends de.haumacher.msgbuf.data.AbstractDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.mcp.server.discovery.RemoteHeader} instance.
	 */
	public static com.top_logic.ai.mcp.server.discovery.RemoteHeader create() {
		return new com.top_logic.ai.mcp.server.discovery.RemoteHeader();
	}

	/** Identifier for the {@link com.top_logic.ai.mcp.server.discovery.RemoteHeader} type in JSON format. */
	public static final String REMOTE_HEADER__TYPE = "RemoteHeader";

	/** @see #getName() */
	private static final String NAME__PROP = "name";

	/** @see #getDescription() */
	private static final String DESCRIPTION__PROP = "description";

	/** @see #isIsRequired() */
	private static final String IS_REQUIRED__PROP = "isRequired";

	/** @see #isIsSecret() */
	private static final String IS_SECRET__PROP = "isSecret";

	private String _name = "";

	private String _description = null;

	private boolean _isRequired = false;

	private boolean _isSecret = false;

	/**
	 * Creates a {@link RemoteHeader} instance.
	 *
	 * @see com.top_logic.ai.mcp.server.discovery.RemoteHeader#create()
	 */
	protected RemoteHeader() {
		super();
	}

	/**
	 * The name of the header, e.g. <code>Authorization</code>.
	 */
	public final String getName() {
		return _name;
	}

	/**
	 * @see #getName()
	 */
	public com.top_logic.ai.mcp.server.discovery.RemoteHeader setName(String value) {
		internalSetName(value);
		return this;
	}

	/** Internal setter for {@link #getName()} without chain call utility. */
	protected final void internalSetName(String value) {
		_name = value;
	}

	/**
	 * What to put into the header.
	 */
	public final String getDescription() {
		return _description;
	}

	/**
	 * @see #getDescription()
	 */
	public com.top_logic.ai.mcp.server.discovery.RemoteHeader setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	/** Internal setter for {@link #getDescription()} without chain call utility. */
	protected final void internalSetDescription(String value) {
		_description = value;
	}

	/**
	 * Checks, whether {@link #getDescription()} has a value.
	 */
	public final boolean hasDescription() {
		return _description != null;
	}

	/**
	 * Whether every request must carry the header.
	 */
	public final boolean isIsRequired() {
		return _isRequired;
	}

	/**
	 * @see #isIsRequired()
	 */
	public com.top_logic.ai.mcp.server.discovery.RemoteHeader setIsRequired(boolean value) {
		internalSetIsRequired(value);
		return this;
	}

	/** Internal setter for {@link #isIsRequired()} without chain call utility. */
	protected final void internalSetIsRequired(boolean value) {
		_isRequired = value;
	}

	/**
	 * Whether the value of the header is a secret a client must not disclose.
	 */
	public final boolean isIsSecret() {
		return _isSecret;
	}

	/**
	 * @see #isIsSecret()
	 */
	public com.top_logic.ai.mcp.server.discovery.RemoteHeader setIsSecret(boolean value) {
		internalSetIsSecret(value);
		return this;
	}

	/** Internal setter for {@link #isIsSecret()} without chain call utility. */
	protected final void internalSetIsSecret(boolean value) {
		_isSecret = value;
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.mcp.server.discovery.RemoteHeader readRemoteHeader(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.mcp.server.discovery.RemoteHeader result = new com.top_logic.ai.mcp.server.discovery.RemoteHeader();
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
		out.name(NAME__PROP);
		out.value(getName());
		if (hasDescription()) {
			out.name(DESCRIPTION__PROP);
			out.value(getDescription());
		}
		out.name(IS_REQUIRED__PROP);
		out.value(isIsRequired());
		out.name(IS_SECRET__PROP);
		out.value(isIsSecret());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case NAME__PROP: setName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DESCRIPTION__PROP: setDescription(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case IS_REQUIRED__PROP: setIsRequired(in.nextBoolean()); break;
			case IS_SECRET__PROP: setIsSecret(in.nextBoolean()); break;
			default: super.readField(in, field);
		}
	}

}
