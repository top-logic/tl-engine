package com.top_logic.tl.ai.core.session.persist.messages;

/**
 * A single part within a turn, representing one atomic piece of content.
 */
public class PersistedPart extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.session.persist.messages.PersistedPart} instance.
	 */
	public static com.top_logic.tl.ai.core.session.persist.messages.PersistedPart create() {
		return new com.top_logic.tl.ai.core.session.persist.messages.PersistedPart();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.session.persist.messages.PersistedPart} type in JSON format. */
	public static final String PERSISTED_PART__TYPE = "PersistedPart";

	/** @see #getId() */
	public static final String ID__PROP = "id";

	/** @see #getTimestamp() */
	public static final String TIMESTAMP__PROP = "timestamp";

	/** @see #getType() */
	public static final String TYPE__PROP = "type";

	/** @see #getTextData() */
	public static final String TEXT_DATA__PROP = "textData";

	/** @see #getThinkingData() */
	public static final String THINKING_DATA__PROP = "thinkingData";

	/** @see #getToolCallData() */
	public static final String TOOL_CALL_DATA__PROP = "toolCallData";

	/** @see #getApprovalData() */
	public static final String APPROVAL_DATA__PROP = "approvalData";

	/** @see #getInteractionData() */
	public static final String INTERACTION_DATA__PROP = "interactionData";

	/** @see #getCompactionData() */
	public static final String COMPACTION_DATA__PROP = "compactionData";

	/** @see #getErrorData() */
	public static final String ERROR_DATA__PROP = "errorData";

	/** @see #getTransactionData() */
	public static final String TRANSACTION_DATA__PROP = "transactionData";

	/** @see #getDelegationData() */
	public static final String DELEGATION_DATA__PROP = "delegationData";

	/** @see #getSystemData() */
	public static final String SYSTEM_DATA__PROP = "systemData";

	private String _id = "";

	private long _timestamp = 0L;

	private String _type = "";

	private com.top_logic.tl.ai.core.session.persist.messages.TextPartDto _textData = null;

	private com.top_logic.tl.ai.core.session.persist.messages.ThinkingPartDto _thinkingData = null;

	private com.top_logic.tl.ai.core.session.persist.messages.ToolCallPartDto _toolCallData = null;

	private com.top_logic.tl.ai.core.session.persist.messages.ApprovalPartDto _approvalData = null;

	private com.top_logic.tl.ai.core.session.persist.messages.InteractionPartDto _interactionData = null;

	private com.top_logic.tl.ai.core.session.persist.messages.CompactionPartDto _compactionData = null;

	private com.top_logic.tl.ai.core.session.persist.messages.ErrorPartDto _errorData = null;

	private com.top_logic.tl.ai.core.session.persist.messages.TransactionPartDto _transactionData = null;

	private com.top_logic.tl.ai.core.session.persist.messages.DelegationPartDto _delegationData = null;

	private com.top_logic.tl.ai.core.session.persist.messages.SystemPartDto _systemData = null;

	/**
	 * Creates a {@link PersistedPart} instance.
	 *
	 * @see com.top_logic.tl.ai.core.session.persist.messages.PersistedPart#create()
	 */
	protected PersistedPart() {
		super();
	}

	/**
	 * Unique part identifier.
	 */
	public final String getId() {
		return _id;
	}

	/**
	 * @see #getId()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedPart setId(String value) {
		internalSetId(value);
		return this;
	}

	/** Internal setter for {@link #getId()} without chain call utility. */
	protected final void internalSetId(String value) {
		_id = value;
	}

	/**
	 * Timestamp when this part was created.
	 */
	public final long getTimestamp() {
		return _timestamp;
	}

	/**
	 * @see #getTimestamp()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedPart setTimestamp(long value) {
		internalSetTimestamp(value);
		return this;
	}

	/** Internal setter for {@link #getTimestamp()} without chain call utility. */
	protected final void internalSetTimestamp(long value) {
		_timestamp = value;
	}

	/**
	 * Discriminator for the part type.
	 */
	public final String getType() {
		return _type;
	}

	/**
	 * @see #getType()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedPart setType(String value) {
		internalSetType(value);
		return this;
	}

	/** Internal setter for {@link #getType()} without chain call utility. */
	protected final void internalSetType(String value) {
		_type = value;
	}

	/**
	 * Text content data.
	 */
	public final com.top_logic.tl.ai.core.session.persist.messages.TextPartDto getTextData() {
		return _textData;
	}

	/**
	 * @see #getTextData()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedPart setTextData(com.top_logic.tl.ai.core.session.persist.messages.TextPartDto value) {
		internalSetTextData(value);
		return this;
	}

	/** Internal setter for {@link #getTextData()} without chain call utility. */
	protected final void internalSetTextData(com.top_logic.tl.ai.core.session.persist.messages.TextPartDto value) {
		_textData = value;
	}

	/**
	 * Checks, whether {@link #getTextData()} has a value.
	 */
	public final boolean hasTextData() {
		return _textData != null;
	}

	/**
	 * Thinking/reasoning data.
	 */
	public final com.top_logic.tl.ai.core.session.persist.messages.ThinkingPartDto getThinkingData() {
		return _thinkingData;
	}

	/**
	 * @see #getThinkingData()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedPart setThinkingData(com.top_logic.tl.ai.core.session.persist.messages.ThinkingPartDto value) {
		internalSetThinkingData(value);
		return this;
	}

	/** Internal setter for {@link #getThinkingData()} without chain call utility. */
	protected final void internalSetThinkingData(com.top_logic.tl.ai.core.session.persist.messages.ThinkingPartDto value) {
		_thinkingData = value;
	}

	/**
	 * Checks, whether {@link #getThinkingData()} has a value.
	 */
	public final boolean hasThinkingData() {
		return _thinkingData != null;
	}

	/**
	 * Tool call data.
	 */
	public final com.top_logic.tl.ai.core.session.persist.messages.ToolCallPartDto getToolCallData() {
		return _toolCallData;
	}

	/**
	 * @see #getToolCallData()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedPart setToolCallData(com.top_logic.tl.ai.core.session.persist.messages.ToolCallPartDto value) {
		internalSetToolCallData(value);
		return this;
	}

	/** Internal setter for {@link #getToolCallData()} without chain call utility. */
	protected final void internalSetToolCallData(com.top_logic.tl.ai.core.session.persist.messages.ToolCallPartDto value) {
		_toolCallData = value;
	}

	/**
	 * Checks, whether {@link #getToolCallData()} has a value.
	 */
	public final boolean hasToolCallData() {
		return _toolCallData != null;
	}

	/**
	 * Approval request data.
	 */
	public final com.top_logic.tl.ai.core.session.persist.messages.ApprovalPartDto getApprovalData() {
		return _approvalData;
	}

	/**
	 * @see #getApprovalData()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedPart setApprovalData(com.top_logic.tl.ai.core.session.persist.messages.ApprovalPartDto value) {
		internalSetApprovalData(value);
		return this;
	}

	/** Internal setter for {@link #getApprovalData()} without chain call utility. */
	protected final void internalSetApprovalData(com.top_logic.tl.ai.core.session.persist.messages.ApprovalPartDto value) {
		_approvalData = value;
	}

	/**
	 * Checks, whether {@link #getApprovalData()} has a value.
	 */
	public final boolean hasApprovalData() {
		return _approvalData != null;
	}

	/**
	 * User interaction data.
	 */
	public final com.top_logic.tl.ai.core.session.persist.messages.InteractionPartDto getInteractionData() {
		return _interactionData;
	}

	/**
	 * @see #getInteractionData()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedPart setInteractionData(com.top_logic.tl.ai.core.session.persist.messages.InteractionPartDto value) {
		internalSetInteractionData(value);
		return this;
	}

	/** Internal setter for {@link #getInteractionData()} without chain call utility. */
	protected final void internalSetInteractionData(com.top_logic.tl.ai.core.session.persist.messages.InteractionPartDto value) {
		_interactionData = value;
	}

	/**
	 * Checks, whether {@link #getInteractionData()} has a value.
	 */
	public final boolean hasInteractionData() {
		return _interactionData != null;
	}

	/**
	 * Context compaction data.
	 */
	public final com.top_logic.tl.ai.core.session.persist.messages.CompactionPartDto getCompactionData() {
		return _compactionData;
	}

	/**
	 * @see #getCompactionData()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedPart setCompactionData(com.top_logic.tl.ai.core.session.persist.messages.CompactionPartDto value) {
		internalSetCompactionData(value);
		return this;
	}

	/** Internal setter for {@link #getCompactionData()} without chain call utility. */
	protected final void internalSetCompactionData(com.top_logic.tl.ai.core.session.persist.messages.CompactionPartDto value) {
		_compactionData = value;
	}

	/**
	 * Checks, whether {@link #getCompactionData()} has a value.
	 */
	public final boolean hasCompactionData() {
		return _compactionData != null;
	}

	/**
	 * Error data.
	 */
	public final com.top_logic.tl.ai.core.session.persist.messages.ErrorPartDto getErrorData() {
		return _errorData;
	}

	/**
	 * @see #getErrorData()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedPart setErrorData(com.top_logic.tl.ai.core.session.persist.messages.ErrorPartDto value) {
		internalSetErrorData(value);
		return this;
	}

	/** Internal setter for {@link #getErrorData()} without chain call utility. */
	protected final void internalSetErrorData(com.top_logic.tl.ai.core.session.persist.messages.ErrorPartDto value) {
		_errorData = value;
	}

	/**
	 * Checks, whether {@link #getErrorData()} has a value.
	 */
	public final boolean hasErrorData() {
		return _errorData != null;
	}

	/**
	 * Transaction data.
	 */
	public final com.top_logic.tl.ai.core.session.persist.messages.TransactionPartDto getTransactionData() {
		return _transactionData;
	}

	/**
	 * @see #getTransactionData()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedPart setTransactionData(com.top_logic.tl.ai.core.session.persist.messages.TransactionPartDto value) {
		internalSetTransactionData(value);
		return this;
	}

	/** Internal setter for {@link #getTransactionData()} without chain call utility. */
	protected final void internalSetTransactionData(com.top_logic.tl.ai.core.session.persist.messages.TransactionPartDto value) {
		_transactionData = value;
	}

	/**
	 * Checks, whether {@link #getTransactionData()} has a value.
	 */
	public final boolean hasTransactionData() {
		return _transactionData != null;
	}

	/**
	 * Agent delegation data.
	 */
	public final com.top_logic.tl.ai.core.session.persist.messages.DelegationPartDto getDelegationData() {
		return _delegationData;
	}

	/**
	 * @see #getDelegationData()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedPart setDelegationData(com.top_logic.tl.ai.core.session.persist.messages.DelegationPartDto value) {
		internalSetDelegationData(value);
		return this;
	}

	/** Internal setter for {@link #getDelegationData()} without chain call utility. */
	protected final void internalSetDelegationData(com.top_logic.tl.ai.core.session.persist.messages.DelegationPartDto value) {
		_delegationData = value;
	}

	/**
	 * Checks, whether {@link #getDelegationData()} has a value.
	 */
	public final boolean hasDelegationData() {
		return _delegationData != null;
	}

	/**
	 * System event data.
	 */
	public final com.top_logic.tl.ai.core.session.persist.messages.SystemPartDto getSystemData() {
		return _systemData;
	}

	/**
	 * @see #getSystemData()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedPart setSystemData(com.top_logic.tl.ai.core.session.persist.messages.SystemPartDto value) {
		internalSetSystemData(value);
		return this;
	}

	/** Internal setter for {@link #getSystemData()} without chain call utility. */
	protected final void internalSetSystemData(com.top_logic.tl.ai.core.session.persist.messages.SystemPartDto value) {
		_systemData = value;
	}

	/**
	 * Checks, whether {@link #getSystemData()} has a value.
	 */
	public final boolean hasSystemData() {
		return _systemData != null;
	}

	@Override
	public String jsonType() {
		return PERSISTED_PART__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			ID__PROP, 
			TIMESTAMP__PROP, 
			TYPE__PROP, 
			TEXT_DATA__PROP, 
			THINKING_DATA__PROP, 
			TOOL_CALL_DATA__PROP, 
			APPROVAL_DATA__PROP, 
			INTERACTION_DATA__PROP, 
			COMPACTION_DATA__PROP, 
			ERROR_DATA__PROP, 
			TRANSACTION_DATA__PROP, 
			DELEGATION_DATA__PROP, 
			SYSTEM_DATA__PROP);
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
			case TIMESTAMP__PROP: return getTimestamp();
			case TYPE__PROP: return getType();
			case TEXT_DATA__PROP: return getTextData();
			case THINKING_DATA__PROP: return getThinkingData();
			case TOOL_CALL_DATA__PROP: return getToolCallData();
			case APPROVAL_DATA__PROP: return getApprovalData();
			case INTERACTION_DATA__PROP: return getInteractionData();
			case COMPACTION_DATA__PROP: return getCompactionData();
			case ERROR_DATA__PROP: return getErrorData();
			case TRANSACTION_DATA__PROP: return getTransactionData();
			case DELEGATION_DATA__PROP: return getDelegationData();
			case SYSTEM_DATA__PROP: return getSystemData();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ID__PROP: internalSetId((String) value); break;
			case TIMESTAMP__PROP: internalSetTimestamp((long) value); break;
			case TYPE__PROP: internalSetType((String) value); break;
			case TEXT_DATA__PROP: internalSetTextData((com.top_logic.tl.ai.core.session.persist.messages.TextPartDto) value); break;
			case THINKING_DATA__PROP: internalSetThinkingData((com.top_logic.tl.ai.core.session.persist.messages.ThinkingPartDto) value); break;
			case TOOL_CALL_DATA__PROP: internalSetToolCallData((com.top_logic.tl.ai.core.session.persist.messages.ToolCallPartDto) value); break;
			case APPROVAL_DATA__PROP: internalSetApprovalData((com.top_logic.tl.ai.core.session.persist.messages.ApprovalPartDto) value); break;
			case INTERACTION_DATA__PROP: internalSetInteractionData((com.top_logic.tl.ai.core.session.persist.messages.InteractionPartDto) value); break;
			case COMPACTION_DATA__PROP: internalSetCompactionData((com.top_logic.tl.ai.core.session.persist.messages.CompactionPartDto) value); break;
			case ERROR_DATA__PROP: internalSetErrorData((com.top_logic.tl.ai.core.session.persist.messages.ErrorPartDto) value); break;
			case TRANSACTION_DATA__PROP: internalSetTransactionData((com.top_logic.tl.ai.core.session.persist.messages.TransactionPartDto) value); break;
			case DELEGATION_DATA__PROP: internalSetDelegationData((com.top_logic.tl.ai.core.session.persist.messages.DelegationPartDto) value); break;
			case SYSTEM_DATA__PROP: internalSetSystemData((com.top_logic.tl.ai.core.session.persist.messages.SystemPartDto) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.session.persist.messages.PersistedPart readPersistedPart(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.session.persist.messages.PersistedPart result = new com.top_logic.tl.ai.core.session.persist.messages.PersistedPart();
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
		out.name(TIMESTAMP__PROP);
		out.value(getTimestamp());
		out.name(TYPE__PROP);
		out.value(getType());
		if (hasTextData()) {
			out.name(TEXT_DATA__PROP);
			getTextData().writeTo(out);
		}
		if (hasThinkingData()) {
			out.name(THINKING_DATA__PROP);
			getThinkingData().writeTo(out);
		}
		if (hasToolCallData()) {
			out.name(TOOL_CALL_DATA__PROP);
			getToolCallData().writeTo(out);
		}
		if (hasApprovalData()) {
			out.name(APPROVAL_DATA__PROP);
			getApprovalData().writeTo(out);
		}
		if (hasInteractionData()) {
			out.name(INTERACTION_DATA__PROP);
			getInteractionData().writeTo(out);
		}
		if (hasCompactionData()) {
			out.name(COMPACTION_DATA__PROP);
			getCompactionData().writeTo(out);
		}
		if (hasErrorData()) {
			out.name(ERROR_DATA__PROP);
			getErrorData().writeTo(out);
		}
		if (hasTransactionData()) {
			out.name(TRANSACTION_DATA__PROP);
			getTransactionData().writeTo(out);
		}
		if (hasDelegationData()) {
			out.name(DELEGATION_DATA__PROP);
			getDelegationData().writeTo(out);
		}
		if (hasSystemData()) {
			out.name(SYSTEM_DATA__PROP);
			getSystemData().writeTo(out);
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ID__PROP: setId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TIMESTAMP__PROP: setTimestamp(in.nextLong()); break;
			case TYPE__PROP: setType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TEXT_DATA__PROP: setTextData(com.top_logic.tl.ai.core.session.persist.messages.TextPartDto.readTextPartDto(in)); break;
			case THINKING_DATA__PROP: setThinkingData(com.top_logic.tl.ai.core.session.persist.messages.ThinkingPartDto.readThinkingPartDto(in)); break;
			case TOOL_CALL_DATA__PROP: setToolCallData(com.top_logic.tl.ai.core.session.persist.messages.ToolCallPartDto.readToolCallPartDto(in)); break;
			case APPROVAL_DATA__PROP: setApprovalData(com.top_logic.tl.ai.core.session.persist.messages.ApprovalPartDto.readApprovalPartDto(in)); break;
			case INTERACTION_DATA__PROP: setInteractionData(com.top_logic.tl.ai.core.session.persist.messages.InteractionPartDto.readInteractionPartDto(in)); break;
			case COMPACTION_DATA__PROP: setCompactionData(com.top_logic.tl.ai.core.session.persist.messages.CompactionPartDto.readCompactionPartDto(in)); break;
			case ERROR_DATA__PROP: setErrorData(com.top_logic.tl.ai.core.session.persist.messages.ErrorPartDto.readErrorPartDto(in)); break;
			case TRANSACTION_DATA__PROP: setTransactionData(com.top_logic.tl.ai.core.session.persist.messages.TransactionPartDto.readTransactionPartDto(in)); break;
			case DELEGATION_DATA__PROP: setDelegationData(com.top_logic.tl.ai.core.session.persist.messages.DelegationPartDto.readDelegationPartDto(in)); break;
			case SYSTEM_DATA__PROP: setSystemData(com.top_logic.tl.ai.core.session.persist.messages.SystemPartDto.readSystemPartDto(in)); break;
			default: super.readField(in, field);
		}
	}

}
