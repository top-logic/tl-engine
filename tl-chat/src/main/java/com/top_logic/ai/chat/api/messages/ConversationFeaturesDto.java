package com.top_logic.ai.chat.api.messages;

/**
 * Which functions beyond sending a message the answerer of a conversation supports.
 *
 * <p>
 * The framework's own agent supports all of them. A conversation answered by an external backend
 * supports what the backend states; the chat hides what it does not support, and the server refuses
 * it.
 * </p>
 */
public class ConversationFeaturesDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.ConversationFeaturesDto} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.ConversationFeaturesDto create() {
		return new com.top_logic.ai.chat.api.messages.ConversationFeaturesDto();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.ConversationFeaturesDto} type in JSON format. */
	public static final String CONVERSATION_FEATURES_DTO__TYPE = "ConversationFeaturesDto";

	/** @see #isRegenerate() */
	public static final String REGENERATE__PROP = "regenerate";

	/** @see #isEditMessages() */
	public static final String EDIT_MESSAGES__PROP = "editMessages";

	/** @see #isAgentPicker() */
	public static final String AGENT_PICKER__PROP = "agentPicker";

	/** @see #isModelPicker() */
	public static final String MODEL_PICKER__PROP = "modelPicker";

	/** @see #isEffortPicker() */
	public static final String EFFORT_PICKER__PROP = "effortPicker";

	/** @see #isMcpPicker() */
	public static final String MCP_PICKER__PROP = "mcpPicker";

	/** @see #isAttachments() */
	public static final String ATTACHMENTS__PROP = "attachments";

	/** @see #isApprovals() */
	public static final String APPROVALS__PROP = "approvals";

	private boolean _regenerate = false;

	private boolean _editMessages = false;

	private boolean _agentPicker = false;

	private boolean _modelPicker = false;

	private boolean _effortPicker = false;

	private boolean _mcpPicker = false;

	private boolean _attachments = false;

	private boolean _approvals = false;

	/**
	 * Creates a {@link ConversationFeaturesDto} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.ConversationFeaturesDto#create()
	 */
	protected ConversationFeaturesDto() {
		super();
	}

	/**
	 * Whether an answer can be produced again for the same message.
	 */
	public final boolean isRegenerate() {
		return _regenerate;
	}

	/**
	 * @see #isRegenerate()
	 */
	public com.top_logic.ai.chat.api.messages.ConversationFeaturesDto setRegenerate(boolean value) {
		internalSetRegenerate(value);
		return this;
	}

	/** Internal setter for {@link #isRegenerate()} without chain call utility. */
	protected final void internalSetRegenerate(boolean value) {
		_regenerate = value;
	}

	/**
	 * Whether a sent message can be edited, which drops it and everything after it.
	 */
	public final boolean isEditMessages() {
		return _editMessages;
	}

	/**
	 * @see #isEditMessages()
	 */
	public com.top_logic.ai.chat.api.messages.ConversationFeaturesDto setEditMessages(boolean value) {
		internalSetEditMessages(value);
		return this;
	}

	/** Internal setter for {@link #isEditMessages()} without chain call utility. */
	protected final void internalSetEditMessages(boolean value) {
		_editMessages = value;
	}

	/**
	 * Whether the user can choose the agent that answers.
	 */
	public final boolean isAgentPicker() {
		return _agentPicker;
	}

	/**
	 * @see #isAgentPicker()
	 */
	public com.top_logic.ai.chat.api.messages.ConversationFeaturesDto setAgentPicker(boolean value) {
		internalSetAgentPicker(value);
		return this;
	}

	/** Internal setter for {@link #isAgentPicker()} without chain call utility. */
	protected final void internalSetAgentPicker(boolean value) {
		_agentPicker = value;
	}

	/**
	 * Whether the user can choose the model.
	 */
	public final boolean isModelPicker() {
		return _modelPicker;
	}

	/**
	 * @see #isModelPicker()
	 */
	public com.top_logic.ai.chat.api.messages.ConversationFeaturesDto setModelPicker(boolean value) {
		internalSetModelPicker(value);
		return this;
	}

	/** Internal setter for {@link #isModelPicker()} without chain call utility. */
	protected final void internalSetModelPicker(boolean value) {
		_modelPicker = value;
	}

	/**
	 * Whether the user can choose the thinking effort.
	 */
	public final boolean isEffortPicker() {
		return _effortPicker;
	}

	/**
	 * @see #isEffortPicker()
	 */
	public com.top_logic.ai.chat.api.messages.ConversationFeaturesDto setEffortPicker(boolean value) {
		internalSetEffortPicker(value);
		return this;
	}

	/** Internal setter for {@link #isEffortPicker()} without chain call utility. */
	protected final void internalSetEffortPicker(boolean value) {
		_effortPicker = value;
	}

	/**
	 * Whether the user can choose the MCP servers attached to the run.
	 */
	public final boolean isMcpPicker() {
		return _mcpPicker;
	}

	/**
	 * @see #isMcpPicker()
	 */
	public com.top_logic.ai.chat.api.messages.ConversationFeaturesDto setMcpPicker(boolean value) {
		internalSetMcpPicker(value);
		return this;
	}

	/** Internal setter for {@link #isMcpPicker()} without chain call utility. */
	protected final void internalSetMcpPicker(boolean value) {
		_mcpPicker = value;
	}

	/**
	 * Whether a message may carry files.
	 */
	public final boolean isAttachments() {
		return _attachments;
	}

	/**
	 * @see #isAttachments()
	 */
	public com.top_logic.ai.chat.api.messages.ConversationFeaturesDto setAttachments(boolean value) {
		internalSetAttachments(value);
		return this;
	}

	/** Internal setter for {@link #isAttachments()} without chain call utility. */
	protected final void internalSetAttachments(boolean value) {
		_attachments = value;
	}

	/**
	 * Whether the run asks the user for approvals in the chat, which the auto-approve switch answers.
	 */
	public final boolean isApprovals() {
		return _approvals;
	}

	/**
	 * @see #isApprovals()
	 */
	public com.top_logic.ai.chat.api.messages.ConversationFeaturesDto setApprovals(boolean value) {
		internalSetApprovals(value);
		return this;
	}

	/** Internal setter for {@link #isApprovals()} without chain call utility. */
	protected final void internalSetApprovals(boolean value) {
		_approvals = value;
	}

	@Override
	public String jsonType() {
		return CONVERSATION_FEATURES_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			REGENERATE__PROP, 
			EDIT_MESSAGES__PROP, 
			AGENT_PICKER__PROP, 
			MODEL_PICKER__PROP, 
			EFFORT_PICKER__PROP, 
			MCP_PICKER__PROP, 
			ATTACHMENTS__PROP, 
			APPROVALS__PROP);
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
			case REGENERATE__PROP: return isRegenerate();
			case EDIT_MESSAGES__PROP: return isEditMessages();
			case AGENT_PICKER__PROP: return isAgentPicker();
			case MODEL_PICKER__PROP: return isModelPicker();
			case EFFORT_PICKER__PROP: return isEffortPicker();
			case MCP_PICKER__PROP: return isMcpPicker();
			case ATTACHMENTS__PROP: return isAttachments();
			case APPROVALS__PROP: return isApprovals();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case REGENERATE__PROP: internalSetRegenerate((boolean) value); break;
			case EDIT_MESSAGES__PROP: internalSetEditMessages((boolean) value); break;
			case AGENT_PICKER__PROP: internalSetAgentPicker((boolean) value); break;
			case MODEL_PICKER__PROP: internalSetModelPicker((boolean) value); break;
			case EFFORT_PICKER__PROP: internalSetEffortPicker((boolean) value); break;
			case MCP_PICKER__PROP: internalSetMcpPicker((boolean) value); break;
			case ATTACHMENTS__PROP: internalSetAttachments((boolean) value); break;
			case APPROVALS__PROP: internalSetApprovals((boolean) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.ConversationFeaturesDto readConversationFeaturesDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.ConversationFeaturesDto result = new com.top_logic.ai.chat.api.messages.ConversationFeaturesDto();
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
		out.name(REGENERATE__PROP);
		out.value(isRegenerate());
		out.name(EDIT_MESSAGES__PROP);
		out.value(isEditMessages());
		out.name(AGENT_PICKER__PROP);
		out.value(isAgentPicker());
		out.name(MODEL_PICKER__PROP);
		out.value(isModelPicker());
		out.name(EFFORT_PICKER__PROP);
		out.value(isEffortPicker());
		out.name(MCP_PICKER__PROP);
		out.value(isMcpPicker());
		out.name(ATTACHMENTS__PROP);
		out.value(isAttachments());
		out.name(APPROVALS__PROP);
		out.value(isApprovals());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case REGENERATE__PROP: setRegenerate(in.nextBoolean()); break;
			case EDIT_MESSAGES__PROP: setEditMessages(in.nextBoolean()); break;
			case AGENT_PICKER__PROP: setAgentPicker(in.nextBoolean()); break;
			case MODEL_PICKER__PROP: setModelPicker(in.nextBoolean()); break;
			case EFFORT_PICKER__PROP: setEffortPicker(in.nextBoolean()); break;
			case MCP_PICKER__PROP: setMcpPicker(in.nextBoolean()); break;
			case ATTACHMENTS__PROP: setAttachments(in.nextBoolean()); break;
			case APPROVALS__PROP: setApprovals(in.nextBoolean()); break;
			default: super.readField(in, field);
		}
	}

}
