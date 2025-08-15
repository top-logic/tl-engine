package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.Diagram}.
 */
public class Diagram_Impl extends com.top_logic.graphic.flow.data.impl.Widget_Impl implements com.top_logic.graphic.flow.data.Diagram {

	private com.top_logic.graphic.flow.data.Box _root = null;

	private final java.util.List<com.top_logic.graphic.flow.data.SelectableBox> _selection = new de.haumacher.msgbuf.util.ReferenceList<com.top_logic.graphic.flow.data.SelectableBox>() {
		@Override
		protected void beforeAdd(int index, com.top_logic.graphic.flow.data.SelectableBox element) {
			_listener.beforeAdd(Diagram_Impl.this, SELECTION__PROP, index, element);
		}

		@Override
		protected void afterRemove(int index, com.top_logic.graphic.flow.data.SelectableBox element) {
			_listener.afterRemove(Diagram_Impl.this, SELECTION__PROP, index, element);
		}

		@Override
		protected void afterChanged() {
			_listener.afterChanged(Diagram_Impl.this, SELECTION__PROP);
		}
	};

	private boolean _multiSelect = false;

	private transient com.top_logic.graphic.blocks.svg.event.Registration _clickHandler = null;

	private transient com.top_logic.graphic.flow.callback.DiagramContext _context = null;

	private double _viewBoxX = 0.0d;

	private double _viewBoxY = 0.0d;

	private double _viewBoxWidth = 0.0d;

	private double _viewBoxHeight = 0.0d;

