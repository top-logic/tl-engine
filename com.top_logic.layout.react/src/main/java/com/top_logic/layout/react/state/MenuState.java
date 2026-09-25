package com.top_logic.layout.react.state;

/**
 * State of a popup menu, the component {@code TLMenu}.
 *
 * Choosing an item sends the command {@code selectItem} with the {@link Entry#getId()} as argument
 * {@code itemId}; closing the menu without choosing sends the command {@code close}.
 */
public interface MenuState extends com.top_logic.layout.react.state.ControlState {

	/**
	 * The kind of an entry.
	 */
	public enum EntryType implements de.haumacher.msgbuf.data.ProtocolEnum {

		/**
		 * An item that can be chosen.
		 */
		ITEM("item"),

		/**
		 * A line between groups of entries.
		 */
		SEPARATOR("separator"),

		/**
		 * A caption naming the entries that follow it; it can neither be chosen nor focused.
		 */
		HEADER("header"),

		;

		private final String _protocolName;

		private EntryType(String protocolName) {
			_protocolName = protocolName;
		}

		/**
		 * The protocol name of a {@link EntryType} constant.
		 *
		 * @see #valueOfProtocol(String)
		 */
		@Override
		public String protocolName() {
			return _protocolName;
		}

		/** Looks up a {@link EntryType} constant by it's protocol name. */
		public static EntryType valueOfProtocol(String protocolName) {
			if (protocolName == null) { return null; }
			switch (protocolName) {
				case "item": return ITEM;
				case "separator": return SEPARATOR;
				case "header": return HEADER;
			}
			return ITEM;
		}

		/** Writes this instance to the given output. */
		public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			out.value(protocolName());
		}

		/** Reads a new instance from the given reader. */
		public static EntryType readEntryType(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
			return valueOfProtocol(in.nextString());
		}

		/** Writes this instance to the given binary output. */
		public final void writeTo(de.haumacher.msgbuf.binary.DataWriter out) throws java.io.IOException {
			switch (this) {
				case ITEM: out.value(1); break;
				case SEPARATOR: out.value(2); break;
				case HEADER: out.value(3); break;
				default: out.value(0);
			}
		}

