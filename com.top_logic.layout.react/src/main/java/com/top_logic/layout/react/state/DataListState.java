package com.top_logic.layout.react.state;

/**
 * State of a selectable, groupable, searchable list of objects in the style of a contact or
 * message list, the component {@code TLDataList} (design system: tl-list).
 *
 * A click on a row sends the command {@code select} with the {@link Row#getId()} of the row as argument
 * {@code id}; an empty {@code id} clears the selection. The trigger of the row commands sends the
 * command {@code rowMenu} with the {@link Row#getId()} as argument {@code id} and the client
 * coordinates of the click as arguments {@code x} and {@code y}; the server opens the menu.
 * Searching, collapsing and the drawer stay on the client.
 */
public interface DataListState extends com.top_logic.layout.react.state.ControlState {

	/**
	 * How prominently a {@link Line} is shown.
	 */
	public enum Emphasis implements de.haumacher.msgbuf.data.ProtocolEnum {

		/**
		 * The prominent line.
		 */
		PRIMARY("primary"),

		/**
		 * A muted secondary line.
		 */
		SECONDARY("secondary"),

		;

		private final String _protocolName;

		private Emphasis(String protocolName) {
			_protocolName = protocolName;
		}

		/**
		 * The protocol name of a {@link Emphasis} constant.
		 *
		 * @see #valueOfProtocol(String)
		 */
		@Override
		public String protocolName() {
			return _protocolName;
		}

		/** Looks up a {@link Emphasis} constant by it's protocol name. */
		public static Emphasis valueOfProtocol(String protocolName) {
			if (protocolName == null) { return null; }
			switch (protocolName) {
				case "primary": return PRIMARY;
				case "secondary": return SECONDARY;
			}
			return PRIMARY;
		}

		/** Writes this instance to the given output. */
		public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			out.value(protocolName());
		}

