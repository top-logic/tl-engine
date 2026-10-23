package com.top_logic.tl.ai.core.tools.script.messages;

/**
 * Type of validation error.
 */
public enum ErrorType implements de.haumacher.msgbuf.data.ProtocolEnum {

	/**
	 * Syntax error (parser error).
	 */
	SYNTAX_ERROR("SYNTAX_ERROR"),

	/**
	 * Lexical error (invalid characters).
	 */
	LEXICAL_ERROR("LEXICAL_ERROR"),

	/**
	 * Validation error (model references, type checking).
	 */
	VALIDATION_ERROR("VALIDATION_ERROR"),

	;

	private final String _protocolName;

	private ErrorType(String protocolName) {
		_protocolName = protocolName;
	}

	/**
	 * The protocol name of a {@link ErrorType} constant.
	 *
	 * @see #valueOfProtocol(String)
	 */
	@Override
	public String protocolName() {
		return _protocolName;
	}

	/** Looks up a {@link ErrorType} constant by it's protocol name. */
	public static ErrorType valueOfProtocol(String protocolName) {
		if (protocolName == null) { return null; }
		switch (protocolName) {
			case "SYNTAX_ERROR": return SYNTAX_ERROR;
			case "LEXICAL_ERROR": return LEXICAL_ERROR;
			case "VALIDATION_ERROR": return VALIDATION_ERROR;
		}
		return SYNTAX_ERROR;
	}

	/** Writes this instance to the given output. */
	public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		out.value(protocolName());
	}

	/** Reads a new instance from the given reader. */
	public static ErrorType readErrorType(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		return valueOfProtocol(in.nextString());
	}

	/** Writes this instance to the given binary output. */
	public final void writeTo(de.haumacher.msgbuf.binary.DataWriter out) throws java.io.IOException {
		switch (this) {
			case SYNTAX_ERROR: out.value(1); break;
			case LEXICAL_ERROR: out.value(2); break;
			case VALIDATION_ERROR: out.value(3); break;
			default: out.value(0);
		}
	}

	/** Reads a new instance from the given binary reader. */
	public static ErrorType readErrorType(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
		switch (in.nextInt()) {
			case 1: return SYNTAX_ERROR;
			case 2: return LEXICAL_ERROR;
			case 3: return VALIDATION_ERROR;
			default: return SYNTAX_ERROR;
		}
	}
}
