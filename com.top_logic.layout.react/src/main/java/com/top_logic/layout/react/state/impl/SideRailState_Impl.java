package com.top_logic.layout.react.state.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.state.SideRailState}.
 */
public class SideRailState_Impl extends com.top_logic.layout.react.state.impl.ControlState_Impl implements com.top_logic.layout.react.state.SideRailState {
	/**
	 * Implementation of {@link com.top_logic.layout.react.state.SideRailState.Section}.
	 */
	public static class Section_Impl extends de.haumacher.msgbuf.data.AbstractDataObject implements com.top_logic.layout.react.state.SideRailState.Section {

		private String _name = "";

		private String _label = "";

		private String _icon = "";

		private boolean _expanded = false;

		private boolean _hidden = false;

		private com.top_logic.layout.react.state.Badge _badge = null;

		private com.top_logic.layout.react.state.ChildControl _content = null;

		/**
		 * Creates a {@link Section_Impl} instance.
		 *
		 * @see com.top_logic.layout.react.state.SideRailState.Section#create()
		 */
		public Section_Impl() {
			super();
		}

		@Override
		public final String getName() {
			return _name;
		}

		@Override
		public com.top_logic.layout.react.state.SideRailState.Section setName(String value) {
			internalSetName(value);
			return this;
		}

		/** Internal setter for {@link #getName()} without chain call utility. */
		protected final void internalSetName(String value) {
			_name = value;
		}

		@Override
		public final String getLabel() {
			return _label;
		}

		@Override
		public com.top_logic.layout.react.state.SideRailState.Section setLabel(String value) {
			internalSetLabel(value);
			return this;
		}

		/** Internal setter for {@link #getLabel()} without chain call utility. */
		protected final void internalSetLabel(String value) {
			_label = value;
		}

		@Override
		public final String getIcon() {
			return _icon;
		}

		@Override
		public com.top_logic.layout.react.state.SideRailState.Section setIcon(String value) {
			internalSetIcon(value);
			return this;
		}

		/** Internal setter for {@link #getIcon()} without chain call utility. */
		protected final void internalSetIcon(String value) {
			_icon = value;
		}

		@Override
		public final boolean isExpanded() {
			return _expanded;
		}

		@Override
		public com.top_logic.layout.react.state.SideRailState.Section setExpanded(boolean value) {
			internalSetExpanded(value);
			return this;
		}

		/** Internal setter for {@link #isExpanded()} without chain call utility. */
		protected final void internalSetExpanded(boolean value) {
			_expanded = value;
		}

		@Override
		public final boolean isHidden() {
			return _hidden;
		}

		@Override
		public com.top_logic.layout.react.state.SideRailState.Section setHidden(boolean value) {
			internalSetHidden(value);
			return this;
		}

		/** Internal setter for {@link #isHidden()} without chain call utility. */
		protected final void internalSetHidden(boolean value) {
			_hidden = value;
		}

		@Override
		public final com.top_logic.layout.react.state.Badge getBadge() {
			return _badge;
		}

		@Override
		public com.top_logic.layout.react.state.SideRailState.Section setBadge(com.top_logic.layout.react.state.Badge value) {
			internalSetBadge(value);
			return this;
		}

		/** Internal setter for {@link #getBadge()} without chain call utility. */
		protected final void internalSetBadge(com.top_logic.layout.react.state.Badge value) {
			_badge = value;
		}

		@Override
		public final boolean hasBadge() {
			return _badge != null;
		}

		@Override
		public final com.top_logic.layout.react.state.ChildControl getContent() {
			return _content;
		}

		@Override
		public com.top_logic.layout.react.state.SideRailState.Section setContent(com.top_logic.layout.react.state.ChildControl value) {
			internalSetContent(value);
			return this;
		}

		/** Internal setter for {@link #getContent()} without chain call utility. */
		protected final void internalSetContent(com.top_logic.layout.react.state.ChildControl value) {
			_content = value;
		}

		@Override
		public final boolean hasContent() {
			return _content != null;
		}

		@Override
		public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			writeContent(out);
		}

