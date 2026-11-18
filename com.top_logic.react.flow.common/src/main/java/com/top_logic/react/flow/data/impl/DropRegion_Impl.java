package com.top_logic.react.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.react.flow.data.DropRegion}.
 */
public class DropRegion_Impl extends com.top_logic.react.flow.data.impl.Decoration_Impl implements com.top_logic.react.flow.data.DropRegion {

	/**
	 * Creates a {@link DropRegion_Impl} instance.
	 *
	 * @see com.top_logic.react.flow.data.DropRegion#create()
	 */
	public DropRegion_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.DROP_REGION;
	}

	@Override
	public com.top_logic.react.flow.data.DropRegion setContent(com.top_logic.react.flow.data.Box value) {
		internalSetContent(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.DropRegion setX(double value) {
		internalSetX(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.DropRegion setY(double value) {
		internalSetY(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.DropRegion setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.DropRegion setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.DropRegion setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.DropRegion setUserObject(java.lang.Object value) {
		internalSetUserObject(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.DropRegion setClientId(String value) {
		internalSetClientId(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.DropRegion setRenderInfo(java.lang.Object value) {
		internalSetRenderInfo(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.DropRegion setDropTarget(boolean value) {
		internalSetDropTarget(value);
		return this;
	}

	@Override
	public String jsonType() {
		return DROP_REGION__TYPE;
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.react.flow.data.Decoration.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
