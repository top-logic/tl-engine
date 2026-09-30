package com.top_logic.layout.react.state;

/**
 * A control embedded in the state of another control, rendered by the component registered
 * under its {@link #getModule()}.
 *
 * The embedding component renders it through {@code TLChild} of 'tl-react-bridge', which mounts
 * the component with the given {@link #getState()} and keeps it up to date.
 */
public interface ChildControl extends de.haumacher.msgbuf.data.DataObject {

	/**
	 * Creates a {@link com.top_logic.layout.react.state.ChildControl} instance.
	 */
	static com.top_logic.layout.react.state.ChildControl create() {
		return new com.top_logic.layout.react.state.impl.ChildControl_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.state.ChildControl} type in JSON format. */
	String CHILD_CONTROL__TYPE = "ChildControl";

	/** @see #getControlId() */
	String CONTROL_ID__PROP = "controlId";

	/** @see #getModule() */
	String MODULE__PROP = "module";

	/** @see #getState() */
	String STATE__PROP = "state";

	/** @see #getViewSource() */
	String VIEW_SOURCE__PROP = "viewSource";

	/**
	 * The ID of the control, the address of its commands and of the updates of its state.
	 */
	String getControlId();

	/**
	 * @see #getControlId()
	 */
	com.top_logic.layout.react.state.ChildControl setControlId(String value);

	/**
	 * The name of the component rendering the control (e.g. {@code TLButton}).
	 */
	String getModule();

	/**
	 * @see #getModule()
	 */
	com.top_logic.layout.react.state.ChildControl setModule(String value);

	/**
	 * The initial state of the control, an object as described by the state message of its
	 * component.
	 */
	Object getState();

	/**
	 * @see #getState()
	 */
	com.top_logic.layout.react.state.ChildControl setState(Object value);

	/**
	 * Checks, whether {@link #getState()} has a value.
	 */
	boolean hasState();

	/**
	 * The source of the view the control is the root of, for development tools. Absent for a
	 * control that is no view root.
	 */
	String getViewSource();

	/**
	 * @see #getViewSource()
	 */
	com.top_logic.layout.react.state.ChildControl setViewSource(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.state.ChildControl readChildControl(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.state.impl.ChildControl_Impl result = new com.top_logic.layout.react.state.impl.ChildControl_Impl();
		result.readContent(in);
		return result;
	}

}
