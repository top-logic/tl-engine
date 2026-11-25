package com.top_logic.layout.react.state;

/**
 * State of a side rail: a collapsible column beside another element, with sections, the component
 * {@code TLSideRail}.
 *
 * The rail shows a strip with the icon of every {@link Section} and, while it is {@link #isOpen()}, a
 * column with the sections stacked ({@link #getBody()}). A click on an icon of the strip sends the command
 * {@code showSection} with the argument {@code name} (the {@link Section#getName()}): it opens the rail
 * with the section expanded, or closes the rail when that section is already shown in it.
 *
 * Where the area the rail measures itself against (its nearest positioned ancestor) is narrower than
 * {@link #getDrawerBelow()} rem, the rail shows only its strip and renders the {@link #getDrawer()} in place of
 * the column; it reports this with the command {@code setDrawer} and the argument {@code drawer}
 * (boolean). While it is a drawer, {@link #isOpen()} says whether the drawer is open.
 *
 * Dragging the inner edge of a {@link #isResizable()} column sends the command {@code setWidth} with the
 * argument {@code width}, the new width in pixels.
 */
public interface SideRailState extends com.top_logic.layout.react.state.ControlState {

	/**
	 * The side of its neighbour the rail stands on.
	 */
	public enum Side implements de.haumacher.msgbuf.data.ProtocolEnum {

		/**
		 * The rail stands at the start (left in a left-to-right page); its column opens towards the end.
		 */
		LEFT("left"),

		/**
		 * The rail stands at the end; its column opens towards the start.
		 */
		RIGHT("right"),

		;

		private final String _protocolName;

		private Side(String protocolName) {
			_protocolName = protocolName;
		}

		/**
		 * The protocol name of a {@link Side} constant.
		 *
		 * @see #valueOfProtocol(String)
		 */
		@Override
		public String protocolName() {
			return _protocolName;
		}

		/** Looks up a {@link Side} constant by it's protocol name. */
		public static Side valueOfProtocol(String protocolName) {
			if (protocolName == null) { return null; }
			switch (protocolName) {
				case "left": return LEFT;
				case "right": return RIGHT;
			}
			return LEFT;
		}

		/** Writes this instance to the given output. */
		public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			out.value(protocolName());
		}

