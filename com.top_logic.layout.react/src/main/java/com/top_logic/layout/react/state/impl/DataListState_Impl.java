package com.top_logic.layout.react.state.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.state.DataListState}.
 */
public class DataListState_Impl extends com.top_logic.layout.react.state.impl.ControlState_Impl implements com.top_logic.layout.react.state.DataListState {
	/**
	 * Implementation of {@link com.top_logic.layout.react.state.DataListState.Line}.
	 */
	public static class Line_Impl extends de.haumacher.msgbuf.data.AbstractDataObject implements com.top_logic.layout.react.state.DataListState.Line {

		private String _text = "";

		private com.top_logic.layout.react.state.DataListState.Emphasis _emphasis = com.top_logic.layout.react.state.DataListState.Emphasis.PRIMARY;

		/**
		 * Creates a {@link Line_Impl} instance.
		 *
		 * @see com.top_logic.layout.react.state.DataListState.Line#create()
		 */
		public Line_Impl() {
			super();
		}

		@Override
		public final String getText() {
			return _text;
		}

		@Override
		public com.top_logic.layout.react.state.DataListState.Line setText(String value) {
			internalSetText(value);
			return this;
		}

		/** Internal setter for {@link #getText()} without chain call utility. */
		protected final void internalSetText(String value) {
			_text = value;
		}

		@Override
		public final com.top_logic.layout.react.state.DataListState.Emphasis getEmphasis() {
			return _emphasis;
		}

		@Override
		public com.top_logic.layout.react.state.DataListState.Line setEmphasis(com.top_logic.layout.react.state.DataListState.Emphasis value) {
			internalSetEmphasis(value);
			return this;
		}

		/** Internal setter for {@link #getEmphasis()} without chain call utility. */
		protected final void internalSetEmphasis(com.top_logic.layout.react.state.DataListState.Emphasis value) {
			if (value == null) throw new IllegalArgumentException("Property 'emphasis' cannot be null.");
			_emphasis = value;
		}

		@Override
		public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			writeContent(out);
		}

