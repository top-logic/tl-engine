package com.top_logic.tools.resources.translate.deepl.protocol;

public interface Glossary extends de.haumacher.msgbuf.data.DataObject {

	/**
	 * Creates a {@link com.top_logic.tools.resources.translate.deepl.protocol.Glossary} instance.
	 */
	static com.top_logic.tools.resources.translate.deepl.protocol.Glossary create() {
		return new com.top_logic.tools.resources.translate.deepl.protocol.impl.Glossary_Impl();
	}

	/** Identifier for the {@link com.top_logic.tools.resources.translate.deepl.protocol.Glossary} type in JSON format. */
	String GLOSSARY__TYPE = "Glossary";

	/** @see #getGlossaryId() */
	String GLOSSARY_ID__PROP = "glossary_id";

	/** @see #getName() */
	String NAME__PROP = "name";

	/** @see #isReady() */
	String READY__PROP = "ready";

	/** @see #getSourceLang() */
	String SOURCE_LANG__PROP = "source_lang";

	/** @see #getTargetLang() */
	String TARGET_LANG__PROP = "target_lang";

	/** @see #getCreationTime() */
	String CREATION_TIME__PROP = "creation_time";

	/** @see #getEntryCount() */
	String ENTRY_COUNT__PROP = "entry_count";

	String getGlossaryId();

	/**
	 * @see #getGlossaryId()
	 */
	com.top_logic.tools.resources.translate.deepl.protocol.Glossary setGlossaryId(String value);

	String getName();

	/**
	 * @see #getName()
	 */
	com.top_logic.tools.resources.translate.deepl.protocol.Glossary setName(String value);

	boolean isReady();

	/**
	 * @see #isReady()
	 */
	com.top_logic.tools.resources.translate.deepl.protocol.Glossary setReady(boolean value);

	String getSourceLang();

	/**
	 * @see #getSourceLang()
	 */
	com.top_logic.tools.resources.translate.deepl.protocol.Glossary setSourceLang(String value);

	String getTargetLang();

	/**
	 * @see #getTargetLang()
	 */
	com.top_logic.tools.resources.translate.deepl.protocol.Glossary setTargetLang(String value);

	String getCreationTime();

	/**
	 * @see #getCreationTime()
	 */
	com.top_logic.tools.resources.translate.deepl.protocol.Glossary setCreationTime(String value);

	int getEntryCount();

	/**
	 * @see #getEntryCount()
	 */
	com.top_logic.tools.resources.translate.deepl.protocol.Glossary setEntryCount(int value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.tools.resources.translate.deepl.protocol.Glossary readGlossary(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tools.resources.translate.deepl.protocol.impl.Glossary_Impl result = new com.top_logic.tools.resources.translate.deepl.protocol.impl.Glossary_Impl();
		result.readContent(in);
		return result;
	}

}
