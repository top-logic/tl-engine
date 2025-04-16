package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.Diagram}.
 */
public class Diagram_Impl extends com.top_logic.graphic.flow.data.impl.Widget_Impl implements com.top_logic.graphic.flow.data.Diagram {

	private com.top_logic.graphic.flow.data.Box _root = null;

	private final java.util.List<com.top_logic.graphic.flow.data.Widget> _selection = new de.haumacher.msgbuf.util.ReferenceList<com.top_logic.graphic.flow.data.Widget>() {
		@Override
		protected void beforeAdd(int index, com.top_logic.graphic.flow.data.Widget element) {
			_listener.beforeAdd(Diagram_Impl.this, SELECTION__PROP, index, element);
		}

		@Override
		protected void afterRemove(int index, com.top_logic.graphic.flow.data.Widget element) {
			_listener.afterRemove(Diagram_Impl.this, SELECTION__PROP, index, element);
		}
	};

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
	}

	@Override
	public final boolean hasRoot() {
		return _root != null;
	}

	@Override
	public final java.util.List<com.top_logic.graphic.flow.data.Widget> getSelection() {
		return _selection;
	}

	@Override
	public com.top_logic.graphic.flow.data.Diagram setSelection(java.util.List<? extends com.top_logic.graphic.flow.data.Widget> value) {
		internalSetSelection(value);
		return this;
	}

	/** Internal setter for {@link #getSelection()} without chain call utility. */
	protected final void internalSetSelection(java.util.List<? extends com.top_logic.graphic.flow.data.Widget> value) {
		if (value == null) throw new IllegalArgumentException("Property 'selection' cannot be null.");
		_selection.clear();
		_selection.addAll(value);
	}

	@Override
	public com.top_logic.graphic.flow.data.Diagram addSelection(com.top_logic.graphic.flow.data.Widget value) {
		internalAddSelection(value);
		return this;
	}

	/** Implementation of {@link #addSelection(com.top_logic.graphic.flow.data.Widget)} without chain call utility. */
	protected final void internalAddSelection(com.top_logic.graphic.flow.data.Widget value) {
		_selection.add(value);
	}

	@Override
	public final void removeSelection(com.top_logic.graphic.flow.data.Widget value) {
		_selection.remove(value);
	}

	@Override
	public com.top_logic.graphic.flow.data.Diagram setUserObject(java.lang.Object value) {
		internalSetUserObject(value);
		return this;
	}

	@Override
	public String jsonType() {
		return DIAGRAM__TYPE;
	}

	private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(
		java.util.Arrays.asList(
			ROOT__PROP, 
			SELECTION__PROP));

	@Override
	public java.util.List<String> properties() {
		return PROPERTIES;
	}

	@Override
	public Object get(String field) {
		switch (field) {
			case ROOT__PROP: return getRoot();
			case SELECTION__PROP: return getSelection();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ROOT__PROP: internalSetRoot((com.top_logic.graphic.flow.data.Box) value); break;
			case SELECTION__PROP: internalSetSelection(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.graphic.flow.data.Widget.class, value)); break;
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
		for (com.top_logic.graphic.flow.data.Widget x : getSelection()) {
			x.writeTo(scope, out);
		}
		out.endArray();
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
				for (com.top_logic.graphic.flow.data.Widget x : getSelection()) {
					x.writeTo(scope, out);
				}
				out.endArray();
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
				java.util.List<com.top_logic.graphic.flow.data.Widget> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.graphic.flow.data.Widget.readWidget(scope, in));
				}
				in.endArray();
				setSelection(newValue);
			}
			break;
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public void writeElement(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field, Object element) throws java.io.IOException {
		switch (field) {
			case SELECTION__PROP: {
				((com.top_logic.graphic.flow.data.Widget) element).writeTo(scope, out);
				break;
			}
			default: super.writeElement(scope, out, field, element);
		}
	}

	@Override
	public Object readElement(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case SELECTION__PROP: {
				return com.top_logic.graphic.flow.data.Widget.readWidget(scope, in);
			}
			default: return super.readElement(scope, in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.Widget.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
