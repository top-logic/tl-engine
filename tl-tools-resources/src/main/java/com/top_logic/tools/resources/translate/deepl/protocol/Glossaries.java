package com.top_logic.tools.resources.translate.deepl.protocol;

public interface Glossaries extends de.haumacher.msgbuf.data.DataObject {

	/**
	 * Creates a {@link com.top_logic.tools.resources.translate.deepl.protocol.Glossaries} instance.
	 */
	static com.top_logic.tools.resources.translate.deepl.protocol.Glossaries create() {
		return new com.top_logic.tools.resources.translate.deepl.protocol.impl.Glossaries_Impl();
	}

	/** Identifier for the {@link com.top_logic.tools.resources.translate.deepl.protocol.Glossaries} type in JSON format. */
	String GLOSSARIES__TYPE = "Glossaries";

	/** @see #getGlossaries() */
	String GLOSSARIES__PROP = "glossaries";

	java.util.List<com.top_logic.tools.resources.translate.deepl.protocol.Glossary> getGlossaries();

	/**
	 * @see #getGlossaries()
	 */
	com.top_logic.tools.resources.translate.deepl.protocol.Glossaries setGlossaries(java.util.List<? extends com.top_logic.tools.resources.translate.deepl.protocol.Glossary> value);

	/**
	 * Adds a value to the {@link #getGlossaries()} list.
	 */
	com.top_logic.tools.resources.translate.deepl.protocol.Glossaries addGlossarie(com.top_logic.tools.resources.translate.deepl.protocol.Glossary value);

	/**
	 * Removes a value from the {@link #getGlossaries()} list.
	 */
	void removeGlossarie(com.top_logic.tools.resources.translate.deepl.protocol.Glossary value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.tools.resources.translate.deepl.protocol.Glossaries readGlossaries(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tools.resources.translate.deepl.protocol.impl.Glossaries_Impl result = new com.top_logic.tools.resources.translate.deepl.protocol.impl.Glossaries_Impl();
		result.readContent(in);
		return result;
	}

}
