package com.top_logic.graphic.flow.data;

/**
 * A layout that connects placed nodes with one-to-many connectors forming a tree.
 */
public interface TreeLayout extends FloatingLayout, com.top_logic.graphic.flow.model.tree.TreeLayoutOperations {

	/**
	 * Creates a {@link com.top_logic.graphic.flow.data.TreeLayout} instance.
	 */
	static com.top_logic.graphic.flow.data.TreeLayout create() {
		return new com.top_logic.graphic.flow.data.impl.TreeLayout_Impl();
	}

	/** Identifier for the {@link com.top_logic.graphic.flow.data.TreeLayout} type in JSON format. */
	String TREE_LAYOUT__TYPE = "TreeLayout";

	/** @see #isCompact() */
	String COMPACT__PROP = "compact";

	/** @see #getDirection() */
	String DIRECTION__PROP = "direction";

	/** @see #getGapX() */
	String GAP_X__PROP = "gapX";

	/** @see #getGapY() */
	String GAP_Y__PROP = "gapY";

	/** @see #getStrokeStyle() */
	String STROKE_STYLE__PROP = "strokeStyle";

	/** @see #getThickness() */
	String THICKNESS__PROP = "thickness";

	/** @see #getConnections() */
	String CONNECTIONS__PROP = "connections";

	/**
	 * Whether to minimize vertical space required to layout the tree.
	 */
	boolean isCompact();

	/**
	 * @see #isCompact()
	 */
	com.top_logic.graphic.flow.data.TreeLayout setCompact(boolean value);

	com.top_logic.graphic.flow.data.DiagramDirection getDirection();

	/**
	 * @see #getDirection()
	 */
	com.top_logic.graphic.flow.data.TreeLayout setDirection(com.top_logic.graphic.flow.data.DiagramDirection value);

	/**
	 * Horizontal gap between columns of tree nodes.
	 */
	double getGapX();

	/**
	 * @see #getGapX()
	 */
	com.top_logic.graphic.flow.data.TreeLayout setGapX(double value);

	/**
	 * Vertical gap between nodes in a tree column.
	 */
	double getGapY();

	/**
	 * @see #getGapY()
	 */
	com.top_logic.graphic.flow.data.TreeLayout setGapY(double value);

	/**
	 * Stroke style of tree connections.
	 */
	String getStrokeStyle();

	/**
	 * @see #getStrokeStyle()
	 */
	com.top_logic.graphic.flow.data.TreeLayout setStrokeStyle(String value);

	/**
	 * Stroke width of tree connections.
	 */
	double getThickness();

	/**
	 * @see #getThickness()
	 */
	com.top_logic.graphic.flow.data.TreeLayout setThickness(double value);

	java.util.List<com.top_logic.graphic.flow.data.TreeConnection> getConnections();

	/**
	 * @see #getConnections()
	 */
	com.top_logic.graphic.flow.data.TreeLayout setConnections(java.util.List<? extends com.top_logic.graphic.flow.data.TreeConnection> value);

	/**
	 * Adds a value to the {@link #getConnections()} list.
	 */
	com.top_logic.graphic.flow.data.TreeLayout addConnection(com.top_logic.graphic.flow.data.TreeConnection value);

	/**
	 * Removes a value from the {@link #getConnections()} list.
	 */
	void removeConnection(com.top_logic.graphic.flow.data.TreeConnection value);

	@Override
	com.top_logic.graphic.flow.data.TreeLayout setNodes(java.util.List<? extends com.top_logic.graphic.flow.data.Box> value);

	@Override
	com.top_logic.graphic.flow.data.TreeLayout addNode(com.top_logic.graphic.flow.data.Box value);

	@Override
	com.top_logic.graphic.flow.data.TreeLayout setX(double value);

	@Override
	com.top_logic.graphic.flow.data.TreeLayout setY(double value);

	@Override
	com.top_logic.graphic.flow.data.TreeLayout setWidth(double value);

	@Override
	com.top_logic.graphic.flow.data.TreeLayout setHeight(double value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.TreeLayout readTreeLayout(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.graphic.flow.data.TreeLayout) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert TREE_LAYOUT__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.graphic.flow.data.impl.TreeLayout_Impl result = new com.top_logic.graphic.flow.data.impl.TreeLayout_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

	@Override
	default TreeLayout self() {
		return this;
	}

	/** Creates a new {@link TreeLayout} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	public static TreeLayout readTreeLayout(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		in.nextTag();
		return com.top_logic.graphic.flow.data.impl.TreeLayout_Impl.readTreeLayout_XmlContent(in);
	}

}
