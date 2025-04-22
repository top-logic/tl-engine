package com.top_logic.graphic.flow.data;

public interface Decoration extends Box, com.top_logic.graphic.flow.operations.DecorationOperations {

	/** Visitor interface for the {@link com.top_logic.graphic.flow.data.Decoration} hierarchy.*/
	public interface Visitor<R,A,E extends Throwable> {

		/** Visit case for {@link com.top_logic.graphic.flow.data.SelectableBox}.*/
		R visit(com.top_logic.graphic.flow.data.SelectableBox self, A arg) throws E;

		/** Visit case for {@link com.top_logic.graphic.flow.data.Align}.*/
		R visit(com.top_logic.graphic.flow.data.Align self, A arg) throws E;

		/** Visit case for {@link com.top_logic.graphic.flow.data.Border}.*/
		R visit(com.top_logic.graphic.flow.data.Border self, A arg) throws E;

		/** Visit case for {@link com.top_logic.graphic.flow.data.Fill}.*/
		R visit(com.top_logic.graphic.flow.data.Fill self, A arg) throws E;

		/** Visit case for {@link com.top_logic.graphic.flow.data.Padding}.*/
		R visit(com.top_logic.graphic.flow.data.Padding self, A arg) throws E;

	}

	/** @see #getContent() */
	String CONTENT__PROP = "content";

	com.top_logic.graphic.flow.data.Box getContent();

	/**
	 * @see #getContent()
	 */
	com.top_logic.graphic.flow.data.Decoration setContent(com.top_logic.graphic.flow.data.Box value);

	/**
	 * Checks, whether {@link #getContent()} has a value.
	 */
	boolean hasContent();

	@Override
	com.top_logic.graphic.flow.data.Decoration setX(double value);

	@Override
	com.top_logic.graphic.flow.data.Decoration setY(double value);

	@Override
	com.top_logic.graphic.flow.data.Decoration setWidth(double value);

	@Override
	com.top_logic.graphic.flow.data.Decoration setHeight(double value);

	@Override
	com.top_logic.graphic.flow.data.Decoration setUserObject(java.lang.Object value);

	@Override
	com.top_logic.graphic.flow.data.Decoration setClientId(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.Decoration readDecoration(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.graphic.flow.data.Decoration) scope.resolveOrFail(in.nextInt());
		}
		com.top_logic.graphic.flow.data.Decoration result;
		in.beginArray();
		String type = in.nextString();
		int id = in.nextInt();
		switch (type) {
			case SelectableBox.SELECTABLE_BOX__TYPE: result = com.top_logic.graphic.flow.data.SelectableBox.create(); break;
			case Align.ALIGN__TYPE: result = com.top_logic.graphic.flow.data.Align.create(); break;
			case Border.BORDER__TYPE: result = com.top_logic.graphic.flow.data.Border.create(); break;
			case Fill.FILL__TYPE: result = com.top_logic.graphic.flow.data.Fill.create(); break;
			case Padding.PADDING__TYPE: result = com.top_logic.graphic.flow.data.Padding.create(); break;
			default: in.skipValue(); result = null; break;
		}
		if (result != null) {
			scope.readData(result, id, in);
		}
		in.endArray();
		return result;
	}

	@Override
	default Decoration self() {
		return this;
	}

	/** Accepts the given visitor. */
	public abstract <R,A,E extends Throwable> R visit(Visitor<R,A,E> v, A arg) throws E;

}
