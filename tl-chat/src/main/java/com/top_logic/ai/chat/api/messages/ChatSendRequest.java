package com.top_logic.ai.chat.api.messages;

/**
 * Request body for POST /chat/send.
 */
public class ChatSendRequest extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.ChatSendRequest} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.ChatSendRequest create() {
		return new com.top_logic.ai.chat.api.messages.ChatSendRequest();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.ChatSendRequest} type in JSON format. */
	public static final String CHAT_SEND_REQUEST__TYPE = "ChatSendRequest";

	/** @see #getText() */
	public static final String TEXT__PROP = "text";

	/** @see #getSessionId() */
	public static final String SESSION_ID__PROP = "sessionId";

	/** @see #getAttachments() */
	public static final String ATTACHMENTS__PROP = "attachments";

	/** @see #getAgent() */
	public static final String AGENT__PROP = "agent";

	/** @see #getModel() */
	public static final String MODEL__PROP = "model";

	/** @see #isMcpOverride() */
	public static final String MCP_OVERRIDE__PROP = "mcpOverride";

	/** @see #getMcpServers() */
	public static final String MCP_SERVERS__PROP = "mcpServers";

	/** @see #getEffort() */
	public static final String EFFORT__PROP = "effort";

	/** @see #getFacts() */
	public static final String FACTS__PROP = "facts";

	private String _text = "";

	private String _sessionId = "";

	private final java.util.List<com.top_logic.ai.chat.api.messages.AttachmentDto> _attachments = new java.util.ArrayList<>();

	private String _agent = "";

	private String _model = "";

	private boolean _mcpOverride = false;

	private final java.util.List<String> _mcpServers = new java.util.ArrayList<>();

	private String _effort = "";

	private final java.util.List<com.top_logic.ai.chat.api.messages.FactDto> _facts = new java.util.ArrayList<>();

	/**
	 * Creates a {@link ChatSendRequest} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.ChatSendRequest#create()
	 */
	protected ChatSendRequest() {
		super();
	}

	/**
	 * User message text.
	 */
	public final String getText() {
		return _text;
	}

	/**
	 * @see #getText()
	 */
	public com.top_logic.ai.chat.api.messages.ChatSendRequest setText(String value) {
		internalSetText(value);
		return this;
	}

	/** Internal setter for {@link #getText()} without chain call utility. */
	protected final void internalSetText(String value) {
		_text = value;
	}

	/**
	 * Active session ID; if set, messages will be persisted to this session.
	 */
	public final String getSessionId() {
		return _sessionId;
	}

	/**
	 * @see #getSessionId()
	 */
	public com.top_logic.ai.chat.api.messages.ChatSendRequest setSessionId(String value) {
		internalSetSessionId(value);
		return this;
	}

	/** Internal setter for {@link #getSessionId()} without chain call utility. */
	protected final void internalSetSessionId(String value) {
		_sessionId = value;
	}

	/**
	 * File attachments (images, PDFs). Max 5, each max 10 MB.
	 */
	public final java.util.List<com.top_logic.ai.chat.api.messages.AttachmentDto> getAttachments() {
		return _attachments;
	}

	/**
	 * @see #getAttachments()
	 */
	public com.top_logic.ai.chat.api.messages.ChatSendRequest setAttachments(java.util.List<? extends com.top_logic.ai.chat.api.messages.AttachmentDto> value) {
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
	public com.top_logic.ai.chat.api.messages.ChatSendRequest addAttachment(com.top_logic.ai.chat.api.messages.AttachmentDto value) {
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

	/**
	 * Agent to run for this turn. Empty uses the configured chat entry-point agent.
	 */
	public final String getAgent() {
		return _agent;
	}

	/**
	 * @see #getAgent()
	 */
	public com.top_logic.ai.chat.api.messages.ChatSendRequest setAgent(String value) {
		internalSetAgent(value);
		return this;
	}

	/** Internal setter for {@link #getAgent()} without chain call utility. */
	protected final void internalSetAgent(String value) {
		_agent = value;
	}

	/**
	 * Model override for this turn. Empty keeps the agent's assigned model.
	 */
	public final String getModel() {
		return _model;
	}

	/**
	 * @see #getModel()
	 */
	public com.top_logic.ai.chat.api.messages.ChatSendRequest setModel(String value) {
		internalSetModel(value);
		return this;
	}

	/** Internal setter for {@link #getModel()} without chain call utility. */
	protected final void internalSetModel(String value) {
		_model = value;
	}

	/**
	 * When true, {@link #getMcpServers()} replaces the agent's configured MCP servers for this turn
	 * (an empty list means no MCP tools). When false, the agent's defaults are used.
	 */
	public final boolean isMcpOverride() {
		return _mcpOverride;
	}

	/**
	 * @see #isMcpOverride()
	 */
	public com.top_logic.ai.chat.api.messages.ChatSendRequest setMcpOverride(boolean value) {
		internalSetMcpOverride(value);
		return this;
	}

	/** Internal setter for {@link #isMcpOverride()} without chain call utility. */
	protected final void internalSetMcpOverride(boolean value) {
		_mcpOverride = value;
	}

	/**
	 * MCP servers to attach when {@link #isMcpOverride()} is true.
	 */
	public final java.util.List<String> getMcpServers() {
		return _mcpServers;
	}

	/**
	 * @see #getMcpServers()
	 */
	public com.top_logic.ai.chat.api.messages.ChatSendRequest setMcpServers(java.util.List<? extends String> value) {
		internalSetMcpServers(value);
		return this;
	}

	/** Internal setter for {@link #getMcpServers()} without chain call utility. */
	protected final void internalSetMcpServers(java.util.List<? extends String> value) {
		_mcpServers.clear();
		_mcpServers.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getMcpServers()} list.
	 */
	public com.top_logic.ai.chat.api.messages.ChatSendRequest addMcpServer(String value) {
		internalAddMcpServer(value);
		return this;
	}

	/** Implementation of {@link #addMcpServer(String)} without chain call utility. */
	protected final void internalAddMcpServer(String value) {
		_mcpServers.add(value);
	}

	/**
	 * Removes a value from the {@link #getMcpServers()} list.
	 */
	public final void removeMcpServer(String value) {
		_mcpServers.remove(value);
	}

	/**
	 * How hard the model may think for this turn, one of the selected model's
	 * <code>effortLevels</code>. Empty keeps the agent's assignment; a level the model does not
	 * offer is ignored with a logged warning rather than failing the run.
	 */
	public final String getEffort() {
		return _effort;
	}

	/**
	 * @see #getEffort()
	 */
	public com.top_logic.ai.chat.api.messages.ChatSendRequest setEffort(String value) {
		internalSetEffort(value);
		return this;
	}

	/** Internal setter for {@link #getEffort()} without chain call utility. */
	protected final void internalSetEffort(String value) {
		_effort = value;
	}

	/**
	 * What the page states about the situation this message is asked in.
	 *
	 * <p>
	 * Computed on the server from the chat element's <code>&lt;context&gt;</code> configuration and
	 * echoed back here by the browser, because the run happens on a background thread long after the
	 * page's channels were read. The harness renders the stable ones into the system prompt; the
	 * message text is never touched.
	 * </p>
	 */
	public final java.util.List<com.top_logic.ai.chat.api.messages.FactDto> getFacts() {
		return _facts;
	}

	/**
	 * @see #getFacts()
	 */
	public com.top_logic.ai.chat.api.messages.ChatSendRequest setFacts(java.util.List<? extends com.top_logic.ai.chat.api.messages.FactDto> value) {
		internalSetFacts(value);
		return this;
	}

	/** Internal setter for {@link #getFacts()} without chain call utility. */
	protected final void internalSetFacts(java.util.List<? extends com.top_logic.ai.chat.api.messages.FactDto> value) {
		if (value == null) throw new IllegalArgumentException("Property 'facts' cannot be null.");
		_facts.clear();
		_facts.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getFacts()} list.
	 */
	public com.top_logic.ai.chat.api.messages.ChatSendRequest addFact(com.top_logic.ai.chat.api.messages.FactDto value) {
		internalAddFact(value);
		return this;
	}

	/** Implementation of {@link #addFact(com.top_logic.ai.chat.api.messages.FactDto)} without chain call utility. */
	protected final void internalAddFact(com.top_logic.ai.chat.api.messages.FactDto value) {
		_facts.add(value);
	}

	/**
	 * Removes a value from the {@link #getFacts()} list.
	 */
	public final void removeFact(com.top_logic.ai.chat.api.messages.FactDto value) {
		_facts.remove(value);
	}

	@Override
	public String jsonType() {
		return CHAT_SEND_REQUEST__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TEXT__PROP, 
			SESSION_ID__PROP, 
			ATTACHMENTS__PROP, 
			AGENT__PROP, 
			MODEL__PROP, 
			MCP_OVERRIDE__PROP, 
			MCP_SERVERS__PROP, 
			EFFORT__PROP, 
			FACTS__PROP);
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
			case TEXT__PROP: return getText();
			case SESSION_ID__PROP: return getSessionId();
			case ATTACHMENTS__PROP: return getAttachments();
			case AGENT__PROP: return getAgent();
			case MODEL__PROP: return getModel();
			case MCP_OVERRIDE__PROP: return isMcpOverride();
			case MCP_SERVERS__PROP: return getMcpServers();
			case EFFORT__PROP: return getEffort();
			case FACTS__PROP: return getFacts();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TEXT__PROP: internalSetText((String) value); break;
			case SESSION_ID__PROP: internalSetSessionId((String) value); break;
			case ATTACHMENTS__PROP: internalSetAttachments(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.ai.chat.api.messages.AttachmentDto.class, value)); break;
			case AGENT__PROP: internalSetAgent((String) value); break;
			case MODEL__PROP: internalSetModel((String) value); break;
			case MCP_OVERRIDE__PROP: internalSetMcpOverride((boolean) value); break;
			case MCP_SERVERS__PROP: internalSetMcpServers(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
			case EFFORT__PROP: internalSetEffort((String) value); break;
			case FACTS__PROP: internalSetFacts(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.ai.chat.api.messages.FactDto.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.ChatSendRequest readChatSendRequest(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.ChatSendRequest result = new com.top_logic.ai.chat.api.messages.ChatSendRequest();
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
		out.name(TEXT__PROP);
		out.value(getText());
		out.name(SESSION_ID__PROP);
		out.value(getSessionId());
		out.name(ATTACHMENTS__PROP);
		out.beginArray();
		for (com.top_logic.ai.chat.api.messages.AttachmentDto x : getAttachments()) {
			x.writeTo(out);
		}
		out.endArray();
		out.name(AGENT__PROP);
		out.value(getAgent());
		out.name(MODEL__PROP);
		out.value(getModel());
		out.name(MCP_OVERRIDE__PROP);
		out.value(isMcpOverride());
		out.name(MCP_SERVERS__PROP);
		out.beginArray();
		for (String x : getMcpServers()) {
			out.value(x);
		}
		out.endArray();
		out.name(EFFORT__PROP);
		out.value(getEffort());
		out.name(FACTS__PROP);
		out.beginArray();
		for (com.top_logic.ai.chat.api.messages.FactDto x : getFacts()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TEXT__PROP: setText(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case SESSION_ID__PROP: setSessionId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
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
			case AGENT__PROP: setAgent(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case MODEL__PROP: setModel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case MCP_OVERRIDE__PROP: setMcpOverride(in.nextBoolean()); break;
			case MCP_SERVERS__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setMcpServers(newValue);
			}
			break;
			case EFFORT__PROP: setEffort(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case FACTS__PROP: {
				java.util.List<com.top_logic.ai.chat.api.messages.FactDto> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.ai.chat.api.messages.FactDto.readFactDto(in));
				}
				in.endArray();
				setFacts(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
