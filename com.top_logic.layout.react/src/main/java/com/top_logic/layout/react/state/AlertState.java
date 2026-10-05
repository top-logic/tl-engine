package com.top_logic.layout.react.state;

/**
 * State of a highlighted message standing in the content of a page, the component {@code TLAlert}.
 *
 * A hidden alert (see {@link ControlState#isHidden()}) renders nothing. Dismissing the alert sends the
 * command {@code dismiss} with the {@link #getGeneration()} of the content dismissed as argument
 * {@code generation}.
 */
public interface AlertState extends com.top_logic.layout.react.state.ControlState {

	/**
	 * Creates a {@link com.top_logic.layout.react.state.AlertState} instance.
	 */
	static com.top_logic.layout.react.state.AlertState create() {
		return new com.top_logic.layout.react.state.impl.AlertState_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.state.AlertState} type in JSON format. */
	String ALERT_STATE__TYPE = "AlertState";

	/** @see #getVariant() */
	String VARIANT__PROP = "variant";

	/** @see #getTitle() */
	String TITLE__PROP = "title";

	/** @see #getMessageText() */
	String MESSAGE_TEXT__PROP = "message";

	/** @see #getIcon() */
	String ICON__PROP = "icon";

	/** @see #isClosable() */
	String CLOSABLE__PROP = "closable";

	/** @see #getActions() */
	String ACTIONS__PROP = "actions";

	/** @see #getGeneration() */
	String GENERATION__PROP = "generation";

	/**
	 * The kind of the message, selecting its color and its icon. Absent: an information.
	 */
	com.top_logic.layout.react.state.SnackbarState.Variant getVariant();

	/**
	 * @see #getVariant()
	 */
	com.top_logic.layout.react.state.AlertState setVariant(com.top_logic.layout.react.state.SnackbarState.Variant value);

	/**
	 * The heading of the message, plain text. Absent: no heading.
	 */
	String getTitle();

	/**
	 * @see #getTitle()
	 */
	com.top_logic.layout.react.state.AlertState setTitle(String value);

	/**
	 * The message, plain text.
	 */
	String getMessageText();

	/**
	 * @see #getMessageText()
	 */
	com.top_logic.layout.react.state.AlertState setMessageText(String value);

	/**
	 * The icon of the message, the encoded form of a theme image matching the {@link #getVariant()}.
	 */
	String getIcon();

	/**
	 * @see #getIcon()
	 */
	com.top_logic.layout.react.state.AlertState setIcon(String value);

	/**
	 * Whether the user can dismiss the message.
	 */
	boolean isClosable();

	/**
	 * @see #isClosable()
	 */
	com.top_logic.layout.react.state.AlertState setClosable(boolean value);

	/**
	 * The buttons offering what to do about the message, in display order.
	 */
	java.util.List<com.top_logic.layout.react.state.ChildControl> getActions();

	/**
	 * @see #getActions()
	 */
	com.top_logic.layout.react.state.AlertState setActions(java.util.List<? extends com.top_logic.layout.react.state.ChildControl> value);

	/**
	 * Adds a value to the {@link #getActions()} list.
	 */
	com.top_logic.layout.react.state.AlertState addAction(com.top_logic.layout.react.state.ChildControl value);

	/**
	 * Removes a value from the {@link #getActions()} list.
	 */
	void removeAction(com.top_logic.layout.react.state.ChildControl value);

	/**
	 * The number of the content shown, counting the contents this control has shown. A dismiss
	 * reporting another number refers to a content that is no longer shown.
	 */
	int getGeneration();

	/**
	 * @see #getGeneration()
	 */
	com.top_logic.layout.react.state.AlertState setGeneration(int value);

	@Override
	com.top_logic.layout.react.state.AlertState setHidden(boolean value);

	@Override
	com.top_logic.layout.react.state.AlertState setCssClass(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.state.AlertState readAlertState(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.state.impl.AlertState_Impl result = new com.top_logic.layout.react.state.impl.AlertState_Impl();
		result.readContent(in);
		return result;
	}

}
