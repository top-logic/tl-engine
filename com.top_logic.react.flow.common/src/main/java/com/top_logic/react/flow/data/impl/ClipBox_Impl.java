package com.top_logic.react.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.react.flow.data.ClipBox}.
 */
public class ClipBox_Impl extends com.top_logic.react.flow.data.impl.Decoration_Impl implements com.top_logic.react.flow.data.ClipBox {

	/**
	 * Creates a {@link ClipBox_Impl} instance.
	 *
	 * @see com.top_logic.react.flow.data.ClipBox#create()
	 */
	public ClipBox_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.CLIP_BOX;
	}

	@Override
	public com.top_logic.react.flow.data.ClipBox setContent(com.top_logic.react.flow.data.Box value) {
		internalSetContent(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.ClipBox setX(double value) {
		internalSetX(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.ClipBox setY(double value) {
		internalSetY(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.ClipBox setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.ClipBox setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.ClipBox setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.ClipBox setUserObject(java.lang.Object value) {
		internalSetUserObject(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.ClipBox setClientId(String value) {
		internalSetClientId(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.ClipBox setRenderInfo(java.lang.Object value) {
		internalSetRenderInfo(value);
		return this;
	}

	@Override
	public String jsonType() {
		return CLIP_BOX__TYPE;
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.react.flow.data.Decoration.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
