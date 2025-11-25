package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.ContextMenu}.
 */
public class ContextMenu_Impl extends com.top_logic.graphic.flow.data.impl.Decoration_Impl implements com.top_logic.graphic.flow.data.ContextMenu {

	private transient com.top_logic.graphic.flow.callback.DiagramContextMenuProvider _menuProvider = null;

	/**
	 * Creates a {@link ContextMenu_Impl} instance.
	 *
	 * @see com.top_logic.graphic.flow.data.ContextMenu#create()
	 */
	public ContextMenu_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.CONTEXT_MENU;
	}

	@Override
	public final com.top_logic.graphic.flow.callback.DiagramContextMenuProvider getMenuProvider() {
		return _menuProvider;
	}

	@Override
	public com.top_logic.graphic.flow.data.ContextMenu setMenuProvider(com.top_logic.graphic.flow.callback.DiagramContextMenuProvider value) {
		internalSetMenuProvider(value);
		return this;
	}

	/** Internal setter for {@link #getMenuProvider()} without chain call utility. */
	protected final void internalSetMenuProvider(com.top_logic.graphic.flow.callback.DiagramContextMenuProvider value) {
		_listener.beforeSet(this, MENU_PROVIDER__PROP, value);
		_menuProvider = value;
		_listener.afterChanged(this, MENU_PROVIDER__PROP);
	}

	@Override
	public final boolean hasMenuProvider() {
		return _menuProvider != null;
	}

	@Override
	public com.top_logic.graphic.flow.data.ContextMenu setContent(com.top_logic.graphic.flow.data.Box value) {
		internalSetContent(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.ContextMenu setX(double value) {
		internalSetX(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.ContextMenu setY(double value) {
		internalSetY(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.ContextMenu setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.ContextMenu setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.ContextMenu setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.ContextMenu setUserObject(java.lang.Object value) {
		internalSetUserObject(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.ContextMenu setClientId(String value) {
		internalSetClientId(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.ContextMenu setRenderInfo(java.lang.Object value) {
		internalSetRenderInfo(value);
		return this;
	}

	@Override
	public String jsonType() {
		return CONTEXT_MENU__TYPE;
	}

	@SuppressWarnings("hiding")
	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			MENU_PROVIDER__PROP);
		java.util.List<String> tmp = new java.util.ArrayList<>();
		tmp.addAll(com.top_logic.graphic.flow.data.impl.Decoration_Impl.PROPERTIES);
		tmp.addAll(local);
		PROPERTIES = java.util.Collections.unmodifiableList(tmp);
	}

	@SuppressWarnings("hiding")
	static final java.util.Set<String> TRANSIENT_PROPERTIES;
	static {
		java.util.HashSet<String> tmp = new java.util.HashSet<>();
		tmp.addAll(com.top_logic.graphic.flow.data.impl.Decoration_Impl.TRANSIENT_PROPERTIES);
		tmp.addAll(java.util.Arrays.asList(
				MENU_PROVIDER__PROP));
		TRANSIENT_PROPERTIES = java.util.Collections.unmodifiableSet(tmp);
	}

	@Override
	public java.util.List<String> properties() {
		return PROPERTIES;
	}

	@Override
	public java.util.Set<String> transientProperties() {
		return TRANSIENT_PROPERTIES;
	}

	@Override
	public Object get(String field) {
		switch (field) {
			case MENU_PROVIDER__PROP: return getMenuProvider();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case MENU_PROVIDER__PROP: internalSetMenuProvider((com.top_logic.graphic.flow.callback.DiagramContextMenuProvider) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case MENU_PROVIDER__PROP: {
				if (hasMenuProvider()) {
				} else {
					out.nullValue();
				}
				break;
			}
			default: super.writeFieldValue(scope, out, field);
		}
	}

	@Override
	public void readField(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.Decoration.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
