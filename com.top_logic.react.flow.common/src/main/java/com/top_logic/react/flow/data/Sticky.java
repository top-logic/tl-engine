package com.top_logic.react.flow.data;

/**
 * A box keeping its content at the left edge of the visible time range of a horizontally
 * scrolled Gantt chart, like <code>position: sticky</code> in CSS.
 *
 * <p>
 * The box takes the whole width it is given (e.g. the width of a span) and gives its content its
 * natural width. While the box is scrolled partly out of the visible area to the left, its
 * content moves right to the left edge of the visible area, but at most so far that it still ends
 * at the right edge of the box. Borders and backgrounds around the box stay in place, only the
 * content moves.
 * </p>
 *
 * <p>
 * The content is sized with the whole width of the box, so that a level-of-detail box in the
 * content chooses its variant by the width of the whole box, not by its visible part.
 * </p>
 *
 * <p>
 * The box takes effect in the header and data rows of a Gantt chart. Elsewhere, its content
 * stays at the left edge of the box.
 * </p>
 */
public interface Sticky extends com.top_logic.react.flow.data.Decoration, com.top_logic.react.flow.operations.StickyOperations {

	/**
	 * Creates a {@link com.top_logic.react.flow.data.Sticky} instance.
	 */
	static com.top_logic.react.flow.data.Sticky create() {
		return new com.top_logic.react.flow.data.impl.Sticky_Impl();
	}

	/** Identifier for the {@link com.top_logic.react.flow.data.Sticky} type in JSON format. */
	String STICKY__TYPE = "Sticky";

	/** @see #getShift() */
	String SHIFT__PROP = "shift";

	/**
	 * The current horizontal offset of the content relative to the left edge of the box, set by
	 * the scrolling container.
	 */
	double getShift();

	/**
	 * @see #getShift()
	 */
	com.top_logic.react.flow.data.Sticky setShift(double value);

	@Override
	com.top_logic.react.flow.data.Sticky setContent(com.top_logic.react.flow.data.Box value);

	@Override
	com.top_logic.react.flow.data.Sticky setX(double value);

	@Override
	com.top_logic.react.flow.data.Sticky setY(double value);

	@Override
	com.top_logic.react.flow.data.Sticky setWidth(double value);

	@Override
	com.top_logic.react.flow.data.Sticky setHeight(double value);

	@Override
	com.top_logic.react.flow.data.Sticky setCssClass(String value);

	@Override
	com.top_logic.react.flow.data.Sticky setUserObject(java.lang.Object value);

	@Override
	com.top_logic.react.flow.data.Sticky setClientId(String value);

	@Override
	com.top_logic.react.flow.data.Sticky setRenderInfo(java.lang.Object value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.react.flow.data.Sticky readSticky(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.react.flow.data.Sticky) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert STICKY__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.react.flow.data.impl.Sticky_Impl result = new com.top_logic.react.flow.data.impl.Sticky_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

	@Override
	default Sticky self() {
		return this;
	}

}
