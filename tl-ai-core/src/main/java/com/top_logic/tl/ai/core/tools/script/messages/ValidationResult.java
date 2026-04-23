package com.top_logic.tl.ai.core.tools.script.messages;

/**
 * Result of TLScript validation.
 */
public class ValidationResult extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.script.messages.ValidationResult} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.script.messages.ValidationResult create() {
		return new com.top_logic.tl.ai.core.tools.script.messages.ValidationResult();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.script.messages.ValidationResult} type in JSON format. */
	public static final String VALIDATION_RESULT__TYPE = "ValidationResult";

	/** @see #isValid() */
	public static final String VALID__PROP = "valid";

	/** @see #getMessage() */
	public static final String MESSAGE__PROP = "message";

	/** @see #getError() */
	public static final String ERROR__PROP = "error";

	/** @see #getExpr() */
	public static final String EXPR__PROP = "expr";

	private boolean _valid = false;

	private String _message = "";

	private com.top_logic.tl.ai.core.tools.script.messages.ValidationError _error = null;

	private transient com.top_logic.model.search.expr.config.dom.Expr _expr = null;

	/**
	 * Creates a {@link ValidationResult} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.script.messages.ValidationResult#create()
	 */
	protected ValidationResult() {
		super();
	}

	/**
	 * Whether the TLScript is valid.
	 */
	public final boolean isValid() {
		return _valid;
	}

	/**
	 * @see #isValid()
	 */
	public com.top_logic.tl.ai.core.tools.script.messages.ValidationResult setValid(boolean value) {
		internalSetValid(value);
		return this;
	}

	/** Internal setter for {@link #isValid()} without chain call utility. */
	protected final void internalSetValid(boolean value) {
		_valid = value;
	}

	/**
	 * Message describing validation success.
	 */
	public final String getMessage() {
		return _message;
	}

	/**
	 * @see #getMessage()
	 */
	public com.top_logic.tl.ai.core.tools.script.messages.ValidationResult setMessage(String value) {
		internalSetMessage(value);
		return this;
	}

	/** Internal setter for {@link #getMessage()} without chain call utility. */
	protected final void internalSetMessage(String value) {
		_message = value;
	}

	/**
	 * Validation error details (only present if valid is false).
	 */
	public final com.top_logic.tl.ai.core.tools.script.messages.ValidationError getError() {
		return _error;
	}

	/**
	 * @see #getError()
	 */
	public com.top_logic.tl.ai.core.tools.script.messages.ValidationResult setError(com.top_logic.tl.ai.core.tools.script.messages.ValidationError value) {
		internalSetError(value);
		return this;
	}

	/** Internal setter for {@link #getError()} without chain call utility. */
	protected final void internalSetError(com.top_logic.tl.ai.core.tools.script.messages.ValidationError value) {
		_error = value;
	}

	/**
	 * Checks, whether {@link #getError()} has a value.
	 */
	public final boolean hasError() {
		return _error != null;
	}

	/**
	 * The parsed expression (only available if validation succeeded).
	 *
	 * <p>
	 * This property is transient and not serialized to JSON.
	 * </p>
	 */
	public final com.top_logic.model.search.expr.config.dom.Expr getExpr() {
		return _expr;
	}

	/**
	 * @see #getExpr()
	 */
	public com.top_logic.tl.ai.core.tools.script.messages.ValidationResult setExpr(com.top_logic.model.search.expr.config.dom.Expr value) {
		internalSetExpr(value);
		return this;
	}

	/** Internal setter for {@link #getExpr()} without chain call utility. */
	protected final void internalSetExpr(com.top_logic.model.search.expr.config.dom.Expr value) {
		_expr = value;
	}

	/**
	 * Checks, whether {@link #getExpr()} has a value.
	 */
	public final boolean hasExpr() {
		return _expr != null;
	}

	@Override
	public String jsonType() {
		return VALIDATION_RESULT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			VALID__PROP, 
			MESSAGE__PROP, 
			ERROR__PROP, 
			EXPR__PROP);
		PROPERTIES = java.util.Collections.unmodifiableList(local);
	}

	static final java.util.Set<String> TRANSIENT_PROPERTIES;
	static {
		java.util.HashSet<String> tmp = new java.util.HashSet<>();
		tmp.addAll(java.util.Arrays.asList(
				EXPR__PROP));
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
			case VALID__PROP: return isValid();
			case MESSAGE__PROP: return getMessage();
			case ERROR__PROP: return getError();
			case EXPR__PROP: return getExpr();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case VALID__PROP: internalSetValid((boolean) value); break;
			case MESSAGE__PROP: internalSetMessage((String) value); break;
			case ERROR__PROP: internalSetError((com.top_logic.tl.ai.core.tools.script.messages.ValidationError) value); break;
			case EXPR__PROP: internalSetExpr((com.top_logic.model.search.expr.config.dom.Expr) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.script.messages.ValidationResult readValidationResult(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.script.messages.ValidationResult result = new com.top_logic.tl.ai.core.tools.script.messages.ValidationResult();
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
		out.name(VALID__PROP);
		out.value(isValid());
		out.name(MESSAGE__PROP);
		out.value(getMessage());
		if (hasError()) {
			out.name(ERROR__PROP);
			getError().writeTo(out);
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case VALID__PROP: setValid(in.nextBoolean()); break;
			case MESSAGE__PROP: setMessage(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ERROR__PROP: setError(com.top_logic.tl.ai.core.tools.script.messages.ValidationError.readValidationError(in)); break;
			default: super.readField(in, field);
		}
	}

}
