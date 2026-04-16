package com.top_logic.react.flow.data;

/**
 * Abstract base for Gantt chart decorations (lines, ranges).
 */
public interface GanttDecoration extends de.haumacher.msgbuf.graph.SharedGraphNode {

	/** Type codes for the {@link com.top_logic.react.flow.data.GanttDecoration} hierarchy. */
	public enum TypeKind {

		/** Type literal for {@link com.top_logic.react.flow.data.GanttLineDecoration}. */
		GANTT_LINE_DECORATION,

		/** Type literal for {@link com.top_logic.react.flow.data.GanttRangeDecoration}. */
		GANTT_RANGE_DECORATION,
		;

	}

	/** Visitor interface for the {@link com.top_logic.react.flow.data.GanttDecoration} hierarchy.*/
	public interface Visitor<R,A,E extends Throwable> {

		/** Visit case for {@link com.top_logic.react.flow.data.GanttLineDecoration}.*/
		R visit(com.top_logic.react.flow.data.GanttLineDecoration self, A arg) throws E;

		/** Visit case for {@link com.top_logic.react.flow.data.GanttRangeDecoration}.*/
		R visit(com.top_logic.react.flow.data.GanttRangeDecoration self, A arg) throws E;

	}

	/** @see #getId() */
	String ID__PROP = "id";

	/** @see #getUserObject() */
	String USER_OBJECT__PROP = "userObject";

	/** @see #getColor() */
	String COLOR__PROP = "color";

	/** @see #getLabel() */
	String LABEL__PROP = "label";

	/** @see #isCanMove() */
	String CAN_MOVE__PROP = "canMove";

	/** @see #getRelevantFor() */
	String RELEVANT_FOR__PROP = "relevantFor";

	/** The type code of this instance. */
	TypeKind kind();

	/**
	 * Opaque identifier, unique within a {@link GanttLayout}.
	 */
	String getId();

	/**
	 * @see #getId()
	 */
	com.top_logic.react.flow.data.GanttDecoration setId(String value);

	/**
	 * Application-defined business object backing this decoration.
	 * Server-only (transient); used as the identity from which the technical
	 * {@link #getId() id} is derived for client-side cross-references.
	 */
	java.lang.Object getUserObject();

	/**
	 * @see #getUserObject()
	 */
	com.top_logic.react.flow.data.GanttDecoration setUserObject(java.lang.Object value);

	/**
	 * Checks, whether {@link #getUserObject()} has a value.
	 */
	boolean hasUserObject();

	/**
	 * CSS-style color.
	 */
	String getColor();

	/**
	 * @see #getColor()
	 */
	com.top_logic.react.flow.data.GanttDecoration setColor(String value);

	/**
	 * Label box drawn near the decoration. The box is owned by the enclosing
	 * {@link GanttLayout#getContents() contents}, not by this decoration.
	 */
	com.top_logic.react.flow.data.Box getLabel();

	/**
	 * @see #getLabel()
	 */
	com.top_logic.react.flow.data.GanttDecoration setLabel(com.top_logic.react.flow.data.Box value);

	/**
	 * Checks, whether {@link #getLabel()} has a value.
	 */
	boolean hasLabel();

	/**
	 * Whether the user may drag the decoration along the time axis.
	 */
	boolean isCanMove();

	/**
	 * @see #isCanMove()
	 */
	com.top_logic.react.flow.data.GanttDecoration setCanMove(boolean value);

	/**
	 * Optional restriction: which rows this decoration applies to.
	 * Empty list means: all rows.
	 */
	java.util.List<String> getRelevantFor();

	/**
	 * @see #getRelevantFor()
	 */
	com.top_logic.react.flow.data.GanttDecoration setRelevantFor(java.util.List<? extends String> value);

	/**
	 * Adds a value to the {@link #getRelevantFor()} list.
	 */
	com.top_logic.react.flow.data.GanttDecoration addRelevantFor(String value);

	/**
	 * Removes a value from the {@link #getRelevantFor()} list.
	 */
	void removeRelevantFor(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.react.flow.data.GanttDecoration readGanttDecoration(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.react.flow.data.GanttDecoration) scope.resolveOrFail(in.nextInt());
		}
		com.top_logic.react.flow.data.GanttDecoration result;
		in.beginArray();
		String type = in.nextString();
		int id = in.nextInt();
		switch (type) {
			case GanttLineDecoration.GANTT_LINE_DECORATION__TYPE: result = com.top_logic.react.flow.data.GanttLineDecoration.create(); break;
			case GanttRangeDecoration.GANTT_RANGE_DECORATION__TYPE: result = com.top_logic.react.flow.data.GanttRangeDecoration.create(); break;
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
