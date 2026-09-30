package com.top_logic.layout.react.state;

/**
 * State of a tab strip above the content of the selected tab, the component {@code TLTabBar}.
 *
 * A click on a tab sends the command {@code selectTab} with the {@link Tab#getId()} as argument
 * {@code tabId}.
 */
public interface TabBarState extends com.top_logic.layout.react.state.ControlState {
	/**
	 * A tab of the strip.
	 */
	public interface Tab extends de.haumacher.msgbuf.data.DataObject {

		/**
		 * Creates a {@link com.top_logic.layout.react.state.TabBarState.Tab} instance.
		 */
		static com.top_logic.layout.react.state.TabBarState.Tab create() {
			return new com.top_logic.layout.react.state.impl.TabBarState_Impl.Tab_Impl();
		}

		/** Identifier for the {@link com.top_logic.layout.react.state.TabBarState.Tab} type in JSON format. */
		String TAB__TYPE = "Tab";

		/** @see #getId() */
		String ID__PROP = "id";

		/** @see #getLabel() */
		String LABEL__PROP = "label";

		/** @see #getIcon() */
		String ICON__PROP = "icon";

		/**
		 * The ID of the tab.
		 */
		String getId();

		/**
		 * @see #getId()
		 */
		com.top_logic.layout.react.state.TabBarState.Tab setId(String value);

		/**
		 * The label of the tab.
		 */
		String getLabel();

		/**
		 * @see #getLabel()
		 */
		com.top_logic.layout.react.state.TabBarState.Tab setLabel(String value);

		/**
		 * The icon of the tab, the encoded form of a theme image.
		 */
		String getIcon();

		/**
		 * @see #getIcon()
		 */
		com.top_logic.layout.react.state.TabBarState.Tab setIcon(String value);

		/** Reads a new instance from the given reader. */
		static com.top_logic.layout.react.state.TabBarState.Tab readTab(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
			com.top_logic.layout.react.state.impl.TabBarState_Impl.Tab_Impl result = new com.top_logic.layout.react.state.impl.TabBarState_Impl.Tab_Impl();
			result.readContent(in);
			return result;
		}

	}

	/**
	 * Creates a {@link com.top_logic.layout.react.state.TabBarState} instance.
	 */
	static com.top_logic.layout.react.state.TabBarState create() {
		return new com.top_logic.layout.react.state.impl.TabBarState_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.state.TabBarState} type in JSON format. */
	String TAB_BAR_STATE__TYPE = "TabBarState";

	/** @see #getTabs() */
	String TABS__PROP = "tabs";

	/** @see #getActiveTabId() */
	String ACTIVE_TAB_ID__PROP = "activeTabId";

	/** @see #getActiveContent() */
	String ACTIVE_CONTENT__PROP = "activeContent";

	/**
	 * The tabs, in the order of the strip.
	 */
	java.util.List<com.top_logic.layout.react.state.TabBarState.Tab> getTabs();

	/**
	 * @see #getTabs()
	 */
	com.top_logic.layout.react.state.TabBarState setTabs(java.util.List<? extends com.top_logic.layout.react.state.TabBarState.Tab> value);

	/**
	 * Adds a value to the {@link #getTabs()} list.
	 */
	com.top_logic.layout.react.state.TabBarState addTab(com.top_logic.layout.react.state.TabBarState.Tab value);

	/**
	 * Removes a value from the {@link #getTabs()} list.
	 */
	void removeTab(com.top_logic.layout.react.state.TabBarState.Tab value);

	/**
	 * The {@link Tab#getId()} of the selected tab.
	 */
	String getActiveTabId();

	/**
	 * @see #getActiveTabId()
	 */
	com.top_logic.layout.react.state.TabBarState setActiveTabId(String value);

	/**
	 * The content of the selected tab.
	 */
	com.top_logic.layout.react.state.ChildControl getActiveContent();

	/**
	 * @see #getActiveContent()
	 */
	com.top_logic.layout.react.state.TabBarState setActiveContent(com.top_logic.layout.react.state.ChildControl value);

	/**
	 * Checks, whether {@link #getActiveContent()} has a value.
	 */
	boolean hasActiveContent();

	@Override
	com.top_logic.layout.react.state.TabBarState setHidden(boolean value);

	@Override
	com.top_logic.layout.react.state.TabBarState setCssClass(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.state.TabBarState readTabBarState(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.state.impl.TabBarState_Impl result = new com.top_logic.layout.react.state.impl.TabBarState_Impl();
		result.readContent(in);
		return result;
	}

}
