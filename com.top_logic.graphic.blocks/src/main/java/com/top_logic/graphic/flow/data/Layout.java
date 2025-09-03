package com.top_logic.graphic.flow.data;

/**
 * A box that computes the size and position of multiple content boxes.
 */
public interface Layout extends Box {

	/** Visitor interface for the {@link com.top_logic.graphic.flow.data.Layout} hierarchy.*/
	public interface Visitor<R,A,E extends Throwable> extends com.top_logic.graphic.flow.data.RowLayout.Visitor<R,A,E> {

		/** Visit case for {@link com.top_logic.graphic.flow.data.GridLayout}.*/
		R visit(com.top_logic.graphic.flow.data.GridLayout self, A arg) throws E;

	}

	/** @see #getContents() */
	String CONTENTS__PROP = "contents";

	/**
	 * The content boxes to be layouted.
	 */
	java.util.List<com.top_logic.graphic.flow.data.Box> getContents();

	/**
	 * @see #getContents()
	 */
	com.top_logic.graphic.flow.data.Layout setContents(java.util.List<? extends com.top_logic.graphic.flow.data.Box> value);

	/**
	 * Adds a value to the {@link #getContents()} list.
	 */
	com.top_logic.graphic.flow.data.Layout addContent(com.top_logic.graphic.flow.data.Box value);

	/**
	 * Removes a value from the {@link #getContents()} list.
	 */
	void removeContent(com.top_logic.graphic.flow.data.Box value);

	@Override
	com.top_logic.graphic.flow.data.Layout setX(double value);

	@Override
	com.top_logic.graphic.flow.data.Layout setY(double value);

	@Override
	com.top_logic.graphic.flow.data.Layout setWidth(double value);

	@Override
	com.top_logic.graphic.flow.data.Layout setHeight(double value);

	@Override
	com.top_logic.graphic.flow.data.Layout setCssClass(String value);

	@Override
	com.top_logic.graphic.flow.data.Layout setUserObject(java.lang.Object value);

	@Override
	com.top_logic.graphic.flow.data.Layout setClientId(String value);

	@Override
	com.top_logic.graphic.flow.data.Layout setRenderInfo(java.lang.Object value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.Layout readLayout(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.graphic.flow.data.Layout) scope.resolveOrFail(in.nextInt());
		}
		com.top_logic.graphic.flow.data.Layout result;
		in.beginArray();
		String type = in.nextString();
		int id = in.nextInt();
		switch (type) {
			case GridLayout.GRID_LAYOUT__TYPE: result = com.top_logic.graphic.flow.data.GridLayout.create(); break;
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
