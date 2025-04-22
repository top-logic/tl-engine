package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.VerticalLayout}.
 */
public class VerticalLayout_Impl extends com.top_logic.graphic.flow.data.impl.RowLayout_Impl implements com.top_logic.graphic.flow.data.VerticalLayout {

	/**
	 * Creates a {@link VerticalLayout_Impl} instance.
	 *
	 * @see com.top_logic.graphic.flow.data.VerticalLayout#create()
	 */
	public VerticalLayout_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.VERTICAL_LAYOUT;
	}

	@Override
	public com.top_logic.graphic.flow.data.VerticalLayout setGap(double value) {
		internalSetGap(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.VerticalLayout setFill(com.top_logic.graphic.flow.data.SpaceDistribution value) {
		internalSetFill(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.VerticalLayout setContents(java.util.List<? extends com.top_logic.graphic.flow.data.Box> value) {
		internalSetContents(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.VerticalLayout addContent(com.top_logic.graphic.flow.data.Box value) {
		internalAddContent(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.VerticalLayout setX(double value) {
		internalSetX(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.VerticalLayout setY(double value) {
		internalSetY(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.VerticalLayout setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.VerticalLayout setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.VerticalLayout setUserObject(java.lang.Object value) {
		internalSetUserObject(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.VerticalLayout setClientId(String value) {
		internalSetClientId(value);
		return this;
	}

	@Override
	public String jsonType() {
		return VERTICAL_LAYOUT__TYPE;
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.RowLayout.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
