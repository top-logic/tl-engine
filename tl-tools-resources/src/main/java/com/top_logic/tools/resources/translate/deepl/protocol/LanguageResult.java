package com.top_logic.tools.resources.translate.deepl.protocol;

/**
 * A single language result of a <code>languages</code> API call.
 */
public interface LanguageResult extends de.haumacher.msgbuf.data.DataObject {

	/**
	 * Creates a {@link com.top_logic.tools.resources.translate.deepl.protocol.LanguageResult} instance.
	 */
	static com.top_logic.tools.resources.translate.deepl.protocol.LanguageResult create() {
		return new com.top_logic.tools.resources.translate.deepl.protocol.impl.LanguageResult_Impl();
	}

	/** Identifier for the {@link com.top_logic.tools.resources.translate.deepl.protocol.LanguageResult} type in JSON format. */
	String LANGUAGE_RESULT__TYPE = "LanguageResult";

	/** @see #getLanguage() */
	String LANGUAGE__PROP = "language";

	/**
	 * The language code.
	 */
	String getLanguage();

	/**
	 * @see #getLanguage()
	 */
	com.top_logic.tools.resources.translate.deepl.protocol.LanguageResult setLanguage(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.tools.resources.translate.deepl.protocol.LanguageResult readLanguageResult(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tools.resources.translate.deepl.protocol.impl.LanguageResult_Impl result = new com.top_logic.tools.resources.translate.deepl.protocol.impl.LanguageResult_Impl();
		result.readContent(in);
		return result;
	}

}
