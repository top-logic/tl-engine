package com.top_logic.tools.resources.translate.deepl.protocol.impl;

/**
 * A single language result of a <code>languages</code> API call.
 */
public class LanguageResult_Impl extends de.haumacher.msgbuf.data.AbstractDataObject implements com.top_logic.tools.resources.translate.deepl.protocol.LanguageResult {

	private String _language = "";

	/**
	 * Creates a {@link LanguageResult_Impl} instance.
	 *
	 * @see com.top_logic.tools.resources.translate.deepl.protocol.LanguageResult#create()
	 */
	public LanguageResult_Impl() {
		super();
	}

	@Override
	public final String getLanguage() {
		return _language;
	}

	@Override
	public com.top_logic.tools.resources.translate.deepl.protocol.LanguageResult setLanguage(String value) {
		internalSetLanguage(value);
		return this;
	}

	/** Internal setter for {@link #getLanguage()} without chain call utility. */
	protected final void internalSetLanguage(String value) {
		_language = value;
	}

	@Override
	public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		writeContent(out);
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(LANGUAGE__PROP);
		out.value(getLanguage());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case LANGUAGE__PROP: setLanguage(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
