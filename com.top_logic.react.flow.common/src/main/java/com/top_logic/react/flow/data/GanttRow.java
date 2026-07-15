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

	/** @see #getUserObject() */
	String USER_OBJECT__PROP = "userObject";

	/** @see #getLabel() */
	String LABEL__PROP = "label";

	/** @see #getChildren() */
	String CHILDREN__PROP = "children";

	/** @see #isAcceptsDrop() */
	String ACCEPTS_DROP__PROP = "acceptsDrop";

	/** @see #getRowPadding() */
	String ROW_PADDING__PROP = "rowPadding";

	/** @see #getMinContentHeight() */
	String MIN_CONTENT_HEIGHT__PROP = "minContentHeight";

	/** @see #getBackgroundColor() */
	String BACKGROUND_COLOR__PROP = "backgroundColor";

	/** @see #getBorderColor() */
	String BORDER_COLOR__PROP = "borderColor";

	/**
	 * Opaque identifier, unique within a {@link GanttLayout}.
	 */
	String getId();

	/**
	 * @see #getId()
	 */
	com.top_logic.react.flow.data.GanttRow setId(String value);

	/**
	 * Application-defined business object backing this row.
	 * Server-only (transient); used as the identity from which the technical
	 * {@link #getId() id} is derived for client-side cross-references.
	 */
	java.lang.Object getUserObject();

	/**
	 * @see #getUserObject()
	 */
	com.top_logic.react.flow.data.GanttRow setUserObject(java.lang.Object value);

	/**
	 * Checks, whether {@link #getUserObject()} has a value.
	 */
	boolean hasUserObject();

	/**
	 * Rendering content shown at the row header.
	 *
	 * <p>
	 * The box is owned by the enclosing {@link GanttLayout#getContents() contents} and is
	 * positioned by the layout algorithm. Applications can use any {@link Box} subtype here:
	 * plain text, icon+text, badges, etc.
	 * </p>
	 */
	com.top_logic.react.flow.data.Box getLabel();

	/**
	 * @see #getLabel()
	 */
	com.top_logic.react.flow.data.GanttRow setLabel(com.top_logic.react.flow.data.Box value);

	/**
	 * Checks, whether {@link #getLabel()} has a value.
	 */
	boolean hasLabel();

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

	/**
	 * Whether items may be dropped into this row during drag. Default true.
	 */
	boolean isAcceptsDrop();

	/**
	 * @see #isAcceptsDrop()
	 */
	com.top_logic.react.flow.data.GanttRow setAcceptsDrop(boolean value);

	/**
	 * Vertical padding (top + bottom) inside this row, in pixels.
	 */
	double getRowPadding();

	/**
	 * @see #getRowPadding()
	 */
	com.top_logic.react.flow.data.GanttRow setRowPadding(double value);

	/**
	 * Minimum content height for this row, in pixels.
	 */
	double getMinContentHeight();

	/**
	 * @see #getMinContentHeight()
	 */
	com.top_logic.react.flow.data.GanttRow setMinContentHeight(double value);

	/**
	 * Background color of the row lane. Empty = transparent (no background drawn).
	 */
	String getBackgroundColor();

	/**
	 * @see #getBackgroundColor()
	 */
	com.top_logic.react.flow.data.GanttRow setBackgroundColor(String value);

	/**
	 * Checks, whether {@link #getBackgroundColor()} has a value.
	 */
	boolean hasBackgroundColor();

	/**
	 * Stroke color of the row lane border. Empty = no border.
	 */
	String getBorderColor();

	/**
	 * @see #getBorderColor()
	 */
	com.top_logic.react.flow.data.GanttRow setBorderColor(String value);

	/**
	 * Checks, whether {@link #getBorderColor()} has a value.
	 */
	boolean hasBorderColor();

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
