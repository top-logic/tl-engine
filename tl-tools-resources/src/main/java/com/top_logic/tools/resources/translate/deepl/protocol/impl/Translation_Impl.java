package com.top_logic.tools.resources.translate.deepl.protocol.impl;

/**
 * A single translation result of a <code>TranslationResult</code> message.
 */
public class Translation_Impl extends de.haumacher.msgbuf.data.AbstractDataObject implements com.top_logic.tools.resources.translate.deepl.protocol.Translation {

	private String _detectedSourceLanguage = "";

	private String _text = "";

	/**
	 * Creates a {@link Translation_Impl} instance.
	 *
	 * @see com.top_logic.tools.resources.translate.deepl.protocol.Translation#create()
	 */
	public Translation_Impl() {
		super();
	}

	@Override
	public final String getDetectedSourceLanguage() {
		return _detectedSourceLanguage;
	}

	@Override
	public com.top_logic.tools.resources.translate.deepl.protocol.Translation setDetectedSourceLanguage(String value) {
		internalSetDetectedSourceLanguage(value);
		return this;
	}

	/** Internal setter for {@link #getDetectedSourceLanguage()} without chain call utility. */
	protected final void internalSetDetectedSourceLanguage(String value) {
		_detectedSourceLanguage = value;
	}

	@Override
	public final String getText() {
		return _text;
	}

	@Override
	public com.top_logic.tools.resources.translate.deepl.protocol.Translation setText(String value) {
		internalSetText(value);
		return this;
	}

	/** Internal setter for {@link #getText()} without chain call utility. */
	protected final void internalSetText(String value) {
		_text = value;
	}

	@Override
	public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		writeContent(out);
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(DETECTED_SOURCE_LANGUAGE__PROP);
		out.value(getDetectedSourceLanguage());
		out.name(TEXT__PROP);
		out.value(getText());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case DETECTED_SOURCE_LANGUAGE__PROP: setDetectedSourceLanguage(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TEXT__PROP: setText(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
