package com.top_logic.layout.react.protocol;

/**
 * Tells the client to clear its i18n translation cache.
 */
public interface I18NCacheInvalidation extends SSEEvent {

	/**
	 * Creates a {@link com.top_logic.layout.react.protocol.I18NCacheInvalidation} instance.
	 */
	static com.top_logic.layout.react.protocol.I18NCacheInvalidation create() {
		return new com.top_logic.layout.react.protocol.impl.I18NCacheInvalidation_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.protocol.I18NCacheInvalidation} type in JSON format. */
	String I_18_NCACHE_INVALIDATION__TYPE = "I18NCacheInvalidation";

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.protocol.I18NCacheInvalidation readI18NCacheInvalidation(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.protocol.impl.I18NCacheInvalidation_Impl result = new com.top_logic.layout.react.protocol.impl.I18NCacheInvalidation_Impl();
		result.readContent(in);
		return result;
	}

}
