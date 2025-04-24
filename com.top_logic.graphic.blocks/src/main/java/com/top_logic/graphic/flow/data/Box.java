package com.top_logic.graphic.flow.data;

/**
 * A rectangular region of a {@link Diagram}.
 */
public interface Box extends Widget, com.top_logic.graphic.flow.operations.BoxOperations {

	/** Visitor interface for the {@link com.top_logic.graphic.flow.data.Box} hierarchy.*/
	public interface Visitor<R,A,E extends Throwable> extends com.top_logic.graphic.flow.data.Decoration.Visitor<R,A,E>, com.top_logic.graphic.flow.data.Layout.Visitor<R,A,E> {

		/** Visit case for {@link com.top_logic.graphic.flow.data.FloatingLayout}.*/
		R visit(com.top_logic.graphic.flow.data.FloatingLayout self, A arg) throws E;

		/** Visit case for {@link com.top_logic.graphic.flow.data.Text}.*/
		R visit(com.top_logic.graphic.flow.data.Text self, A arg) throws E;

		/** Visit case for {@link com.top_logic.graphic.flow.data.Image}.*/
		R visit(com.top_logic.graphic.flow.data.Image self, A arg) throws E;

		/** Visit case for {@link com.top_logic.graphic.flow.data.Empty}.*/
		R visit(com.top_logic.graphic.flow.data.Empty self, A arg) throws E;

		/** Visit case for {@link com.top_logic.graphic.flow.data.CompassLayout}.*/
		R visit(com.top_logic.graphic.flow.data.CompassLayout self, A arg) throws E;

	}

	/** @see #getParent() */
	String PARENT__PROP = "parent";

	/** @see #getX() */
	String X__PROP = "x";

	/** @see #getY() */
	String Y__PROP = "y";

	/** @see #getWidth() */
	String WIDTH__PROP = "width";

	/** @see #getHeight() */
	String HEIGHT__PROP = "height";

	/**
	 * The widget that contains/renders this widget.
	 */
	com.top_logic.graphic.flow.data.Widget getParent();

	/**
	 * Checks, whether {@link #getParent()} has a value.
	 */
	boolean hasParent();

	/**
	 * The X coordinate of the top-left edge of the rectangular region of this box.
	 */
	double getX();

	/**
	 * @see #getX()
	 */
	com.top_logic.graphic.flow.data.Box setX(double value);

	/**
	 * The Y coordinate of the top-left edge of the rectangular region of this box.
	 */
	double getY();

	/**
	 * @see #getY()
	 */
	com.top_logic.graphic.flow.data.Box setY(double value);

	/**
	 * The width of the rectangular region of this box.
	 */
	double getWidth();

	/**
	 * @see #getWidth()
	 */
	com.top_logic.graphic.flow.data.Box setWidth(double value);

	/**
	 * The height of the rectangular region of this box.
	 */
	double getHeight();

	/**
	 * @see #getHeight()
	 */
	com.top_logic.graphic.flow.data.Box setHeight(double value);

	@Override
	com.top_logic.graphic.flow.data.Box setCssClass(String value);

	@Override
	com.top_logic.graphic.flow.data.Box setUserObject(java.lang.Object value);

	@Override
	com.top_logic.graphic.flow.data.Box setClientId(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.Box readBox(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.graphic.flow.data.Box) scope.resolveOrFail(in.nextInt());
		}
		com.top_logic.graphic.flow.data.Box result;
		in.beginArray();
		String type = in.nextString();
		int id = in.nextInt();
		switch (type) {
			case FloatingLayout.FLOATING_LAYOUT__TYPE: result = com.top_logic.graphic.flow.data.FloatingLayout.create(); break;
			case Text.TEXT__TYPE: result = com.top_logic.graphic.flow.data.Text.create(); break;
			case Image.IMAGE__TYPE: result = com.top_logic.graphic.flow.data.Image.create(); break;
			case Empty.EMPTY__TYPE: result = com.top_logic.graphic.flow.data.Empty.create(); break;
			case CompassLayout.COMPASS_LAYOUT__TYPE: result = com.top_logic.graphic.flow.data.CompassLayout.create(); break;
			case TreeLayout.TREE_LAYOUT__TYPE: result = com.top_logic.graphic.flow.data.TreeLayout.create(); break;
			case SelectableBox.SELECTABLE_BOX__TYPE: result = com.top_logic.graphic.flow.data.SelectableBox.create(); break;
			case Align.ALIGN__TYPE: result = com.top_logic.graphic.flow.data.Align.create(); break;
			case Border.BORDER__TYPE: result = com.top_logic.graphic.flow.data.Border.create(); break;
			case Fill.FILL__TYPE: result = com.top_logic.graphic.flow.data.Fill.create(); break;
			case Padding.PADDING__TYPE: result = com.top_logic.graphic.flow.data.Padding.create(); break;
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

	@Override
	default Box self() {
		return this;
	}

	/** Accepts the given visitor. */
	public abstract <R,A,E extends Throwable> R visit(Visitor<R,A,E> v, A arg) throws E;

}
