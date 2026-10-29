package com.top_logic.layout.react.state;

/**
 * State of a non-modal surface of arbitrary content anchored to the trigger that opens it, the
 * component {@code TLPopover}.
 *
 * The component renders a trigger button in place and, while the popover is {@link #isOpen()}, the
 * {@link #getContent()} on a surface portaled to the document body and placed at the trigger as
 * {@link #getPlacement()} says. The trigger button shows the {@link #getTrigger()} content if there is one,
 * else the {@link #getIcon()} alone (with the {@link #getLabel()} as its accessible name) or the
 * {@link #getLabel()}. The surface is a dialog labelled by the trigger.
 *
 * The client asks for the popover to open with the command {@code open} and for it to close with
 * the command {@code close}, both without arguments. It sends {@code close} on Escape, on a press
 * outside of trigger and surface, and on a click on the trigger of an open popover in mode
 * {@code click}. In mode {@code hover}, it sends {@code open} after the pointer has rested on the
 * trigger for a moment or when the trigger receives the keyboard focus, and {@code close} after the
 * pointer has left trigger and surface for a moment or when the focus leaves both. The server may
 * open and close the popover on its own as well.
 */
public interface PopoverState extends com.top_logic.layout.react.state.ControlState {

	/**
	 * The side of the trigger the surface stands on, and its alignment along that side: {@code start}
	 * and {@code end} align the surface with the start or end edge of the trigger, no alignment
	 * centers it. A surface that does not fit on its side flips to the opposite one.
	 */
	public enum Placement implements de.haumacher.msgbuf.data.ProtocolEnum {

		/**
		 * Above the trigger, centered.
		 */
		TOP("top"),

		/**
		 * Above the trigger, aligned with its start edge.
		 */
		TOP_START("top-start"),

		/**
		 * Above the trigger, aligned with its end edge.
		 */
		TOP_END("top-end"),

		/**
		 * Below the trigger, centered.
		 */
		BOTTOM("bottom"),

		/**
		 * Below the trigger, aligned with its start edge.
		 */
		BOTTOM_START("bottom-start"),

		/**
		 * Below the trigger, aligned with its end edge.
		 */
		BOTTOM_END("bottom-end"),

		/**
		 * Left of the trigger, centered.
		 */
		LEFT("left"),

		/**
		 * Left of the trigger, aligned with its top edge.
		 */
		LEFT_START("left-start"),

		/**
		 * Left of the trigger, aligned with its bottom edge.
		 */
		LEFT_END("left-end"),

		/**
		 * Right of the trigger, centered.
		 */
		RIGHT("right"),

		/**
		 * Right of the trigger, aligned with its top edge.
		 */
		RIGHT_START("right-start"),

		/**
		 * Right of the trigger, aligned with its bottom edge.
		 */
		RIGHT_END("right-end"),

		;

		private final String _protocolName;

		private Placement(String protocolName) {
			_protocolName = protocolName;
		}

		/**
		 * The protocol name of a {@link Placement} constant.
		 *
		 * @see #valueOfProtocol(String)
		 */
		@Override
		public String protocolName() {
			return _protocolName;
		}

		/** Looks up a {@link Placement} constant by it's protocol name. */
		public static Placement valueOfProtocol(String protocolName) {
			if (protocolName == null) { return null; }
			switch (protocolName) {
				case "top": return TOP;
				case "top-start": return TOP_START;
				case "top-end": return TOP_END;
				case "bottom": return BOTTOM;
				case "bottom-start": return BOTTOM_START;
				case "bottom-end": return BOTTOM_END;
				case "left": return LEFT;
				case "left-start": return LEFT_START;
				case "left-end": return LEFT_END;
				case "right": return RIGHT;
				case "right-start": return RIGHT_START;
				case "right-end": return RIGHT_END;
			}
			return TOP;
		}

		/** Writes this instance to the given output. */
		public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			out.value(protocolName());
		}

