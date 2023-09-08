package com.top_logic.tools.resources.translate.deepl.protocol.impl;

public class Glossary_Impl extends de.haumacher.msgbuf.data.AbstractDataObject implements com.top_logic.tools.resources.translate.deepl.protocol.Glossary {

	private String _glossaryId = "";

	private String _name = "";

	private boolean _ready = false;

	private String _sourceLang = "";

	private String _targetLang = "";

	private String _creationTime = "";

	private int _entryCount = 0;

	/**
	 * Creates a {@link Glossary_Impl} instance.
	 *
	 * @see com.top_logic.tools.resources.translate.deepl.protocol.Glossary#create()
	 */
	public Glossary_Impl() {
		super();
	}

	@Override
	public final String getGlossaryId() {
		return _glossaryId;
	}

	@Override
	public com.top_logic.tools.resources.translate.deepl.protocol.Glossary setGlossaryId(String value) {
		internalSetGlossaryId(value);
		return this;
	}

	/** Internal setter for {@link #getGlossaryId()} without chain call utility. */
	protected final void internalSetGlossaryId(String value) {
		_glossaryId = value;
	}

	@Override
	public final String getName() {
		return _name;
	}

	@Override
	public com.top_logic.tools.resources.translate.deepl.protocol.Glossary setName(String value) {
		internalSetName(value);
		return this;
	}

	/** Internal setter for {@link #getName()} without chain call utility. */
	protected final void internalSetName(String value) {
		_name = value;
	}

	@Override
	public final boolean isReady() {
		return _ready;
	}

	@Override
	public com.top_logic.tools.resources.translate.deepl.protocol.Glossary setReady(boolean value) {
		internalSetReady(value);
		return this;
	}

	/** Internal setter for {@link #isReady()} without chain call utility. */
	protected final void internalSetReady(boolean value) {
		_ready = value;
	}

	@Override
	public final String getSourceLang() {
		return _sourceLang;
	}

	@Override
	public com.top_logic.tools.resources.translate.deepl.protocol.Glossary setSourceLang(String value) {
		internalSetSourceLang(value);
		return this;
	}

	/** Internal setter for {@link #getSourceLang()} without chain call utility. */
	protected final void internalSetSourceLang(String value) {
		_sourceLang = value;
	}

	@Override
	public final String getTargetLang() {
		return _targetLang;
	}

	@Override
	public com.top_logic.tools.resources.translate.deepl.protocol.Glossary setTargetLang(String value) {
		internalSetTargetLang(value);
		return this;
	}

	/** Internal setter for {@link #getTargetLang()} without chain call utility. */
	protected final void internalSetTargetLang(String value) {
		_targetLang = value;
	}

	@Override
	public final String getCreationTime() {
		return _creationTime;
	}

	@Override
	public com.top_logic.tools.resources.translate.deepl.protocol.Glossary setCreationTime(String value) {
		internalSetCreationTime(value);
		return this;
	}

	/** Internal setter for {@link #getCreationTime()} without chain call utility. */
	protected final void internalSetCreationTime(String value) {
		_creationTime = value;
	}

	@Override
	public final int getEntryCount() {
		return _entryCount;
	}

	@Override
	public com.top_logic.tools.resources.translate.deepl.protocol.Glossary setEntryCount(int value) {
		internalSetEntryCount(value);
		return this;
	}

	/** Internal setter for {@link #getEntryCount()} without chain call utility. */
	protected final void internalSetEntryCount(int value) {
		_entryCount = value;
	}

	@Override
	public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		writeContent(out);
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(GLOSSARY_ID__PROP);
		out.value(getGlossaryId());
		out.name(NAME__PROP);
		out.value(getName());
		out.name(READY__PROP);
		out.value(isReady());
		out.name(SOURCE_LANG__PROP);
		out.value(getSourceLang());
		out.name(TARGET_LANG__PROP);
		out.value(getTargetLang());
		out.name(CREATION_TIME__PROP);
		out.value(getCreationTime());
		out.name(ENTRY_COUNT__PROP);
		out.value(getEntryCount());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case GLOSSARY_ID__PROP: setGlossaryId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case NAME__PROP: setName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case READY__PROP: setReady(in.nextBoolean()); break;
			case SOURCE_LANG__PROP: setSourceLang(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TARGET_LANG__PROP: setTargetLang(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CREATION_TIME__PROP: setCreationTime(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ENTRY_COUNT__PROP: setEntryCount(in.nextInt()); break;
			default: super.readField(in, field);
		}
	}

}