		/** Reads a new instance from the given binary reader. */
		public static EntryType readEntryType(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
			switch (in.nextInt()) {
				case 1: return ITEM;
				case 2: return SEPARATOR;
				case 3: return HEADER;
				default: return ITEM;
			}
		}
	}
	/**
	 * An entry of the menu.
	 */
	public interface Entry extends de.haumacher.msgbuf.data.DataObject {

		/**
		 * Creates a {@link com.top_logic.layout.react.state.MenuState.Entry} instance.
		 */
		static com.top_logic.layout.react.state.MenuState.Entry create() {
			return new com.top_logic.layout.react.state.impl.MenuState_Impl.Entry_Impl();
		}

		/** Identifier for the {@link com.top_logic.layout.react.state.MenuState.Entry} type in JSON format. */
		String ENTRY__TYPE = "Entry";

		/** @see #getType() */
		String TYPE__PROP = "type";

		/** @see #getId() */
		String ID__PROP = "id";

		/** @see #getLabel() */
		String LABEL__PROP = "label";

		/** @see #getIcon() */
		String ICON__PROP = "icon";

		/** @see #isDisabled() */
		String DISABLED__PROP = "disabled";

		/** @see #isActive() */
		String ACTIVE__PROP = "active";

		/** @see #getCssClasses() */
		String CSS_CLASSES__PROP = "cssClasses";

		/**
		 * The kind of the entry.
		 */
		com.top_logic.layout.react.state.MenuState.EntryType getType();

		/**
		 * @see #getType()
		 */
		com.top_logic.layout.react.state.MenuState.Entry setType(com.top_logic.layout.react.state.MenuState.EntryType value);

		/**
		 * The ID of an item.
		 */
		String getId();

		/**
		 * @see #getId()
		 */
		com.top_logic.layout.react.state.MenuState.Entry setId(String value);

		/**
		 * The label of an item or a header.
		 */
		String getLabel();

		/**
		 * @see #getLabel()
		 */
		com.top_logic.layout.react.state.MenuState.Entry setLabel(String value);

		/**
		 * The icon of an item, the encoded form of a theme image.
		 */
		String getIcon();

		/**
		 * @see #getIcon()
		 */
		com.top_logic.layout.react.state.MenuState.Entry setIcon(String value);

		/**
		 * Whether the item cannot be chosen.
		 */
		boolean isDisabled();

		/**
		 * @see #isDisabled()
		 */
		com.top_logic.layout.react.state.MenuState.Entry setDisabled(boolean value);

		/**
		 * Whether the effect of the item's command is currently in force, marking the item as the
		 * chosen one among its alternatives.
		 */
		boolean isActive();

		/**
		 * @see #isActive()
		 */
		com.top_logic.layout.react.state.MenuState.Entry setActive(boolean value);

		/**
		 * Additional CSS classes of the item, separated by spaces.
		 */
		String getCssClasses();

		/**
		 * @see #getCssClasses()
		 */
		com.top_logic.layout.react.state.MenuState.Entry setCssClasses(String value);

		/** Reads a new instance from the given reader. */
		static com.top_logic.layout.react.state.MenuState.Entry readEntry(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
			com.top_logic.layout.react.state.impl.MenuState_Impl.Entry_Impl result = new com.top_logic.layout.react.state.impl.MenuState_Impl.Entry_Impl();
			result.readContent(in);
			return result;
		}

	}

	/**
	 * Creates a {@link com.top_logic.layout.react.state.MenuState} instance.
	 */
	static com.top_logic.layout.react.state.MenuState create() {
		return new com.top_logic.layout.react.state.impl.MenuState_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.state.MenuState} type in JSON format. */
	String MENU_STATE__TYPE = "MenuState";

	/** @see #isOpen() */
	String OPEN__PROP = "open";

	/** @see #getAnchorId() */
	String ANCHOR_ID__PROP = "anchorId";

	/** @see #getAnchorX() */
	String ANCHOR_X__PROP = "anchorX";

	/** @see #getAnchorY() */
	String ANCHOR_Y__PROP = "anchorY";

	/** @see #getItems() */
	String ITEMS__PROP = "items";

	/**
	 * Whether the menu is shown.
	 */
	boolean isOpen();

	/**
	 * @see #isOpen()
	 */
	com.top_logic.layout.react.state.MenuState setOpen(boolean value);

	/**
	 * The ID of the element the menu is shown at. Absent while it is shown at
	 * {@link #getAnchorX()}/{@link #getAnchorY()}.
	 */
	String getAnchorId();

	/**
	 * @see #getAnchorId()
	 */
	com.top_logic.layout.react.state.MenuState setAnchorId(String value);

	/**
	 * The horizontal position in the viewport the menu is shown at, in pixels.
	 */
	int getAnchorX();

	/**
	 * @see #getAnchorX()
	 */
	com.top_logic.layout.react.state.MenuState setAnchorX(int value);

	/**
	 * The vertical position in the viewport the menu is shown at, in pixels.
	 */
	int getAnchorY();

	/**
	 * @see #getAnchorY()
	 */
	com.top_logic.layout.react.state.MenuState setAnchorY(int value);

	/**
	 * The entries of the menu.
	 */
	java.util.List<com.top_logic.layout.react.state.MenuState.Entry> getItems();

	/**
	 * @see #getItems()
	 */
	com.top_logic.layout.react.state.MenuState setItems(java.util.List<? extends com.top_logic.layout.react.state.MenuState.Entry> value);

	/**
	 * Adds a value to the {@link #getItems()} list.
	 */
	com.top_logic.layout.react.state.MenuState addItem(com.top_logic.layout.react.state.MenuState.Entry value);

	/**
	 * Removes a value from the {@link #getItems()} list.
	 */
	void removeItem(com.top_logic.layout.react.state.MenuState.Entry value);

	@Override
	com.top_logic.layout.react.state.MenuState setHidden(boolean value);

	@Override
	com.top_logic.layout.react.state.MenuState setCssClass(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.state.MenuState readMenuState(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.state.impl.MenuState_Impl result = new com.top_logic.layout.react.state.impl.MenuState_Impl();
		result.readContent(in);
		return result;
	}

}
