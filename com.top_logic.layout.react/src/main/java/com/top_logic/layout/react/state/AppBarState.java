package com.top_logic.layout.react.state;

/**
 * State of the bar at the top of an application, the component {@code TLAppBar}: a control
 * opening it, the title, inline content, the commands placed in it and a control closing it, in
 * this order.
 *
 * The component suggests the appearance {@code ghost} to the buttons of its {@link #getActions()} (see
	 * {@link ButtonState#getAppearance()}). It sends no commands.
 */
public interface AppBarState extends com.top_logic.layout.react.state.ControlState {

	/**
	 * How the bar sets itself off from the content below it.
	 */
	public enum Variant implements de.haumacher.msgbuf.data.ProtocolEnum {

		/**
		 * A flat bar without a shadow.
		 */
		FLAT("flat"),

		/**
		 * A bar raised above the content by a drop shadow.
		 */
		ELEVATED("elevated"),

		;

		private final String _protocolName;

		private Variant(String protocolName) {
			_protocolName = protocolName;
		}

		/**
		 * The protocol name of a {@link Variant} constant.
		 *
		 * @see #valueOfProtocol(String)
		 */
		@Override
		public String protocolName() {
			return _protocolName;
		}

		/** Looks up a {@link Variant} constant by it's protocol name. */
		public static Variant valueOfProtocol(String protocolName) {
			if (protocolName == null) { return null; }
			switch (protocolName) {
				case "flat": return FLAT;
				case "elevated": return ELEVATED;
			}
			return FLAT;
		}

		/** Writes this instance to the given output. */
		public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			out.value(protocolName());
		}

		/** Reads a new instance from the given reader. */
		public static Variant readVariant(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
			return valueOfProtocol(in.nextString());
		}
	}

	/**
	 * Creates a {@link com.top_logic.layout.react.state.AppBarState} instance.
	 */
	static com.top_logic.layout.react.state.AppBarState create() {
		return new com.top_logic.layout.react.state.impl.AppBarState_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.state.AppBarState} type in JSON format. */
	String APP_BAR_STATE__TYPE = "AppBarState";

	/** @see #getTitle() */
	String TITLE__PROP = "title";

	/** @see #getVariant() */
	String VARIANT__PROP = "variant";

	/** @see #getLeading() */
	String LEADING__PROP = "leading";

	/** @see #getChildren() */
	String CHILDREN__PROP = "children";

	/** @see #getActions() */
	String ACTIONS__PROP = "actions";

	/** @see #getTrailing() */
	String TRAILING__PROP = "trailing";

	/**
	 * The text naming the application, plain text. Absent: empty.
	 */
	String getTitle();

	/**
	 * @see #getTitle()
	 */
	com.top_logic.layout.react.state.AppBarState setTitle(String value);

	/**
	 * How the bar sets itself off. Absent means {@link Variant#FLAT}, a flat bar.
	 */
	com.top_logic.layout.react.state.AppBarState.Variant getVariant();

	/**
	 * @see #getVariant()
	 */
	com.top_logic.layout.react.state.AppBarState setVariant(com.top_logic.layout.react.state.AppBarState.Variant value);

	/**
	 * The control opening the bar, ahead of the title (e.g. a button opening the navigation).
	 * Absent: none.
	 */
	com.top_logic.layout.react.state.ChildControl getLeading();

	/**
	 * @see #getLeading()
	 */
	com.top_logic.layout.react.state.AppBarState setLeading(com.top_logic.layout.react.state.ChildControl value);

	/**
	 * Checks, whether {@link #getLeading()} has a value.
	 */
	boolean hasLeading();

	/**
	 * Inline content between the title and the {@link #getActions()}, in display order. Absent: none.
	 */
	java.util.List<com.top_logic.layout.react.state.ChildControl> getChildren();

	/**
	 * @see #getChildren()
	 */
	com.top_logic.layout.react.state.AppBarState setChildren(java.util.List<? extends com.top_logic.layout.react.state.ChildControl> value);

	/**
	 * Adds a value to the {@link #getChildren()} list.
	 */
	com.top_logic.layout.react.state.AppBarState addChildren(com.top_logic.layout.react.state.ChildControl value);

	/**
	 * Removes a value from the {@link #getChildren()} list.
	 */
	void removeChildren(com.top_logic.layout.react.state.ChildControl value);

	/**
	 * The toolbar of the commands placed in the bar. It renders nothing while it holds no command,
	 * and folds the commands that do not fit into its overflow menu. Absent: none.
	 */
	com.top_logic.layout.react.state.ChildControl getActions();

	/**
	 * @see #getActions()
	 */
	com.top_logic.layout.react.state.AppBarState setActions(com.top_logic.layout.react.state.ChildControl value);

	/**
	 * Checks, whether {@link #getActions()} has a value.
	 */
	boolean hasActions();

	/**
	 * The control closing the bar, after the {@link #getActions()}. Absent: none.
	 */
	com.top_logic.layout.react.state.ChildControl getTrailing();

	/**
	 * @see #getTrailing()
	 */
	com.top_logic.layout.react.state.AppBarState setTrailing(com.top_logic.layout.react.state.ChildControl value);

	/**
	 * Checks, whether {@link #getTrailing()} has a value.
	 */
	boolean hasTrailing();

	@Override
	com.top_logic.layout.react.state.AppBarState setHidden(boolean value);

	@Override
	com.top_logic.layout.react.state.AppBarState setCssClass(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.state.AppBarState readAppBarState(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.state.impl.AppBarState_Impl result = new com.top_logic.layout.react.state.impl.AppBarState_Impl();
		result.readContent(in);
		return result;
	}

}
