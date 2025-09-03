package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.Layout}.
 */
public abstract class Layout_Impl extends com.top_logic.graphic.flow.data.impl.Box_Impl implements com.top_logic.graphic.flow.data.Layout {

	private final java.util.List<com.top_logic.graphic.flow.data.Box> _contents = new de.haumacher.msgbuf.util.ReferenceList<com.top_logic.graphic.flow.data.Box>() {
		@Override
		protected void beforeAdd(int index, com.top_logic.graphic.flow.data.Box element) {
			com.top_logic.graphic.flow.data.impl.Box_Impl added = (com.top_logic.graphic.flow.data.impl.Box_Impl) element;
			com.top_logic.graphic.flow.data.Widget oldContainer = added.getParent();
			if (oldContainer != null && oldContainer != Layout_Impl.this) {
				throw new IllegalStateException("Object may not be part of two different containers.");
			}
			_listener.beforeAdd(Layout_Impl.this, CONTENTS__PROP, index, element);
			added.internalSetParent(Layout_Impl.this);
		}

		@Override
		protected void afterRemove(int index, com.top_logic.graphic.flow.data.Box element) {
			com.top_logic.graphic.flow.data.impl.Box_Impl removed = (com.top_logic.graphic.flow.data.impl.Box_Impl) element;
			removed.internalSetParent(null);
			_listener.afterRemove(Layout_Impl.this, CONTENTS__PROP, index, element);
		}

		@Override
		protected void afterChanged() {
			_listener.afterChanged(Layout_Impl.this, CONTENTS__PROP);
		}
	};

	/**
	 * Creates a {@link Layout_Impl} instance.
	 */
	public Layout_Impl() {
		super();
	}

	@Override
	public final java.util.List<com.top_logic.graphic.flow.data.Box> getContents() {
		return _contents;
	}

	@Override
	public com.top_logic.graphic.flow.data.Layout setContents(java.util.List<? extends com.top_logic.graphic.flow.data.Box> value) {
		internalSetContents(value);
		return this;
	}

	/** Internal setter for {@link #getContents()} without chain call utility. */
	protected final void internalSetContents(java.util.List<? extends com.top_logic.graphic.flow.data.Box> value) {
		if (value == null) throw new IllegalArgumentException("Property 'contents' cannot be null.");
		_contents.clear();
		_contents.addAll(value);
	}

	@Override
	public com.top_logic.graphic.flow.data.Layout addContent(com.top_logic.graphic.flow.data.Box value) {
		internalAddContent(value);
		return this;
	}

	/** Implementation of {@link #addContent(com.top_logic.graphic.flow.data.Box)} without chain call utility. */
	protected final void internalAddContent(com.top_logic.graphic.flow.data.Box value) {
		_contents.add(value);
	}

	@Override
	public final void removeContent(com.top_logic.graphic.flow.data.Box value) {
		_contents.remove(value);
	}

	@Override
	public com.top_logic.graphic.flow.data.Layout setX(double value) {
		internalSetX(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Layout setY(double value) {
		internalSetY(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Layout setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Layout setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Layout setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Layout setUserObject(java.lang.Object value) {
		internalSetUserObject(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Layout setClientId(String value) {
		internalSetClientId(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Layout setRenderInfo(java.lang.Object value) {
		internalSetRenderInfo(value);
		return this;
	}

	@SuppressWarnings("hiding")
	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			CONTENTS__PROP);
		java.util.List<String> tmp = new java.util.ArrayList<>();
		tmp.addAll(com.top_logic.graphic.flow.data.impl.Box_Impl.PROPERTIES);
		tmp.addAll(local);
		PROPERTIES = java.util.Collections.unmodifiableList(tmp);
	}

	@SuppressWarnings("hiding")
	static final java.util.Set<String> TRANSIENT_PROPERTIES;
	static {
		java.util.HashSet<String> tmp = new java.util.HashSet<>();
		tmp.addAll(com.top_logic.graphic.flow.data.impl.Box_Impl.TRANSIENT_PROPERTIES);
		tmp.addAll(java.util.Arrays.asList(
				));
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
			case CONTENTS__PROP: return getContents();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case CONTENTS__PROP: internalSetContents(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.graphic.flow.data.Box.class, value)); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		out.name(CONTENTS__PROP);
		out.beginArray();
		for (com.top_logic.graphic.flow.data.Box x : getContents()) {
			x.writeTo(scope, out);
		}
		out.endArray();
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case CONTENTS__PROP: {
				out.beginArray();
				for (com.top_logic.graphic.flow.data.Box x : getContents()) {
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
			case CONTENTS__PROP: {
				java.util.List<com.top_logic.graphic.flow.data.Box> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.graphic.flow.data.Box.readBox(scope, in));
				}
				in.endArray();
				setContents(newValue);
			}
			break;
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public void writeElement(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field, Object element) throws java.io.IOException {
		switch (field) {
			case CONTENTS__PROP: {
				((com.top_logic.graphic.flow.data.Box) element).writeTo(scope, out);
				break;
			}
			default: super.writeElement(scope, out, field, element);
		}
	}

	@Override
	public Object readElement(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case CONTENTS__PROP: {
				return com.top_logic.graphic.flow.data.Box.readBox(scope, in);
			}
			default: return super.readElement(scope, in, field);
		}
	}

	@Override
	public final <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.Box.Visitor<R,A,E> v, A arg) throws E {
		return visit((com.top_logic.graphic.flow.data.Layout.Visitor<R,A,E>) v, arg);
	}

}
