package com.top_logic.layout.react.state;

/**
 * State of a bar displaying a fraction, with an optional label beside it, the component
 * {@code TLProgress}.
 *
 * The component sends no commands.
 */
public interface ProgressState extends com.top_logic.layout.react.state.ControlState {

	/**
	 * Creates a {@link com.top_logic.layout.react.state.ProgressState} instance.
	 */
	static com.top_logic.layout.react.state.ProgressState create() {
		return new com.top_logic.layout.react.state.impl.ProgressState_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.state.ProgressState} type in JSON format. */
	String PROGRESS_STATE__TYPE = "ProgressState";

	/** @see #getFraction() */
	String FRACTION__PROP = "fraction";

	/** @see #getLabel() */
	String LABEL__PROP = "label";

	/**
	 * The filled part of the track, a number between 0 and 1. Absent or {@code null}: the
	 * indeterminate bar, which says that something is going on without saying how far it has come.
	 */
	double getFraction();

	/**
	 * @see #getFraction()
	 */
	com.top_logic.layout.react.state.ProgressState setFraction(double value);

	/**
	 * The text beside the bar, plain text (e.g. {@code 3 / 4}). Absent: the bar stands alone.
	 */
	String getLabel();

	/**
	 * @see #getLabel()
	 */
	com.top_logic.layout.react.state.ProgressState setLabel(String value);

	@Override
	com.top_logic.layout.react.state.ProgressState setHidden(boolean value);

	@Override
	com.top_logic.layout.react.state.ProgressState setCssClass(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.state.ProgressState readProgressState(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.state.impl.ProgressState_Impl result = new com.top_logic.layout.react.state.impl.ProgressState_Impl();
		result.readContent(in);
		return result;
	}

}
