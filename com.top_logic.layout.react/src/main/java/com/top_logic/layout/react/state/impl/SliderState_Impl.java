package com.top_logic.layout.react.state.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.state.SliderState}.
 */
public class SliderState_Impl extends com.top_logic.layout.react.state.impl.FieldState_Impl implements com.top_logic.layout.react.state.SliderState {

	private double _min = 0.0d;

	private double _max = 0.0d;

	private double _step = 0.0d;

	private String _valueLabel = "";

	private long _debounceMs = 0L;

	/**
	 * Creates a {@link SliderState_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.state.SliderState#create()
	 */
	public SliderState_Impl() {
		super();
	}

	@Override
	public final double getMin() {
		return _min;
	}

	@Override
	public com.top_logic.layout.react.state.SliderState setMin(double value) {
		internalSetMin(value);
		return this;
	}

	/** Internal setter for {@link #getMin()} without chain call utility. */
	protected final void internalSetMin(double value) {
		_min = value;
	}

	@Override
	public final double getMax() {
		return _max;
	}

	@Override
	public com.top_logic.layout.react.state.SliderState setMax(double value) {
		internalSetMax(value);
		return this;
	}

	/** Internal setter for {@link #getMax()} without chain call utility. */
	protected final void internalSetMax(double value) {
		_max = value;
	}

	@Override
	public final double getStep() {
		return _step;
	}

	@Override
	public com.top_logic.layout.react.state.SliderState setStep(double value) {
		internalSetStep(value);
		return this;
	}

	/** Internal setter for {@link #getStep()} without chain call utility. */
	protected final void internalSetStep(double value) {
		_step = value;
	}

	@Override
	public final String getValueLabel() {
		return _valueLabel;
	}

	@Override
	public com.top_logic.layout.react.state.SliderState setValueLabel(String value) {
		internalSetValueLabel(value);
		return this;
	}

	/** Internal setter for {@link #getValueLabel()} without chain call utility. */
	protected final void internalSetValueLabel(String value) {
		_valueLabel = value;
	}

	@Override
	public final long getDebounceMs() {
		return _debounceMs;
	}

	@Override
	public com.top_logic.layout.react.state.SliderState setDebounceMs(long value) {
		internalSetDebounceMs(value);
		return this;
	}

	/** Internal setter for {@link #getDebounceMs()} without chain call utility. */
	protected final void internalSetDebounceMs(long value) {
		_debounceMs = value;
	}

	@Override
	public com.top_logic.layout.react.state.SliderState setValue(Object value) {
		internalSetValue(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.SliderState setEditable(boolean value) {
		internalSetEditable(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.SliderState setDisabled(boolean value) {
		internalSetDisabled(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.SliderState setMandatory(boolean value) {
		internalSetMandatory(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.SliderState setNullable(boolean value) {
		internalSetNullable(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.SliderState setHasError(boolean value) {
		internalSetHasError(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.SliderState setErrorMessage(String value) {
		internalSetErrorMessage(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.SliderState setHasWarnings(boolean value) {
		internalSetHasWarnings(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.SliderState setLabel(String value) {
		internalSetLabel(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.SliderState setTooltip(String value) {
		internalSetTooltip(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.SliderState setPlaceholder(String value) {
		internalSetPlaceholder(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.SliderState setSubmitOnEnter(boolean value) {
		internalSetSubmitOnEnter(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.SliderState setHidden(boolean value) {
		internalSetHidden(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.SliderState setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public String jsonType() {
		return SLIDER_STATE__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(MIN__PROP);
		out.value(getMin());
		out.name(MAX__PROP);
		out.value(getMax());
		out.name(STEP__PROP);
		out.value(getStep());
		out.name(VALUE_LABEL__PROP);
		out.value(getValueLabel());
		out.name(DEBOUNCE_MS__PROP);
		out.value(getDebounceMs());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case MIN__PROP: setMin(in.nextDouble()); break;
			case MAX__PROP: setMax(in.nextDouble()); break;
			case STEP__PROP: setStep(in.nextDouble()); break;
			case VALUE_LABEL__PROP: setValueLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DEBOUNCE_MS__PROP: setDebounceMs(in.nextLong()); break;
			default: super.readField(in, field);
		}
	}

}
