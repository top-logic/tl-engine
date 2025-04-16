package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.Text}.
 */
public class Text_Impl extends com.top_logic.graphic.flow.data.impl.Box_Impl implements com.top_logic.graphic.flow.data.Text {

	private String _value = "";

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
	public com.top_logic.graphic.flow.data.Text setUserObject(java.lang.Object value) {
		internalSetUserObject(value);
		return this;
	}

	@Override
	public String jsonType() {
		return TEXT__TYPE;
	}

	private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(
		java.util.Arrays.asList(
			VALUE__PROP, 
			BASE_LINE__PROP));

	@Override
	public java.util.List<String> properties() {
		return PROPERTIES;
	}

	@Override
	public Object get(String field) {
		switch (field) {
			case VALUE__PROP: return getValue();
			case BASE_LINE__PROP: return getBaseLine();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case VALUE__PROP: internalSetValue((String) value); break;
			case BASE_LINE__PROP: internalSetBaseLine((double) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		out.name(VALUE__PROP);
		out.value(getValue());
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
			case BASE_LINE__PROP: setBaseLine(in.nextDouble()); break;
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.Box.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