		/** Reads a new instance from the given reader. */
		public static Placement readPlacement(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
			return valueOfProtocol(in.nextString());
		}
	}

	/**
	 * The gesture opening the popover.
	 */
	public enum Mode implements de.haumacher.msgbuf.data.ProtocolEnum {

		/**
		 * A click on the trigger opens the popover and a further click closes it. Opening moves the
		 * keyboard focus into the surface, closing returns it to the trigger.
		 */
		CLICK("click"),

		/**
		 * Resting the pointer on the trigger opens the popover, and it stays open while the pointer
		 * moves on into the surface. Opening by the pointer leaves the keyboard focus where it is;
		 * opening by the keyboard focus on the trigger moves it into the surface. A click on the
		 * trigger opens the popover, too.
		 */
		HOVER("hover"),

		;

		private final String _protocolName;

		private Mode(String protocolName) {
			_protocolName = protocolName;
		}

		/**
		 * The protocol name of a {@link Mode} constant.
		 *
		 * @see #valueOfProtocol(String)
		 */
		@Override
		public String protocolName() {
			return _protocolName;
		}

		/** Looks up a {@link Mode} constant by it's protocol name. */
		public static Mode valueOfProtocol(String protocolName) {
			if (protocolName == null) { return null; }
			switch (protocolName) {
				case "click": return CLICK;
				case "hover": return HOVER;
			}
			return CLICK;
		}

		/** Writes this instance to the given output. */
		public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			out.value(protocolName());
		}

		/** Reads a new instance from the given reader. */
		public static Mode readMode(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
			return valueOfProtocol(in.nextString());
		}
	}

	/**
	 * Creates a {@link com.top_logic.layout.react.state.PopoverState} instance.
	 */
	static com.top_logic.layout.react.state.PopoverState create() {
		return new com.top_logic.layout.react.state.impl.PopoverState_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.state.PopoverState} type in JSON format. */
	String POPOVER_STATE__TYPE = "PopoverState";

	/** @see #isOpen() */
	String OPEN__PROP = "open";

	/** @see #getPlacement() */
	String PLACEMENT__PROP = "placement";

	/** @see #getMode() */
	String MODE__PROP = "mode";

	/** @see #getLabel() */
	String LABEL__PROP = "label";

	/** @see #getIcon() */
	String ICON__PROP = "icon";

	/** @see #getTrigger() */
	String TRIGGER__PROP = "trigger";

	/** @see #getContent() */
	String CONTENT__PROP = "content";

	/**
	 * Whether the surface is shown.
	 */
	boolean isOpen();

	/**
	 * @see #isOpen()
	 */
	com.top_logic.layout.react.state.PopoverState setOpen(boolean value);

	/**
	 * Where the surface stands at the trigger. Absent: {@code bottom-start}.
	 */
	com.top_logic.layout.react.state.PopoverState.Placement getPlacement();

	/**
	 * @see #getPlacement()
	 */
	com.top_logic.layout.react.state.PopoverState setPlacement(com.top_logic.layout.react.state.PopoverState.Placement value);

	/**
	 * The gesture opening the popover. Absent: {@code click}.
	 */
	com.top_logic.layout.react.state.PopoverState.Mode getMode();

	/**
	 * @see #getMode()
	 */
	com.top_logic.layout.react.state.PopoverState setMode(com.top_logic.layout.react.state.PopoverState.Mode value);

	/**
	 * The label of the trigger button, its accessible name when it shows an {@link #getIcon()} alone or
	 * {@link #getTrigger()} content, and with it the name of the surface. Absent: none.
	 */
	String getLabel();

	/**
	 * @see #getLabel()
	 */
	com.top_logic.layout.react.state.PopoverState setLabel(String value);

	/**
	 * The icon of the trigger button, the encoded form of a theme image. Absent: none.
	 */
	String getIcon();

	/**
	 * @see #getIcon()
	 */
	com.top_logic.layout.react.state.PopoverState setIcon(String value);

	/**
	 * Content shown inside the trigger button instead of {@link #getLabel()} and {@link #getIcon()}: text, an
	 * icon, a badge. The trigger button is the interactive element, so the content holds no
	 * interactive controls of its own. Absent: the trigger button shows {@link #getLabel()} and
	 * {@link #getIcon()}.
	 */
	com.top_logic.layout.react.state.ChildControl getTrigger();

	/**
	 * @see #getTrigger()
	 */
	com.top_logic.layout.react.state.PopoverState setTrigger(com.top_logic.layout.react.state.ChildControl value);

	/**
	 * Checks, whether {@link #getTrigger()} has a value.
	 */
	boolean hasTrigger();

	/**
	 * The content of the surface. Present while the popover is {@link #isOpen()}: the content is created
	 * when the popover opens for the first time and kept on the server while it is closed, so that
	 * it shows what the user left there when it opens again.
	 */
	com.top_logic.layout.react.state.ChildControl getContent();

	/**
	 * @see #getContent()
	 */
	com.top_logic.layout.react.state.PopoverState setContent(com.top_logic.layout.react.state.ChildControl value);

	/**
	 * Checks, whether {@link #getContent()} has a value.
	 */
	boolean hasContent();

	@Override
	com.top_logic.layout.react.state.PopoverState setHidden(boolean value);

	@Override
	com.top_logic.layout.react.state.PopoverState setCssClass(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.state.PopoverState readPopoverState(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.state.impl.PopoverState_Impl result = new com.top_logic.layout.react.state.impl.PopoverState_Impl();
		result.readContent(in);
		return result;
	}

}
