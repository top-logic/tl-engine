package com.top_logic.layout.react.state.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.state.MenuState}.
 */
public class MenuState_Impl extends com.top_logic.layout.react.state.impl.ControlState_Impl implements com.top_logic.layout.react.state.MenuState {
	/**
	 * Implementation of {@link com.top_logic.layout.react.state.MenuState.Entry}.
	 */
	public static class Entry_Impl extends de.haumacher.msgbuf.data.AbstractDataObject implements com.top_logic.layout.react.state.MenuState.Entry {

		private com.top_logic.layout.react.state.MenuState.EntryType _type = com.top_logic.layout.react.state.MenuState.EntryType.ITEM;

		private String _id = "";

		private String _label = "";

		private String _icon = "";

		private boolean _disabled = false;

		private boolean _active = false;

		private String _cssClasses = "";

		/**
		 * Creates a {@link Entry_Impl} instance.
		 *
		 * @see com.top_logic.layout.react.state.MenuState.Entry#create()
		 */
		public Entry_Impl() {
			super();
		}

		@Override
		public final com.top_logic.layout.react.state.MenuState.EntryType getType() {
			return _type;
		}

		@Override
		public com.top_logic.layout.react.state.MenuState.Entry setType(com.top_logic.layout.react.state.MenuState.EntryType value) {
			internalSetType(value);
			return this;
		}

		/** Internal setter for {@link #getType()} without chain call utility. */
		protected final void internalSetType(com.top_logic.layout.react.state.MenuState.EntryType value) {
			if (value == null) throw new IllegalArgumentException("Property 'type' cannot be null.");
			_type = value;
		}

		@Override
		public final String getId() {
			return _id;
		}

		@Override
		public com.top_logic.layout.react.state.MenuState.Entry setId(String value) {
			internalSetId(value);
			return this;
		}

		/** Internal setter for {@link #getId()} without chain call utility. */
		protected final void internalSetId(String value) {
			_id = value;
		}

		@Override
		public final String getLabel() {
			return _label;
		}

		@Override
		public com.top_logic.layout.react.state.MenuState.Entry setLabel(String value) {
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
		public com.top_logic.layout.react.state.MenuState.Entry setIcon(String value) {
			internalSetIcon(value);
			return this;
		}

		/** Internal setter for {@link #getIcon()} without chain call utility. */
		protected final void internalSetIcon(String value) {
			_icon = value;
		}

		@Override
		public final boolean isDisabled() {
			return _disabled;
		}

		@Override
		public com.top_logic.layout.react.state.MenuState.Entry setDisabled(boolean value) {
			internalSetDisabled(value);
			return this;
		}

		/** Internal setter for {@link #isDisabled()} without chain call utility. */
		protected final void internalSetDisabled(boolean value) {
			_disabled = value;
		}

		@Override
		public final boolean isActive() {
			return _active;
		}

		@Override
		public com.top_logic.layout.react.state.MenuState.Entry setActive(boolean value) {
			internalSetActive(value);
			return this;
		}

		/** Internal setter for {@link #isActive()} without chain call utility. */
		protected final void internalSetActive(boolean value) {
			_active = value;
		}

		@Override
		public final String getCssClasses() {
			return _cssClasses;
		}

		@Override
		public com.top_logic.layout.react.state.MenuState.Entry setCssClasses(String value) {
			internalSetCssClasses(value);
			return this;
		}

		/** Internal setter for {@link #getCssClasses()} without chain call utility. */
		protected final void internalSetCssClasses(String value) {
			_cssClasses = value;
		}

		@Override
		public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			writeContent(out);
		}

		@Override
		protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
			super.writeFields(out);
			out.name(TYPE__PROP);
			getType().writeTo(out);
			out.name(ID__PROP);
			out.value(getId());
			out.name(LABEL__PROP);
			out.value(getLabel());
			out.name(ICON__PROP);
			out.value(getIcon());
			out.name(DISABLED__PROP);
			out.value(isDisabled());
			out.name(ACTIVE__PROP);
			out.value(isActive());
			out.name(CSS_CLASSES__PROP);
			out.value(getCssClasses());
		}

		@Override
		protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
			switch (field) {
				case TYPE__PROP: setType(com.top_logic.layout.react.state.MenuState.EntryType.readEntryType(in)); break;
				case ID__PROP: setId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
				case LABEL__PROP: setLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
				case ICON__PROP: setIcon(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
				case DISABLED__PROP: setDisabled(in.nextBoolean()); break;
				case ACTIVE__PROP: setActive(in.nextBoolean()); break;
				case CSS_CLASSES__PROP: setCssClasses(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
				default: super.readField(in, field);
			}
		}

	}

