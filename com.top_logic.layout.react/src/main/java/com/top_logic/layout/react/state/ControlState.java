package com.top_logic.layout.react.state;

/**
 * State properties every control carries.
 */
public interface ControlState extends de.haumacher.msgbuf.data.DataObject {

	/**
	 * Creates a {@link com.top_logic.layout.react.state.ControlState} instance.
	 */
	static com.top_logic.layout.react.state.ControlState create() {
		return new com.top_logic.layout.react.state.impl.ControlState_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.state.ControlState} type in JSON format. */
	String CONTROL_STATE__TYPE = "ControlState";

	/** @see #isHidden() */
	String HIDDEN__PROP = "hidden";

	/** @see #getCssClass() */
	String CSS_CLASS__PROP = "cssClass";

	/**
	 * Whether the control is hidden. A hidden control keeps its component mounted (with its local
	 * state), the component renders nothing or is styled away.
	 */
	boolean isHidden();

	/**
	 * @see #isHidden()
	 */
	com.top_logic.layout.react.state.ControlState setHidden(boolean value);

	/**
	 * An additional CSS class for the root element of the component, standing beside the classes
	 * the component brings itself.
	 */
	String getCssClass();

	/**
	 * @see #getCssClass()
	 */
	com.top_logic.layout.react.state.ControlState setCssClass(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.state.ControlState readControlState(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.state.impl.ControlState_Impl result = new com.top_logic.layout.react.state.impl.ControlState_Impl();
		result.readContent(in);
		return result;
	}

}
