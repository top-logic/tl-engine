package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.HorizontalLayout}.
 */
public class HorizontalLayout_Impl extends com.top_logic.graphic.flow.data.impl.RowLayout_Impl implements com.top_logic.graphic.flow.data.HorizontalLayout {

	/**
	 * Creates a {@link HorizontalLayout_Impl} instance.
	 *
	 * @see com.top_logic.graphic.flow.data.HorizontalLayout#create()
	 */
	public HorizontalLayout_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.HORIZONTAL_LAYOUT;
	}

	@Override
	public com.top_logic.graphic.flow.data.HorizontalLayout setGap(double value) {
		internalSetGap(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.HorizontalLayout setFill(com.top_logic.graphic.flow.data.SpaceDistribution value) {
		internalSetFill(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.HorizontalLayout setContents(java.util.List<? extends com.top_logic.graphic.flow.data.Box> value) {
		internalSetContents(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.HorizontalLayout addContent(com.top_logic.graphic.flow.data.Box value) {
		internalAddContent(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.HorizontalLayout setX(double value) {
		internalSetX(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.HorizontalLayout setY(double value) {
		internalSetY(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.HorizontalLayout setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.HorizontalLayout setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.HorizontalLayout setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.HorizontalLayout setUserObject(java.lang.Object value) {
		internalSetUserObject(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.HorizontalLayout setClientId(String value) {
		internalSetClientId(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.HorizontalLayout setRenderInfo(java.lang.Object value) {
		internalSetRenderInfo(value);
		return this;
	}

	@Override
	public String jsonType() {
		return HORIZONTAL_LAYOUT__TYPE;
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.RowLayout.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
