package com.top_logic.graphic.flow.data;

/**
 * A horizontal row of boxes.
 */
public interface HorizontalLayout extends RowLayout, com.top_logic.graphic.flow.operations.layout.HorizontalLayoutOperations {

	/**
	 * Creates a {@link com.top_logic.graphic.flow.data.HorizontalLayout} instance.
	 */
	static com.top_logic.graphic.flow.data.HorizontalLayout create() {
		return new com.top_logic.graphic.flow.data.impl.HorizontalLayout_Impl();
	}

	/** Identifier for the {@link com.top_logic.graphic.flow.data.HorizontalLayout} type in JSON format. */
	String HORIZONTAL_LAYOUT__TYPE = "HorizontalLayout";

	@Override
	com.top_logic.graphic.flow.data.HorizontalLayout setGap(double value);

	@Override
	com.top_logic.graphic.flow.data.HorizontalLayout setFill(com.top_logic.graphic.flow.data.SpaceDistribution value);

	@Override
	com.top_logic.graphic.flow.data.HorizontalLayout setContents(java.util.List<? extends com.top_logic.graphic.flow.data.Box> value);

	@Override
	com.top_logic.graphic.flow.data.HorizontalLayout addContent(com.top_logic.graphic.flow.data.Box value);

	@Override
	com.top_logic.graphic.flow.data.HorizontalLayout setX(double value);

	@Override
	com.top_logic.graphic.flow.data.HorizontalLayout setY(double value);

	@Override
	com.top_logic.graphic.flow.data.HorizontalLayout setWidth(double value);

	@Override
	com.top_logic.graphic.flow.data.HorizontalLayout setHeight(double value);

	@Override
	com.top_logic.graphic.flow.data.HorizontalLayout setCssClass(String value);

	@Override
	com.top_logic.graphic.flow.data.HorizontalLayout setUserObject(java.lang.Object value);

	@Override
	com.top_logic.graphic.flow.data.HorizontalLayout setClientId(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.HorizontalLayout readHorizontalLayout(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.graphic.flow.data.HorizontalLayout) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert HORIZONTAL_LAYOUT__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.graphic.flow.data.impl.HorizontalLayout_Impl result = new com.top_logic.graphic.flow.data.impl.HorizontalLayout_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

	@Override
	default HorizontalLayout self() {
		return this;
	}

}