	/**
	 * Creates a {@link Diagram_Impl} instance.
	 *
	 * @see com.top_logic.graphic.flow.data.Diagram#create()
	 */
	public Diagram_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.DIAGRAM;
	}

	@Override
	public final com.top_logic.graphic.flow.data.Box getRoot() {
		return _root;
	}

	@Override
	public com.top_logic.graphic.flow.data.Diagram setRoot(com.top_logic.graphic.flow.data.Box value) {
		internalSetRoot(value);
		return this;
	}

	/** Internal setter for {@link #getRoot()} without chain call utility. */
	protected final void internalSetRoot(com.top_logic.graphic.flow.data.Box value) {
		com.top_logic.graphic.flow.data.impl.Box_Impl before = (com.top_logic.graphic.flow.data.impl.Box_Impl) _root;
		com.top_logic.graphic.flow.data.impl.Box_Impl after = (com.top_logic.graphic.flow.data.impl.Box_Impl) value;
		if (after != null) {
			com.top_logic.graphic.flow.data.Widget oldContainer = after.getParent();
			if (oldContainer != null && oldContainer != this) {
				throw new IllegalStateException("Object may not be part of two different containers.");
			}
		}
		_listener.beforeSet(this, ROOT__PROP, value);
		if (before != null) {
			before.internalSetParent(null);
		}
		_root = value;
		if (after != null) {
			after.internalSetParent(this);
		}
		_listener.afterChanged(this, ROOT__PROP);
	}

	@Override
	public final boolean hasRoot() {
		return _root != null;
	}

	@Override
	public final java.util.List<com.top_logic.graphic.flow.data.SelectableBox> getSelection() {
		return _selection;
	}

	@Override
	public com.top_logic.graphic.flow.data.Diagram setSelection(java.util.List<? extends com.top_logic.graphic.flow.data.SelectableBox> value) {
		internalSetSelection(value);
		return this;
	}

	/** Internal setter for {@link #getSelection()} without chain call utility. */
	protected final void internalSetSelection(java.util.List<? extends com.top_logic.graphic.flow.data.SelectableBox> value) {
		if (value == null) throw new IllegalArgumentException("Property 'selection' cannot be null.");
		_selection.clear();
		_selection.addAll(value);
	}

	@Override
	public com.top_logic.graphic.flow.data.Diagram addSelection(com.top_logic.graphic.flow.data.SelectableBox value) {
		internalAddSelection(value);
		return this;
	}

	/** Implementation of {@link #addSelection(com.top_logic.graphic.flow.data.SelectableBox)} without chain call utility. */
	protected final void internalAddSelection(com.top_logic.graphic.flow.data.SelectableBox value) {
		_selection.add(value);
	}

	@Override
	public final void removeSelection(com.top_logic.graphic.flow.data.SelectableBox value) {
		_selection.remove(value);
	}

	@Override
	public final boolean isMultiSelect() {
		return _multiSelect;
	}

	@Override
	public com.top_logic.graphic.flow.data.Diagram setMultiSelect(boolean value) {
		internalSetMultiSelect(value);
		return this;
	}

	/** Internal setter for {@link #isMultiSelect()} without chain call utility. */
	protected final void internalSetMultiSelect(boolean value) {
		_listener.beforeSet(this, MULTI_SELECT__PROP, value);
		_multiSelect = value;
		_listener.afterChanged(this, MULTI_SELECT__PROP);
	}

	@Override
	public final com.top_logic.graphic.blocks.svg.event.Registration getClickHandler() {
		return _clickHandler;
	}

	@Override
	public com.top_logic.graphic.flow.data.Diagram setClickHandler(com.top_logic.graphic.blocks.svg.event.Registration value) {
		internalSetClickHandler(value);
		return this;
	}

	/** Internal setter for {@link #getClickHandler()} without chain call utility. */
	protected final void internalSetClickHandler(com.top_logic.graphic.blocks.svg.event.Registration value) {
		_listener.beforeSet(this, CLICK_HANDLER__PROP, value);
		_clickHandler = value;
		_listener.afterChanged(this, CLICK_HANDLER__PROP);
	}

	@Override
	public final boolean hasClickHandler() {
		return _clickHandler != null;
	}

	@Override
	public final com.top_logic.graphic.flow.callback.DiagramContext getContext() {
		return _context;
	}

	@Override
	public com.top_logic.graphic.flow.data.Diagram setContext(com.top_logic.graphic.flow.callback.DiagramContext value) {
		internalSetContext(value);
		return this;
	}

	/** Internal setter for {@link #getContext()} without chain call utility. */
	protected final void internalSetContext(com.top_logic.graphic.flow.callback.DiagramContext value) {
		_listener.beforeSet(this, CONTEXT__PROP, value);
		_context = value;
		_listener.afterChanged(this, CONTEXT__PROP);
	}

	@Override
	public final boolean hasContext() {
		return _context != null;
	}

	@Override
	public final double getViewBoxX() {
		return _viewBoxX;
	}

	@Override
	public com.top_logic.graphic.flow.data.Diagram setViewBoxX(double value) {
		internalSetViewBoxX(value);
		return this;
	}

	/** Internal setter for {@link #getViewBoxX()} without chain call utility. */
	protected final void internalSetViewBoxX(double value) {
		_listener.beforeSet(this, VIEW_BOX_X__PROP, value);
		_viewBoxX = value;
		_listener.afterChanged(this, VIEW_BOX_X__PROP);
	}

	@Override
	public final double getViewBoxY() {
		return _viewBoxY;
	}

	@Override
	public com.top_logic.graphic.flow.data.Diagram setViewBoxY(double value) {
		internalSetViewBoxY(value);
		return this;
	}

	/** Internal setter for {@link #getViewBoxY()} without chain call utility. */
	protected final void internalSetViewBoxY(double value) {
		_listener.beforeSet(this, VIEW_BOX_Y__PROP, value);
		_viewBoxY = value;
		_listener.afterChanged(this, VIEW_BOX_Y__PROP);
	}

	@Override
	public final double getViewBoxWidth() {
		return _viewBoxWidth;
	}

	@Override
	public com.top_logic.graphic.flow.data.Diagram setViewBoxWidth(double value) {
		internalSetViewBoxWidth(value);
		return this;
	}

	/** Internal setter for {@link #getViewBoxWidth()} without chain call utility. */
	protected final void internalSetViewBoxWidth(double value) {
		_listener.beforeSet(this, VIEW_BOX_WIDTH__PROP, value);
		_viewBoxWidth = value;
		_listener.afterChanged(this, VIEW_BOX_WIDTH__PROP);
	}

	@Override
	public final double getViewBoxHeight() {
		return _viewBoxHeight;
	}

	@Override
	public com.top_logic.graphic.flow.data.Diagram setViewBoxHeight(double value) {
		internalSetViewBoxHeight(value);
		return this;
	}

	/** Internal setter for {@link #getViewBoxHeight()} without chain call utility. */
	protected final void internalSetViewBoxHeight(double value) {
		_listener.beforeSet(this, VIEW_BOX_HEIGHT__PROP, value);
		_viewBoxHeight = value;
		_listener.afterChanged(this, VIEW_BOX_HEIGHT__PROP);
	}

	@Override
	public com.top_logic.graphic.flow.data.Diagram setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Diagram setUserObject(java.lang.Object value) {
		internalSetUserObject(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Diagram setClientId(String value) {
		internalSetClientId(value);
		return this;
	}

	@Override
	public String jsonType() {
		return DIAGRAM__TYPE;
	}

	private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(
		java.util.Arrays.asList(
			ROOT__PROP, 
			SELECTION__PROP, 
			MULTI_SELECT__PROP, 
			CLICK_HANDLER__PROP, 
			CONTEXT__PROP, 
			VIEW_BOX_X__PROP, 
			VIEW_BOX_Y__PROP, 
			VIEW_BOX_WIDTH__PROP, 
			VIEW_BOX_HEIGHT__PROP));

	private static java.util.Set<String> TRANSIENT_PROPERTIES = java.util.Collections.unmodifiableSet(new java.util.HashSet<>(
			java.util.Arrays.asList(
				CLICK_HANDLER__PROP, 
				CONTEXT__PROP)));

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
			case ROOT__PROP: return getRoot();
			case SELECTION__PROP: return getSelection();
			case MULTI_SELECT__PROP: return isMultiSelect();
			case CLICK_HANDLER__PROP: return getClickHandler();
			case CONTEXT__PROP: return getContext();
			case VIEW_BOX_X__PROP: return getViewBoxX();
			case VIEW_BOX_Y__PROP: return getViewBoxY();
			case VIEW_BOX_WIDTH__PROP: return getViewBoxWidth();
			case VIEW_BOX_HEIGHT__PROP: return getViewBoxHeight();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ROOT__PROP: internalSetRoot((com.top_logic.graphic.flow.data.Box) value); break;
			case SELECTION__PROP: internalSetSelection(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.graphic.flow.data.SelectableBox.class, value)); break;
			case MULTI_SELECT__PROP: internalSetMultiSelect((boolean) value); break;
			case CLICK_HANDLER__PROP: internalSetClickHandler((com.top_logic.graphic.blocks.svg.event.Registration) value); break;
			case CONTEXT__PROP: internalSetContext((com.top_logic.graphic.flow.callback.DiagramContext) value); break;
			case VIEW_BOX_X__PROP: internalSetViewBoxX((double) value); break;
			case VIEW_BOX_Y__PROP: internalSetViewBoxY((double) value); break;
			case VIEW_BOX_WIDTH__PROP: internalSetViewBoxWidth((double) value); break;
			case VIEW_BOX_HEIGHT__PROP: internalSetViewBoxHeight((double) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		if (hasRoot()) {
			out.name(ROOT__PROP);
			getRoot().writeTo(scope, out);
		}
		out.name(SELECTION__PROP);
		out.beginArray();
		for (com.top_logic.graphic.flow.data.SelectableBox x : getSelection()) {
			x.writeTo(scope, out);
		}
		out.endArray();
		out.name(MULTI_SELECT__PROP);
		out.value(isMultiSelect());
		out.name(VIEW_BOX_X__PROP);
		out.value(getViewBoxX());
		out.name(VIEW_BOX_Y__PROP);
		out.value(getViewBoxY());
		out.name(VIEW_BOX_WIDTH__PROP);
		out.value(getViewBoxWidth());
		out.name(VIEW_BOX_HEIGHT__PROP);
		out.value(getViewBoxHeight());
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case ROOT__PROP: {
				if (hasRoot()) {
					getRoot().writeTo(scope, out);
				} else {
					out.nullValue();
				}
				break;
			}
			case SELECTION__PROP: {
				out.beginArray();
				for (com.top_logic.graphic.flow.data.SelectableBox x : getSelection()) {
					x.writeTo(scope, out);
				}
				out.endArray();
				break;
			}
			case MULTI_SELECT__PROP: {
				out.value(isMultiSelect());
				break;
			}
			case CLICK_HANDLER__PROP: {
				if (hasClickHandler()) {
				} else {
					out.nullValue();
				}
				break;
			}
			case CONTEXT__PROP: {
				if (hasContext()) {
				} else {
					out.nullValue();
				}
				break;
			}
			case VIEW_BOX_X__PROP: {
				out.value(getViewBoxX());
				break;
			}
			case VIEW_BOX_Y__PROP: {
				out.value(getViewBoxY());
				break;
			}
			case VIEW_BOX_WIDTH__PROP: {
				out.value(getViewBoxWidth());
				break;
			}
			case VIEW_BOX_HEIGHT__PROP: {
				out.value(getViewBoxHeight());
				break;
			}
			default: super.writeFieldValue(scope, out, field);
		}
	}

	@Override
	public void readField(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ROOT__PROP: setRoot(com.top_logic.graphic.flow.data.Box.readBox(scope, in)); break;
			case SELECTION__PROP: {
				java.util.List<com.top_logic.graphic.flow.data.SelectableBox> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.graphic.flow.data.SelectableBox.readSelectableBox(scope, in));
				}
				in.endArray();
				setSelection(newValue);
			}
			break;
			case MULTI_SELECT__PROP: setMultiSelect(in.nextBoolean()); break;
			case VIEW_BOX_X__PROP: setViewBoxX(in.nextDouble()); break;
			case VIEW_BOX_Y__PROP: setViewBoxY(in.nextDouble()); break;
			case VIEW_BOX_WIDTH__PROP: setViewBoxWidth(in.nextDouble()); break;
			case VIEW_BOX_HEIGHT__PROP: setViewBoxHeight(in.nextDouble()); break;
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public void writeElement(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field, Object element) throws java.io.IOException {
		switch (field) {
			case SELECTION__PROP: {
				((com.top_logic.graphic.flow.data.SelectableBox) element).writeTo(scope, out);
				break;
			}
			default: super.writeElement(scope, out, field, element);
		}
	}

	@Override
	public Object readElement(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case SELECTION__PROP: {
				return com.top_logic.graphic.flow.data.SelectableBox.readSelectableBox(scope, in);
			}
			default: return super.readElement(scope, in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.Widget.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