	private boolean _open = false;

	private String _anchorId = "";

	private int _anchorX = 0;

	private int _anchorY = 0;

	private final java.util.List<com.top_logic.layout.react.state.MenuState.Entry> _items = new java.util.ArrayList<>();

	/**
	 * Creates a {@link MenuState_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.state.MenuState#create()
	 */
	public MenuState_Impl() {
		super();
	}

	@Override
	public final boolean isOpen() {
		return _open;
	}

	@Override
	public com.top_logic.layout.react.state.MenuState setOpen(boolean value) {
		internalSetOpen(value);
		return this;
	}

	/** Internal setter for {@link #isOpen()} without chain call utility. */
	protected final void internalSetOpen(boolean value) {
		_open = value;
	}

	@Override
	public final String getAnchorId() {
		return _anchorId;
	}

	@Override
	public com.top_logic.layout.react.state.MenuState setAnchorId(String value) {
		internalSetAnchorId(value);
		return this;
	}

	/** Internal setter for {@link #getAnchorId()} without chain call utility. */
	protected final void internalSetAnchorId(String value) {
		_anchorId = value;
	}

	@Override
	public final int getAnchorX() {
		return _anchorX;
	}

	@Override
	public com.top_logic.layout.react.state.MenuState setAnchorX(int value) {
		internalSetAnchorX(value);
		return this;
	}

	/** Internal setter for {@link #getAnchorX()} without chain call utility. */
	protected final void internalSetAnchorX(int value) {
		_anchorX = value;
	}

	@Override
	public final int getAnchorY() {
		return _anchorY;
	}

	@Override
	public com.top_logic.layout.react.state.MenuState setAnchorY(int value) {
		internalSetAnchorY(value);
		return this;
	}

	/** Internal setter for {@link #getAnchorY()} without chain call utility. */
	protected final void internalSetAnchorY(int value) {
		_anchorY = value;
	}

	@Override
	public final java.util.List<com.top_logic.layout.react.state.MenuState.Entry> getItems() {
		return _items;
	}

	@Override
	public com.top_logic.layout.react.state.MenuState setItems(java.util.List<? extends com.top_logic.layout.react.state.MenuState.Entry> value) {
		internalSetItems(value);
		return this;
	}

	/** Internal setter for {@link #getItems()} without chain call utility. */
	protected final void internalSetItems(java.util.List<? extends com.top_logic.layout.react.state.MenuState.Entry> value) {
		if (value == null) throw new IllegalArgumentException("Property 'items' cannot be null.");
		_items.clear();
		_items.addAll(value);
	}

	@Override
	public com.top_logic.layout.react.state.MenuState addItem(com.top_logic.layout.react.state.MenuState.Entry value) {
		internalAddItem(value);
		return this;
	}

	/** Implementation of {@link #addItem(com.top_logic.layout.react.state.MenuState.Entry)} without chain call utility. */
	protected final void internalAddItem(com.top_logic.layout.react.state.MenuState.Entry value) {
		_items.add(value);
	}

	@Override
	public final void removeItem(com.top_logic.layout.react.state.MenuState.Entry value) {
		_items.remove(value);
	}

	@Override
	public com.top_logic.layout.react.state.MenuState setHidden(boolean value) {
		internalSetHidden(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.MenuState setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public String jsonType() {
		return MENU_STATE__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(OPEN__PROP);
		out.value(isOpen());
		out.name(ANCHOR_ID__PROP);
		out.value(getAnchorId());
		out.name(ANCHOR_X__PROP);
		out.value(getAnchorX());
		out.name(ANCHOR_Y__PROP);
		out.value(getAnchorY());
		out.name(ITEMS__PROP);
		out.beginArray();
		for (com.top_logic.layout.react.state.MenuState.Entry x : getItems()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case OPEN__PROP: setOpen(in.nextBoolean()); break;
			case ANCHOR_ID__PROP: setAnchorId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ANCHOR_X__PROP: setAnchorX(in.nextInt()); break;
			case ANCHOR_Y__PROP: setAnchorY(in.nextInt()); break;
			case ITEMS__PROP: {
				java.util.List<com.top_logic.layout.react.state.MenuState.Entry> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.layout.react.state.MenuState.Entry.readEntry(in));
				}
				in.endArray();
				setItems(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
