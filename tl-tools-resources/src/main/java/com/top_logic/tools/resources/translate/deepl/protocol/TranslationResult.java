package com.top_logic.tools.resources.translate.deepl.protocol;

/**
 * Result of the <code>translate</code> API call.
 */
public interface TranslationResult extends de.haumacher.msgbuf.data.DataObject {

	/**
	 * Creates a {@link com.top_logic.tools.resources.translate.deepl.protocol.TranslationResult} instance.
	 */
	static com.top_logic.tools.resources.translate.deepl.protocol.TranslationResult create() {
		return new com.top_logic.tools.resources.translate.deepl.protocol.impl.TranslationResult_Impl();
	}

	/** Identifier for the {@link com.top_logic.tools.resources.translate.deepl.protocol.TranslationResult} type in JSON format. */
	String TRANSLATION_RESULT__TYPE = "TranslationResult";

	/** @see #getTranslations() */
	String TRANSLATIONS__PROP = "translations";

	/**
	 * Translations of the requested source text.
	 */
	java.util.List<com.top_logic.tools.resources.translate.deepl.protocol.Translation> getTranslations();

	/**
	 * @see #getTranslations()
	 */
	com.top_logic.tools.resources.translate.deepl.protocol.TranslationResult setTranslations(java.util.List<? extends com.top_logic.tools.resources.translate.deepl.protocol.Translation> value);

	/**
	 * Adds a value to the {@link #getTranslations()} list.
	 */
	com.top_logic.tools.resources.translate.deepl.protocol.TranslationResult addTranslation(com.top_logic.tools.resources.translate.deepl.protocol.Translation value);

	/**
	 * Removes a value from the {@link #getTranslations()} list.
	 */
	void removeTranslation(com.top_logic.tools.resources.translate.deepl.protocol.Translation value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.tools.resources.translate.deepl.protocol.TranslationResult readTranslationResult(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tools.resources.translate.deepl.protocol.impl.TranslationResult_Impl result = new com.top_logic.tools.resources.translate.deepl.protocol.impl.TranslationResult_Impl();
		result.readContent(in);
		return result;
	}

}