		@Override
		protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			super.writeFields(out);
			out.name(TEXT__PROP);
			out.value(getText());
			out.name(EMPHASIS__PROP);
			getEmphasis().writeTo(out);
		}

		@Override
		protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
			switch (field) {
				case TEXT__PROP: setText(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
				case EMPHASIS__PROP: setEmphasis(com.top_logic.layout.react.state.DataListState.Emphasis.readEmphasis(in)); break;
				default: super.readField(in, field);
			}
		}

	}
	/**
	 * Implementation of {@link com.top_logic.layout.react.state.DataListState.Row}.
	 */
	public static class Row_Impl extends de.haumacher.msgbuf.data.AbstractDataObject implements com.top_logic.layout.react.state.DataListState.Row {

		private String _id = "";

		private final java.util.List<com.top_logic.layout.react.state.DataListState.Line> _lines = new java.util.ArrayList<>();

		private String _group = "";

		private String _cssClass = "";

		/**
		 * Creates a {@link Row_Impl} instance.
		 *
		 * @see com.top_logic.layout.react.state.DataListState.Row#create()
		 */
		public Row_Impl() {
			super();
		}

		@Override
		public final String getId() {
			return _id;
		}

		@Override
		public com.top_logic.layout.react.state.DataListState.Row setId(String value) {
			internalSetId(value);
			return this;
		}

		/** Internal setter for {@link #getId()} without chain call utility. */
		protected final void internalSetId(String value) {
			_id = value;
		}

		@Override
		public final java.util.List<com.top_logic.layout.react.state.DataListState.Line> getLines() {
			return _lines;
		}

		@Override
		public com.top_logic.layout.react.state.DataListState.Row setLines(java.util.List<? extends com.top_logic.layout.react.state.DataListState.Line> value) {
			internalSetLines(value);
			return this;
		}

		/** Internal setter for {@link #getLines()} without chain call utility. */
		protected final void internalSetLines(java.util.List<? extends com.top_logic.layout.react.state.DataListState.Line> value) {
			if (value == null) throw new IllegalArgumentException("Property 'lines' cannot be null.");
			_lines.clear();
			_lines.addAll(value);
		}

		@Override
		public com.top_logic.layout.react.state.DataListState.Row addLine(com.top_logic.layout.react.state.DataListState.Line value) {
			internalAddLine(value);
			return this;
		}

		/** Implementation of {@link #addLine(com.top_logic.layout.react.state.DataListState.Line)} without chain call utility. */
		protected final void internalAddLine(com.top_logic.layout.react.state.DataListState.Line value) {
			_lines.add(value);
		}

		@Override
		public final void removeLine(com.top_logic.layout.react.state.DataListState.Line value) {
			_lines.remove(value);
		}

		@Override
		public final String getGroup() {
			return _group;
		}

		@Override
		public com.top_logic.layout.react.state.DataListState.Row setGroup(String value) {
			internalSetGroup(value);
			return this;
		}

		/** Internal setter for {@link #getGroup()} without chain call utility. */
		protected final void internalSetGroup(String value) {
			_group = value;
		}

		@Override
		public final String getCssClass() {
			return _cssClass;
		}

		@Override
		public com.top_logic.layout.react.state.DataListState.Row setCssClass(String value) {
			internalSetCssClass(value);
			return this;
		}

		/** Internal setter for {@link #getCssClass()} without chain call utility. */
		protected final void internalSetCssClass(String value) {
			_cssClass = value;
		}

		@Override
		public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			writeContent(out);
		}

		@Override
		protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			super.writeFields(out);
			out.name(ID__PROP);
			out.value(getId());
			out.name(LINES__PROP);
			out.beginArray();
			for (com.top_logic.layout.react.state.DataListState.Line x : getLines()) {
				x.writeTo(out);
			}
			out.endArray();
			out.name(GROUP__PROP);
			out.value(getGroup());
			out.name(CSS_CLASS__PROP);
			out.value(getCssClass());
		}

		@Override
		protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
			switch (field) {
				case ID__PROP: setId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
				case LINES__PROP: {
					java.util.List<com.top_logic.layout.react.state.DataListState.Line> newValue = new java.util.ArrayList<>();
					in.beginArray();
					while (in.hasNext()) {
						newValue.add(com.top_logic.layout.react.state.DataListState.Line.readLine(in));
					}
					in.endArray();
					setLines(newValue);
				}
				break;
				case GROUP__PROP: setGroup(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
				case CSS_CLASS__PROP: setCssClass(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
				default: super.readField(in, field);
			}
		}

	}

	private final java.util.List<com.top_logic.layout.react.state.DataListState.Row> _rows = new java.util.ArrayList<>();

	private String _selectedId = "";

	private String _title = "";

	private String _emptyText = "";

	private boolean _search = false;

	private boolean _collapsible = false;

	private boolean _collapsed = false;

	private int _drawerBelow = 0;

	private final java.util.List<com.top_logic.layout.react.state.ChildControl> _buttons = new java.util.ArrayList<>();

	private boolean _hasRowMenu = false;

	private com.top_logic.layout.react.state.DataListState.RowActions _rowActions = com.top_logic.layout.react.state.DataListState.RowActions.HOVER;

	/**
	 * Creates a {@link DataListState_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.state.DataListState#create()
	 */
	public DataListState_Impl() {
		super();
	}

	@Override
	public final java.util.List<com.top_logic.layout.react.state.DataListState.Row> getRows() {
		return _rows;
	}

	@Override
	public com.top_logic.layout.react.state.DataListState setRows(java.util.List<? extends com.top_logic.layout.react.state.DataListState.Row> value) {
		internalSetRows(value);
		return this;
	}

	/** Internal setter for {@link #getRows()} without chain call utility. */
	protected final void internalSetRows(java.util.List<? extends com.top_logic.layout.react.state.DataListState.Row> value) {
		if (value == null) throw new IllegalArgumentException("Property 'rows' cannot be null.");
		_rows.clear();
		_rows.addAll(value);
	}

	@Override
	public com.top_logic.layout.react.state.DataListState addRow(com.top_logic.layout.react.state.DataListState.Row value) {
		internalAddRow(value);
		return this;
	}

	/** Implementation of {@link #addRow(com.top_logic.layout.react.state.DataListState.Row)} without chain call utility. */
	protected final void internalAddRow(com.top_logic.layout.react.state.DataListState.Row value) {
		_rows.add(value);
	}

	@Override
	public final void removeRow(com.top_logic.layout.react.state.DataListState.Row value) {
		_rows.remove(value);
	}

	@Override
	public final String getSelectedId() {
		return _selectedId;
	}

	@Override
	public com.top_logic.layout.react.state.DataListState setSelectedId(String value) {
		internalSetSelectedId(value);
		return this;
	}

	/** Internal setter for {@link #getSelectedId()} without chain call utility. */
	protected final void internalSetSelectedId(String value) {
		_selectedId = value;
	}

	@Override
	public final String getTitle() {
		return _title;
	}

	@Override
	public com.top_logic.layout.react.state.DataListState setTitle(String value) {
		internalSetTitle(value);
		return this;
	}

	/** Internal setter for {@link #getTitle()} without chain call utility. */
	protected final void internalSetTitle(String value) {
		_title = value;
	}

	@Override
	public final String getEmptyText() {
		return _emptyText;
	}

	@Override
	public com.top_logic.layout.react.state.DataListState setEmptyText(String value) {
		internalSetEmptyText(value);
		return this;
	}

	/** Internal setter for {@link #getEmptyText()} without chain call utility. */
	protected final void internalSetEmptyText(String value) {
		_emptyText = value;
	}

	@Override
	public final boolean isSearch() {
		return _search;
	}

	@Override
	public com.top_logic.layout.react.state.DataListState setSearch(boolean value) {
		internalSetSearch(value);
		return this;
	}

	/** Internal setter for {@link #isSearch()} without chain call utility. */
	protected final void internalSetSearch(boolean value) {
		_search = value;
	}

	@Override
	public final boolean isCollapsible() {
		return _collapsible;
	}

	@Override
	public com.top_logic.layout.react.state.DataListState setCollapsible(boolean value) {
		internalSetCollapsible(value);
		return this;
	}

	/** Internal setter for {@link #isCollapsible()} without chain call utility. */
	protected final void internalSetCollapsible(boolean value) {
		_collapsible = value;
	}

	@Override
	public final boolean isCollapsed() {
		return _collapsed;
	}

	@Override
	public com.top_logic.layout.react.state.DataListState setCollapsed(boolean value) {
		internalSetCollapsed(value);
		return this;
	}

	/** Internal setter for {@link #isCollapsed()} without chain call utility. */
	protected final void internalSetCollapsed(boolean value) {
		_collapsed = value;
	}

	@Override
	public final int getDrawerBelow() {
		return _drawerBelow;
	}

	@Override
	public com.top_logic.layout.react.state.DataListState setDrawerBelow(int value) {
		internalSetDrawerBelow(value);
		return this;
	}

	/** Internal setter for {@link #getDrawerBelow()} without chain call utility. */
	protected final void internalSetDrawerBelow(int value) {
		_drawerBelow = value;
	}

	@Override
	public final java.util.List<com.top_logic.layout.react.state.ChildControl> getButtons() {
		return _buttons;
	}

	@Override
	public com.top_logic.layout.react.state.DataListState setButtons(java.util.List<? extends com.top_logic.layout.react.state.ChildControl> value) {
		internalSetButtons(value);
		return this;
	}

	/** Internal setter for {@link #getButtons()} without chain call utility. */
	protected final void internalSetButtons(java.util.List<? extends com.top_logic.layout.react.state.ChildControl> value) {
		if (value == null) throw new IllegalArgumentException("Property 'buttons' cannot be null.");
		_buttons.clear();
		_buttons.addAll(value);
	}

	@Override
	public com.top_logic.layout.react.state.DataListState addButton(com.top_logic.layout.react.state.ChildControl value) {
		internalAddButton(value);
		return this;
	}

	/** Implementation of {@link #addButton(com.top_logic.layout.react.state.ChildControl)} without chain call utility. */
	protected final void internalAddButton(com.top_logic.layout.react.state.ChildControl value) {
		_buttons.add(value);
	}

	@Override
	public final void removeButton(com.top_logic.layout.react.state.ChildControl value) {
		_buttons.remove(value);
	}

	@Override
	public final boolean isHasRowMenu() {
		return _hasRowMenu;
	}

	@Override
	public com.top_logic.layout.react.state.DataListState setHasRowMenu(boolean value) {
		internalSetHasRowMenu(value);
		return this;
	}

	/** Internal setter for {@link #isHasRowMenu()} without chain call utility. */
	protected final void internalSetHasRowMenu(boolean value) {
		_hasRowMenu = value;
	}

	@Override
	public final com.top_logic.layout.react.state.DataListState.RowActions getRowActions() {
		return _rowActions;
	}

	@Override
	public com.top_logic.layout.react.state.DataListState setRowActions(com.top_logic.layout.react.state.DataListState.RowActions value) {
		internalSetRowActions(value);
		return this;
	}

	/** Internal setter for {@link #getRowActions()} without chain call utility. */
	protected final void internalSetRowActions(com.top_logic.layout.react.state.DataListState.RowActions value) {
		if (value == null) throw new IllegalArgumentException("Property 'rowActions' cannot be null.");
		_rowActions = value;
	}

	@Override
	public com.top_logic.layout.react.state.DataListState setHidden(boolean value) {
		internalSetHidden(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.DataListState setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public String jsonType() {
		return DATA_LIST_STATE__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(ROWS__PROP);
		out.beginArray();
		for (com.top_logic.layout.react.state.DataListState.Row x : getRows()) {
			x.writeTo(out);
		}
		out.endArray();
		out.name(SELECTED_ID__PROP);
		out.value(getSelectedId());
		out.name(TITLE__PROP);
		out.value(getTitle());
		out.name(EMPTY_TEXT__PROP);
		out.value(getEmptyText());
		out.name(SEARCH__PROP);
		out.value(isSearch());
		out.name(COLLAPSIBLE__PROP);
		out.value(isCollapsible());
		out.name(COLLAPSED__PROP);
		out.value(isCollapsed());
		out.name(DRAWER_BELOW__PROP);
		out.value(getDrawerBelow());
		out.name(BUTTONS__PROP);
		out.beginArray();
		for (com.top_logic.layout.react.state.ChildControl x : getButtons()) {
			x.writeTo(out);
		}
		out.endArray();
		out.name(HAS_ROW_MENU__PROP);
		out.value(isHasRowMenu());
		out.name(ROW_ACTIONS__PROP);
		getRowActions().writeTo(out);
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ROWS__PROP: {
				java.util.List<com.top_logic.layout.react.state.DataListState.Row> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.layout.react.state.DataListState.Row.readRow(in));
				}
				in.endArray();
				setRows(newValue);
			}
			break;
			case SELECTED_ID__PROP: setSelectedId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TITLE__PROP: setTitle(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case EMPTY_TEXT__PROP: setEmptyText(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case SEARCH__PROP: setSearch(in.nextBoolean()); break;
			case COLLAPSIBLE__PROP: setCollapsible(in.nextBoolean()); break;
			case COLLAPSED__PROP: setCollapsed(in.nextBoolean()); break;
			case DRAWER_BELOW__PROP: setDrawerBelow(in.nextInt()); break;
			case BUTTONS__PROP: {
				java.util.List<com.top_logic.layout.react.state.ChildControl> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.layout.react.state.ChildControl.readChildControl(in));
				}
				in.endArray();
				setButtons(newValue);
			}
			break;
			case HAS_ROW_MENU__PROP: setHasRowMenu(in.nextBoolean()); break;
			case ROW_ACTIONS__PROP: setRowActions(com.top_logic.layout.react.state.DataListState.RowActions.readRowActions(in)); break;
			default: super.readField(in, field);
		}
	}

}
