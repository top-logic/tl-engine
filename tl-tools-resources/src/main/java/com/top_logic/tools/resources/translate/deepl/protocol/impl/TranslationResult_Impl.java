package com.top_logic.tools.resources.translate.deepl.protocol.impl;

/**
 * Result of the <code>translate</code> API call.
 */
public class TranslationResult_Impl extends de.haumacher.msgbuf.data.AbstractDataObject implements com.top_logic.tools.resources.translate.deepl.protocol.TranslationResult {

	private final java.util.List<com.top_logic.tools.resources.translate.deepl.protocol.Translation> _translations = new java.util.ArrayList<>();

	/**
	 * Creates a {@link TranslationResult_Impl} instance.
	 *
	 * @see com.top_logic.tools.resources.translate.deepl.protocol.TranslationResult#create()
	 */
	public TranslationResult_Impl() {
		super();
	}

	@Override
	public final java.util.List<com.top_logic.tools.resources.translate.deepl.protocol.Translation> getTranslations() {
		return _translations;
	}

	@Override
	public com.top_logic.tools.resources.translate.deepl.protocol.TranslationResult setTranslations(java.util.List<? extends com.top_logic.tools.resources.translate.deepl.protocol.Translation> value) {
		internalSetTranslations(value);
		return this;
	}

	/** Internal setter for {@link #getTranslations()} without chain call utility. */
	protected final void internalSetTranslations(java.util.List<? extends com.top_logic.tools.resources.translate.deepl.protocol.Translation> value) {
		if (value == null) throw new IllegalArgumentException("Property 'translations' cannot be null.");
		_translations.clear();
		_translations.addAll(value);
	}

	@Override
	public com.top_logic.tools.resources.translate.deepl.protocol.TranslationResult addTranslation(com.top_logic.tools.resources.translate.deepl.protocol.Translation value) {
		internalAddTranslation(value);
		return this;
	}

	/** Implementation of {@link #addTranslation(com.top_logic.tools.resources.translate.deepl.protocol.Translation)} without chain call utility. */
	protected final void internalAddTranslation(com.top_logic.tools.resources.translate.deepl.protocol.Translation value) {
		_translations.add(value);
	}

	@Override
	public final void removeTranslation(com.top_logic.tools.resources.translate.deepl.protocol.Translation value) {
		_translations.remove(value);
	}

	@Override
	public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		writeContent(out);
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(TRANSLATIONS__PROP);
		out.beginArray();
		for (com.top_logic.tools.resources.translate.deepl.protocol.Translation x : getTranslations()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TRANSLATIONS__PROP: {
				in.beginArray();
				while (in.hasNext()) {
					addTranslation(com.top_logic.tools.resources.translate.deepl.protocol.Translation.readTranslation(in));
				}
				in.endArray();
			}
			break;
			default: super.readField(in, field);
		}
	}

}
