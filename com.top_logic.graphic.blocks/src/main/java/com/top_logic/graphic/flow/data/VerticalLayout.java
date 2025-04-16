package com.top_logic.graphic.flow.data;

public interface VerticalLayout extends RowLayout, com.top_logic.graphic.flow.operations.layout.VerticalLayoutOperations {

	/**
	 * Creates a {@link com.top_logic.graphic.flow.data.VerticalLayout} instance.
	 */
	static com.top_logic.graphic.flow.data.VerticalLayout create() {
		return new com.top_logic.graphic.flow.data.impl.VerticalLayout_Impl();
	}

	/** Identifier for the {@link com.top_logic.graphic.flow.data.VerticalLayout} type in JSON format. */
	String VERTICAL_LAYOUT__TYPE = "VerticalLayout";

	@Override
	com.top_logic.graphic.flow.data.VerticalLayout setGap(double value);

	@Override
	com.top_logic.graphic.flow.data.VerticalLayout setFill(com.top_logic.graphic.flow.data.SpaceDistribution value);

	@Override
	com.top_logic.graphic.flow.data.VerticalLayout setContents(java.util.List<? extends com.top_logic.graphic.flow.data.Box> value);

	@Override
	com.top_logic.graphic.flow.data.VerticalLayout addContent(com.top_logic.graphic.flow.data.Box value);

	@Override
	com.top_logic.graphic.flow.data.VerticalLayout setX(double value);

	@Override
	com.top_logic.graphic.flow.data.VerticalLayout setY(double value);

	@Override
	com.top_logic.graphic.flow.data.VerticalLayout setWidth(double value);

	@Override
	com.top_logic.graphic.flow.data.VerticalLayout setHeight(double value);

	@Override
	com.top_logic.graphic.flow.data.VerticalLayout setUserObject(java.lang.Object value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.VerticalLayout readVerticalLayout(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.graphic.flow.data.VerticalLayout) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert VERTICAL_LAYOUT__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.graphic.flow.data.impl.VerticalLayout_Impl result = new com.top_logic.graphic.flow.data.impl.VerticalLayout_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

	@Override
	default VerticalLayout self() {
		return this;
	}

}
