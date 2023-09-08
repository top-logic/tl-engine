package com.top_logic.tools.resources.translate.deepl.protocol.impl;

public class Glossaries_Impl extends de.haumacher.msgbuf.data.AbstractDataObject implements com.top_logic.tools.resources.translate.deepl.protocol.Glossaries {

	private final java.util.List<com.top_logic.tools.resources.translate.deepl.protocol.Glossary> _glossaries = new java.util.ArrayList<>();

	/**
	 * Creates a {@link Glossaries_Impl} instance.
	 *
	 * @see com.top_logic.tools.resources.translate.deepl.protocol.Glossaries#create()
	 */
	public Glossaries_Impl() {
		super();
	}

	@Override
	public final java.util.List<com.top_logic.tools.resources.translate.deepl.protocol.Glossary> getGlossaries() {
		return _glossaries;
	}

	@Override
	public com.top_logic.tools.resources.translate.deepl.protocol.Glossaries setGlossaries(java.util.List<? extends com.top_logic.tools.resources.translate.deepl.protocol.Glossary> value) {
		internalSetGlossaries(value);
		return this;
	}

	/** Internal setter for {@link #getGlossaries()} without chain call utility. */
	protected final void internalSetGlossaries(java.util.List<? extends com.top_logic.tools.resources.translate.deepl.protocol.Glossary> value) {
		if (value == null) throw new IllegalArgumentException("Property 'glossaries' cannot be null.");
		_glossaries.clear();
		_glossaries.addAll(value);
	}

	@Override
	public com.top_logic.tools.resources.translate.deepl.protocol.Glossaries addGlossarie(com.top_logic.tools.resources.translate.deepl.protocol.Glossary value) {
		internalAddGlossarie(value);
		return this;
	}

	/** Implementation of {@link #addGlossarie(com.top_logic.tools.resources.translate.deepl.protocol.Glossary)} without chain call utility. */
	protected final void internalAddGlossarie(com.top_logic.tools.resources.translate.deepl.protocol.Glossary value) {
		_glossaries.add(value);
	}

	@Override
	public final void removeGlossarie(com.top_logic.tools.resources.translate.deepl.protocol.Glossary value) {
		_glossaries.remove(value);
	}

	@Override
	public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		writeContent(out);
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(GLOSSARIES__PROP);
		out.beginArray();
		for (com.top_logic.tools.resources.translate.deepl.protocol.Glossary x : getGlossaries()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case GLOSSARIES__PROP: {
				in.beginArray();
				while (in.hasNext()) {
					addGlossarie(com.top_logic.tools.resources.translate.deepl.protocol.Glossary.readGlossary(in));
				}
				in.endArray();
			}
			break;
			default: super.readField(in, field);
		}
	}

}
