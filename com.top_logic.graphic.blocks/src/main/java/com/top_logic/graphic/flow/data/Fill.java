package com.top_logic.graphic.flow.data;

/**
 * A background drawn below some content.
 */
public interface Fill extends Decoration, com.top_logic.graphic.flow.operations.FillOperations {

	/**
	 * Creates a {@link com.top_logic.graphic.flow.data.Fill} instance.
	 */
	static com.top_logic.graphic.flow.data.Fill create() {
		return new com.top_logic.graphic.flow.data.impl.Fill_Impl();
	}

	/** Identifier for the {@link com.top_logic.graphic.flow.data.Fill} type in JSON format. */
	String FILL__TYPE = "Fill";

	/** @see #getFillStyle() */
	String FILL_STYLE__PROP = "fillStyle";

	/**
	 * The SVG <code>fill</code> style.
	 */
	String getFillStyle();

	/**
	 * @see #getFillStyle()
	 */
	com.top_logic.graphic.flow.data.Fill setFillStyle(String value);

	@Override
	com.top_logic.graphic.flow.data.Fill setContent(com.top_logic.graphic.flow.data.Box value);

	@Override
	com.top_logic.graphic.flow.data.Fill setX(double value);

	@Override
	com.top_logic.graphic.flow.data.Fill setY(double value);

	@Override
	com.top_logic.graphic.flow.data.Fill setWidth(double value);

	@Override
	com.top_logic.graphic.flow.data.Fill setHeight(double value);

	@Override
	com.top_logic.graphic.flow.data.Fill setCssClass(String value);

	@Override
	com.top_logic.graphic.flow.data.Fill setUserObject(java.lang.Object value);

	@Override
	com.top_logic.graphic.flow.data.Fill setClientId(String value);

	@Override
	com.top_logic.graphic.flow.data.Fill setRenderInfo(java.lang.Object value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.Fill readFill(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.graphic.flow.data.Fill) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert FILL__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.graphic.flow.data.impl.Fill_Impl result = new com.top_logic.graphic.flow.data.impl.Fill_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

	@Override
	default Fill self() {
		return this;
	}

}