		/** Reads a new instance from the given reader. */
		public static Emphasis readEmphasis(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
			return valueOfProtocol(in.nextString());
		}
	}

	/**
	 * When the trigger of the row commands is shown.
	 */
	public enum RowActions implements de.haumacher.msgbuf.data.ProtocolEnum {

		/**
		 * Revealed when the pointer is over the row or something in it has the keyboard focus.
		 */
		HOVER("hover"),

		/**
		 * Always visible.
		 */
		ALWAYS("always"),

		/**
		 * Never rendered.
		 */
		NONE("none"),

		;

		private final String _protocolName;

		private RowActions(String protocolName) {
			_protocolName = protocolName;
		}

		/**
		 * The protocol name of a {@link RowActions} constant.
		 *
		 * @see #valueOfProtocol(String)
		 */
		@Override
		public String protocolName() {
			return _protocolName;
		}

		/** Looks up a {@link RowActions} constant by it's protocol name. */
		public static RowActions valueOfProtocol(String protocolName) {
			if (protocolName == null) { return null; }
			switch (protocolName) {
				case "hover": return HOVER;
				case "always": return ALWAYS;
				case "none": return NONE;
			}
			return HOVER;
		}

		/** Writes this instance to the given output. */
		public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			out.value(protocolName());
		}

		/** Reads a new instance from the given reader. */
		public static RowActions readRowActions(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
			return valueOfProtocol(in.nextString());
		}
	}
	/**
	 * A line of text of a {@link Row}.
	 */
	public interface Line extends de.haumacher.msgbuf.data.DataObject {

		/**
		 * Creates a {@link com.top_logic.layout.react.state.DataListState.Line} instance.
		 */
		static com.top_logic.layout.react.state.DataListState.Line create() {
			return new com.top_logic.layout.react.state.impl.DataListState_Impl.Line_Impl();
		}

		/** Identifier for the {@link com.top_logic.layout.react.state.DataListState.Line} type in JSON format. */
		String LINE__TYPE = "Line";

		/** @see #getText() */
		String TEXT__PROP = "text";

		/** @see #getEmphasis() */
		String EMPHASIS__PROP = "emphasis";

		/**
		 * The text of the line, plain text. A line after the first one whose text is empty is not
		 * drawn.
		 */
		String getText();

		/**
		 * @see #getText()
		 */
		com.top_logic.layout.react.state.DataListState.Line setText(String value);

		/**
		 * How prominently the line is shown. Absent: the first line of a row is
		 * {@link Emphasis#PRIMARY}, every further line {@link Emphasis#SECONDARY}.
		 */
		com.top_logic.layout.react.state.DataListState.Emphasis getEmphasis();

		/**
		 * @see #getEmphasis()
		 */
		com.top_logic.layout.react.state.DataListState.Line setEmphasis(com.top_logic.layout.react.state.DataListState.Emphasis value);

		/** Reads a new instance from the given reader. */
		static com.top_logic.layout.react.state.DataListState.Line readLine(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
			com.top_logic.layout.react.state.impl.DataListState_Impl.Line_Impl result = new com.top_logic.layout.react.state.impl.DataListState_Impl.Line_Impl();
			result.readContent(in);
			return result;
		}

	}
	/**
	 * What stands in front of the lines of a {@link Row}: an icon, or an avatar showing a picture or
	 * the initials of a name.
	 */
	public interface Leading extends de.haumacher.msgbuf.data.DataObject {

		/**
		 * Creates a {@link com.top_logic.layout.react.state.DataListState.Leading} instance.
		 */
		static com.top_logic.layout.react.state.DataListState.Leading create() {
			return new com.top_logic.layout.react.state.impl.DataListState_Impl.Leading_Impl();
		}

		/** Identifier for the {@link com.top_logic.layout.react.state.DataListState.Leading} type in JSON format. */
		String LEADING__TYPE = "Leading";

		/** @see #getName() */
		String NAME__PROP = "name";

		/** @see #getIcon() */
		String ICON__PROP = "icon";

		/** @see #getImage() */
		String IMAGE__PROP = "image";

		/**
		 * The display name the avatar stands for, plain text: the source of its initials, its category
		 * and its accessible name. Empty: none.
		 */
		String getName();

		/**
		 * @see #getName()
		 */
		com.top_logic.layout.react.state.DataListState.Leading setName(String value);

		/**
		 * An icon drawn instead of an avatar, the encoded form of an icon-font image. Empty: no icon.
		 */
		String getIcon();

		/**
		 * @see #getIcon()
		 */
		com.top_logic.layout.react.state.DataListState.Leading setIcon(String value);

		/**
		 * The picture of the avatar, the address of an image (a path or a {@code data} URI). Empty: the
		 * avatar shows the initials of the {@link #getName()}.
		 */
		String getImage();

		/**
		 * @see #getImage()
		 */
		com.top_logic.layout.react.state.DataListState.Leading setImage(String value);

		/** Reads a new instance from the given reader. */
		static com.top_logic.layout.react.state.DataListState.Leading readLeading(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
			com.top_logic.layout.react.state.impl.DataListState_Impl.Leading_Impl result = new com.top_logic.layout.react.state.impl.DataListState_Impl.Leading_Impl();
			result.readContent(in);
			return result;
		}

	}
	/**
	 * What stands after the lines of a {@link Row}: metadata text, or a badge.
	 */
	public interface Trailing extends de.haumacher.msgbuf.data.DataObject {

		/**
		 * Creates a {@link com.top_logic.layout.react.state.DataListState.Trailing} instance.
		 */
		static com.top_logic.layout.react.state.DataListState.Trailing create() {
			return new com.top_logic.layout.react.state.impl.DataListState_Impl.Trailing_Impl();
		}

		/** Identifier for the {@link com.top_logic.layout.react.state.DataListState.Trailing} type in JSON format. */
		String TRAILING__TYPE = "Trailing";

		/** @see #getText() */
		String TEXT__PROP = "text";

		/** @see #isBadge() */
		String BADGE__PROP = "badge";

		/**
		 * The text, plain text.
		 */
		String getText();

		/**
		 * @see #getText()
		 */
		com.top_logic.layout.react.state.DataListState.Trailing setText(String value);

		/**
		 * Whether the {@link #getText()} is drawn as a badge (a count or a short status) instead of
		 * metadata text.
		 */
		boolean isBadge();

		/**
		 * @see #isBadge()
		 */
		com.top_logic.layout.react.state.DataListState.Trailing setBadge(boolean value);

		/** Reads a new instance from the given reader. */
		static com.top_logic.layout.react.state.DataListState.Trailing readTrailing(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
			com.top_logic.layout.react.state.impl.DataListState_Impl.Trailing_Impl result = new com.top_logic.layout.react.state.impl.DataListState_Impl.Trailing_Impl();
			result.readContent(in);
			return result;
		}

	}
	/**
	 * An entry of the list.
	 */
	public interface Row extends de.haumacher.msgbuf.data.DataObject {

		/**
		 * Creates a {@link com.top_logic.layout.react.state.DataListState.Row} instance.
		 */
		static com.top_logic.layout.react.state.DataListState.Row create() {
			return new com.top_logic.layout.react.state.impl.DataListState_Impl.Row_Impl();
		}

		/** Identifier for the {@link com.top_logic.layout.react.state.DataListState.Row} type in JSON format. */
		String ROW__TYPE = "Row";

		/** @see #getId() */
		String ID__PROP = "id";

		/** @see #getLines() */
		String LINES__PROP = "lines";

		/** @see #getGroup() */
		String GROUP__PROP = "group";

		/** @see #getLeading() */
		String LEADING__PROP = "leading";

		/** @see #getTrailing() */
		String TRAILING__PROP = "trailing";

		/** @see #getCssClass() */
		String CSS_CLASS__PROP = "cssClass";

		/**
		 * The ID of the row, by which the client names it to the server.
		 */
		String getId();

		/**
		 * @see #getId()
		 */
		com.top_logic.layout.react.state.DataListState.Row setId(String value);

		/**
		 * The lines of the row, top to bottom.
		 */
		java.util.List<com.top_logic.layout.react.state.DataListState.Line> getLines();

		/**
		 * @see #getLines()
		 */
		com.top_logic.layout.react.state.DataListState.Row setLines(java.util.List<? extends com.top_logic.layout.react.state.DataListState.Line> value);

		/**
		 * Adds a value to the {@link #getLines()} list.
		 */
		com.top_logic.layout.react.state.DataListState.Row addLine(com.top_logic.layout.react.state.DataListState.Line value);

		/**
		 * Removes a value from the {@link #getLines()} list.
		 */
		void removeLine(com.top_logic.layout.react.state.DataListState.Line value);

		/**
		 * The heading of the group the row belongs to. Groups stand in the order of their first row.
		 * Absent: the row belongs to the group without heading.
		 */
		String getGroup();

		/**
		 * @see #getGroup()
		 */
		com.top_logic.layout.react.state.DataListState.Row setGroup(String value);

		/**
		 * What stands in front of the lines. Absent: nothing.
		 */
		com.top_logic.layout.react.state.DataListState.Leading getLeading();

		/**
		 * @see #getLeading()
		 */
		com.top_logic.layout.react.state.DataListState.Row setLeading(com.top_logic.layout.react.state.DataListState.Leading value);

		/**
		 * Checks, whether {@link #getLeading()} has a value.
		 */
		boolean hasLeading();

		/**
		 * What stands after the lines. Absent: nothing.
		 */
		com.top_logic.layout.react.state.DataListState.Trailing getTrailing();

		/**
		 * @see #getTrailing()
		 */
		com.top_logic.layout.react.state.DataListState.Row setTrailing(com.top_logic.layout.react.state.DataListState.Trailing value);

		/**
		 * Checks, whether {@link #getTrailing()} has a value.
		 */
		boolean hasTrailing();

		/**
		 * Additional CSS classes of the row element, separated by spaces, computed by the
		 * application. Absent: none.
		 */
		String getCssClass();

		/**
		 * @see #getCssClass()
		 */
		com.top_logic.layout.react.state.DataListState.Row setCssClass(String value);

		/** Reads a new instance from the given reader. */
		static com.top_logic.layout.react.state.DataListState.Row readRow(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
			com.top_logic.layout.react.state.impl.DataListState_Impl.Row_Impl result = new com.top_logic.layout.react.state.impl.DataListState_Impl.Row_Impl();
			result.readContent(in);
			return result;
		}

	}

	/**
	 * Creates a {@link com.top_logic.layout.react.state.DataListState} instance.
	 */
	static com.top_logic.layout.react.state.DataListState create() {
		return new com.top_logic.layout.react.state.impl.DataListState_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.state.DataListState} type in JSON format. */
	String DATA_LIST_STATE__TYPE = "DataListState";

	/** @see #getRows() */
	String ROWS__PROP = "rows";

	/** @see #getSelectedId() */
	String SELECTED_ID__PROP = "selectedId";

	/** @see #getTitle() */
	String TITLE__PROP = "title";

	/** @see #getEmptyText() */
	String EMPTY_TEXT__PROP = "emptyText";

	/** @see #isSearch() */
	String SEARCH__PROP = "search";

	/** @see #getButtons() */
	String BUTTONS__PROP = "buttons";

	/** @see #isHasRowMenu() */
	String HAS_ROW_MENU__PROP = "hasRowMenu";

	/** @see #getRowActions() */
	String ROW_ACTIONS__PROP = "rowActions";

	/**
	 * The rows in display order.
	 */
	java.util.List<com.top_logic.layout.react.state.DataListState.Row> getRows();

	/**
	 * @see #getRows()
	 */
	com.top_logic.layout.react.state.DataListState setRows(java.util.List<? extends com.top_logic.layout.react.state.DataListState.Row> value);

	/**
	 * Adds a value to the {@link #getRows()} list.
	 */
	com.top_logic.layout.react.state.DataListState addRow(com.top_logic.layout.react.state.DataListState.Row value);

	/**
	 * Removes a value from the {@link #getRows()} list.
	 */
	void removeRow(com.top_logic.layout.react.state.DataListState.Row value);

	/**
	 * The {@link Row#getId()} of the selected row. Empty: none.
	 */
	String getSelectedId();

	/**
	 * @see #getSelectedId()
	 */
	com.top_logic.layout.react.state.DataListState setSelectedId(String value);

	/**
	 * The title in the header of the list, plain text. Empty: no title.
	 */
	String getTitle();

	/**
	 * @see #getTitle()
	 */
	com.top_logic.layout.react.state.DataListState setTitle(String value);

	/**
	 * The text of an empty list, plain text. Empty: a general text of the component.
	 */
	String getEmptyText();

	/**
	 * @see #getEmptyText()
	 */
	com.top_logic.layout.react.state.DataListState setEmptyText(String value);

	/**
	 * Whether the list shows a search field filtering the rows by the text of their lines.
	 */
	boolean isSearch();

	/**
	 * @see #isSearch()
	 */
	com.top_logic.layout.react.state.DataListState setSearch(boolean value);

	/**
	 * The command buttons of the list, in display order. Absent: none.
	 */
	java.util.List<com.top_logic.layout.react.state.ChildControl> getButtons();

	/**
	 * @see #getButtons()
	 */
	com.top_logic.layout.react.state.DataListState setButtons(java.util.List<? extends com.top_logic.layout.react.state.ChildControl> value);

	/**
	 * Adds a value to the {@link #getButtons()} list.
	 */
	com.top_logic.layout.react.state.DataListState addButton(com.top_logic.layout.react.state.ChildControl value);

	/**
	 * Removes a value from the {@link #getButtons()} list.
	 */
	void removeButton(com.top_logic.layout.react.state.ChildControl value);

	/**
	 * Whether there are commands per row that the trigger of a row opens.
	 */
	boolean isHasRowMenu();

	/**
	 * @see #isHasRowMenu()
	 */
	com.top_logic.layout.react.state.DataListState setHasRowMenu(boolean value);

	/**
	 * When the trigger of the row commands is shown. Absent means {@link RowActions#HOVER}.
	 */
	com.top_logic.layout.react.state.DataListState.RowActions getRowActions();

	/**
	 * @see #getRowActions()
	 */
	com.top_logic.layout.react.state.DataListState setRowActions(com.top_logic.layout.react.state.DataListState.RowActions value);

	@Override
	com.top_logic.layout.react.state.DataListState setHidden(boolean value);

	@Override
	com.top_logic.layout.react.state.DataListState setCssClass(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.state.DataListState readDataListState(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.state.impl.DataListState_Impl result = new com.top_logic.layout.react.state.impl.DataListState_Impl();
		result.readContent(in);
		return result;
	}

}
