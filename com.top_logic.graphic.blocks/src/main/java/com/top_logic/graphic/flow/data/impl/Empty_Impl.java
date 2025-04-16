package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.Empty}.
 */
public class Empty_Impl extends com.top_logic.graphic.flow.data.impl.Box_Impl implements com.top_logic.graphic.flow.data.Empty {

	/**
	 * Creates a {@link Empty_Impl} instance.
	 *
	 * @see com.top_logic.graphic.flow.data.Empty#create()
	 */
	public Empty_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.EMPTY;
	}

	@Override
	public com.top_logic.graphic.flow.data.Empty setX(double value) {
		internalSetX(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Empty setY(double value) {
		internalSetY(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Empty setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Empty setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Empty setUserObject(java.lang.Object value) {
		internalSetUserObject(value);
		return this;
	}

	@Override
	public String jsonType() {
		return EMPTY__TYPE;
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.Box.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
