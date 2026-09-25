package com.top_logic.layout.react.state.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.state.WindowState}.
 */
public class WindowState_Impl extends com.top_logic.layout.react.state.impl.ControlState_Impl implements com.top_logic.layout.react.state.WindowState {

	private String _title = "";

	private String _width = "";

	private String _height = "";

	private String _minHeight = "";

	private boolean _resizable = false;

	private boolean _closable = false;

	private com.top_logic.layout.react.state.ChildControl _child = null;

	private com.top_logic.layout.react.state.ChildControl _toolbar = null;

	private com.top_logic.layout.react.state.ChildControl _footer = null;

	private final java.util.List<com.top_logic.layout.react.state.ChildControl> _toolbarButtons = new java.util.ArrayList<>();

	/**
	 * Creates a {@link WindowState_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.state.WindowState#create()
	 */
	public WindowState_Impl() {
		super();
	}

	@Override
	public final String getTitle() {
		return _title;
	}

	@Override
	public com.top_logic.layout.react.state.WindowState setTitle(String value) {
		internalSetTitle(value);
		return this;
	}

	/** Internal setter for {@link #getTitle()} without chain call utility. */
	protected final void internalSetTitle(String value) {
		_title = value;
	}

	@Override
	public final String getWidth() {
		return _width;
	}

	@Override
	public com.top_logic.layout.react.state.WindowState setWidth(String value) {
		internalSetWidth(value);
		return this;
	}

	/** Internal setter for {@link #getWidth()} without chain call utility. */
	protected final void internalSetWidth(String value) {
		_width = value;
	}

	@Override
	public final String getHeight() {
		return _height;
	}

	@Override
	public com.top_logic.layout.react.state.WindowState setHeight(String value) {
		internalSetHeight(value);
		return this;
	}

	/** Internal setter for {@link #getHeight()} without chain call utility. */
	protected final void internalSetHeight(String value) {
		_height = value;
	}

	@Override
	public final String getMinHeight() {
		return _minHeight;
	}

	@Override
	public com.top_logic.layout.react.state.WindowState setMinHeight(String value) {
		internalSetMinHeight(value);
		return this;
	}

	/** Internal setter for {@link #getMinHeight()} without chain call utility. */
	protected final void internalSetMinHeight(String value) {
		_minHeight = value;
	}

	@Override
	public final boolean isResizable() {
		return _resizable;
	}

	@Override
	public com.top_logic.layout.react.state.WindowState setResizable(boolean value) {
		internalSetResizable(value);
		return this;
	}

	/** Internal setter for {@link #isResizable()} without chain call utility. */
	protected final void internalSetResizable(boolean value) {
		_resizable = value;
	}

	@Override
	public final boolean isClosable() {
		return _closable;
	}

	@Override
	public com.top_logic.layout.react.state.WindowState setClosable(boolean value) {
		internalSetClosable(value);
		return this;
	}

	/** Internal setter for {@link #isClosable()} without chain call utility. */
	protected final void internalSetClosable(boolean value) {
		_closable = value;
	}

	@Override
	public final com.top_logic.layout.react.state.ChildControl getChild() {
		return _child;
	}

	@Override
	public com.top_logic.layout.react.state.WindowState setChild(com.top_logic.layout.react.state.ChildControl value) {
		internalSetChild(value);
		return this;
	}

	/** Internal setter for {@link #getChild()} without chain call utility. */
	protected final void internalSetChild(com.top_logic.layout.react.state.ChildControl value) {
		_child = value;
	}

	@Override
	public final boolean hasChild() {
		return _child != null;
	}

	@Override
	public final com.top_logic.layout.react.state.ChildControl getToolbar() {
		return _toolbar;
	}

	@Override
	public com.top_logic.layout.react.state.WindowState setToolbar(com.top_logic.layout.react.state.ChildControl value) {
		internalSetToolbar(value);
		return this;
	}

	/** Internal setter for {@link #getToolbar()} without chain call utility. */
	protected final void internalSetToolbar(com.top_logic.layout.react.state.ChildControl value) {
		_toolbar = value;
	}

	@Override
	public final boolean hasToolbar() {
		return _toolbar != null;
	}

