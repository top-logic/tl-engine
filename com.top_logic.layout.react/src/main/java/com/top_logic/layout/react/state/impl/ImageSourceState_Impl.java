package com.top_logic.layout.react.state.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.state.ImageSourceState}.
 */
public class ImageSourceState_Impl extends com.top_logic.layout.react.state.impl.ControlState_Impl implements com.top_logic.layout.react.state.ImageSourceState {

	private String _url = "";

	private boolean _hasData = false;

	private int _dataRevision = 0;

	/**
	 * Creates a {@link ImageSourceState_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.state.ImageSourceState#create()
	 */
	public ImageSourceState_Impl() {
		super();
	}

	@Override
	public final String getUrl() {
		return _url;
	}

	@Override
	public com.top_logic.layout.react.state.ImageSourceState setUrl(String value) {
		internalSetUrl(value);
		return this;
	}

	/** Internal setter for {@link #getUrl()} without chain call utility. */
	protected final void internalSetUrl(String value) {
		_url = value;
	}

	@Override
	public final boolean isHasData() {
		return _hasData;
	}

	@Override
	public com.top_logic.layout.react.state.ImageSourceState setHasData(boolean value) {
		internalSetHasData(value);
		return this;
	}

	/** Internal setter for {@link #isHasData()} without chain call utility. */
	protected final void internalSetHasData(boolean value) {
		_hasData = value;
	}

	@Override
	public final int getDataRevision() {
		return _dataRevision;
	}

	@Override
	public com.top_logic.layout.react.state.ImageSourceState setDataRevision(int value) {
		internalSetDataRevision(value);
		return this;
	}

	/** Internal setter for {@link #getDataRevision()} without chain call utility. */
	protected final void internalSetDataRevision(int value) {
		_dataRevision = value;
	}

	@Override
	public com.top_logic.layout.react.state.ImageSourceState setHidden(boolean value) {
		internalSetHidden(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.ImageSourceState setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public String jsonType() {
		return IMAGE_SOURCE_STATE__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(URL__PROP);
		out.value(getUrl());
		out.name(HAS_DATA__PROP);
		out.value(isHasData());
		out.name(DATA_REVISION__PROP);
		out.value(getDataRevision());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case URL__PROP: setUrl(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case HAS_DATA__PROP: setHasData(in.nextBoolean()); break;
			case DATA_REVISION__PROP: setDataRevision(in.nextInt()); break;
			default: super.readField(in, field);
		}
	}

}
