package com.top_logic.layout.react.state.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.state.SuggestionEntry}.
 */
public class SuggestionEntry_Impl extends de.haumacher.msgbuf.data.AbstractDataObject implements com.top_logic.layout.react.state.SuggestionEntry {

	private String _label = "";

	private String _text = "";

	private int _from = 0;

	private int _to = 0;

	/**
	 * Creates a {@link SuggestionEntry_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.state.SuggestionEntry#create()
	 */
	public SuggestionEntry_Impl() {
		super();
	}

	@Override
	public final String getLabel() {
		return _label;
	}

	@Override
	public com.top_logic.layout.react.state.SuggestionEntry setLabel(String value) {
		internalSetLabel(value);
		return this;
	}

	/** Internal setter for {@link #getLabel()} without chain call utility. */
	protected final void internalSetLabel(String value) {
		_label = value;
	}

	@Override
	public final String getText() {
		return _text;
	}

	@Override
	public com.top_logic.layout.react.state.SuggestionEntry setText(String value) {
		internalSetText(value);
		return this;
	}

	/** Internal setter for {@link #getText()} without chain call utility. */
	protected final void internalSetText(String value) {
		_text = value;
	}

	@Override
	public final int getFrom() {
		return _from;
	}

	@Override
	public com.top_logic.layout.react.state.SuggestionEntry setFrom(int value) {
		internalSetFrom(value);
		return this;
	}

	/** Internal setter for {@link #getFrom()} without chain call utility. */
	protected final void internalSetFrom(int value) {
		_from = value;
	}

	@Override
	public final int getTo() {
		return _to;
	}

	@Override
	public com.top_logic.layout.react.state.SuggestionEntry setTo(int value) {
		internalSetTo(value);
		return this;
	}

	/** Internal setter for {@link #getTo()} without chain call utility. */
	protected final void internalSetTo(int value) {
		_to = value;
	}

	@Override
	public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		writeContent(out);
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(LABEL__PROP);
		out.value(getLabel());
		out.name(TEXT__PROP);
		out.value(getText());
		out.name(FROM__PROP);
		out.value(getFrom());
		out.name(TO__PROP);
		out.value(getTo());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case LABEL__PROP: setLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TEXT__PROP: setText(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case FROM__PROP: setFrom(in.nextInt()); break;
			case TO__PROP: setTo(in.nextInt()); break;
			default: super.readField(in, field);
		}
	}

}
