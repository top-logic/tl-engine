package com.top_logic.graphic.flow.data;

/**
 * A layout that places boxes around some centerd content box.
 */
public interface CompassLayout extends Box, com.top_logic.graphic.flow.operations.layout.CompassLayoutOperations {

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

	/**
	 * The heading placed on top of the content.
	 */
	com.top_logic.graphic.flow.data.Box getNorth();

	/**
	 * @see #getNorth()
	 */
	com.top_logic.graphic.flow.data.CompassLayout setNorth(com.top_logic.graphic.flow.data.Box value);

	/**
	 * Checks, whether {@link #getNorth()} has a value.
	 */
	boolean hasNorth();

	/**
	 * Sidebar placed left to the content.
	 */
	com.top_logic.graphic.flow.data.Box getWest();

	/**
	 * @see #getWest()
	 */
	com.top_logic.graphic.flow.data.CompassLayout setWest(com.top_logic.graphic.flow.data.Box value);

	/**
	 * Checks, whether {@link #getWest()} has a value.
	 */
	boolean hasWest();

	/**
	 * Sidebar placed right to the content.
	 */
	com.top_logic.graphic.flow.data.Box getEast();

	/**
	 * @see #getEast()
	 */
	com.top_logic.graphic.flow.data.CompassLayout setEast(com.top_logic.graphic.flow.data.Box value);

	/**
	 * Checks, whether {@link #getEast()} has a value.
	 */
	boolean hasEast();

	/**
	 * Footer placed below th content.
	 */
	com.top_logic.graphic.flow.data.Box getSouth();

	/**
	 * @see #getSouth()
	 */
	com.top_logic.graphic.flow.data.CompassLayout setSouth(com.top_logic.graphic.flow.data.Box value);

	/**
	 * Checks, whether {@link #getSouth()} has a value.
	 */
	boolean hasSouth();

	/**
	 * The main content.
	 */
	com.top_logic.graphic.flow.data.Box getCenter();

	/**
	 * @see #getCenter()
	 */
	com.top_logic.graphic.flow.data.CompassLayout setCenter(com.top_logic.graphic.flow.data.Box value);

	/**
	 * Checks, whether {@link #getCenter()} has a value.
	 */
	boolean hasCenter();

	/**
	 * The height of the main content.
	 */
	double getCenterHeight();

	/**
	 * @see #getCenterHeight()
	 */
	com.top_logic.graphic.flow.data.CompassLayout setCenterHeight(double value);

	/**
	 * The horizontal padding around the center where the sidebars are placed.
	 */
	double getHPadding();

	/**
	 * @see #getHPadding()
	 */
	com.top_logic.graphic.flow.data.CompassLayout setHPadding(double value);

	/**
	 * The vertical padding around the center, where the header and footer is placed.
	 */
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

	@Override
	com.top_logic.graphic.flow.data.CompassLayout setCssClass(String value);

	@Override
	com.top_logic.graphic.flow.data.CompassLayout setUserObject(java.lang.Object value);

	@Override
	com.top_logic.graphic.flow.data.CompassLayout setClientId(String value);

	@Override
	com.top_logic.graphic.flow.data.CompassLayout setRenderInfo(java.lang.Object value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.CompassLayout readCompassLayout(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.graphic.flow.data.CompassLayout) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert COMPASS_LAYOUT__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.graphic.flow.data.impl.CompassLayout_Impl result = new com.top_logic.graphic.flow.data.impl.CompassLayout_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

	@Override
	default CompassLayout self() {
		return this;
	}

}
