package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.Image}.
 */
public class Image_Impl extends com.top_logic.graphic.flow.data.impl.Box_Impl implements com.top_logic.graphic.flow.data.Image {

	private String _href = "";

	private double _imgWidth = 0.0d;

	private double _imgHeight = 0.0d;

	private com.top_logic.graphic.flow.data.ImageAlign _align = com.top_logic.graphic.flow.data.ImageAlign.X_MID_YMID;

	private com.top_logic.graphic.flow.data.ImageScale _scale = com.top_logic.graphic.flow.data.ImageScale.MEET;

	private com.top_logic.graphic.flow.data.ImageOrientation _orientation = com.top_logic.graphic.flow.data.ImageOrientation.NORMAL;

	/**
	 * Creates a {@link Image_Impl} instance.
	 *
	 * @see com.top_logic.graphic.flow.data.Image#create()
	 */
	public Image_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.IMAGE;
	}

	@Override
	public final String getHref() {
		return _href;
	}

	@Override
	public com.top_logic.graphic.flow.data.Image setHref(String value) {
		internalSetHref(value);
		return this;
	}

	/** Internal setter for {@link #getHref()} without chain call utility. */
	protected final void internalSetHref(String value) {
		_listener.beforeSet(this, HREF__PROP, value);
		_href = value;
		_listener.afterChanged(this, HREF__PROP);
	}

	@Override
	public final double getImgWidth() {
		return _imgWidth;
	}

	@Override
	public com.top_logic.graphic.flow.data.Image setImgWidth(double value) {
		internalSetImgWidth(value);
		return this;
	}

	/** Internal setter for {@link #getImgWidth()} without chain call utility. */
	protected final void internalSetImgWidth(double value) {
		_listener.beforeSet(this, IMG_WIDTH__PROP, value);
		_imgWidth = value;
		_listener.afterChanged(this, IMG_WIDTH__PROP);
	}

	@Override
	public final double getImgHeight() {
		return _imgHeight;
	}

	@Override
	public com.top_logic.graphic.flow.data.Image setImgHeight(double value) {
		internalSetImgHeight(value);
		return this;
	}

	/** Internal setter for {@link #getImgHeight()} without chain call utility. */
	protected final void internalSetImgHeight(double value) {
		_listener.beforeSet(this, IMG_HEIGHT__PROP, value);
		_imgHeight = value;
		_listener.afterChanged(this, IMG_HEIGHT__PROP);
	}

	@Override
	public final com.top_logic.graphic.flow.data.ImageAlign getAlign() {
		return _align;
	}

	@Override
	public com.top_logic.graphic.flow.data.Image setAlign(com.top_logic.graphic.flow.data.ImageAlign value) {
		internalSetAlign(value);
		return this;
	}

	/** Internal setter for {@link #getAlign()} without chain call utility. */
	protected final void internalSetAlign(com.top_logic.graphic.flow.data.ImageAlign value) {
		if (value == null) throw new IllegalArgumentException("Property 'align' cannot be null.");
		_listener.beforeSet(this, ALIGN__PROP, value);
		_align = value;
		_listener.afterChanged(this, ALIGN__PROP);
	}

	@Override
	public final com.top_logic.graphic.flow.data.ImageScale getScale() {
		return _scale;
	}

	@Override
	public com.top_logic.graphic.flow.data.Image setScale(com.top_logic.graphic.flow.data.ImageScale value) {
		internalSetScale(value);
		return this;
	}

	/** Internal setter for {@link #getScale()} without chain call utility. */
	protected final void internalSetScale(com.top_logic.graphic.flow.data.ImageScale value) {
		if (value == null) throw new IllegalArgumentException("Property 'scale' cannot be null.");
		_listener.beforeSet(this, SCALE__PROP, value);
		_scale = value;
		_listener.afterChanged(this, SCALE__PROP);
	}

	@Override
	public final com.top_logic.graphic.flow.data.ImageOrientation getOrientation() {
		return _orientation;
	}

	@Override
	public com.top_logic.graphic.flow.data.Image setOrientation(com.top_logic.graphic.flow.data.ImageOrientation value) {
		internalSetOrientation(value);
		return this;
	}

	/** Internal setter for {@link #getOrientation()} without chain call utility. */
	protected final void internalSetOrientation(com.top_logic.graphic.flow.data.ImageOrientation value) {
		if (value == null) throw new IllegalArgumentException("Property 'orientation' cannot be null.");
		_listener.beforeSet(this, ORIENTATION__PROP, value);
		_orientation = value;
		_listener.afterChanged(this, ORIENTATION__PROP);
	}

	@Override
	public com.top_logic.graphic.flow.data.Image setX(double value) {
		internalSetX(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Image setY(double value) {
		internalSetY(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Image setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Image setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Image setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Image setUserObject(java.lang.Object value) {
		internalSetUserObject(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Image setClientId(String value) {
		internalSetClientId(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Image setRenderInfo(java.lang.Object value) {
		internalSetRenderInfo(value);
		return this;
	}

	@Override
	public String jsonType() {
		return IMAGE__TYPE;
	}

	@SuppressWarnings("hiding")
	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			HREF__PROP, 
			IMG_WIDTH__PROP, 
			IMG_HEIGHT__PROP, 
			ALIGN__PROP, 
			SCALE__PROP, 
			ORIENTATION__PROP);
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
			case HREF__PROP: return getHref();
			case IMG_WIDTH__PROP: return getImgWidth();
			case IMG_HEIGHT__PROP: return getImgHeight();
			case ALIGN__PROP: return getAlign();
			case SCALE__PROP: return getScale();
			case ORIENTATION__PROP: return getOrientation();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case HREF__PROP: internalSetHref((String) value); break;
			case IMG_WIDTH__PROP: internalSetImgWidth((double) value); break;
			case IMG_HEIGHT__PROP: internalSetImgHeight((double) value); break;
			case ALIGN__PROP: internalSetAlign((com.top_logic.graphic.flow.data.ImageAlign) value); break;
			case SCALE__PROP: internalSetScale((com.top_logic.graphic.flow.data.ImageScale) value); break;
			case ORIENTATION__PROP: internalSetOrientation((com.top_logic.graphic.flow.data.ImageOrientation) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		out.name(HREF__PROP);
		out.value(getHref());
		out.name(IMG_WIDTH__PROP);
		out.value(getImgWidth());
		out.name(IMG_HEIGHT__PROP);
		out.value(getImgHeight());
		out.name(ALIGN__PROP);
		getAlign().writeTo(out);
		out.name(SCALE__PROP);
		getScale().writeTo(out);
		out.name(ORIENTATION__PROP);
		getOrientation().writeTo(out);
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case HREF__PROP: {
				out.value(getHref());
				break;
			}
			case IMG_WIDTH__PROP: {
				out.value(getImgWidth());
				break;
			}
			case IMG_HEIGHT__PROP: {
				out.value(getImgHeight());
				break;
			}
			case ALIGN__PROP: {
				getAlign().writeTo(out);
				break;
			}
			case SCALE__PROP: {
				getScale().writeTo(out);
				break;
			}
			case ORIENTATION__PROP: {
				getOrientation().writeTo(out);
				break;
			}
			default: super.writeFieldValue(scope, out, field);
		}
	}

	@Override
	public void readField(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case HREF__PROP: setHref(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case IMG_WIDTH__PROP: setImgWidth(in.nextDouble()); break;
			case IMG_HEIGHT__PROP: setImgHeight(in.nextDouble()); break;
			case ALIGN__PROP: setAlign(com.top_logic.graphic.flow.data.ImageAlign.readImageAlign(in)); break;
			case SCALE__PROP: setScale(com.top_logic.graphic.flow.data.ImageScale.readImageScale(in)); break;
			case ORIENTATION__PROP: setOrientation(com.top_logic.graphic.flow.data.ImageOrientation.readImageOrientation(in)); break;
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.Box.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
