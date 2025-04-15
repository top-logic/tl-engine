package com.top_logic.graphic.flow.data;

public interface CompassLayout extends Box, com.top_logic.graphic.flow.model.layout.CompassLayoutOperations {

	/**
	 * Creates a {@link com.top_logic.graphic.flow.data.CompassLayout} instance.
	 */
	static com.top_logic.graphic.flow.data.CompassLayout create() {
		return new com.top_logic.graphic.flow.data.impl.CompassLayout_Impl();
	}

	/** Identifier for the {@link com.top_logic.graphic.flow.data.CompassLayout} type in JSON format. */
	String COMPASS_LAYOUT__TYPE = "CompassLayout";

	/** @see #getNorth() */
	String NORTH__PROP = "north";

	/** @see #getWest() */
	String WEST__PROP = "west";

	/** @see #getEast() */
	String EAST__PROP = "east";

	/** @see #getSouth() */
	String SOUTH__PROP = "south";

	/** @see #getCenter() */
	String CENTER__PROP = "center";

	/** @see #getCenterHeight() */
	String CENTER_HEIGHT__PROP = "centerHeight";

	/** @see #getHPadding() */
	String H_PADDING__PROP = "hPadding";

	/** @see #getVPadding() */
	String V_PADDING__PROP = "vPadding";

	/** Identifier for the {@link com.top_logic.graphic.flow.data.CompassLayout} type in binary format. */
	static final int COMPASS_LAYOUT__TYPE_ID = 8;

	/** Identifier for the property {@link #getNorth()} in binary format. */
	static final int NORTH__ID = 5;

	/** Identifier for the property {@link #getWest()} in binary format. */
	static final int WEST__ID = 6;

	/** Identifier for the property {@link #getEast()} in binary format. */
	static final int EAST__ID = 7;

	/** Identifier for the property {@link #getSouth()} in binary format. */
	static final int SOUTH__ID = 8;

	/** Identifier for the property {@link #getCenter()} in binary format. */
	static final int CENTER__ID = 9;

	/** Identifier for the property {@link #getCenterHeight()} in binary format. */
	static final int CENTER_HEIGHT__ID = 10;

	/** Identifier for the property {@link #getHPadding()} in binary format. */
	static final int H_PADDING__ID = 11;

	/** Identifier for the property {@link #getVPadding()} in binary format. */
	static final int V_PADDING__ID = 12;

	com.top_logic.graphic.flow.data.Box getNorth();

	/**
	 * @see #getNorth()
	 */
	com.top_logic.graphic.flow.data.CompassLayout setNorth(com.top_logic.graphic.flow.data.Box value);

	/**
	 * Checks, whether {@link #getNorth()} has a value.
	 */
	boolean hasNorth();

	com.top_logic.graphic.flow.data.Box getWest();

	/**
	 * @see #getWest()
	 */
	com.top_logic.graphic.flow.data.CompassLayout setWest(com.top_logic.graphic.flow.data.Box value);

	/**
	 * Checks, whether {@link #getWest()} has a value.
	 */
	boolean hasWest();

	com.top_logic.graphic.flow.data.Box getEast();

	/**
	 * @see #getEast()
	 */
	com.top_logic.graphic.flow.data.CompassLayout setEast(com.top_logic.graphic.flow.data.Box value);

	/**
	 * Checks, whether {@link #getEast()} has a value.
	 */
	boolean hasEast();

	com.top_logic.graphic.flow.data.Box getSouth();

	/**
	 * @see #getSouth()
	 */
	com.top_logic.graphic.flow.data.CompassLayout setSouth(com.top_logic.graphic.flow.data.Box value);

	/**
	 * Checks, whether {@link #getSouth()} has a value.
	 */
	boolean hasSouth();

	com.top_logic.graphic.flow.data.Box getCenter();

	/**
	 * @see #getCenter()
	 */
	com.top_logic.graphic.flow.data.CompassLayout setCenter(com.top_logic.graphic.flow.data.Box value);

	/**
	 * Checks, whether {@link #getCenter()} has a value.
	 */
	boolean hasCenter();

	double getCenterHeight();

	/**
	 * @see #getCenterHeight()
	 */
	com.top_logic.graphic.flow.data.CompassLayout setCenterHeight(double value);

	double getHPadding();

	/**
	 * @see #getHPadding()
	 */
	com.top_logic.graphic.flow.data.CompassLayout setHPadding(double value);

	double getVPadding();

	/**
	 * @see #getVPadding()
	 */
	com.top_logic.graphic.flow.data.CompassLayout setVPadding(double value);

	@Override
	com.top_logic.graphic.flow.data.CompassLayout setX(double value);

	@Override
	com.top_logic.graphic.flow.data.CompassLayout setY(double value);

	@Override
	com.top_logic.graphic.flow.data.CompassLayout setWidth(double value);

	@Override
	com.top_logic.graphic.flow.data.CompassLayout setHeight(double value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.CompassLayout readCompassLayout(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.graphic.flow.data.impl.CompassLayout_Impl result = new com.top_logic.graphic.flow.data.impl.CompassLayout_Impl();
		result.readContent(in);
		return result;
	}

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.CompassLayout readCompassLayout(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
		in.beginObject();
		com.top_logic.graphic.flow.data.CompassLayout result = com.top_logic.graphic.flow.data.impl.CompassLayout_Impl.readCompassLayout_Content(in);
		in.endObject();
		return result;
	}

	@Override
	default CompassLayout self() {
		return this;
	}

	/** Creates a new {@link CompassLayout} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	public static CompassLayout readCompassLayout(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		in.nextTag();
		return com.top_logic.graphic.flow.data.impl.CompassLayout_Impl.readCompassLayout_XmlContent(in);
	}

}
