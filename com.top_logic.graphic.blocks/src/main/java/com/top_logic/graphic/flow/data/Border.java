package com.top_logic.graphic.flow.data;

public interface Border extends Decoration, com.top_logic.graphic.flow.operations.BorderOperations {

	/**
	 * Creates a {@link com.top_logic.graphic.flow.data.Border} instance.
	 */
	static com.top_logic.graphic.flow.data.Border create() {
		return new com.top_logic.graphic.flow.data.impl.Border_Impl();
	}

	/** Identifier for the {@link com.top_logic.graphic.flow.data.Border} type in JSON format. */
	String BORDER__TYPE = "Border";

	/** @see #getStrokeStyle() */
	String STROKE_STYLE__PROP = "strokeStyle";

	/** @see #getThickness() */
	String THICKNESS__PROP = "thickness";

	/** @see #isTop() */
	String TOP__PROP = "top";

	/** @see #isLeft() */
	String LEFT__PROP = "left";

	/** @see #isBottom() */
	String BOTTOM__PROP = "bottom";

	/** @see #isRight() */
	String RIGHT__PROP = "right";

	/** @see #getDashes() */
	String DASHES__PROP = "dashes";

	String getStrokeStyle();

	/**
	 * @see #getStrokeStyle()
	 */
	com.top_logic.graphic.flow.data.Border setStrokeStyle(String value);

	double getThickness();

	/**
	 * @see #getThickness()
	 */
	com.top_logic.graphic.flow.data.Border setThickness(double value);

	boolean isTop();

	/**
	 * @see #isTop()
	 */
	com.top_logic.graphic.flow.data.Border setTop(boolean value);

	boolean isLeft();

	/**
	 * @see #isLeft()
	 */
	com.top_logic.graphic.flow.data.Border setLeft(boolean value);

	boolean isBottom();

	/**
	 * @see #isBottom()
	 */
	com.top_logic.graphic.flow.data.Border setBottom(boolean value);

	boolean isRight();

	/**
	 * @see #isRight()
	 */
	com.top_logic.graphic.flow.data.Border setRight(boolean value);

	java.util.List<Double> getDashes();

	/**
	 * @see #getDashes()
	 */
	com.top_logic.graphic.flow.data.Border setDashes(java.util.List<? extends Double> value);

	/**
	 * Adds a value to the {@link #getDashes()} list.
	 */
	com.top_logic.graphic.flow.data.Border addDashe(double value);

	/**
	 * Removes a value from the {@link #getDashes()} list.
	 */
	void removeDashe(double value);

	@Override
	com.top_logic.graphic.flow.data.Border setContent(com.top_logic.graphic.flow.data.Box value);

	@Override
	com.top_logic.graphic.flow.data.Border setX(double value);

	@Override
	com.top_logic.graphic.flow.data.Border setY(double value);

	@Override
	com.top_logic.graphic.flow.data.Border setWidth(double value);

	@Override
	com.top_logic.graphic.flow.data.Border setHeight(double value);

	@Override
	com.top_logic.graphic.flow.data.Border setUserObject(java.lang.Object value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.Border readBorder(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.graphic.flow.data.Border) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert BORDER__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.graphic.flow.data.impl.Border_Impl result = new com.top_logic.graphic.flow.data.impl.Border_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

	@Override
	default Border self() {
		return this;
	}

}
