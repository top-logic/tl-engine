package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.Text}.
 */
public class Text_Impl extends com.top_logic.graphic.flow.data.impl.Box_Impl implements com.top_logic.graphic.flow.data.Text {

	private String _value = "";

	private String _fontWeight = null;

	private String _fontSize = null;

	private String _fontFamily = null;

	private String _strokeStyle = null;

	private String _fillStyle = null;

	private double _baseLine = 0.0d;

	/**
	 * Creates a {@link Text_Impl} instance.
	 *
	 * @see com.top_logic.graphic.flow.data.Text#create()
	 */
	public Text_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.TEXT;
	}

	@Override
	public final String getValue() {
		return _value;
	}

	@Override
	public com.top_logic.graphic.flow.data.Text setValue(String value) {
		internalSetValue(value);
		return this;
	}

	/** Internal setter for {@link #getValue()} without chain call utility. */
	protected final void internalSetValue(String value) {
		_listener.beforeSet(this, VALUE__PROP, value);
		_value = value;
	}

	@Override
	public final String getFontWeight() {
		return _fontWeight;
	}

	@Override
	public com.top_logic.graphic.flow.data.Text setFontWeight(String value) {
		internalSetFontWeight(value);
		return this;
	}

	/** Internal setter for {@link #getFontWeight()} without chain call utility. */
	protected final void internalSetFontWeight(String value) {
		_listener.beforeSet(this, FONT_WEIGHT__PROP, value);
		_fontWeight = value;
	}

	@Override
	public final boolean hasFontWeight() {
		return _fontWeight != null;
	}

	@Override
	public final String getFontSize() {
		return _fontSize;
	}

	@Override
	public com.top_logic.graphic.flow.data.Text setFontSize(String value) {
		internalSetFontSize(value);
		return this;
	}

	/** Internal setter for {@link #getFontSize()} without chain call utility. */
	protected final void internalSetFontSize(String value) {
		_listener.beforeSet(this, FONT_SIZE__PROP, value);
		_fontSize = value;
	}

	@Override
	public final boolean hasFontSize() {
		return _fontSize != null;
	}

	@Override
	public final String getFontFamily() {
		return _fontFamily;
	}

	@Override
	public com.top_logic.graphic.flow.data.Text setFontFamily(String value) {
		internalSetFontFamily(value);
		return this;
	}

	/** Internal setter for {@link #getFontFamily()} without chain call utility. */
	protected final void internalSetFontFamily(String value) {
		_listener.beforeSet(this, FONT_FAMILY__PROP, value);
		_fontFamily = value;
	}

	@Override
	public final boolean hasFontFamily() {
		return _fontFamily != null;
	}

	@Override
	public final String getStrokeStyle() {
		return _strokeStyle;
	}

	@Override
	public com.top_logic.graphic.flow.data.Text setStrokeStyle(String value) {
		internalSetStrokeStyle(value);
		return this;
	}

	/** Internal setter for {@link #getStrokeStyle()} without chain call utility. */
	protected final void internalSetStrokeStyle(String value) {
		_listener.beforeSet(this, STROKE_STYLE__PROP, value);
		_strokeStyle = value;
	}

	@Override
	public final boolean hasStrokeStyle() {
		return _strokeStyle != null;
	}

	@Override
	public final String getFillStyle() {
		return _fillStyle;
	}

	@Override
	public com.top_logic.graphic.flow.data.Text setFillStyle(String value) {
		internalSetFillStyle(value);
		return this;
	}

	/** Internal setter for {@link #getFillStyle()} without chain call utility. */
	protected final void internalSetFillStyle(String value) {
		_listener.beforeSet(this, FILL_STYLE__PROP, value);
		_fillStyle = value;
	}

	@Override
	public final boolean hasFillStyle() {
		return _fillStyle != null;
	}

	@Override
	public final double getBaseLine() {
		return _baseLine;
	}

	@Override
	public com.top_logic.graphic.flow.data.Text setBaseLine(double value) {
		internalSetBaseLine(value);
		return this;
	}

	/** Internal setter for {@link #getBaseLine()} without chain call utility. */
	protected final void internalSetBaseLine(double value) {
		_listener.beforeSet(this, BASE_LINE__PROP, value);
		_baseLine = value;
	}

	@Override
	public com.top_logic.graphic.flow.data.Text setX(double value) {
		internalSetX(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Text setY(double value) {
		internalSetY(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Text setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Text setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Text setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Text setUserObject(java.lang.Object value) {
		internalSetUserObject(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Text setClientId(String value) {
		internalSetClientId(value);
		return this;
	}

	@Override
	public String jsonType() {
		return TEXT__TYPE;
	}

	private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(
		java.util.Arrays.asList(
			VALUE__PROP, 
			FONT_WEIGHT__PROP, 
			FONT_SIZE__PROP, 
			FONT_FAMILY__PROP, 
			STROKE_STYLE__PROP, 
			FILL_STYLE__PROP, 
			BASE_LINE__PROP));

	private static java.util.Set<String> TRANSIENT_PROPERTIES = java.util.Collections.unmodifiableSet(new java.util.HashSet<>(
			java.util.Arrays.asList(
				)));

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
			case VALUE__PROP: return getValue();
			case FONT_WEIGHT__PROP: return getFontWeight();
			case FONT_SIZE__PROP: return getFontSize();
			case FONT_FAMILY__PROP: return getFontFamily();
			case STROKE_STYLE__PROP: return getStrokeStyle();
			case FILL_STYLE__PROP: return getFillStyle();
			case BASE_LINE__PROP: return getBaseLine();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case VALUE__PROP: internalSetValue((String) value); break;
			case FONT_WEIGHT__PROP: internalSetFontWeight((String) value); break;
			case FONT_SIZE__PROP: internalSetFontSize((String) value); break;
			case FONT_FAMILY__PROP: internalSetFontFamily((String) value); break;
			case STROKE_STYLE__PROP: internalSetStrokeStyle((String) value); break;
			case FILL_STYLE__PROP: internalSetFillStyle((String) value); break;
			case BASE_LINE__PROP: internalSetBaseLine((double) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		out.name(VALUE__PROP);
		out.value(getValue());
		if (hasFontWeight()) {
			out.name(FONT_WEIGHT__PROP);
			out.value(getFontWeight());
		}
		if (hasFontSize()) {
			out.name(FONT_SIZE__PROP);
			out.value(getFontSize());
		}
		if (hasFontFamily()) {
			out.name(FONT_FAMILY__PROP);
			out.value(getFontFamily());
		}
		if (hasStrokeStyle()) {
			out.name(STROKE_STYLE__PROP);
			out.value(getStrokeStyle());
		}
		if (hasFillStyle()) {
			out.name(FILL_STYLE__PROP);
			out.value(getFillStyle());
		}
		out.name(BASE_LINE__PROP);
		out.value(getBaseLine());
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case VALUE__PROP: {
				out.value(getValue());
				break;
			}
			case FONT_WEIGHT__PROP: {
				if (hasFontWeight()) {
					out.value(getFontWeight());
				} else {
					out.nullValue();
				}
				break;
			}
			case FONT_SIZE__PROP: {
				if (hasFontSize()) {
					out.value(getFontSize());
				} else {
					out.nullValue();
				}
				break;
			}
			case FONT_FAMILY__PROP: {
				if (hasFontFamily()) {
					out.value(getFontFamily());
				} else {
					out.nullValue();
				}
				break;
			}
			case STROKE_STYLE__PROP: {
				if (hasStrokeStyle()) {
					out.value(getStrokeStyle());
				} else {
					out.nullValue();
				}
				break;
			}
			case FILL_STYLE__PROP: {
				if (hasFillStyle()) {
					out.value(getFillStyle());
				} else {
					out.nullValue();
				}
				break;
			}
			case BASE_LINE__PROP: {
				out.value(getBaseLine());
				break;
			}
			default: super.writeFieldValue(scope, out, field);
		}
	}

	@Override
	public void readField(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case VALUE__PROP: setValue(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case FONT_WEIGHT__PROP: setFontWeight(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case FONT_SIZE__PROP: setFontSize(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case FONT_FAMILY__PROP: setFontFamily(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case STROKE_STYLE__PROP: setStrokeStyle(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case FILL_STYLE__PROP: setFillStyle(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case BASE_LINE__PROP: setBaseLine(in.nextDouble()); break;
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.Box.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
