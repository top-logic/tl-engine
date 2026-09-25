package com.top_logic.layout.react.state;

/**
 * State of a button, the component {@code TLButton}.
 *
 * A click sends the command {@code click} to the server, unless {@link #getNavigateUrl()} is set.
 */
public interface ButtonState extends com.top_logic.layout.react.state.ControlState {

	/**
	 * What a button shows of its icon and its label.
	 */
	public enum DisplayMode implements de.haumacher.msgbuf.data.ProtocolEnum {

		/**
		 * Only the icon is shown; the label serves as tooltip and accessible name.
		 */
		ICON_ONLY("icon-only"),

		/**
		 * Icon and label side by side.
		 */
		ICON_LABEL("icon-label"),

		/**
		 * Only the label is shown.
		 */
		LABEL_ONLY("label-only"),

		;

		private final String _protocolName;

		private DisplayMode(String protocolName) {
			_protocolName = protocolName;
		}

		/**
		 * The protocol name of a {@link DisplayMode} constant.
		 *
		 * @see #valueOfProtocol(String)
		 */
		@Override
		public String protocolName() {
			return _protocolName;
		}

		/** Looks up a {@link DisplayMode} constant by it's protocol name. */
		public static DisplayMode valueOfProtocol(String protocolName) {
			if (protocolName == null) { return null; }
			switch (protocolName) {
				case "icon-only": return ICON_ONLY;
				case "icon-label": return ICON_LABEL;
				case "label-only": return LABEL_ONLY;
			}
			return ICON_ONLY;
		}

		/** Writes this instance to the given output. */
		public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			out.value(protocolName());
		}

		/** Reads a new instance from the given reader. */
		public static DisplayMode readDisplayMode(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
			return valueOfProtocol(in.nextString());
		}

		/** Writes this instance to the given binary output. */
		public final void writeTo(de.haumacher.msgbuf.binary.DataWriter out) throws java.io.IOException {
			switch (this) {
				case ICON_ONLY: out.value(1); break;
				case ICON_LABEL: out.value(2); break;
				case LABEL_ONLY: out.value(3); break;
				default: out.value(0);
			}
		}

