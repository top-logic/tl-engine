package com.top_logic.tools.resources.translate.deepl.protocol;

/**
 * A single translation result of a <code>TranslationResult</code> message.
 */
public interface Translation extends de.haumacher.msgbuf.data.DataObject {

	/**
	 * Creates a {@link com.top_logic.tools.resources.translate.deepl.protocol.Translation} instance.
	 */
	static com.top_logic.tools.resources.translate.deepl.protocol.Translation create() {
		return new com.top_logic.tools.resources.translate.deepl.protocol.impl.Translation_Impl();
	}

	/** Identifier for the {@link com.top_logic.tools.resources.translate.deepl.protocol.Translation} type in JSON format. */
	String TRANSLATION__TYPE = "Translation";

	/** @see #getDetectedSourceLanguage() */
	String DETECTED_SOURCE_LANGUAGE__PROP = "detected_source_language";

	/** @see #getText() */
	String TEXT__PROP = "text";

	/**
	 * The source language of the translation source text.
	 */
	String getDetectedSourceLanguage();

	/**
	 * @see #getDetectedSourceLanguage()
	 */
	com.top_logic.tools.resources.translate.deepl.protocol.Translation setDetectedSourceLanguage(String value);

	/**
	 * The translated text.
	 */
	String getText();

	/**
	 * @see #getText()
	 */
	com.top_logic.tools.resources.translate.deepl.protocol.Translation setText(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.tools.resources.translate.deepl.protocol.Translation readTranslation(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tools.resources.translate.deepl.protocol.impl.Translation_Impl result = new com.top_logic.tools.resources.translate.deepl.protocol.impl.Translation_Impl();
		result.readContent(in);
		return result;
	}

}
