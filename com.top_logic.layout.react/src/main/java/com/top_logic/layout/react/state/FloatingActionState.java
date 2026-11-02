package com.top_logic.layout.react.state;

/**
 * State of a floating action button, the component {@code TLFloatingAction}: one or more actions
 * floating over a corner of the frame they stand in, staying in place while the content beneath
 * them scrolls.
 *
 * The frame is the body of the panel or of the window the control is placed in, which offers a
 * layer for it through {@code useFloatingAnchor} of 'tl-react-bridge'; outside of such a frame,
 * the corner of the browser window. A transient overlay the actions would cover - a snackbar
 * reported through {@code useTransientOverlay} of 'tl-react-bridge' - moves them aside, read with
 * {@code useTransientOverlayRects}.
 *
 * Each action is a button ({@link #getActions()}): its label, icon, tooltip, display mode, whether it is
 * disabled and whether it is hidden are the state of that button ({@link ButtonState}), which
 * follows the executability and visibility of its command. The component renders the actions with
 * TLChild. A component drawing them with components of its own reads their current states with
 * {@code useChildStates} of 'tl-react-bridge' and presses an action by sending the command
 * {@code click} to it with {@code useChildCommand}.
 *
 * The actions not hidden decide on the presentation:
 *
 * - none: nothing is rendered;
 * - one: a round button with the icon of the action, also with its label when the display mode of
 *   the action asks for the label (an extended floating action button);
 * - several: a speed dial, a round button ({@link #getTriggerImage()}, named by {@link #getTriggerLabel()})
 *   that shows the actions as small round buttons beside it, their labels as tooltips. The dial
 *   marks the trigger with {@code aria-expanded}; it closes on Escape, on a press outside of it and
 *   after an action was pressed.
 *
 * The component sends no command of its own.
 */
public interface FloatingActionState extends com.top_logic.layout.react.state.ControlState {

	/**
	 * The corner of the frame the actions float over. Start and end follow the direction of the
	 * text: the end is the right corner in a left-to-right text.
	 *
	 * The control does not send {@link #BOTTOM_END}: an absent position means the default.
	 */
	public enum Position implements de.haumacher.msgbuf.data.ProtocolEnum {

		/**
		 * The bottom corner at the end of the line, the default.
		 */
		BOTTOM_END("bottom-end"),

		/**
		 * The bottom corner at the start of the line.
		 */
		BOTTOM_START("bottom-start"),

		/**
		 * The top corner at the end of the line.
		 */
		TOP_END("top-end"),

		/**
		 * The top corner at the start of the line.
		 */
		TOP_START("top-start"),

		;

		private final String _protocolName;

		private Position(String protocolName) {
			_protocolName = protocolName;
		}

		/**
		 * The protocol name of a {@link Position} constant.
		 *
		 * @see #valueOfProtocol(String)
		 */
		@Override
		public String protocolName() {
			return _protocolName;
		}

		/** Looks up a {@link Position} constant by it's protocol name. */
		public static Position valueOfProtocol(String protocolName) {
			if (protocolName == null) { return null; }
			switch (protocolName) {
				case "bottom-end": return BOTTOM_END;
				case "bottom-start": return BOTTOM_START;
				case "top-end": return TOP_END;
				case "top-start": return TOP_START;
			}
			return BOTTOM_END;
		}

		/** Writes this instance to the given output. */
		public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			out.value(protocolName());
		}

		/** Reads a new instance from the given reader. */
		public static Position readPosition(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
			return valueOfProtocol(in.nextString());
		}
	}

	/**
	 * Creates a {@link com.top_logic.layout.react.state.FloatingActionState} instance.
	 */
	static com.top_logic.layout.react.state.FloatingActionState create() {
		return new com.top_logic.layout.react.state.impl.FloatingActionState_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.state.FloatingActionState} type in JSON format. */
	String FLOATING_ACTION_STATE__TYPE = "FloatingActionState";

	/** @see #getActions() */
	String ACTIONS__PROP = "actions";

	/** @see #getPosition() */
	String POSITION__PROP = "position";

	/** @see #getTriggerImage() */
	String TRIGGER_IMAGE__PROP = "triggerImage";

	/** @see #getTriggerLabel() */
	String TRIGGER_LABEL__PROP = "triggerLabel";

	/**
	 * The actions, button controls with the state of a {@link ButtonState}, in the order they are
	 * offered: in a speed dial, the first stands next to the trigger.
	 */
	java.util.List<com.top_logic.layout.react.state.ChildControl> getActions();

	/**
	 * @see #getActions()
	 */
	com.top_logic.layout.react.state.FloatingActionState setActions(java.util.List<? extends com.top_logic.layout.react.state.ChildControl> value);

	/**
	 * Adds a value to the {@link #getActions()} list.
	 */
	com.top_logic.layout.react.state.FloatingActionState addAction(com.top_logic.layout.react.state.ChildControl value);

	/**
	 * Removes a value from the {@link #getActions()} list.
	 */
	void removeAction(com.top_logic.layout.react.state.ChildControl value);

	/**
	 * The corner the actions float over. Absent: {@link Position#BOTTOM_END}.
	 */
	com.top_logic.layout.react.state.FloatingActionState.Position getPosition();

	/**
	 * @see #getPosition()
	 */
	com.top_logic.layout.react.state.FloatingActionState setPosition(com.top_logic.layout.react.state.FloatingActionState.Position value);

	/**
	 * The icon of the button opening a speed dial, the encoded form of a theme image.
	 */
	String getTriggerImage();

	/**
	 * @see #getTriggerImage()
	 */
	com.top_logic.layout.react.state.FloatingActionState setTriggerImage(String value);

	/**
	 * The label of the button opening a speed dial: its accessible name and its tooltip.
	 */
	String getTriggerLabel();

	/**
	 * @see #getTriggerLabel()
	 */
	com.top_logic.layout.react.state.FloatingActionState setTriggerLabel(String value);

	@Override
	com.top_logic.layout.react.state.FloatingActionState setHidden(boolean value);

	@Override
	com.top_logic.layout.react.state.FloatingActionState setCssClass(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.state.FloatingActionState readFloatingActionState(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.state.impl.FloatingActionState_Impl result = new com.top_logic.layout.react.state.impl.FloatingActionState_Impl();
		result.readContent(in);
		return result;
	}

}
