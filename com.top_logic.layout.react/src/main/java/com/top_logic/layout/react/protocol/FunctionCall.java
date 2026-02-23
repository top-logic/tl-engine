package com.top_logic.layout.react.protocol;

/**
 * Calls a JavaScript function.
 */
public interface FunctionCall extends SSEEvent {

	/**
	 * Creates a {@link com.top_logic.layout.react.protocol.FunctionCall} instance.
	 */
	static com.top_logic.layout.react.protocol.FunctionCall create() {
		return new com.top_logic.layout.react.protocol.impl.FunctionCall_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.protocol.FunctionCall} type in JSON format. */
	String FUNCTION_CALL__TYPE = "FunctionCall";

	/** @see #getElementId() */
	String ELEMENT_ID__PROP = "elementId";

	/** @see #getFunctionRef() */
	String FUNCTION_REF__PROP = "functionRef";

	/** @see #getFunctionName() */
	String FUNCTION_NAME__PROP = "functionName";

	/** @see #getArguments() */
	String ARGUMENTS__PROP = "arguments";

	/**
	 * The ID of the context element.
	 */
	String getElementId();

	/**
	 * @see #getElementId()
	 */
	com.top_logic.layout.react.protocol.FunctionCall setElementId(String value);

	/**
	 * The object reference containing the function (e.g. "BAL").
	 */
	String getFunctionRef();

	/**
	 * @see #getFunctionRef()
	 */
	com.top_logic.layout.react.protocol.FunctionCall setFunctionRef(String value);

	/**
	 * The name of the function to call.
	 */
	String getFunctionName();

	/**
	 * @see #getFunctionName()
	 */
	com.top_logic.layout.react.protocol.FunctionCall setFunctionName(String value);

	/**
	 * The arguments serialized as a JSON array string.
	 */
	String getArguments();

	/**
	 * @see #getArguments()
	 */
	com.top_logic.layout.react.protocol.FunctionCall setArguments(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.protocol.FunctionCall readFunctionCall(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.protocol.impl.FunctionCall_Impl result = new com.top_logic.layout.react.protocol.impl.FunctionCall_Impl();
		result.readContent(in);
		return result;
	}

}
