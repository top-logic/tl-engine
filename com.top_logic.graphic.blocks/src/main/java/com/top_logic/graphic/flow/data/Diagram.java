package com.top_logic.graphic.flow.data;

/**
 * Top-level element that can be rendered.
 */
public interface Diagram extends Widget, com.top_logic.graphic.flow.operations.DiagramOperations {

	/**
	 * Creates a {@link com.top_logic.graphic.flow.data.Diagram} instance.
	 */
	static com.top_logic.graphic.flow.data.Diagram create() {
		return new com.top_logic.graphic.flow.data.impl.Diagram_Impl();
	}

	/** Identifier for the {@link com.top_logic.graphic.flow.data.Diagram} type in JSON format. */
	String DIAGRAM__TYPE = "Diagram";

	/** @see #getRoot() */
	String ROOT__PROP = "root";

	/** @see #getSelection() */
	String SELECTION__PROP = "selection";

	/**
	 * The top-level diagram element.
	 */
	com.top_logic.graphic.flow.data.Box getRoot();

	/**
	 * @see #getRoot()
	 */
	com.top_logic.graphic.flow.data.Diagram setRoot(com.top_logic.graphic.flow.data.Box value);

	/**
	 * Checks, whether {@link #getRoot()} has a value.
	 */
	boolean hasRoot();

	java.util.List<com.top_logic.graphic.flow.data.Widget> getSelection();

	/**
	 * @see #getSelection()
	 */
	com.top_logic.graphic.flow.data.Diagram setSelection(java.util.List<? extends com.top_logic.graphic.flow.data.Widget> value);

	/**
	 * Adds a value to the {@link #getSelection()} list.
	 */
	com.top_logic.graphic.flow.data.Diagram addSelection(com.top_logic.graphic.flow.data.Widget value);

	/**
	 * Removes a value from the {@link #getSelection()} list.
	 */
	void removeSelection(com.top_logic.graphic.flow.data.Widget value);

	@Override
	com.top_logic.graphic.flow.data.Diagram setUserObject(java.lang.Object value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.Diagram readDiagram(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.graphic.flow.data.Diagram) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert DIAGRAM__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.graphic.flow.data.impl.Diagram_Impl result = new com.top_logic.graphic.flow.data.impl.Diagram_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

	@Override
	default Diagram self() {
		return this;
	}

}
