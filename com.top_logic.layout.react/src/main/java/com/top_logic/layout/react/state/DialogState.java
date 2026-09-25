package com.top_logic.layout.react.state;

/**
 * State of a modal surface over the page, the component {@code TLDialog}.
 *
 * Dismissing the dialog sends the command {@code close}.
 */
public interface DialogState extends com.top_logic.layout.react.state.ControlState {

	/**
	 * Creates a {@link com.top_logic.layout.react.state.DialogState} instance.
	 */
	static com.top_logic.layout.react.state.DialogState create() {
		return new com.top_logic.layout.react.state.impl.DialogState_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.state.DialogState} type in JSON format. */
	String DIALOG_STATE__TYPE = "DialogState";

	/** @see #isOpen() */
	String OPEN__PROP = "open";

	/** @see #isCloseOnBackdrop() */
	String CLOSE_ON_BACKDROP__PROP = "closeOnBackdrop";

	/** @see #isClosable() */
	String CLOSABLE__PROP = "closable";

	/** @see #getChild() */
	String CHILD__PROP = "child";

	/**
	 * Whether the dialog is shown.
	 */
	boolean isOpen();

	/**
	 * @see #isOpen()
	 */
	com.top_logic.layout.react.state.DialogState setOpen(boolean value);

	/**
	 * Whether a click beside the dialog dismisses it. Absent: dismissed.
	 */
	boolean isCloseOnBackdrop();

	/**
	 * @see #isCloseOnBackdrop()
	 */
	com.top_logic.layout.react.state.DialogState setCloseOnBackdrop(boolean value);

	/**
	 * Whether the user can dismiss the dialog. Absent: dismissable.
	 */
	boolean isClosable();

	/**
	 * @see #isClosable()
	 */
	com.top_logic.layout.react.state.DialogState setClosable(boolean value);

	/**
	 * The content of the dialog.
	 */
	com.top_logic.layout.react.state.ChildControl getChild();

	/**
	 * @see #getChild()
	 */
	com.top_logic.layout.react.state.DialogState setChild(com.top_logic.layout.react.state.ChildControl value);

	/**
	 * Checks, whether {@link #getChild()} has a value.
	 */
	boolean hasChild();

	@Override
	com.top_logic.layout.react.state.DialogState setHidden(boolean value);

	@Override
	com.top_logic.layout.react.state.DialogState setCssClass(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.state.DialogState readDialogState(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.state.impl.DialogState_Impl result = new com.top_logic.layout.react.state.impl.DialogState_Impl();
		result.readContent(in);
		return result;
	}

}
