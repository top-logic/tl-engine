package com.top_logic.ai.service.tools.model.messages;

/**
 * Response for model element deletion operations.
 */
public class DeleteResponse extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.service.tools.model.messages.DeleteResponse} instance.
	 */
	public static com.top_logic.ai.service.tools.model.messages.DeleteResponse create() {
		return new com.top_logic.ai.service.tools.model.messages.DeleteResponse();
	}

	/** Identifier for the {@link com.top_logic.ai.service.tools.model.messages.DeleteResponse} type in JSON format. */
	public static final String DELETE_RESPONSE__TYPE = "DeleteResponse";

	/** @see #isSuccess() */
	public static final String SUCCESS__PROP = "success";

	/** @see #getName() */
	public static final String NAME__PROP = "name";

	/** @see #getQualifiedName() */
	public static final String QUALIFIED_NAME__PROP = "qualifiedName";

	/** @see #getElementType() */
	public static final String ELEMENT_TYPE__PROP = "elementType";

	/** @see #getResultMessage() */
	public static final String RESULT_MESSAGE__PROP = "resultMessage";

	/** @see #getDeletedParts() */
	public static final String DELETED_PARTS__PROP = "deletedParts";

	/** @see #getBlockers() */
	public static final String BLOCKERS__PROP = "blockers";

	private boolean _success = false;

	private String _name = "";

	private String _qualifiedName = "";

	private String _elementType = "";

	private String _resultMessage = "";

	private final java.util.List<String> _deletedParts = new java.util.ArrayList<>();

	private final java.util.List<com.top_logic.ai.service.tools.model.messages.DeletionBlocker> _blockers = new java.util.ArrayList<>();

	/**
	 * Creates a {@link DeleteResponse} instance.
	 *
	 * @see com.top_logic.ai.service.tools.model.messages.DeleteResponse#create()
	 */
	protected DeleteResponse() {
		super();
	}

	/**
	 * Whether the deletion succeeded.
	 */
	public final boolean isSuccess() {
		return _success;
	}

	/**
	 * @see #isSuccess()
	 */
	public com.top_logic.ai.service.tools.model.messages.DeleteResponse setSuccess(boolean value) {
		internalSetSuccess(value);
		return this;
	}

	/** Internal setter for {@link #isSuccess()} without chain call utility. */
	protected final void internalSetSuccess(boolean value) {
		_success = value;
	}

	/**
	 * Element name.
	 */
	public final String getName() {
		return _name;
	}

	/**
	 * @see #getName()
	 */
	public com.top_logic.ai.service.tools.model.messages.DeleteResponse setName(String value) {
		internalSetName(value);
		return this;
	}

	/** Internal setter for {@link #getName()} without chain call utility. */
	protected final void internalSetName(String value) {
		_name = value;
	}

	/**
	 * Qualified name of the element.
	 */
	public final String getQualifiedName() {
		return _qualifiedName;
	}

	/**
	 * @see #getQualifiedName()
	 */
	public com.top_logic.ai.service.tools.model.messages.DeleteResponse setQualifiedName(String value) {
		internalSetQualifiedName(value);
		return this;
	}

	/** Internal setter for {@link #getQualifiedName()} without chain call utility. */
	protected final void internalSetQualifiedName(String value) {
		_qualifiedName = value;
	}

	/**
	 * Type of element: MODULE, CLASS, ENUMERATION, PROPERTY, REFERENCE, CLASSIFIER.
	 */
	public final String getElementType() {
		return _elementType;
	}

	/**
	 * @see #getElementType()
	 */
	public com.top_logic.ai.service.tools.model.messages.DeleteResponse setElementType(String value) {
		internalSetElementType(value);
		return this;
	}

	/** Internal setter for {@link #getElementType()} without chain call utility. */
	protected final void internalSetElementType(String value) {
		_elementType = value;
	}

	/**
	 * Human-readable result message.
	 */
	public final String getResultMessage() {
		return _resultMessage;
	}

	/**
	 * @see #getResultMessage()
	 */
	public com.top_logic.ai.service.tools.model.messages.DeleteResponse setResultMessage(String value) {
		internalSetResultMessage(value);
		return this;
	}

	/** Internal setter for {@link #getResultMessage()} without chain call utility. */
	protected final void internalSetResultMessage(String value) {
		_resultMessage = value;
	}

	/**
	 * List of parts that were cascade-deleted (on success).
	 */
	public final java.util.List<String> getDeletedParts() {
		return _deletedParts;
	}

	/**
	 * @see #getDeletedParts()
	 */
	public com.top_logic.ai.service.tools.model.messages.DeleteResponse setDeletedParts(java.util.List<? extends String> value) {
		internalSetDeletedParts(value);
		return this;
	}

	/** Internal setter for {@link #getDeletedParts()} without chain call utility. */
	protected final void internalSetDeletedParts(java.util.List<? extends String> value) {
		_deletedParts.clear();
		_deletedParts.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getDeletedParts()} list.
	 */
	public com.top_logic.ai.service.tools.model.messages.DeleteResponse addDeletedPart(String value) {
		internalAddDeletedPart(value);
		return this;
	}

	/** Implementation of {@link #addDeletedPart(String)} without chain call utility. */
	protected final void internalAddDeletedPart(String value) {
		_deletedParts.add(value);
	}

	/**
	 * Removes a value from the {@link #getDeletedParts()} list.
	 */
	public final void removeDeletedPart(String value) {
		_deletedParts.remove(value);
	}

	/**
	 * List of blockers preventing deletion (on failure).
	 */
	public final java.util.List<com.top_logic.ai.service.tools.model.messages.DeletionBlocker> getBlockers() {
		return _blockers;
	}

	/**
	 * @see #getBlockers()
	 */
	public com.top_logic.ai.service.tools.model.messages.DeleteResponse setBlockers(java.util.List<? extends com.top_logic.ai.service.tools.model.messages.DeletionBlocker> value) {
		internalSetBlockers(value);
		return this;
	}

	/** Internal setter for {@link #getBlockers()} without chain call utility. */
	protected final void internalSetBlockers(java.util.List<? extends com.top_logic.ai.service.tools.model.messages.DeletionBlocker> value) {
		if (value == null) throw new IllegalArgumentException("Property 'blockers' cannot be null.");
		_blockers.clear();
		_blockers.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getBlockers()} list.
	 */
	public com.top_logic.ai.service.tools.model.messages.DeleteResponse addBlocker(com.top_logic.ai.service.tools.model.messages.DeletionBlocker value) {
		internalAddBlocker(value);
		return this;
	}

	/** Implementation of {@link #addBlocker(com.top_logic.ai.service.tools.model.messages.DeletionBlocker)} without chain call utility. */
	protected final void internalAddBlocker(com.top_logic.ai.service.tools.model.messages.DeletionBlocker value) {
		_blockers.add(value);
	}

	/**
	 * Removes a value from the {@link #getBlockers()} list.
	 */
	public final void removeBlocker(com.top_logic.ai.service.tools.model.messages.DeletionBlocker value) {
		_blockers.remove(value);
	}

	@Override
	public String jsonType() {
		return DELETE_RESPONSE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			SUCCESS__PROP, 
			NAME__PROP, 
			QUALIFIED_NAME__PROP, 
			ELEMENT_TYPE__PROP, 
			RESULT_MESSAGE__PROP, 
			DELETED_PARTS__PROP, 
			BLOCKERS__PROP);
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
			case SUCCESS__PROP: return isSuccess();
			case NAME__PROP: return getName();
			case QUALIFIED_NAME__PROP: return getQualifiedName();
			case ELEMENT_TYPE__PROP: return getElementType();
			case RESULT_MESSAGE__PROP: return getResultMessage();
			case DELETED_PARTS__PROP: return getDeletedParts();
			case BLOCKERS__PROP: return getBlockers();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case SUCCESS__PROP: internalSetSuccess((boolean) value); break;
			case NAME__PROP: internalSetName((String) value); break;
			case QUALIFIED_NAME__PROP: internalSetQualifiedName((String) value); break;
			case ELEMENT_TYPE__PROP: internalSetElementType((String) value); break;
			case RESULT_MESSAGE__PROP: internalSetResultMessage((String) value); break;
			case DELETED_PARTS__PROP: internalSetDeletedParts(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
			case BLOCKERS__PROP: internalSetBlockers(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.ai.service.tools.model.messages.DeletionBlocker.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.service.tools.model.messages.DeleteResponse readDeleteResponse(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.service.tools.model.messages.DeleteResponse result = new com.top_logic.ai.service.tools.model.messages.DeleteResponse();
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
		out.name(SUCCESS__PROP);
		out.value(isSuccess());
		out.name(NAME__PROP);
		out.value(getName());
		out.name(QUALIFIED_NAME__PROP);
		out.value(getQualifiedName());
		out.name(ELEMENT_TYPE__PROP);
		out.value(getElementType());
		out.name(RESULT_MESSAGE__PROP);
		out.value(getResultMessage());
		out.name(DELETED_PARTS__PROP);
		out.beginArray();
		for (String x : getDeletedParts()) {
			out.value(x);
		}
		out.endArray();
		out.name(BLOCKERS__PROP);
		out.beginArray();
		for (com.top_logic.ai.service.tools.model.messages.DeletionBlocker x : getBlockers()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case SUCCESS__PROP: setSuccess(in.nextBoolean()); break;
			case NAME__PROP: setName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case QUALIFIED_NAME__PROP: setQualifiedName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ELEMENT_TYPE__PROP: setElementType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case RESULT_MESSAGE__PROP: setResultMessage(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DELETED_PARTS__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setDeletedParts(newValue);
			}
			break;
			case BLOCKERS__PROP: {
				java.util.List<com.top_logic.ai.service.tools.model.messages.DeletionBlocker> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.ai.service.tools.model.messages.DeletionBlocker.readDeletionBlocker(in));
				}
				in.endArray();
				setBlockers(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
