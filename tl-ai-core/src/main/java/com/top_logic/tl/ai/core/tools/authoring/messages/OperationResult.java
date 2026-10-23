package com.top_logic.tl.ai.core.tools.authoring.messages;

/**
 * Result of a create/update/delete authoring operation.
 */
public class OperationResult extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.authoring.messages.OperationResult} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.authoring.messages.OperationResult create() {
		return new com.top_logic.tl.ai.core.tools.authoring.messages.OperationResult();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.authoring.messages.OperationResult} type in JSON format. */
	public static final String OPERATION_RESULT__TYPE = "OperationResult";

	/** @see #isSuccess() */
	public static final String SUCCESS__PROP = "success";

	/** @see #getResultMessage() */
	public static final String RESULT_MESSAGE__PROP = "resultMessage";

	/** @see #getWarnings() */
	public static final String WARNINGS__PROP = "warnings";

	private boolean _success = false;

	private String _resultMessage = "";

	private final java.util.List<String> _warnings = new java.util.ArrayList<>();

	/**
	 * Creates a {@link OperationResult} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.authoring.messages.OperationResult#create()
	 */
	protected OperationResult() {
		super();
	}

	/**
	 * Whether the operation succeeded.
	 */
	public final boolean isSuccess() {
		return _success;
	}

	/**
	 * @see #isSuccess()
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.OperationResult setSuccess(boolean value) {
		internalSetSuccess(value);
		return this;
	}

	/** Internal setter for {@link #isSuccess()} without chain call utility. */
	protected final void internalSetSuccess(boolean value) {
		_success = value;
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
	public com.top_logic.tl.ai.core.tools.authoring.messages.OperationResult setResultMessage(String value) {
		internalSetResultMessage(value);
		return this;
	}

	/** Internal setter for {@link #getResultMessage()} without chain call utility. */
	protected final void internalSetResultMessage(String value) {
		_resultMessage = value;
	}

	/**
	 * Non-fatal warnings (e.g. references to not-yet-existing skills or agents).
	 */
	public final java.util.List<String> getWarnings() {
		return _warnings;
	}

	/**
	 * @see #getWarnings()
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.OperationResult setWarnings(java.util.List<? extends String> value) {
		internalSetWarnings(value);
		return this;
	}

	/** Internal setter for {@link #getWarnings()} without chain call utility. */
	protected final void internalSetWarnings(java.util.List<? extends String> value) {
		_warnings.clear();
		_warnings.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getWarnings()} list.
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.OperationResult addWarning(String value) {
		internalAddWarning(value);
		return this;
	}

	/** Implementation of {@link #addWarning(String)} without chain call utility. */
	protected final void internalAddWarning(String value) {
		_warnings.add(value);
	}

	/**
	 * Removes a value from the {@link #getWarnings()} list.
	 */
	public final void removeWarning(String value) {
		_warnings.remove(value);
	}

	@Override
	public String jsonType() {
		return OPERATION_RESULT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			SUCCESS__PROP, 
			RESULT_MESSAGE__PROP, 
			WARNINGS__PROP);
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
			case RESULT_MESSAGE__PROP: return getResultMessage();
			case WARNINGS__PROP: return getWarnings();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case SUCCESS__PROP: internalSetSuccess((boolean) value); break;
			case RESULT_MESSAGE__PROP: internalSetResultMessage((String) value); break;
			case WARNINGS__PROP: internalSetWarnings(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.authoring.messages.OperationResult readOperationResult(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.authoring.messages.OperationResult result = new com.top_logic.tl.ai.core.tools.authoring.messages.OperationResult();
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
		out.name(RESULT_MESSAGE__PROP);
		out.value(getResultMessage());
		out.name(WARNINGS__PROP);
		out.beginArray();
		for (String x : getWarnings()) {
			out.value(x);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case SUCCESS__PROP: setSuccess(in.nextBoolean()); break;
			case RESULT_MESSAGE__PROP: setResultMessage(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case WARNINGS__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setWarnings(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
