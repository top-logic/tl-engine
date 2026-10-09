package com.top_logic.layout.react.state.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.state.ProgressState}.
 */
public class ProgressState_Impl extends com.top_logic.layout.react.state.impl.ControlState_Impl implements com.top_logic.layout.react.state.ProgressState {

	private double _fraction = 0.0d;

	private String _label = "";

	/**
	 * Creates a {@link ProgressState_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.state.ProgressState#create()
	 */
	public ProgressState_Impl() {
		super();
	}

	@Override
	public final double getFraction() {
		return _fraction;
	}

	@Override
	public com.top_logic.layout.react.state.ProgressState setFraction(double value) {
		internalSetFraction(value);
		return this;
	}

	/** Internal setter for {@link #getFraction()} without chain call utility. */
	protected final void internalSetFraction(double value) {
		_fraction = value;
	}

	@Override
	public final String getLabel() {
		return _label;
	}

	@Override
	public com.top_logic.layout.react.state.ProgressState setLabel(String value) {
		internalSetLabel(value);
		return this;
	}

	/** Internal setter for {@link #getLabel()} without chain call utility. */
	protected final void internalSetLabel(String value) {
		_label = value;
	}

	@Override
	public com.top_logic.layout.react.state.ProgressState setHidden(boolean value) {
		internalSetHidden(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.ProgressState setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public String jsonType() {
		return PROGRESS_STATE__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(FRACTION__PROP);
		out.value(getFraction());
		out.name(LABEL__PROP);
		out.value(getLabel());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case FRACTION__PROP: setFraction(in.nextDouble()); break;
			case LABEL__PROP: setLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