		/** Reads a new instance from the given binary reader. */
		public static DisplayMode readDisplayMode(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
			switch (in.nextInt()) {
				case 1: return ICON_ONLY;
				case 2: return ICON_LABEL;
				case 3: return LABEL_ONLY;
				default: return ICON_ONLY;
			}
		}
	}

	/**
	 * How a button is drawn. Absent for the standard appearance.
	 */
	public enum Appearance implements de.haumacher.msgbuf.data.ProtocolEnum {

		/**
		 * Emphasized, for the primary action of a dialog.
		 */
		PRIMARY("primary"),

		/**
		 * No frame and no fill until pointed at: for toolbars and icon buttons.
		 */
		GHOST("ghost"),

		/**
		 * An inline text link rather than a button.
		 */
		LINK("link"),

		;

		private final String _protocolName;

		private Appearance(String protocolName) {
			_protocolName = protocolName;
		}

		/**
		 * The protocol name of a {@link Appearance} constant.
		 *
		 * @see #valueOfProtocol(String)
		 */
		@Override
		public String protocolName() {
			return _protocolName;
		}

		/** Looks up a {@link Appearance} constant by it's protocol name. */
		public static Appearance valueOfProtocol(String protocolName) {
			if (protocolName == null) { return null; }
			switch (protocolName) {
				case "primary": return PRIMARY;
				case "ghost": return GHOST;
				case "link": return LINK;
			}
			return PRIMARY;
		}

		/** Writes this instance to the given output. */
		public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			out.value(protocolName());
		}

		/** Reads a new instance from the given reader. */
		public static Appearance readAppearance(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
			return valueOfProtocol(in.nextString());
		}

		/** Writes this instance to the given binary output. */
		public final void writeTo(de.haumacher.msgbuf.binary.DataWriter out) throws java.io.IOException {
			switch (this) {
				case PRIMARY: out.value(1); break;
				case GHOST: out.value(2); break;
				case LINK: out.value(3); break;
				default: out.value(0);
			}
		}

		/** Reads a new instance from the given binary reader. */
		public static Appearance readAppearance(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
			switch (in.nextInt()) {
				case 1: return PRIMARY;
				case 2: return GHOST;
				case 3: return LINK;
				default: return PRIMARY;
			}
		}
	}

	/**
	 * The kind of action a button stands for. Absent for an ordinary action.
	 */
	public enum Tone implements de.haumacher.msgbuf.data.ProtocolEnum {

		/**
		 * A destructive action: delete, discard, revoke.
		 */
		DANGER("danger"),

		;

		private final String _protocolName;

		private Tone(String protocolName) {
			_protocolName = protocolName;
		}

		/**
		 * The protocol name of a {@link Tone} constant.
		 *
		 * @see #valueOfProtocol(String)
		 */
		@Override
		public String protocolName() {
			return _protocolName;
		}

		/** Looks up a {@link Tone} constant by it's protocol name. */
		public static Tone valueOfProtocol(String protocolName) {
			if (protocolName == null) { return null; }
			switch (protocolName) {
				case "danger": return DANGER;
			}
			return DANGER;
		}

		/** Writes this instance to the given output. */
		public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			out.value(protocolName());
		}

		/** Reads a new instance from the given reader. */
		public static Tone readTone(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
			return valueOfProtocol(in.nextString());
		}

		/** Writes this instance to the given binary output. */
		public final void writeTo(de.haumacher.msgbuf.binary.DataWriter out) throws java.io.IOException {
			switch (this) {
				case DANGER: out.value(1); break;
				default: out.value(0);
			}
		}

		/** Reads a new instance from the given binary reader. */
		public static Tone readTone(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
			switch (in.nextInt()) {
				case 1: return DANGER;
				default: return DANGER;
			}
		}
	}

	/**
	 * The size of a button. Absent for the standard size.
	 */
	public enum Size implements de.haumacher.msgbuf.data.ProtocolEnum {

		/**
		 * A compact button, e.g. for a secondary inline action.
		 */
		SMALL("small"),

		;

		private final String _protocolName;

		private Size(String protocolName) {
			_protocolName = protocolName;
		}

		/**
		 * The protocol name of a {@link Size} constant.
		 *
		 * @see #valueOfProtocol(String)
		 */
		@Override
		public String protocolName() {
			return _protocolName;
		}

		/** Looks up a {@link Size} constant by it's protocol name. */
		public static Size valueOfProtocol(String protocolName) {
			if (protocolName == null) { return null; }
			switch (protocolName) {
				case "small": return SMALL;
			}
			return SMALL;
		}

		/** Writes this instance to the given output. */
		public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			out.value(protocolName());
		}

		/** Reads a new instance from the given reader. */
		public static Size readSize(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
			return valueOfProtocol(in.nextString());
		}

		/** Writes this instance to the given binary output. */
		public final void writeTo(de.haumacher.msgbuf.binary.DataWriter out) throws java.io.IOException {
			switch (this) {
				case SMALL: out.value(1); break;
				default: out.value(0);
			}
		}

		/** Reads a new instance from the given binary reader. */
		public static Size readSize(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
			switch (in.nextInt()) {
				case 1: return SMALL;
				default: return SMALL;
			}
		}
	}

	/**
	 * Creates a {@link com.top_logic.layout.react.state.ButtonState} instance.
	 */
	static com.top_logic.layout.react.state.ButtonState create() {
		return new com.top_logic.layout.react.state.impl.ButtonState_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.state.ButtonState} type in JSON format. */
	String BUTTON_STATE__TYPE = "ButtonState";

	/** @see #getLabel() */
	String LABEL__PROP = "label";

	/** @see #isDisabled() */
	String DISABLED__PROP = "disabled";

	/** @see #isActive() */
	String ACTIVE__PROP = "active";

	/** @see #getImage() */
	String IMAGE__PROP = "image";

	/** @see #getTooltip() */
	String TOOLTIP__PROP = "tooltip";

	/** @see #getDisplayMode() */
	String DISPLAY_MODE__PROP = "displayMode";

	/** @see #getCssClasses() */
	String CSS_CLASSES__PROP = "cssClasses";

	/** @see #getAppearance() */
	String APPEARANCE__PROP = "appearance";

	/** @see #getTone() */
	String TONE__PROP = "tone";

	/** @see #getSize() */
	String SIZE__PROP = "size";

	/** @see #getNavigateUrl() */
	String NAVIGATE_URL__PROP = "navigateUrl";

	/** @see #isNavigateNewWindow() */
	String NAVIGATE_NEW_WINDOW__PROP = "navigateNewWindow";

	/** @see #getKeyGesture() */
	String KEY_GESTURE__PROP = "keyGesture";

	/**
	 * The label of the button.
	 */
	String getLabel();

	/**
	 * @see #getLabel()
	 */
	com.top_logic.layout.react.state.ButtonState setLabel(String value);

	/**
	 * Whether the button cannot be pressed.
	 */
	boolean isDisabled();

	/**
	 * @see #isDisabled()
	 */
	com.top_logic.layout.react.state.ButtonState setDisabled(boolean value);

	/**
	 * Whether the effect of the button's command is currently in force: the alternative chosen, or
	 * a pressed toggle. Absent when not.
	 */
	boolean isActive();

	/**
	 * @see #isActive()
	 */
	com.top_logic.layout.react.state.ButtonState setActive(boolean value);

	/**
	 * The icon of the button, the encoded form of a theme image (e.g. {@code css:fas fa-edit} for
	 * an icon font class, or the path of an image file).
	 */
	String getImage();

	/**
	 * @see #getImage()
	 */
	com.top_logic.layout.react.state.ButtonState setImage(String value);

	/**
	 * An explicit tooltip (plain text), shown whatever the button displays.
	 */
	String getTooltip();

	/**
	 * @see #getTooltip()
	 */
	com.top_logic.layout.react.state.ButtonState setTooltip(String value);

	/**
	 * What the button shows of its {@link #getImage()} and its {@link #getLabel()}. Absent: the label only.
	 */
	com.top_logic.layout.react.state.ButtonState.DisplayMode getDisplayMode();

	/**
	 * @see #getDisplayMode()
	 */
	com.top_logic.layout.react.state.ButtonState setDisplayMode(com.top_logic.layout.react.state.ButtonState.DisplayMode value);

	/**
	 * Additional CSS classes of the button, separated by spaces.
	 */
	String getCssClasses();

	/**
	 * @see #getCssClasses()
	 */
	com.top_logic.layout.react.state.ButtonState setCssClasses(String value);

	/**
	 * How the button is drawn. Absent: the standard appearance, or the appearance its container
	 * suggests.
	 */
	com.top_logic.layout.react.state.ButtonState.Appearance getAppearance();

	/**
	 * @see #getAppearance()
	 */
	com.top_logic.layout.react.state.ButtonState setAppearance(com.top_logic.layout.react.state.ButtonState.Appearance value);

	/**
	 * The kind of action the button stands for. Absent: an ordinary action.
	 */
	com.top_logic.layout.react.state.ButtonState.Tone getTone();

	/**
	 * @see #getTone()
	 */
	com.top_logic.layout.react.state.ButtonState setTone(com.top_logic.layout.react.state.ButtonState.Tone value);

	/**
	 * The size of the button. Absent: the standard size.
	 */
	com.top_logic.layout.react.state.ButtonState.Size getSize();

	/**
	 * @see #getSize()
	 */
	com.top_logic.layout.react.state.ButtonState setSize(com.top_logic.layout.react.state.ButtonState.Size value);

	/**
	 * A URL the browser navigates to on a click, instead of sending a command to the server.
	 */
	String getNavigateUrl();

	/**
	 * @see #getNavigateUrl()
	 */
	com.top_logic.layout.react.state.ButtonState setNavigateUrl(String value);

	/**
	 * Whether {@link #getNavigateUrl()} is opened in a browser window of its own, instead of replacing
	 * the page the button is on.
	 */
	boolean isNavigateNewWindow();

	/**
	 * @see #isNavigateNewWindow()
	 */
	com.top_logic.layout.react.state.ButtonState setNavigateNewWindow(boolean value);

	/**
	 * The keyboard gesture that triggers the button (e.g. {@code ENTER}, {@code Ctrl+S}).
	 */
	String getKeyGesture();

	/**
	 * @see #getKeyGesture()
	 */
	com.top_logic.layout.react.state.ButtonState setKeyGesture(String value);

	@Override
	com.top_logic.layout.react.state.ButtonState setHidden(boolean value);

	@Override
	com.top_logic.layout.react.state.ButtonState setCssClass(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.state.ButtonState readButtonState(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.state.impl.ButtonState_Impl result = new com.top_logic.layout.react.state.impl.ButtonState_Impl();
		result.readContent(in);
		return result;
	}

}
