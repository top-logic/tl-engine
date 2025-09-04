package com.top_logic.graphic.flow.data;

/**
 * A connection in a {@link TreeLayout} that links contents of the diagram with one-to-many edges.
 */
public interface TreeConnection extends Widget, com.top_logic.graphic.flow.operations.tree.TreeConnectionOperations {

	/**
	 * Creates a {@link com.top_logic.graphic.flow.data.TreeConnection} instance.
	 */
	static com.top_logic.graphic.flow.data.TreeConnection create() {
		return new com.top_logic.graphic.flow.data.impl.TreeConnection_Impl();
	}

	/** Identifier for the {@link com.top_logic.graphic.flow.data.TreeConnection} type in JSON format. */
	String TREE_CONNECTION__TYPE = "TreeConnection";

	/** @see #getOwner() */
	String OWNER__PROP = "owner";

	/** @see #getParent() */
	String PARENT__PROP = "parent";

	/** @see #getChild() */
	String CHILD__PROP = "child";

	/** @see #getBarPosition() */
	String BAR_POSITION__PROP = "barPosition";

	/**
	 * The {@link TreeLayout} that is responsible for this connection.
	 */
	com.top_logic.graphic.flow.data.TreeLayout getOwner();

	/**
	 * Checks, whether {@link #getOwner()} has a value.
	 */
	boolean hasOwner();

	/**
	 * The connector that is anchored to the parent node of the connection.
	 */
	com.top_logic.graphic.flow.data.TreeConnector getParent();

	/**
	 * @see #getParent()
	 */
	com.top_logic.graphic.flow.data.TreeConnection setParent(com.top_logic.graphic.flow.data.TreeConnector value);

	/**
	 * Checks, whether {@link #getParent()} has a value.
	 */
	boolean hasParent();

	/**
	 * The connector that is  anchored to the child node of the connection.
	 */
	com.top_logic.graphic.flow.data.TreeConnector getChild();

	/**
	 * @see #getChild()
	 */
	com.top_logic.graphic.flow.data.TreeConnection setChild(com.top_logic.graphic.flow.data.TreeConnector value);

	/**
	 * Checks, whether {@link #getChild()} has a value.
	 */
	boolean hasChild();

	/**
	 * The placement of the connection bar that represents the fan-out from the one parent to its many children. 
	 * The bar is rendered perpendicular to the line connecting the nodes with the bar. 
	 * The position of the bar is given as absolute coordinate (X or Y depending on the layout direction) within 
	 * the coordinate system of the {@link TreeLayout}.
	 */
	double getBarPosition();

	/**
	 * @see #getBarPosition()
	 */
	com.top_logic.graphic.flow.data.TreeConnection setBarPosition(double value);

	@Override
	com.top_logic.graphic.flow.data.TreeConnection setCssClass(String value);

	@Override
	com.top_logic.graphic.flow.data.TreeConnection setUserObject(java.lang.Object value);

	@Override
	com.top_logic.graphic.flow.data.TreeConnection setClientId(String value);

	@Override
	com.top_logic.graphic.flow.data.TreeConnection setRenderInfo(java.lang.Object value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.TreeConnection readTreeConnection(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.graphic.flow.data.TreeConnection) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert TREE_CONNECTION__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.graphic.flow.data.impl.TreeConnection_Impl result = new com.top_logic.graphic.flow.data.impl.TreeConnection_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

	@Override
	default TreeConnection self() {
		return this;
	}

}
