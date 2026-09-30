package com.top_logic.layout.react.state;

/**
 * State of a toggle button, the component {@code TLToggleButton}.
 *
 * A click sends the command {@code click} to the server.
 */
public interface ToggleButtonState extends com.top_logic.layout.react.state.ControlState {

	/**
	 * Creates a {@link com.top_logic.layout.react.state.ToggleButtonState} instance.
	 */
	static com.top_logic.layout.react.state.ToggleButtonState create() {
		return new com.top_logic.layout.react.state.impl.ToggleButtonState_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.state.ToggleButtonState} type in JSON format. */
	String TOGGLE_BUTTON_STATE__TYPE = "ToggleButtonState";

	/** @see #getLabel() */
	String LABEL__PROP = "label";

	/** @see #isActive() */
	String ACTIVE__PROP = "active";

	/**
	 * The label of the button.
	 */
	String getLabel();

	/**
	 * @see #getLabel()
	 */
	com.top_logic.layout.react.state.ToggleButtonState setLabel(String value);

	/**
	 * Whether the button is pressed.
	 */
	boolean isActive();

	/**
	 * @see #isActive()
	 */
	com.top_logic.layout.react.state.ToggleButtonState setActive(boolean value);

	@Override
	com.top_logic.layout.react.state.ToggleButtonState setHidden(boolean value);

	@Override
	com.top_logic.layout.react.state.ToggleButtonState setCssClass(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.state.ToggleButtonState readToggleButtonState(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.state.impl.ToggleButtonState_Impl result = new com.top_logic.layout.react.state.impl.ToggleButtonState_Impl();
		result.readContent(in);
		return result;
	}

}
