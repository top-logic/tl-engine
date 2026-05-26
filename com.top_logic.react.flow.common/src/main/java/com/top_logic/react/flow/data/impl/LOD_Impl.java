package com.top_logic.react.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.react.flow.data.LOD}.
 */
public class LOD_Impl extends com.top_logic.react.flow.data.impl.Box_Impl implements com.top_logic.react.flow.data.LOD {

	private final java.util.List<com.top_logic.react.flow.data.LODVariant> _variants = new de.haumacher.msgbuf.util.ReferenceList<com.top_logic.react.flow.data.LODVariant>() {
		@Override
		protected void beforeAdd(int index, com.top_logic.react.flow.data.LODVariant element) {
			_listener.beforeAdd(LOD_Impl.this, VARIANTS__PROP, index, element);
		}

		@Override
		protected void afterRemove(int index, com.top_logic.react.flow.data.LODVariant element) {
			_listener.afterRemove(LOD_Impl.this, VARIANTS__PROP, index, element);
		}

		@Override
		protected void afterChanged() {
			_listener.afterChanged(LOD_Impl.this, VARIANTS__PROP);
		}
	};

	private double _fixedHeight = 0.0d;

	private transient int _chosenIndex = 0;

	/**
	 * Creates a {@link LOD_Impl} instance.
	 *
	 * @see com.top_logic.react.flow.data.LOD#create()
	 */
	public LOD_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.LOD;
	}

	@Override
	public final java.util.List<com.top_logic.react.flow.data.LODVariant> getVariants() {
		return _variants;
	}

	@Override
	public com.top_logic.react.flow.data.LOD setVariants(java.util.List<? extends com.top_logic.react.flow.data.LODVariant> value) {
		internalSetVariants(value);
		return this;
	}

	/** Internal setter for {@link #getVariants()} without chain call utility. */
	protected final void internalSetVariants(java.util.List<? extends com.top_logic.react.flow.data.LODVariant> value) {
		if (value == null) throw new IllegalArgumentException("Property 'variants' cannot be null.");
		_variants.clear();
		_variants.addAll(value);
	}

	@Override
	public com.top_logic.react.flow.data.LOD addVariant(com.top_logic.react.flow.data.LODVariant value) {
		internalAddVariant(value);
		return this;
	}

	/** Implementation of {@link #addVariant(com.top_logic.react.flow.data.LODVariant)} without chain call utility. */
	protected final void internalAddVariant(com.top_logic.react.flow.data.LODVariant value) {
		_variants.add(value);
	}

	@Override
	public final void removeVariant(com.top_logic.react.flow.data.LODVariant value) {
		_variants.remove(value);
	}

	@Override
	public final double getFixedHeight() {
		return _fixedHeight;
	}

	@Override
	public com.top_logic.react.flow.data.LOD setFixedHeight(double value) {
		internalSetFixedHeight(value);
		return this;
	}

	/** Internal setter for {@link #getFixedHeight()} without chain call utility. */
	protected final void internalSetFixedHeight(double value) {
		_listener.beforeSet(this, FIXED_HEIGHT__PROP, value);
		_fixedHeight = value;
		_listener.afterChanged(this, FIXED_HEIGHT__PROP);
	}

	@Override
	public final int getChosenIndex() {
		return _chosenIndex;
	}

	@Override
	public com.top_logic.react.flow.data.LOD setChosenIndex(int value) {
		internalSetChosenIndex(value);
		return this;
	}

	/** Internal setter for {@link #getChosenIndex()} without chain call utility. */
	protected final void internalSetChosenIndex(int value) {
		_listener.beforeSet(this, CHOSEN_INDEX__PROP, value);
		_chosenIndex = value;
		_listener.afterChanged(this, CHOSEN_INDEX__PROP);
	}

	@Override
	public com.top_logic.react.flow.data.LOD setX(double value) {
		internalSetX(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.LOD setY(double value) {
		internalSetY(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.LOD setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.LOD setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.LOD setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.LOD setUserObject(java.lang.Object value) {
		internalSetUserObject(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.LOD setClientId(String value) {
		internalSetClientId(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.LOD setRenderInfo(java.lang.Object value) {
		internalSetRenderInfo(value);
		return this;
	}

	@Override
	public String jsonType() {
		return LOD__TYPE;
	}

	@SuppressWarnings("hiding")
	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			VARIANTS__PROP, 
			FIXED_HEIGHT__PROP, 
			CHOSEN_INDEX__PROP);
		java.util.List<String> tmp = new java.util.ArrayList<>();
		tmp.addAll(com.top_logic.react.flow.data.impl.Box_Impl.PROPERTIES);
		tmp.addAll(local);
		PROPERTIES = java.util.Collections.unmodifiableList(tmp);
	}

	@SuppressWarnings("hiding")
	static final java.util.Set<String> TRANSIENT_PROPERTIES;
	static {
		java.util.HashSet<String> tmp = new java.util.HashSet<>();
		tmp.addAll(com.top_logic.react.flow.data.impl.Box_Impl.TRANSIENT_PROPERTIES);
		tmp.addAll(java.util.Arrays.asList(
				CHOSEN_INDEX__PROP));
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
			case VARIANTS__PROP: return getVariants();
			case FIXED_HEIGHT__PROP: return getFixedHeight();
			case CHOSEN_INDEX__PROP: return getChosenIndex();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case VARIANTS__PROP: internalSetVariants(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.react.flow.data.LODVariant.class, value)); break;
			case FIXED_HEIGHT__PROP: internalSetFixedHeight((double) value); break;
			case CHOSEN_INDEX__PROP: internalSetChosenIndex((int) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		out.name(VARIANTS__PROP);
		out.beginArray();
		for (com.top_logic.react.flow.data.LODVariant x : getVariants()) {
			x.writeTo(scope, out);
		}
		out.endArray();
		out.name(FIXED_HEIGHT__PROP);
		out.value(getFixedHeight());
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case VARIANTS__PROP: {
				out.beginArray();
				for (com.top_logic.react.flow.data.LODVariant x : getVariants()) {
					x.writeTo(scope, out);
				}
				out.endArray();
				break;
			}
			case FIXED_HEIGHT__PROP: {
				out.value(getFixedHeight());
				break;
			}
			case CHOSEN_INDEX__PROP: {
				out.value(getChosenIndex());
				break;
			}
			default: super.writeFieldValue(scope, out, field);
		}
	}

	@Override
	public void readField(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case VARIANTS__PROP: {
				java.util.List<com.top_logic.react.flow.data.LODVariant> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.react.flow.data.LODVariant.readLODVariant(scope, in));
				}
				in.endArray();
				setVariants(newValue);
			}
			break;
			case FIXED_HEIGHT__PROP: setFixedHeight(in.nextDouble()); break;
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public void writeElement(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field, Object element) throws java.io.IOException {
		switch (field) {
			case VARIANTS__PROP: {
				((com.top_logic.react.flow.data.LODVariant) element).writeTo(scope, out);
				break;
			}
			default: super.writeElement(scope, out, field, element);
		}
	}

	@Override
	public Object readElement(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case VARIANTS__PROP: {
				return com.top_logic.react.flow.data.LODVariant.readLODVariant(scope, in);
			}
			default: return super.readElement(scope, in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.react.flow.data.Box.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
