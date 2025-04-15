package com.top_logic.graphic.flow.data;

public interface Border extends Decoration, com.top_logic.graphic.flow.model.BorderOperations {

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

	/** Identifier for the {@link com.top_logic.graphic.flow.data.Border} type in binary format. */
	static final int BORDER__TYPE_ID = 5;

	/** Identifier for the property {@link #getStrokeStyle()} in binary format. */
	static final int STROKE_STYLE__ID = 6;

	/** Identifier for the property {@link #getThickness()} in binary format. */
	static final int THICKNESS__ID = 7;

	/** Identifier for the property {@link #isTop()} in binary format. */
	static final int TOP__ID = 8;

	/** Identifier for the property {@link #isLeft()} in binary format. */
	static final int LEFT__ID = 9;

	/** Identifier for the property {@link #isBottom()} in binary format. */
	static final int BOTTOM__ID = 10;

	/** Identifier for the property {@link #isRight()} in binary format. */
	static final int RIGHT__ID = 11;

	/** Identifier for the property {@link #getDashes()} in binary format. */
	static final int DASHES__ID = 12;

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

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.Border readBorder(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.graphic.flow.data.impl.Border_Impl result = new com.top_logic.graphic.flow.data.impl.Border_Impl();
		result.readContent(in);
		return result;
	}

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.Border readBorder(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
		in.beginObject();
		com.top_logic.graphic.flow.data.Border result = com.top_logic.graphic.flow.data.impl.Border_Impl.readBorder_Content(in);
		in.endObject();
		return result;
	}

	@Override
	default Border self() {
		return this;
	}

	/** Creates a new {@link Border} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	public static Border readBorder(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		in.nextTag();
		return com.top_logic.graphic.flow.data.impl.Border_Impl.readBorder_XmlContent(in);
	}

}