	@Override
	public final com.top_logic.layout.react.state.ChildControl getFooter() {
		return _footer;
	}

	@Override
	public com.top_logic.layout.react.state.WindowState setFooter(com.top_logic.layout.react.state.ChildControl value) {
		internalSetFooter(value);
		return this;
	}

	/** Internal setter for {@link #getFooter()} without chain call utility. */
	protected final void internalSetFooter(com.top_logic.layout.react.state.ChildControl value) {
		_footer = value;
	}

	@Override
	public final boolean hasFooter() {
		return _footer != null;
	}

	@Override
	public final java.util.List<com.top_logic.layout.react.state.ChildControl> getToolbarButtons() {
		return _toolbarButtons;
	}

	@Override
	public com.top_logic.layout.react.state.WindowState setToolbarButtons(java.util.List<? extends com.top_logic.layout.react.state.ChildControl> value) {
		internalSetToolbarButtons(value);
		return this;
	}

	/** Internal setter for {@link #getToolbarButtons()} without chain call utility. */
	protected final void internalSetToolbarButtons(java.util.List<? extends com.top_logic.layout.react.state.ChildControl> value) {
		if (value == null) throw new IllegalArgumentException("Property 'toolbarButtons' cannot be null.");
		_toolbarButtons.clear();
		_toolbarButtons.addAll(value);
	}

	@Override
	public com.top_logic.layout.react.state.WindowState addToolbarButton(com.top_logic.layout.react.state.ChildControl value) {
		internalAddToolbarButton(value);
		return this;
	}

	/** Implementation of {@link #addToolbarButton(com.top_logic.layout.react.state.ChildControl)} without chain call utility. */
	protected final void internalAddToolbarButton(com.top_logic.layout.react.state.ChildControl value) {
		_toolbarButtons.add(value);
	}

	@Override
	public final void removeToolbarButton(com.top_logic.layout.react.state.ChildControl value) {
		_toolbarButtons.remove(value);
	}

	@Override
	public com.top_logic.layout.react.state.WindowState setHidden(boolean value) {
		internalSetHidden(value);
		return this;
	}

	@Override
	public com.top_logic.layout.react.state.WindowState setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public String jsonType() {
		return WINDOW_STATE__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(TITLE__PROP);
		out.value(getTitle());
		out.name(WIDTH__PROP);
		out.value(getWidth());
		out.name(HEIGHT__PROP);
		out.value(getHeight());
		out.name(MIN_HEIGHT__PROP);
		out.value(getMinHeight());
		out.name(RESIZABLE__PROP);
		out.value(isResizable());
		out.name(CLOSABLE__PROP);
		out.value(isClosable());
		if (hasChild()) {
			out.name(CHILD__PROP);
			getChild().writeTo(out);
		}
		if (hasToolbar()) {
			out.name(TOOLBAR__PROP);
			getToolbar().writeTo(out);
		}
		if (hasFooter()) {
			out.name(FOOTER__PROP);
			getFooter().writeTo(out);
		}
		out.name(TOOLBAR_BUTTONS__PROP);
		out.beginArray();
		for (com.top_logic.layout.react.state.ChildControl x : getToolbarButtons()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TITLE__PROP: setTitle(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case WIDTH__PROP: setWidth(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case HEIGHT__PROP: setHeight(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case MIN_HEIGHT__PROP: setMinHeight(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case RESIZABLE__PROP: setResizable(in.nextBoolean()); break;
			case CLOSABLE__PROP: setClosable(in.nextBoolean()); break;
			case CHILD__PROP: setChild(com.top_logic.layout.react.state.ChildControl.readChildControl(in)); break;
			case TOOLBAR__PROP: setToolbar(com.top_logic.layout.react.state.ChildControl.readChildControl(in)); break;
			case FOOTER__PROP: setFooter(com.top_logic.layout.react.state.ChildControl.readChildControl(in)); break;
			case TOOLBAR_BUTTONS__PROP: {
				java.util.List<com.top_logic.layout.react.state.ChildControl> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.layout.react.state.ChildControl.readChildControl(in));
				}
				in.endArray();
				setToolbarButtons(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
