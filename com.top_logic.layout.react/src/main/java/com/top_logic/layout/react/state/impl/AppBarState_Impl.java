package com.top_logic.layout.react.state.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.state.AppBarState}.
 */
public class AppBarState_Impl extends com.top_logic.layout.react.state.impl.ControlState_Impl implements com.top_logic.layout.react.state.AppBarState {

	private String _title = "";

	private com.top_logic.layout.react.state.AppBarState.Variant _variant = com.top_logic.layout.react.state.AppBarState.Variant.FLAT;

	private com.top_logic.layout.react.state.ChildControl _leading = null;

	private final java.util.List<com.top_logic.layout.react.state.ChildControl> _children = new java.util.ArrayList<>();

	private com.top_logic.layout.react.state.ChildControl _actions = null;

	private com.top_logic.layout.react.state.ChildControl _trailing = null;

	/**
	 * Creates a {@link AppBarState_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.state.AppBarState#create()
	 */
	public AppBarState_Impl() {
		super();
	}

	@Override
	public final String getTitle() {
		return _title;
	}

	@Override
	public com.top_logic.layout.react.state.AppBarState setTitle(String value) {
		internalSetTitle(value);
		return this;
	}

	/** Internal setter for {@link #getTitle()} without chain call utility. */
	protected final void internalSetTitle(String value) {
		_title = value;
	}

	@Override
	public final com.top_logic.layout.react.state.AppBarState.Variant getVariant() {
		return _variant;
	}

	@Override
	public com.top_logic.layout.react.state.AppBarState setVariant(com.top_logic.layout.react.state.AppBarState.Variant value) {
		internalSetVariant(value);
		return this;
	}

	/** Internal setter for {@link #getVariant()} without chain call utility. */
	protected final void internalSetVariant(com.top_logic.layout.react.state.AppBarState.Variant value) {
		if (value == null) throw new IllegalArgumentException("Property 'variant' cannot be null.");
		_variant = value;
	}

	@Override
	public final com.top_logic.layout.react.state.ChildControl getLeading() {
		return _leading;
	}

	@Override
	public com.top_logic.layout.react.state.AppBarState setLeading(com.top_logic.layout.react.state.ChildControl value) {
		internalSetLeading(value);
		return this;
	}

	/** Internal setter for {@link #getLeading()} without chain call utility. */
	protected final void internalSetLeading(com.top_logic.layout.react.state.ChildControl value) {
		_leading = value;
	}

	@Override
	public final boolean hasLeading() {
		return _leading != null;
	}

	@Override
	public final java.util.List<com.top_logic.layout.react.state.ChildControl> getChildren() {
		return _children;
	}

	@Override
	public com.top_logic.layout.react.state.AppBarState setChildren(java.util.List<? extends com.top_logic.layout.react.state.ChildControl> value) {
		internalSetChildren(value);
		return this;
	}

	/** Internal setter for {@link #getChildren()} without chain call utility. */
	protected final void internalSetChildren(java.util.List<? extends com.top_logic.layout.react.state.ChildControl> value) {
		if (value == null) throw new IllegalArgumentException("Property 'children' cannot be null.");
		_children.clear();
		_children.addAll(value);
	}

	@Override
	public com.top_logic.layout.react.state.AppBarState addChildren(com.top_logic.layout.react.state.ChildControl value) {
		internalAddChildren(value);
		return this;
	}

	/** Implementation of {@link #addChildren(com.top_logic.layout.react.state.ChildControl)} without chain call utility. */
	protected final void internalAddChildren(com.top_logic.layout.react.state.ChildControl value) {
		_children.add(value);
	}

	@Override
	public final void removeChildren(com.top_logic.layout.react.state.ChildControl value) {
		_children.remove(value);
	}

	@Override
	public final com.top_logic.layout.react.state.ChildControl getActions() {
		return _actions;
	}

	@Override
	public com.top_logic.layout.react.state.AppBarState setActions(com.top_logic.layout.react.state.ChildControl value) {
		internalSetActions(value);
		return this;
	}

	/** Internal setter for {@link #getActions()} without chain call utility. */
	protected final void internalSetActions(com.top_logic.layout.react.state.ChildControl value) {
		_actions = value;
	}

	@Override
	public final boolean hasActions() {
		return _actions != null;
	}

	@Override
	public final com.top_logic.layout.react.state.ChildControl getTrailing() {
		return _trailing;
	}

	@Override
	public com.top_logic.layout.react.state.AppBarState setTrailing(com.top_logic.layout.react.state.ChildControl value) {
		internalSetTrailing(value);
		return this;
	}

	/** Internal setter for {@link #getTrailing()} without chain call utility. */
	protected final void internalSetTrailing(com.top_logic.layout.react.state.ChildControl value) {
		_trailing = value;
	}

	@Override
	public final boolean hasTrailing() {
		return _trailing != null;
	}

	@Override
	public com.top_logic.layout.react.state.AppBarState setHidden(boolean value) {
		internalSetHidden(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.AppBarState setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public String jsonType() {
		return APP_BAR_STATE__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(TITLE__PROP);
		out.value(getTitle());
		out.name(VARIANT__PROP);
		getVariant().writeTo(out);
		if (hasLeading()) {
			out.name(LEADING__PROP);
			getLeading().writeTo(out);
		}
		out.name(CHILDREN__PROP);
		out.beginArray();
		for (com.top_logic.layout.react.state.ChildControl x : getChildren()) {
			x.writeTo(out);
		}
		out.endArray();
		if (hasActions()) {
			out.name(ACTIONS__PROP);
			getActions().writeTo(out);
		}
		if (hasTrailing()) {
			out.name(TRAILING__PROP);
			getTrailing().writeTo(out);
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TITLE__PROP: setTitle(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case VARIANT__PROP: setVariant(com.top_logic.layout.react.state.AppBarState.Variant.readVariant(in)); break;
			case LEADING__PROP: setLeading(com.top_logic.layout.react.state.ChildControl.readChildControl(in)); break;
			case CHILDREN__PROP: {
				java.util.List<com.top_logic.layout.react.state.ChildControl> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.layout.react.state.ChildControl.readChildControl(in));
				}
				in.endArray();
				setChildren(newValue);
			}
			break;
			case ACTIONS__PROP: setActions(com.top_logic.layout.react.state.ChildControl.readChildControl(in)); break;
			case TRAILING__PROP: setTrailing(com.top_logic.layout.react.state.ChildControl.readChildControl(in)); break;
			default: super.readField(in, field);
		}
	}

}
