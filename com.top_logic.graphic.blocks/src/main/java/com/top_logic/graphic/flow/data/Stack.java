package com.top_logic.graphic.flow.data;

/**
 * Write several boxes on top of each other.
 */
public interface Stack extends Box, com.top_logic.graphic.flow.operations.StackOperations {

	/**
	 * Creates a {@link com.top_logic.graphic.flow.data.Stack} instance.
	 */
	static com.top_logic.graphic.flow.data.Stack create() {
		return new com.top_logic.graphic.flow.data.impl.Stack_Impl();
	}

	/** Identifier for the {@link com.top_logic.graphic.flow.data.Stack} type in JSON format. */
	String STACK__TYPE = "Stack";

	/** @see #getContents() */
	String CONTENTS__PROP = "contents";

	/**
	 * The boxes that are rendered one over the other. The box that is on the bottom is the first in the list.
	 */
	java.util.List<com.top_logic.graphic.flow.data.Box> getContents();

	/**
	 * @see #getContents()
	 */
	com.top_logic.graphic.flow.data.Stack setContents(java.util.List<? extends com.top_logic.graphic.flow.data.Box> value);

	/**
	 * Adds a value to the {@link #getContents()} list.
	 */
	com.top_logic.graphic.flow.data.Stack addContent(com.top_logic.graphic.flow.data.Box value);

	/**
	 * Removes a value from the {@link #getContents()} list.
	 */
	void removeContent(com.top_logic.graphic.flow.data.Box value);

	@Override
	com.top_logic.graphic.flow.data.Stack setX(double value);

	@Override
	com.top_logic.graphic.flow.data.Stack setY(double value);

	@Override
	com.top_logic.graphic.flow.data.Stack setWidth(double value);

	@Override
	com.top_logic.graphic.flow.data.Stack setHeight(double value);

	@Override
	com.top_logic.graphic.flow.data.Stack setCssClass(String value);

	@Override
	com.top_logic.graphic.flow.data.Stack setUserObject(java.lang.Object value);

	@Override
	com.top_logic.graphic.flow.data.Stack setClientId(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.Stack readStack(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.graphic.flow.data.Stack) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert STACK__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.graphic.flow.data.impl.Stack_Impl result = new com.top_logic.graphic.flow.data.impl.Stack_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

	@Override
	default Stack self() {
		return this;
	}

}
