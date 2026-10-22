package com.top_logic.ai.chat.api.messages;

/**
 * One selectable MCP server.
 */
public class McpServerOptionDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.McpServerOptionDto} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.McpServerOptionDto create() {
		return new com.top_logic.ai.chat.api.messages.McpServerOptionDto();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.McpServerOptionDto} type in JSON format. */
	public static final String MCP_SERVER_OPTION_DTO__TYPE = "McpServerOptionDto";

	/** @see #getName() */
	public static final String NAME__PROP = "name";

	/** @see #getStatus() */
	public static final String STATUS__PROP = "status";

	private String _name = "";

	private String _status = "";

	/**
	 * Creates a {@link McpServerOptionDto} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.McpServerOptionDto#create()
	 */
	protected McpServerOptionDto() {
		super();
	}

	/**
	 * Configured server name.
	 */
	public final String getName() {
		return _name;
	}

	/**
	 * @see #getName()
	 */
	public com.top_logic.ai.chat.api.messages.McpServerOptionDto setName(String value) {
		internalSetName(value);
		return this;
	}

	/** Internal setter for {@link #getName()} without chain call utility. */
	protected final void internalSetName(String value) {
		_name = value;
	}

	/**
	 * "connected" or "disconnected".
	 */
	public final String getStatus() {
		return _status;
	}

	/**
	 * @see #getStatus()
	 */
	public com.top_logic.ai.chat.api.messages.McpServerOptionDto setStatus(String value) {
		internalSetStatus(value);
		return this;
	}

	/** Internal setter for {@link #getStatus()} without chain call utility. */
	protected final void internalSetStatus(String value) {
		_status = value;
	}

	@Override
	public String jsonType() {
		return MCP_SERVER_OPTION_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			NAME__PROP, 
			STATUS__PROP);
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
			case NAME__PROP: return getName();
			case STATUS__PROP: return getStatus();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case NAME__PROP: internalSetName((String) value); break;
			case STATUS__PROP: internalSetStatus((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.McpServerOptionDto readMcpServerOptionDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.McpServerOptionDto result = new com.top_logic.ai.chat.api.messages.McpServerOptionDto();
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
		out.name(STATUS__PROP);
		out.value(getStatus());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case NAME__PROP: setName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case STATUS__PROP: setStatus(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
