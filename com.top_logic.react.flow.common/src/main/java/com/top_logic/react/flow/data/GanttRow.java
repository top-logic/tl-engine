package com.top_logic.react.flow.data;

/**
 * A row in a {@link GanttLayout}. Rows form a forest (tree of trees).
 */
public interface GanttRow extends de.haumacher.msgbuf.graph.SharedGraphNode {

	/**
	 * Creates a {@link com.top_logic.react.flow.data.GanttRow} instance.
	 */
	static com.top_logic.react.flow.data.GanttRow create() {
		return new com.top_logic.react.flow.data.impl.GanttRow_Impl();
	}

	/** Identifier for the {@link com.top_logic.react.flow.data.GanttRow} type in JSON format. */
	String GANTT_ROW__TYPE = "GanttRow";

	/** @see #getId() */
	String ID__PROP = "id";

	/** @see #getLabel() */
	String LABEL__PROP = "label";

	/** @see #getChildren() */
	String CHILDREN__PROP = "children";

	/**
	 * Opaque identifier, unique within a {@link GanttLayout}.
	 */
	String getId();

	/**
	 * @see #getId()
	 */
	com.top_logic.react.flow.data.GanttRow setId(String value);

	/**
	 * Human-readable label shown at the row header.
	 */
	String getLabel();

	/**
	 * @see #getLabel()
	 */
	com.top_logic.react.flow.data.GanttRow setLabel(String value);

	/**
	 * Child rows forming a hierarchy. Phase 1 renders the tree flat with indentation.
	 */
	java.util.List<com.top_logic.react.flow.data.GanttRow> getChildren();

	/**
	 * @see #getChildren()
	 */
	com.top_logic.react.flow.data.GanttRow setChildren(java.util.List<? extends com.top_logic.react.flow.data.GanttRow> value);

	/**
	 * Adds a value to the {@link #getChildren()} list.
	 */
	com.top_logic.react.flow.data.GanttRow addChildren(com.top_logic.react.flow.data.GanttRow value);

	/**
	 * Removes a value from the {@link #getChildren()} list.
	 */
	void removeChildren(com.top_logic.react.flow.data.GanttRow value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.react.flow.data.GanttRow readGanttRow(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.react.flow.data.GanttRow) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert GANTT_ROW__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.react.flow.data.impl.GanttRow_Impl result = new com.top_logic.react.flow.data.impl.GanttRow_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

}
