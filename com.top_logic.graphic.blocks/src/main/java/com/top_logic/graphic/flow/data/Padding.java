package com.top_logic.graphic.flow.data;

public interface Padding extends Decoration, com.top_logic.graphic.flow.model.PaddingOperations {

	/**
	 * Creates a {@link com.top_logic.graphic.flow.data.Padding} instance.
	 */
	static com.top_logic.graphic.flow.data.Padding create() {
		return new com.top_logic.graphic.flow.data.impl.Padding_Impl();
	}

	/** Identifier for the {@link com.top_logic.graphic.flow.data.Padding} type in JSON format. */
	String PADDING__TYPE = "Padding";

	/** @see #getTop() */
	String TOP__PROP = "top";

	/** @see #getLeft() */
	String LEFT__PROP = "left";

	/** @see #getBottom() */
	String BOTTOM__PROP = "bottom";

	/** @see #getRight() */
	String RIGHT__PROP = "right";

	/** Identifier for the {@link com.top_logic.graphic.flow.data.Padding} type in binary format. */
	static final int PADDING__TYPE_ID = 7;

	/** Identifier for the property {@link #getTop()} in binary format. */
	static final int TOP__ID = 6;

	/** Identifier for the property {@link #getLeft()} in binary format. */
	static final int LEFT__ID = 7;

	/** Identifier for the property {@link #getBottom()} in binary format. */
	static final int BOTTOM__ID = 8;

	/** Identifier for the property {@link #getRight()} in binary format. */
	static final int RIGHT__ID = 9;

	double getTop();

	/**
	 * @see #getTop()
	 */
	com.top_logic.graphic.flow.data.Padding setTop(double value);

	double getLeft();

	/**
	 * @see #getLeft()
	 */
	com.top_logic.graphic.flow.data.Padding setLeft(double value);

	double getBottom();

	/**
	 * @see #getBottom()
	 */
	com.top_logic.graphic.flow.data.Padding setBottom(double value);

	double getRight();

	/**
	 * @see #getRight()
	 */
	com.top_logic.graphic.flow.data.Padding setRight(double value);

	@Override
	com.top_logic.graphic.flow.data.Padding setContent(com.top_logic.graphic.flow.data.Box value);

	@Override
	com.top_logic.graphic.flow.data.Padding setX(double value);

	@Override
	com.top_logic.graphic.flow.data.Padding setY(double value);

	@Override
	com.top_logic.graphic.flow.data.Padding setWidth(double value);

	@Override
	com.top_logic.graphic.flow.data.Padding setHeight(double value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.Padding readPadding(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.graphic.flow.data.impl.Padding_Impl result = new com.top_logic.graphic.flow.data.impl.Padding_Impl();
		result.readContent(in);
		return result;
	}

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.Padding readPadding(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
		in.beginObject();
		com.top_logic.graphic.flow.data.Padding result = com.top_logic.graphic.flow.data.impl.Padding_Impl.readPadding_Content(in);
		in.endObject();
		return result;
	}

	@Override
	default Padding self() {
		return this;
	}

	/** Creates a new {@link Padding} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	public static Padding readPadding(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		in.nextTag();
		return com.top_logic.graphic.flow.data.impl.Padding_Impl.readPadding_XmlContent(in);
	}

}
