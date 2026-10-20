package com.top_logic.tl.ai.core.tools.script.messages;

/**
 * Details about a validation error.
 */
public class ValidationError extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.script.messages.ValidationError} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.script.messages.ValidationError create() {
		return new com.top_logic.tl.ai.core.tools.script.messages.ValidationError();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.script.messages.ValidationError} type in JSON format. */
	public static final String VALIDATION_ERROR__TYPE = "ValidationError";

	/** @see #getType() */
	public static final String TYPE__PROP = "type";

	/** @see #getMessage() */
	public static final String MESSAGE__PROP = "message";

	/** @see #getLine() */
	public static final String LINE__PROP = "line";

	/** @see #getColumn() */
	public static final String COLUMN__PROP = "column";

	private com.top_logic.tl.ai.core.tools.script.messages.ErrorType _type = com.top_logic.tl.ai.core.tools.script.messages.ErrorType.SYNTAX_ERROR;

	private String _message = "";

	private int _line = 0;

	private int _column = 0;

	/**
	 * Creates a {@link ValidationError} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.script.messages.ValidationError#create()
	 */
	protected ValidationError() {
		super();
	}

	/**
	 * Type of validation error.
	 */
	public final com.top_logic.tl.ai.core.tools.script.messages.ErrorType getType() {
		return _type;
	}

	/**
	 * @see #getType()
	 */
	public com.top_logic.tl.ai.core.tools.script.messages.ValidationError setType(com.top_logic.tl.ai.core.tools.script.messages.ErrorType value) {
		internalSetType(value);
		return this;
	}

	/** Internal setter for {@link #getType()} without chain call utility. */
	protected final void internalSetType(com.top_logic.tl.ai.core.tools.script.messages.ErrorType value) {
		if (value == null) throw new IllegalArgumentException("Property 'type' cannot be null.");
		_type = value;
	}

	/**
	 * The error message.
	 */
	public final String getMessage() {
		return _message;
	}

	/**
	 * @see #getMessage()
	 */
	public com.top_logic.tl.ai.core.tools.script.messages.ValidationError setMessage(String value) {
		internalSetMessage(value);
		return this;
	}

	/** Internal setter for {@link #getMessage()} without chain call utility. */
	protected final void internalSetMessage(String value) {
		_message = value;
	}

	/**
	 * Line number where the error occurred (0 if not applicable).
	 */
	public final int getLine() {
		return _line;
	}

	/**
	 * @see #getLine()
	 */
	public com.top_logic.tl.ai.core.tools.script.messages.ValidationError setLine(int value) {
		internalSetLine(value);
		return this;
	}

	/** Internal setter for {@link #getLine()} without chain call utility. */
	protected final void internalSetLine(int value) {
		_line = value;
	}

	/**
	 * Column number where the error occurred (0 if not applicable).
	 */
	public final int getColumn() {
		return _column;
	}

	/**
	 * @see #getColumn()
	 */
	public com.top_logic.tl.ai.core.tools.script.messages.ValidationError setColumn(int value) {
		internalSetColumn(value);
		return this;
	}

	/** Internal setter for {@link #getColumn()} without chain call utility. */
	protected final void internalSetColumn(int value) {
		_column = value;
	}

	@Override
	public String jsonType() {
		return VALIDATION_ERROR__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TYPE__PROP, 
			MESSAGE__PROP, 
			LINE__PROP, 
			COLUMN__PROP);
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
			case TYPE__PROP: return getType();
			case MESSAGE__PROP: return getMessage();
			case LINE__PROP: return getLine();
			case COLUMN__PROP: return getColumn();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TYPE__PROP: internalSetType((com.top_logic.tl.ai.core.tools.script.messages.ErrorType) value); break;
			case MESSAGE__PROP: internalSetMessage((String) value); break;
			case LINE__PROP: internalSetLine((int) value); break;
			case COLUMN__PROP: internalSetColumn((int) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.script.messages.ValidationError readValidationError(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.script.messages.ValidationError result = new com.top_logic.tl.ai.core.tools.script.messages.ValidationError();
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
		out.name(TYPE__PROP);
		getType().writeTo(out);
		out.name(MESSAGE__PROP);
		out.value(getMessage());
		out.name(LINE__PROP);
		out.value(getLine());
		out.name(COLUMN__PROP);
		out.value(getColumn());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TYPE__PROP: setType(com.top_logic.tl.ai.core.tools.script.messages.ErrorType.readErrorType(in)); break;
			case MESSAGE__PROP: setMessage(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case LINE__PROP: setLine(in.nextInt()); break;
			case COLUMN__PROP: setColumn(in.nextInt()); break;
			default: super.readField(in, field);
		}
	}

}
