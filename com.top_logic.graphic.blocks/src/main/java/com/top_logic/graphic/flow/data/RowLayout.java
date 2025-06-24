package com.top_logic.graphic.flow.data;

/**
 * A layout of a row of boxes.
 */
public interface RowLayout extends Layout {

	/** Visitor interface for the {@link com.top_logic.graphic.flow.data.RowLayout} hierarchy.*/
	public interface Visitor<R,A,E extends Throwable> {

		/** Visit case for {@link com.top_logic.graphic.flow.data.HorizontalLayout}.*/
		R visit(com.top_logic.graphic.flow.data.HorizontalLayout self, A arg) throws E;

		/** Visit case for {@link com.top_logic.graphic.flow.data.VerticalLayout}.*/
		R visit(com.top_logic.graphic.flow.data.VerticalLayout self, A arg) throws E;

	}

	/** @see #getGap() */
	String GAP__PROP = "gap";

	/** @see #getFill() */
	String FILL__PROP = "fill";

	/**
	 * The gap between boxes in the row.
	 */
	double getGap();

	/**
	 * @see #getGap()
	 */
	com.top_logic.graphic.flow.data.RowLayout setGap(double value);

	/**
	 * Instruction how to use extra space available.
	 */
	com.top_logic.graphic.flow.data.SpaceDistribution getFill();

	/**
	 * @see #getFill()
	 */
	com.top_logic.graphic.flow.data.RowLayout setFill(com.top_logic.graphic.flow.data.SpaceDistribution value);

	@Override
	com.top_logic.graphic.flow.data.RowLayout setContents(java.util.List<? extends com.top_logic.graphic.flow.data.Box> value);

	@Override
	com.top_logic.graphic.flow.data.RowLayout addContent(com.top_logic.graphic.flow.data.Box value);

	@Override
	com.top_logic.graphic.flow.data.RowLayout setX(double value);

	@Override
	com.top_logic.graphic.flow.data.RowLayout setY(double value);

	@Override
	com.top_logic.graphic.flow.data.RowLayout setWidth(double value);

	@Override
	com.top_logic.graphic.flow.data.RowLayout setHeight(double value);

	@Override
	com.top_logic.graphic.flow.data.RowLayout setCssClass(String value);

	@Override
	com.top_logic.graphic.flow.data.RowLayout setUserObject(java.lang.Object value);

	@Override
	com.top_logic.graphic.flow.data.RowLayout setClientId(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.RowLayout readRowLayout(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.graphic.flow.data.RowLayout) scope.resolveOrFail(in.nextInt());
		}
		com.top_logic.graphic.flow.data.RowLayout result;
		in.beginArray();
		String type = in.nextString();
		int id = in.nextInt();
		switch (type) {
			case HorizontalLayout.HORIZONTAL_LAYOUT__TYPE: result = com.top_logic.graphic.flow.data.HorizontalLayout.create(); break;
			case VerticalLayout.VERTICAL_LAYOUT__TYPE: result = com.top_logic.graphic.flow.data.VerticalLayout.create(); break;
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
