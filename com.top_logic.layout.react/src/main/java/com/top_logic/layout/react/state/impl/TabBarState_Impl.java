package com.top_logic.layout.react.state.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.state.TabBarState}.
 */
public class TabBarState_Impl extends com.top_logic.layout.react.state.impl.ControlState_Impl implements com.top_logic.layout.react.state.TabBarState {
	/**
	 * Implementation of {@link com.top_logic.layout.react.state.TabBarState.Tab}.
	 */
	public static class Tab_Impl extends de.haumacher.msgbuf.data.AbstractDataObject implements com.top_logic.layout.react.state.TabBarState.Tab {

		private String _id = "";

		private String _label = "";

		private String _icon = "";

		/**
		 * Creates a {@link Tab_Impl} instance.
		 *
		 * @see com.top_logic.layout.react.state.TabBarState.Tab#create()
		 */
		public Tab_Impl() {
			super();
		}

		@Override
		public final String getId() {
			return _id;
		}

		@Override
		public com.top_logic.layout.react.state.TabBarState.Tab setId(String value) {
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
		public com.top_logic.layout.react.state.TabBarState.Tab setLabel(String value) {
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
		public com.top_logic.layout.react.state.TabBarState.Tab setIcon(String value) {
			internalSetIcon(value);
			return this;
		}

		/** Internal setter for {@link #getIcon()} without chain call utility. */
		protected final void internalSetIcon(String value) {
			_icon = value;
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
			out.name(LABEL__PROP);
			out.value(getLabel());
			out.name(ICON__PROP);
			out.value(getIcon());
		}

		@Override
		protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
			switch (field) {
				case ID__PROP: setId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
				case LABEL__PROP: setLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
				case ICON__PROP: setIcon(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
				default: super.readField(in, field);
			}
		}

	}

	private final java.util.List<com.top_logic.layout.react.state.TabBarState.Tab> _tabs = new java.util.ArrayList<>();

	private String _activeTabId = "";

	private com.top_logic.layout.react.state.ChildControl _activeContent = null;

	/**
	 * Creates a {@link TabBarState_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.state.TabBarState#create()
	 */
	public TabBarState_Impl() {
		super();
	}

	@Override
	public final java.util.List<com.top_logic.layout.react.state.TabBarState.Tab> getTabs() {
		return _tabs;
	}

	@Override
	public com.top_logic.layout.react.state.TabBarState setTabs(java.util.List<? extends com.top_logic.layout.react.state.TabBarState.Tab> value) {
		internalSetTabs(value);
		return this;
	}

	/** Internal setter for {@link #getTabs()} without chain call utility. */
	protected final void internalSetTabs(java.util.List<? extends com.top_logic.layout.react.state.TabBarState.Tab> value) {
		if (value == null) throw new IllegalArgumentException("Property 'tabs' cannot be null.");
		_tabs.clear();
		_tabs.addAll(value);
	}

	@Override
	public com.top_logic.layout.react.state.TabBarState addTab(com.top_logic.layout.react.state.TabBarState.Tab value) {
		internalAddTab(value);
		return this;
	}

	/** Implementation of {@link #addTab(com.top_logic.layout.react.state.TabBarState.Tab)} without chain call utility. */
	protected final void internalAddTab(com.top_logic.layout.react.state.TabBarState.Tab value) {
		_tabs.add(value);
	}

	@Override
	public final void removeTab(com.top_logic.layout.react.state.TabBarState.Tab value) {
		_tabs.remove(value);
	}

	@Override
	public final String getActiveTabId() {
		return _activeTabId;
	}

	@Override
	public com.top_logic.layout.react.state.TabBarState setActiveTabId(String value) {
		internalSetActiveTabId(value);
		return this;
	}

	/** Internal setter for {@link #getActiveTabId()} without chain call utility. */
	protected final void internalSetActiveTabId(String value) {
		_activeTabId = value;
	}

	@Override
	public final com.top_logic.layout.react.state.ChildControl getActiveContent() {
		return _activeContent;
	}

	@Override
	public com.top_logic.layout.react.state.TabBarState setActiveContent(com.top_logic.layout.react.state.ChildControl value) {
		internalSetActiveContent(value);
		return this;
	}

	/** Internal setter for {@link #getActiveContent()} without chain call utility. */
	protected final void internalSetActiveContent(com.top_logic.layout.react.state.ChildControl value) {
		_activeContent = value;
	}

	@Override
	public final boolean hasActiveContent() {
		return _activeContent != null;
	}

	@Override
	public com.top_logic.layout.react.state.TabBarState setHidden(boolean value) {
		internalSetHidden(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.TabBarState setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public String jsonType() {
		return TAB_BAR_STATE__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(TABS__PROP);
		out.beginArray();
		for (com.top_logic.layout.react.state.TabBarState.Tab x : getTabs()) {
			x.writeTo(out);
		}
		out.endArray();
		out.name(ACTIVE_TAB_ID__PROP);
		out.value(getActiveTabId());
		if (hasActiveContent()) {
			out.name(ACTIVE_CONTENT__PROP);
			getActiveContent().writeTo(out);
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TABS__PROP: {
				java.util.List<com.top_logic.layout.react.state.TabBarState.Tab> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.layout.react.state.TabBarState.Tab.readTab(in));
				}
				in.endArray();
				setTabs(newValue);
			}
			break;
			case ACTIVE_TAB_ID__PROP: setActiveTabId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ACTIVE_CONTENT__PROP: setActiveContent(com.top_logic.layout.react.state.ChildControl.readChildControl(in)); break;
			default: super.readField(in, field);
		}
	}

}
