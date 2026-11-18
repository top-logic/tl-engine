package com.top_logic.react.flow.data;

/**
 * A box marking the target of a drop on the diagram: the drop of a flow diagram is made onto the
 * user object of the region, or on the diagram as a whole for a region without one.
 */
public interface DropRegion extends com.top_logic.react.flow.data.Decoration, com.top_logic.react.flow.operations.DropRegionOperations {

	/**
	 * Creates a {@link com.top_logic.react.flow.data.DropRegion} instance.
	 */
	static com.top_logic.react.flow.data.DropRegion create() {
		return new com.top_logic.react.flow.data.impl.DropRegion_Impl();
	}

	/** Identifier for the {@link com.top_logic.react.flow.data.DropRegion} type in JSON format. */
	String DROP_REGION__TYPE = "DropRegion";

	@Override
	com.top_logic.react.flow.data.DropRegion setContent(com.top_logic.react.flow.data.Box value);

	@Override
	com.top_logic.react.flow.data.DropRegion setX(double value);

	@Override
	com.top_logic.react.flow.data.DropRegion setY(double value);

	@Override
	com.top_logic.react.flow.data.DropRegion setWidth(double value);

	@Override
	com.top_logic.react.flow.data.DropRegion setHeight(double value);

	@Override
	com.top_logic.react.flow.data.DropRegion setCssClass(String value);

	@Override
	com.top_logic.react.flow.data.DropRegion setUserObject(java.lang.Object value);

	@Override
	com.top_logic.react.flow.data.DropRegion setClientId(String value);

	@Override
	com.top_logic.react.flow.data.DropRegion setRenderInfo(java.lang.Object value);

	@Override
	com.top_logic.react.flow.data.DropRegion setDropTarget(boolean value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.react.flow.data.DropRegion readDropRegion(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.react.flow.data.DropRegion) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert DROP_REGION__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.react.flow.data.impl.DropRegion_Impl result = new com.top_logic.react.flow.data.impl.DropRegion_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

	@Override
	default DropRegion self() {
		return this;
	}

}
