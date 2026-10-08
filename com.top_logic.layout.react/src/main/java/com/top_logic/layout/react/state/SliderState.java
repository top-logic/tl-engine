package com.top_logic.layout.react.state;

/**
 * State of a number field set by dragging a handle along a track, the component
 * {@code TLSlider}.
 *
 * The {@link #getValue()} is the number itself, or {@code null} for a field holding no value; the
 * handle of an empty field stands at the {@link #getMin()} and no text is shown. A change is sent as
 * the command {@code valueChanged} with the number the handle stands on as argument
 * {@code value}, once the handle comes to rest.
 */
public interface SliderState extends com.top_logic.layout.react.state.FieldState {

	/**
	 * Creates a {@link com.top_logic.layout.react.state.SliderState} instance.
	 */
	static com.top_logic.layout.react.state.SliderState create() {
		return new com.top_logic.layout.react.state.impl.SliderState_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.state.SliderState} type in JSON format. */
	String SLIDER_STATE__TYPE = "SliderState";

	/** @see #getMin() */
	String MIN__PROP = "min";

	/** @see #getMax() */
	String MAX__PROP = "max";

	/** @see #getStep() */
	String STEP__PROP = "step";

	/** @see #getValueLabel() */
	String VALUE_LABEL__PROP = "valueLabel";

	/** @see #getMinLabel() */
	String MIN_LABEL__PROP = "minLabel";

	/** @see #getMaxLabel() */
	String MAX_LABEL__PROP = "maxLabel";

	/** @see #getDebounceMs() */
	String DEBOUNCE_MS__PROP = "debounceMs";

	/**
	 * The smallest value the handle can stand on. Absent: 0.
	 */
	double getMin();

	/**
	 * @see #getMin()
	 */
	com.top_logic.layout.react.state.SliderState setMin(double value);

	/**
	 * The largest value the handle can stand on. Absent: 100.
	 */
	double getMax();

	/**
	 * @see #getMax()
	 */
	com.top_logic.layout.react.state.SliderState setMax(double value);

	/**
	 * The distance between two values the handle can stand on. Absent: 1.
	 */
	double getStep();

	/**
	 * @see #getStep()
	 */
	com.top_logic.layout.react.state.SliderState setStep(double value);

	/**
	 * The value written in the format of the field, plain text, shown beside the handle and as the
	 * whole display of a field that is not editable. Absent: no text.
	 */
	String getValueLabel();

	/**
	 * @see #getValueLabel()
	 */
	com.top_logic.layout.react.state.SliderState setValueLabel(String value);

	/**
	 * The smallest value written in the format of the field, plain text. The client reserves the
	 * width of the wider of the two bound texts for the value text, so the track keeps its length
	 * while the value changes. Absent: no text.
	 */
	String getMinLabel();

	/**
	 * @see #getMinLabel()
	 */
	com.top_logic.layout.react.state.SliderState setMinLabel(String value);

	/**
	 * The largest value written in the format of the field, plain text. See {@link #getMinLabel()}.
	 * Absent: no text.
	 */
	String getMaxLabel();

	/**
	 * @see #getMaxLabel()
	 */
	com.top_logic.layout.react.state.SliderState setMaxLabel(String value);

	/**
	 * How long a value is held back after the handle last moved before it is sent, in
	 * milliseconds; releasing the handle sends it at once. Absent: the default of the component.
	 */
	long getDebounceMs();

	/**
	 * @see #getDebounceMs()
	 */
	com.top_logic.layout.react.state.SliderState setDebounceMs(long value);

	@Override
	com.top_logic.layout.react.state.SliderState setValue(Object value);

	@Override
	com.top_logic.layout.react.state.SliderState setEditable(boolean value);

	@Override
	com.top_logic.layout.react.state.SliderState setDisabled(boolean value);

	@Override
	com.top_logic.layout.react.state.SliderState setMandatory(boolean value);

	@Override
	com.top_logic.layout.react.state.SliderState setNullable(boolean value);

	@Override
	com.top_logic.layout.react.state.SliderState setHasError(boolean value);

	@Override
	com.top_logic.layout.react.state.SliderState setErrorMessage(String value);

	@Override
	com.top_logic.layout.react.state.SliderState setHasWarnings(boolean value);

	@Override
	com.top_logic.layout.react.state.SliderState setLabel(String value);

	@Override
	com.top_logic.layout.react.state.SliderState setTooltip(String value);

	@Override
	com.top_logic.layout.react.state.SliderState setPlaceholder(String value);

	@Override
	com.top_logic.layout.react.state.SliderState setSubmitOnEnter(boolean value);

	@Override
	com.top_logic.layout.react.state.SliderState setHidden(boolean value);

	@Override
	com.top_logic.layout.react.state.SliderState setCssClass(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.state.SliderState readSliderState(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.state.impl.SliderState_Impl result = new com.top_logic.layout.react.state.impl.SliderState_Impl();
		result.readContent(in);
		return result;
	}

}
