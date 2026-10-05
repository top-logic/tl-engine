package com.top_logic.layout.react.state;

/**
 * State of a navigation trail showing the current location in a hierarchy, the component
 * {@code TLBreadcrumb}.
 *
 * Every item before the last one leads back to its place: a click on it sends the command
 * {@code navigate} with the {@link Item#getId()} of the item as argument {@code itemId}. The last item
 * is the current location and leads nowhere.
 */
public interface BreadcrumbState extends com.top_logic.layout.react.state.ControlState {
	/**
	 * A place in the trail.
	 */
	public interface Item extends de.haumacher.msgbuf.data.DataObject {

		/**
		 * Creates a {@link com.top_logic.layout.react.state.BreadcrumbState.Item} instance.
		 */
		static com.top_logic.layout.react.state.BreadcrumbState.Item create() {
			return new com.top_logic.layout.react.state.impl.BreadcrumbState_Impl.Item_Impl();
		}

		/** Identifier for the {@link com.top_logic.layout.react.state.BreadcrumbState.Item} type in JSON format. */
		String ITEM__TYPE = "Item";

		/** @see #getId() */
		String ID__PROP = "id";

		/** @see #getLabel() */
		String LABEL__PROP = "label";

		/**
		 * The ID of the item, by which the client names it to the server.
		 */
		String getId();

		/**
		 * @see #getId()
		 */
		com.top_logic.layout.react.state.BreadcrumbState.Item setId(String value);

		/**
		 * The label of the item, plain text.
		 */
		String getLabel();

		/**
		 * @see #getLabel()
		 */
		com.top_logic.layout.react.state.BreadcrumbState.Item setLabel(String value);

		/** Reads a new instance from the given reader. */
		static com.top_logic.layout.react.state.BreadcrumbState.Item readItem(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
			com.top_logic.layout.react.state.impl.BreadcrumbState_Impl.Item_Impl result = new com.top_logic.layout.react.state.impl.BreadcrumbState_Impl.Item_Impl();
			result.readContent(in);
			return result;
		}

	}

	/**
	 * Creates a {@link com.top_logic.layout.react.state.BreadcrumbState} instance.
	 */
	static com.top_logic.layout.react.state.BreadcrumbState create() {
		return new com.top_logic.layout.react.state.impl.BreadcrumbState_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.state.BreadcrumbState} type in JSON format. */
	String BREADCRUMB_STATE__TYPE = "BreadcrumbState";

	/** @see #getItems() */
	String ITEMS__PROP = "items";

	/**
	 * The places from the outermost to the current location, which is the last item. Absent: none.
	 */
	java.util.List<com.top_logic.layout.react.state.BreadcrumbState.Item> getItems();

	/**
	 * @see #getItems()
	 */
	com.top_logic.layout.react.state.BreadcrumbState setItems(java.util.List<? extends com.top_logic.layout.react.state.BreadcrumbState.Item> value);

	/**
	 * Adds a value to the {@link #getItems()} list.
	 */
	com.top_logic.layout.react.state.BreadcrumbState addItem(com.top_logic.layout.react.state.BreadcrumbState.Item value);

	/**
	 * Removes a value from the {@link #getItems()} list.
	 */
	void removeItem(com.top_logic.layout.react.state.BreadcrumbState.Item value);

	@Override
	com.top_logic.layout.react.state.BreadcrumbState setHidden(boolean value);

	@Override
	com.top_logic.layout.react.state.BreadcrumbState setCssClass(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.state.BreadcrumbState readBreadcrumbState(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.state.impl.BreadcrumbState_Impl result = new com.top_logic.layout.react.state.impl.BreadcrumbState_Impl();
		result.readContent(in);
		return result;
	}

}