		@Override
		protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			super.writeFields(out);
			out.name(NAME__PROP);
			out.value(getName());
			out.name(LABEL__PROP);
			out.value(getLabel());
			out.name(ICON__PROP);
			out.value(getIcon());
			out.name(EXPANDED__PROP);
			out.value(isExpanded());
			out.name(HIDDEN__PROP);
			out.value(isHidden());
			if (hasBadge()) {
				out.name(BADGE__PROP);
				getBadge().writeTo(out);
			}
			if (hasContent()) {
				out.name(CONTENT__PROP);
				getContent().writeTo(out);
			}
		}

		@Override
		protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
			switch (field) {
				case NAME__PROP: setName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
				case LABEL__PROP: setLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
				case ICON__PROP: setIcon(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
				case EXPANDED__PROP: setExpanded(in.nextBoolean()); break;
				case HIDDEN__PROP: setHidden(in.nextBoolean()); break;
				case BADGE__PROP: setBadge(com.top_logic.layout.react.state.Badge.readBadge(in)); break;
				case CONTENT__PROP: setContent(com.top_logic.layout.react.state.ChildControl.readChildControl(in)); break;
				default: super.readField(in, field);
			}
		}

	}

	private com.top_logic.layout.react.state.SideRailState.Side _side = com.top_logic.layout.react.state.SideRailState.Side.LEFT;

	private boolean _strip = false;

	private boolean _open = false;

	private int _drawerBelow = 0;

	private final java.util.List<com.top_logic.layout.react.state.SideRailState.Section> _sections = new java.util.ArrayList<>();

	private com.top_logic.layout.react.state.ChildControl _body = null;

	private com.top_logic.layout.react.state.ChildControl _drawer = null;

	private boolean _resizable = false;

	private int _width = 0;

	private int _sectionWidth = 0;

	/**
	 * Creates a {@link SideRailState_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.state.SideRailState#create()
	 */
	public SideRailState_Impl() {
		super();
	}

	@Override
	public final com.top_logic.layout.react.state.SideRailState.Side getSide() {
		return _side;
	}

	@Override
	public com.top_logic.layout.react.state.SideRailState setSide(com.top_logic.layout.react.state.SideRailState.Side value) {
		internalSetSide(value);
		return this;
	}

	/** Internal setter for {@link #getSide()} without chain call utility. */
	protected final void internalSetSide(com.top_logic.layout.react.state.SideRailState.Side value) {
		if (value == null) throw new IllegalArgumentException("Property 'side' cannot be null.");
		_side = value;
	}

	@Override
	public final boolean isStrip() {
		return _strip;
	}

	@Override
	public com.top_logic.layout.react.state.SideRailState setStrip(boolean value) {
		internalSetStrip(value);
		return this;
	}

	/** Internal setter for {@link #isStrip()} without chain call utility. */
	protected final void internalSetStrip(boolean value) {
		_strip = value;
	}

	@Override
	public final boolean isOpen() {
		return _open;
	}

	@Override
	public com.top_logic.layout.react.state.SideRailState setOpen(boolean value) {
		internalSetOpen(value);
		return this;
	}

	/** Internal setter for {@link #isOpen()} without chain call utility. */
	protected final void internalSetOpen(boolean value) {
		_open = value;
	}

	@Override
	public final int getDrawerBelow() {
		return _drawerBelow;
	}

	@Override
	public com.top_logic.layout.react.state.SideRailState setDrawerBelow(int value) {
		internalSetDrawerBelow(value);
		return this;
	}

	/** Internal setter for {@link #getDrawerBelow()} without chain call utility. */
	protected final void internalSetDrawerBelow(int value) {
		_drawerBelow = value;
	}

	@Override
	public final java.util.List<com.top_logic.layout.react.state.SideRailState.Section> getSections() {
		return _sections;
	}

	@Override
	public com.top_logic.layout.react.state.SideRailState setSections(java.util.List<? extends com.top_logic.layout.react.state.SideRailState.Section> value) {
		internalSetSections(value);
		return this;
	}

	/** Internal setter for {@link #getSections()} without chain call utility. */
	protected final void internalSetSections(java.util.List<? extends com.top_logic.layout.react.state.SideRailState.Section> value) {
		if (value == null) throw new IllegalArgumentException("Property 'sections' cannot be null.");
		_sections.clear();
		_sections.addAll(value);
	}

	@Override
	public com.top_logic.layout.react.state.SideRailState addSection(com.top_logic.layout.react.state.SideRailState.Section value) {
		internalAddSection(value);
		return this;
	}

	/** Implementation of {@link #addSection(com.top_logic.layout.react.state.SideRailState.Section)} without chain call utility. */
	protected final void internalAddSection(com.top_logic.layout.react.state.SideRailState.Section value) {
		_sections.add(value);
	}

	@Override
	public final void removeSection(com.top_logic.layout.react.state.SideRailState.Section value) {
		_sections.remove(value);
	}

	@Override
	public final com.top_logic.layout.react.state.ChildControl getBody() {
		return _body;
	}

	@Override
	public com.top_logic.layout.react.state.SideRailState setBody(com.top_logic.layout.react.state.ChildControl value) {
		internalSetBody(value);
		return this;
	}

	/** Internal setter for {@link #getBody()} without chain call utility. */
	protected final void internalSetBody(com.top_logic.layout.react.state.ChildControl value) {
		_body = value;
	}

	@Override
	public final boolean hasBody() {
		return _body != null;
	}

	@Override
	public final com.top_logic.layout.react.state.ChildControl getDrawer() {
		return _drawer;
	}

	@Override
	public com.top_logic.layout.react.state.SideRailState setDrawer(com.top_logic.layout.react.state.ChildControl value) {
		internalSetDrawer(value);
		return this;
	}

	/** Internal setter for {@link #getDrawer()} without chain call utility. */
	protected final void internalSetDrawer(com.top_logic.layout.react.state.ChildControl value) {
		_drawer = value;
	}

	@Override
	public final boolean hasDrawer() {
		return _drawer != null;
	}

	@Override
	public final boolean isResizable() {
		return _resizable;
	}

	@Override
	public com.top_logic.layout.react.state.SideRailState setResizable(boolean value) {
		internalSetResizable(value);
		return this;
	}

	/** Internal setter for {@link #isResizable()} without chain call utility. */
	protected final void internalSetResizable(boolean value) {
		_resizable = value;
	}

	@Override
	public final int getWidth() {
		return _width;
	}

	@Override
	public com.top_logic.layout.react.state.SideRailState setWidth(int value) {
		internalSetWidth(value);
		return this;
	}

	/** Internal setter for {@link #getWidth()} without chain call utility. */
	protected final void internalSetWidth(int value) {
		_width = value;
	}

	@Override
	public final int getSectionWidth() {
		return _sectionWidth;
	}

	@Override
	public com.top_logic.layout.react.state.SideRailState setSectionWidth(int value) {
		internalSetSectionWidth(value);
		return this;
	}

	/** Internal setter for {@link #getSectionWidth()} without chain call utility. */
	protected final void internalSetSectionWidth(int value) {
		_sectionWidth = value;
	}

	@Override
	public com.top_logic.layout.react.state.SideRailState setHidden(boolean value) {
		internalSetHidden(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.SideRailState setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public String jsonType() {
		return SIDE_RAIL_STATE__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(SIDE__PROP);
		getSide().writeTo(out);
		out.name(STRIP__PROP);
		out.value(isStrip());
		out.name(OPEN__PROP);
		out.value(isOpen());
		out.name(DRAWER_BELOW__PROP);
		out.value(getDrawerBelow());
		out.name(SECTIONS__PROP);
		out.beginArray();
		for (com.top_logic.layout.react.state.SideRailState.Section x : getSections()) {
			x.writeTo(out);
		}
		out.endArray();
		if (hasBody()) {
			out.name(BODY__PROP);
			getBody().writeTo(out);
		}
		if (hasDrawer()) {
			out.name(DRAWER__PROP);
			getDrawer().writeTo(out);
		}
		out.name(RESIZABLE__PROP);
		out.value(isResizable());
		out.name(WIDTH__PROP);
		out.value(getWidth());
		out.name(SECTION_WIDTH__PROP);
		out.value(getSectionWidth());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case SIDE__PROP: setSide(com.top_logic.layout.react.state.SideRailState.Side.readSide(in)); break;
			case STRIP__PROP: setStrip(in.nextBoolean()); break;
			case OPEN__PROP: setOpen(in.nextBoolean()); break;
			case DRAWER_BELOW__PROP: setDrawerBelow(in.nextInt()); break;
			case SECTIONS__PROP: {
				java.util.List<com.top_logic.layout.react.state.SideRailState.Section> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.layout.react.state.SideRailState.Section.readSection(in));
				}
				in.endArray();
				setSections(newValue);
			}
			break;
			case BODY__PROP: setBody(com.top_logic.layout.react.state.ChildControl.readChildControl(in)); break;
			case DRAWER__PROP: setDrawer(com.top_logic.layout.react.state.ChildControl.readChildControl(in)); break;
			case RESIZABLE__PROP: setResizable(in.nextBoolean()); break;
			case WIDTH__PROP: setWidth(in.nextInt()); break;
			case SECTION_WIDTH__PROP: setSectionWidth(in.nextInt()); break;
			default: super.readField(in, field);
		}
	}

}