		/** Reads a new instance from the given reader. */
		public static Side readSide(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
			return valueOfProtocol(in.nextString());
		}
	}
	/**
	 * A section of the rail, as the strip and the column show it.
	 */
	public interface Section extends de.haumacher.msgbuf.data.DataObject {

		/**
		 * Creates a {@link com.top_logic.layout.react.state.SideRailState.Section} instance.
		 */
		static com.top_logic.layout.react.state.SideRailState.Section create() {
			return new com.top_logic.layout.react.state.impl.SideRailState_Impl.Section_Impl();
		}

		/** Identifier for the {@link com.top_logic.layout.react.state.SideRailState.Section} type in JSON format. */
		String SECTION__TYPE = "Section";

		/** @see #getName() */
		String NAME__PROP = "name";

		/** @see #getLabel() */
		String LABEL__PROP = "label";

		/** @see #getIcon() */
		String ICON__PROP = "icon";

		/** @see #isExpanded() */
		String EXPANDED__PROP = "expanded";

		/** @see #isHidden() */
		String HIDDEN__PROP = "hidden";

		/** @see #getBadge() */
		String BADGE__PROP = "badge";

		/** @see #getContent() */
		String CONTENT__PROP = "content";

		/**
		 * The name of the section, unique within the rail.
		 */
		String getName();

		/**
		 * @see #getName()
		 */
		com.top_logic.layout.react.state.SideRailState.Section setName(String value);

		/**
		 * The label of the section, in its header and as the name of its strip icon.
		 */
		String getLabel();

		/**
		 * @see #getLabel()
		 */
		com.top_logic.layout.react.state.SideRailState.Section setLabel(String value);

		/**
		 * The icon on the strip, the encoded form of a theme image. Absent: a generic glyph.
		 */
		String getIcon();

		/**
		 * @see #getIcon()
		 */
		com.top_logic.layout.react.state.SideRailState.Section setIcon(String value);

		/**
		 * Whether the body of the section is expanded in the column.
		 */
		boolean isExpanded();

		/**
		 * @see #isExpanded()
		 */
		com.top_logic.layout.react.state.SideRailState.Section setExpanded(boolean value);

		/**
		 * Whether the section is left out of the rail, strip icon and header included.
		 */
		boolean isHidden();

		/**
		 * @see #isHidden()
		 */
		com.top_logic.layout.react.state.SideRailState.Section setHidden(boolean value);

		/**
		 * The badge of the section, on its strip icon and in its header. Absent: none.
		 */
		com.top_logic.layout.react.state.Badge getBadge();

		/**
		 * @see #getBadge()
		 */
		com.top_logic.layout.react.state.SideRailState.Section setBadge(com.top_logic.layout.react.state.Badge value);

		/**
		 * Checks, whether {@link #getBadge()} has a value.
		 */
		boolean hasBadge();

		/**
		 * The content of the section. Present in {@link SideRailSectionsState#getSections()} only.
		 */
		com.top_logic.layout.react.state.ChildControl getContent();

		/**
		 * @see #getContent()
		 */
		com.top_logic.layout.react.state.SideRailState.Section setContent(com.top_logic.layout.react.state.ChildControl value);

		/**
		 * Checks, whether {@link #getContent()} has a value.
		 */
		boolean hasContent();

		/** Reads a new instance from the given reader. */
		static com.top_logic.layout.react.state.SideRailState.Section readSection(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
			com.top_logic.layout.react.state.impl.SideRailState_Impl.Section_Impl result = new com.top_logic.layout.react.state.impl.SideRailState_Impl.Section_Impl();
			result.readContent(in);
			return result;
		}

	}

	/**
	 * Creates a {@link com.top_logic.layout.react.state.SideRailState} instance.
	 */
	static com.top_logic.layout.react.state.SideRailState create() {
		return new com.top_logic.layout.react.state.impl.SideRailState_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.state.SideRailState} type in JSON format. */
	String SIDE_RAIL_STATE__TYPE = "SideRailState";

	/** @see #getSide() */
	String SIDE__PROP = "side";

	/** @see #isStrip() */
	String STRIP__PROP = "strip";

	/** @see #isOpen() */
	String OPEN__PROP = "open";

	/** @see #getDrawerBelow() */
	String DRAWER_BELOW__PROP = "drawerBelow";

	/** @see #getSections() */
	String SECTIONS__PROP = "sections";

	/** @see #getBody() */
	String BODY__PROP = "body";

	/** @see #getDrawer() */
	String DRAWER__PROP = "drawer";

	/** @see #isResizable() */
	String RESIZABLE__PROP = "resizable";

	/** @see #getWidth() */
	String WIDTH__PROP = "width";

	/** @see #getSectionWidth() */
	String SECTION_WIDTH__PROP = "sectionWidth";

	/**
	 * The side of its neighbour the rail stands on. Absent means {@link Side#LEFT}.
	 */
	com.top_logic.layout.react.state.SideRailState.Side getSide();

	/**
	 * @see #getSide()
	 */
	com.top_logic.layout.react.state.SideRailState setSide(com.top_logic.layout.react.state.SideRailState.Side value);

	/**
	 * Whether the strip of section icons is shown. A rail without strip shows nothing while it is
	 * closed.
	 */
	boolean isStrip();

	/**
	 * @see #isStrip()
	 */
	com.top_logic.layout.react.state.SideRailState setStrip(boolean value);

	/**
	 * Whether the rail is open: its column, or - while it is a drawer - its drawer.
	 */
	boolean isOpen();

	/**
	 * @see #isOpen()
	 */
	com.top_logic.layout.react.state.SideRailState setOpen(boolean value);

	/**
	 * The width of the area the rail stands in, in rem, below which it is a drawer. Zero: never.
	 */
	int getDrawerBelow();

	/**
	 * @see #getDrawerBelow()
	 */
	com.top_logic.layout.react.state.SideRailState setDrawerBelow(int value);

	/**
	 * The sections, top to bottom, without their content.
	 */
	java.util.List<com.top_logic.layout.react.state.SideRailState.Section> getSections();

	/**
	 * @see #getSections()
	 */
	com.top_logic.layout.react.state.SideRailState setSections(java.util.List<? extends com.top_logic.layout.react.state.SideRailState.Section> value);

	/**
	 * Adds a value to the {@link #getSections()} list.
	 */
	com.top_logic.layout.react.state.SideRailState addSection(com.top_logic.layout.react.state.SideRailState.Section value);

	/**
	 * Removes a value from the {@link #getSections()} list.
	 */
	void removeSection(com.top_logic.layout.react.state.SideRailState.Section value);

	/**
	 * The column with the sections and their content, rendered while the rail is open and not a
	 * drawer.
	 */
	com.top_logic.layout.react.state.ChildControl getBody();

	/**
	 * @see #getBody()
	 */
	com.top_logic.layout.react.state.SideRailState setBody(com.top_logic.layout.react.state.ChildControl value);

	/**
	 * Checks, whether {@link #getBody()} has a value.
	 */
	boolean hasBody();

	/**
	 * The drawer showing the {@link #getBody()}, rendered in place of the column while the rail is a
	 * drawer.
	 */
	com.top_logic.layout.react.state.ChildControl getDrawer();

	/**
	 * @see #getDrawer()
	 */
	com.top_logic.layout.react.state.SideRailState setDrawer(com.top_logic.layout.react.state.ChildControl value);

	/**
	 * Checks, whether {@link #getDrawer()} has a value.
	 */
	boolean hasDrawer();

	/**
	 * Whether the column has a width of its own, which the user changes by dragging its inner edge.
	 * Otherwise the column fills the area of the rail, e.g. the pane of a split panel, whose size the
	 * split panel decides.
	 */
	boolean isResizable();

	/**
	 * @see #isResizable()
	 */
	com.top_logic.layout.react.state.SideRailState setResizable(boolean value);

	/**
	 * The width of a {@link #isResizable()} column in pixels.
	 */
	int getWidth();

	/**
	 * @see #getWidth()
	 */
	com.top_logic.layout.react.state.SideRailState setWidth(int value);

	/**
	 * The width in pixels an expanded section asks for while it is shown, taking precedence over
	 * {@link #getWidth()}. Zero: none.
	 */
	int getSectionWidth();

	/**
	 * @see #getSectionWidth()
	 */
	com.top_logic.layout.react.state.SideRailState setSectionWidth(int value);

	@Override
	com.top_logic.layout.react.state.SideRailState setHidden(boolean value);

	@Override
	com.top_logic.layout.react.state.SideRailState setCssClass(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.state.SideRailState readSideRailState(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.state.impl.SideRailState_Impl result = new com.top_logic.layout.react.state.impl.SideRailState_Impl();
		result.readContent(in);
		return result;
	}

}
