package com.top_logic.graphic.flow.data;

/**
 * A polygonal chain in the diagram.
 */
public interface PolygonalChain extends Box, com.top_logic.graphic.flow.operations.PolygonalChainOperations {

	/**
	 * Creates a {@link com.top_logic.graphic.flow.data.PolygonalChain} instance.
	 */
	static com.top_logic.graphic.flow.data.PolygonalChain create() {
		return new com.top_logic.graphic.flow.data.impl.PolygonalChain_Impl();
	}

	/** Identifier for the {@link com.top_logic.graphic.flow.data.PolygonalChain} type in JSON format. */
	String POLYGONAL_CHAIN__TYPE = "PolygonalChain";

	/** @see #getPoints() */
	String POINTS__PROP = "points";

	/** @see #isClosed() */
	String CLOSED__PROP = "closed";

	/** @see #getStrokeStyle() */
	String STROKE_STYLE__PROP = "strokeStyle";

	/** @see #getFillStyle() */
	String FILL_STYLE__PROP = "fillStyle";

	/** @see #getThickness() */
	String THICKNESS__PROP = "thickness";

	/**
	 * The corners in the polygonal chain.
	 */
	java.util.List<com.top_logic.graphic.flow.data.Point> getPoints();

	/**
	 * @see #getPoints()
	 */
	com.top_logic.graphic.flow.data.PolygonalChain setPoints(java.util.List<? extends com.top_logic.graphic.flow.data.Point> value);

	/**
	 * Adds a value to the {@link #getPoints()} list.
	 */
	com.top_logic.graphic.flow.data.PolygonalChain addPoint(com.top_logic.graphic.flow.data.Point value);

	/**
	 * Removes a value from the {@link #getPoints()} list.
	 */
	void removePoint(com.top_logic.graphic.flow.data.Point value);

	/**
	 * Whether a polygonal chain is closed, i.e. there is an additional synthetic line between the first and the last point.
	 */
	boolean isClosed();

	/**
	 * @see #isClosed()
	 */
	com.top_logic.graphic.flow.data.PolygonalChain setClosed(boolean value);

	/**
	 * The SVG <code>stroke</code> style.
	 */
	String getStrokeStyle();

	/**
	 * @see #getStrokeStyle()
	 */
	com.top_logic.graphic.flow.data.PolygonalChain setStrokeStyle(String value);

	/**
	 * Checks, whether {@link #getStrokeStyle()} has a value.
	 */
	boolean hasStrokeStyle();

	/**
	 * The SVG <code>fill</code> style.
	 */
	String getFillStyle();

	/**
	 * @see #getFillStyle()
	 */
	com.top_logic.graphic.flow.data.PolygonalChain setFillStyle(String value);

	/**
	 * Checks, whether {@link #getFillStyle()} has a value.
	 */
	boolean hasFillStyle();

	/**
	 * Stroke width of lines.
	 */
	double getThickness();

	/**
	 * @see #getThickness()
	 */
	com.top_logic.graphic.flow.data.PolygonalChain setThickness(double value);

	@Override
	com.top_logic.graphic.flow.data.PolygonalChain setX(double value);

	@Override
	com.top_logic.graphic.flow.data.PolygonalChain setY(double value);

	@Override
	com.top_logic.graphic.flow.data.PolygonalChain setWidth(double value);

	@Override
	com.top_logic.graphic.flow.data.PolygonalChain setHeight(double value);

	@Override
	com.top_logic.graphic.flow.data.PolygonalChain setCssClass(String value);

	@Override
	com.top_logic.graphic.flow.data.PolygonalChain setUserObject(java.lang.Object value);

	@Override
	com.top_logic.graphic.flow.data.PolygonalChain setClientId(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.PolygonalChain readPolygonalChain(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.graphic.flow.data.PolygonalChain) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert POLYGONAL_CHAIN__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.graphic.flow.data.impl.PolygonalChain_Impl result = new com.top_logic.graphic.flow.data.impl.PolygonalChain_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

	@Override
	default PolygonalChain self() {
		return this;
	}

}
