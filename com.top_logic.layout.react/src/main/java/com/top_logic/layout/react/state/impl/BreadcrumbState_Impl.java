package com.top_logic.layout.react.state.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.state.BreadcrumbState}.
 */
public class BreadcrumbState_Impl extends com.top_logic.layout.react.state.impl.ControlState_Impl implements com.top_logic.layout.react.state.BreadcrumbState {
	/**
	 * Implementation of {@link com.top_logic.layout.react.state.BreadcrumbState.Item}.
	 */
	public static class Item_Impl extends de.haumacher.msgbuf.data.AbstractDataObject implements com.top_logic.layout.react.state.BreadcrumbState.Item {

		private String _id = "";

		private String _label = "";

		/**
		 * Creates a {@link Item_Impl} instance.
		 *
		 * @see com.top_logic.layout.react.state.BreadcrumbState.Item#create()
		 */
		public Item_Impl() {
			super();
		}

		@Override
		public final String getId() {
			return _id;
		}

		@Override
		public com.top_logic.layout.react.state.BreadcrumbState.Item setId(String value) {
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
		public com.top_logic.layout.react.state.BreadcrumbState.Item setLabel(String value) {
			internalSetLabel(value);
			return this;
		}

		/** Internal setter for {@link #getLabel()} without chain call utility. */
		protected final void internalSetLabel(String value) {
			_label = value;
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
		}

		@Override
		protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
			switch (field) {
				case ID__PROP: setId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
				case LABEL__PROP: setLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
				default: super.readField(in, field);
			}
		}

	}

	private final java.util.List<com.top_logic.layout.react.state.BreadcrumbState.Item> _items = new java.util.ArrayList<>();

	/**
	 * Creates a {@link BreadcrumbState_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.state.BreadcrumbState#create()
	 */
	public BreadcrumbState_Impl() {
		super();
	}

	@Override
	public final java.util.List<com.top_logic.layout.react.state.BreadcrumbState.Item> getItems() {
		return _items;
	}

	@Override
	public com.top_logic.layout.react.state.BreadcrumbState setItems(java.util.List<? extends com.top_logic.layout.react.state.BreadcrumbState.Item> value) {
		internalSetItems(value);
		return this;
	}

	/** Internal setter for {@link #getItems()} without chain call utility. */
	protected final void internalSetItems(java.util.List<? extends com.top_logic.layout.react.state.BreadcrumbState.Item> value) {
		if (value == null) throw new IllegalArgumentException("Property 'items' cannot be null.");
		_items.clear();
		_items.addAll(value);
	}

	@Override
	public com.top_logic.layout.react.state.BreadcrumbState addItem(com.top_logic.layout.react.state.BreadcrumbState.Item value) {
		internalAddItem(value);
		return this;
	}

	/** Implementation of {@link #addItem(com.top_logic.layout.react.state.BreadcrumbState.Item)} without chain call utility. */
	protected final void internalAddItem(com.top_logic.layout.react.state.BreadcrumbState.Item value) {
		_items.add(value);
	}

	@Override
	public final void removeItem(com.top_logic.layout.react.state.BreadcrumbState.Item value) {
		_items.remove(value);
	}

	@Override
	public com.top_logic.layout.react.state.BreadcrumbState setHidden(boolean value) {
		internalSetHidden(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.BreadcrumbState setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public String jsonType() {
		return BREADCRUMB_STATE__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(ITEMS__PROP);
		out.beginArray();
		for (com.top_logic.layout.react.state.BreadcrumbState.Item x : getItems()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ITEMS__PROP: {
				java.util.List<com.top_logic.layout.react.state.BreadcrumbState.Item> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.layout.react.state.BreadcrumbState.Item.readItem(in));
				}
				in.endArray();
				setItems(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
