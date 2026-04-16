package com.top_logic.react.flow.data;

/**
 * Abstract base for entries on a Gantt time axis.
 */
public interface GanttItem extends de.haumacher.msgbuf.graph.SharedGraphNode {

	/** Type codes for the {@link com.top_logic.react.flow.data.GanttItem} hierarchy. */
	public enum TypeKind {

		/** Type literal for {@link com.top_logic.react.flow.data.GanttSpan}. */
		GANTT_SPAN,

		/** Type literal for {@link com.top_logic.react.flow.data.GanttMilestone}. */
		GANTT_MILESTONE,
		;

	}

	/** Visitor interface for the {@link com.top_logic.react.flow.data.GanttItem} hierarchy.*/
	public interface Visitor<R,A,E extends Throwable> {

		/** Visit case for {@link com.top_logic.react.flow.data.GanttSpan}.*/
		R visit(com.top_logic.react.flow.data.GanttSpan self, A arg) throws E;

		/** Visit case for {@link com.top_logic.react.flow.data.GanttMilestone}.*/
		R visit(com.top_logic.react.flow.data.GanttMilestone self, A arg) throws E;

	}

	/** @see #getId() */
	String ID__PROP = "id";

	/** @see #getRowId() */
	String ROW_ID__PROP = "rowId";

	/** @see #getBox() */
	String BOX__PROP = "box";

	/** @see #isCanMoveTime() */
	String CAN_MOVE_TIME__PROP = "canMoveTime";

	/** @see #isCanMoveRow() */
	String CAN_MOVE_ROW__PROP = "canMoveRow";

	/** @see #isCanBeEdgeSource() */
	String CAN_BE_EDGE_SOURCE__PROP = "canBeEdgeSource";

	/** @see #isCanBeEdgeTarget() */
	String CAN_BE_EDGE_TARGET__PROP = "canBeEdgeTarget";

	/** The type code of this instance. */
	TypeKind kind();

	/**
	 * Opaque identifier, unique within a {@link GanttLayout}.
	 */
	String getId();

	/**
	 * @see #getId()
	 */
	com.top_logic.react.flow.data.GanttItem setId(String value);

	/**
	 * Reference to the {@link GanttRow#getId()} this item lives in.
	 */
	String getRowId();

	/**
	 * @see #getRowId()
	 */
	com.top_logic.react.flow.data.GanttItem setRowId(String value);

	/**
	 * Rendering content; positioned by the layout, drawn via its own operations.
	 *
	 * <p>
	 * The box is owned by the enclosing {@link GanttLayout#getContents() contents},
	 * not by this item.
	 * </p>
	 */
	com.top_logic.react.flow.data.Box getBox();

	/**
	 * @see #getBox()
	 */
	com.top_logic.react.flow.data.GanttItem setBox(com.top_logic.react.flow.data.Box value);

	/**
	 * Checks, whether {@link #getBox()} has a value.
	 */
	boolean hasBox();

	/**
	 * Whether the user may drag this item along the time axis.
	 */
	boolean isCanMoveTime();

	/**
	 * @see #isCanMoveTime()
	 */
	com.top_logic.react.flow.data.GanttItem setCanMoveTime(boolean value);

	/**
	 * Whether the user may drag this item to a different row.
	 */
	boolean isCanMoveRow();

	/**
	 * @see #isCanMoveRow()
	 */
	com.top_logic.react.flow.data.GanttItem setCanMoveRow(boolean value);

	/**
	 * Whether a new dependency edge may originate from this item.
	 */
	boolean isCanBeEdgeSource();

	/**
	 * @see #isCanBeEdgeSource()
	 */
	com.top_logic.react.flow.data.GanttItem setCanBeEdgeSource(boolean value);

	/**
	 * Whether a new dependency edge may terminate at this item.
	 */
	boolean isCanBeEdgeTarget();

	/**
	 * @see #isCanBeEdgeTarget()
	 */
	com.top_logic.react.flow.data.GanttItem setCanBeEdgeTarget(boolean value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.react.flow.data.GanttItem readGanttItem(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.react.flow.data.GanttItem) scope.resolveOrFail(in.nextInt());
		}
		com.top_logic.react.flow.data.GanttItem result;
		in.beginArray();
		String type = in.nextString();
		int id = in.nextInt();
		switch (type) {
			case GanttSpan.GANTT_SPAN__TYPE: result = com.top_logic.react.flow.data.GanttSpan.create(); break;
			case GanttMilestone.GANTT_MILESTONE__TYPE: result = com.top_logic.react.flow.data.GanttMilestone.create(); break;
			default: in.skipValue(); result = null; break;
		}
		if (result != null) {
			scope.readData(result, id, in);
		}
		in.endArray();
		return result;
	}

	/** Accepts the given visitor. */
	public abstract <R,A,E extends Throwable> R visit(Visitor<R,A,E> v, A arg) throws E;

}
