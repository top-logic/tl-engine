package com.top_logic.graphic.flow.data;

/**
 * An anchor point of a connection to a node of the diagram.
 */
public interface TreeConnector extends Widget, com.top_logic.graphic.flow.operations.tree.TreeConnectorOperations {

	/**
	 * Creates a {@link com.top_logic.graphic.flow.data.TreeConnector} instance.
	 */
	static com.top_logic.graphic.flow.data.TreeConnector create() {
		return new com.top_logic.graphic.flow.data.impl.TreeConnector_Impl();
	}

	/** Identifier for the {@link com.top_logic.graphic.flow.data.TreeConnector} type in JSON format. */
	String TREE_CONNECTOR__TYPE = "TreeConnector";

	/** @see #getConnection() */
	String CONNECTION__PROP = "connection";

	/** @see #getAnchor() */
	String ANCHOR__PROP = "anchor";

	/** @see #getConnectPosition() */
	String CONNECT_POSITION__PROP = "connectPosition";

	/**
	 * The connection that owns this connector.
	 */
	com.top_logic.graphic.flow.data.TreeConnection getConnection();

	/**
	 * Checks, whether {@link #getConnection()} has a value.
	 */
	boolean hasConnection();

	/**
	 * The connected box. The box is expected to be part of some top-level node of the owning {@link TreeLayout}.
	 */
	com.top_logic.graphic.flow.data.Box getAnchor();

	/**
	 * @see #getAnchor()
	 */
	com.top_logic.graphic.flow.data.TreeConnector setAnchor(com.top_logic.graphic.flow.data.Box value);

	/**
	 * Checks, whether {@link #getAnchor()} has a value.
	 */
	boolean hasAnchor();

	/**
	 * The starting point of the connection within the {@link #getAnchor()} box. 
	 * <p>
	 * The position describes the starting point by giving a percentage (0..1) of the length of the side of the {@link #getAnchor()} box, where this connection is placed. 
	 * The side of the {@link #getAnchor()} box where the connection starts depends on the {@link DiagramDirection} of the owning {@link TreeLayout}.
	 * </p>
	 */
	double getConnectPosition();

	/**
	 * @see #getConnectPosition()
	 */
	com.top_logic.graphic.flow.data.TreeConnector setConnectPosition(double value);

	@Override
	com.top_logic.graphic.flow.data.TreeConnector setCssClass(String value);

	@Override
	com.top_logic.graphic.flow.data.TreeConnector setUserObject(java.lang.Object value);

	@Override
	com.top_logic.graphic.flow.data.TreeConnector setClientId(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.TreeConnector readTreeConnector(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.graphic.flow.data.TreeConnector) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert TREE_CONNECTOR__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.graphic.flow.data.impl.TreeConnector_Impl result = new com.top_logic.graphic.flow.data.impl.TreeConnector_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

	@Override
	default TreeConnector self() {
		return this;
	}

}
