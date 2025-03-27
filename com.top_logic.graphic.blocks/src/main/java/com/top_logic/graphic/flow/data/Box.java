package com.top_logic.graphic.flow.data;

public interface Box extends Widget, com.top_logic.graphic.flow.model.DrawElement {

	/** Visitor interface for the {@link com.top_logic.graphic.flow.data.Box} hierarchy.*/
	public interface Visitor<R,A,E extends Throwable> extends com.top_logic.graphic.flow.data.Decoration.Visitor<R,A,E>, com.top_logic.graphic.flow.data.Layout.Visitor<R,A,E> {

		/** Visit case for {@link com.top_logic.graphic.flow.data.Text}.*/
		R visit(com.top_logic.graphic.flow.data.Text self, A arg) throws E;

		/** Visit case for {@link com.top_logic.graphic.flow.data.Image}.*/
		R visit(com.top_logic.graphic.flow.data.Image self, A arg) throws E;

		/** Visit case for {@link com.top_logic.graphic.flow.data.Empty}.*/
		R visit(com.top_logic.graphic.flow.data.Empty self, A arg) throws E;

		/** Visit case for {@link com.top_logic.graphic.flow.data.CompassLayout}.*/
		R visit(com.top_logic.graphic.flow.data.CompassLayout self, A arg) throws E;

	}

	/** @see #getX() */
	String X__PROP = "x";

	/** @see #getY() */
	String Y__PROP = "y";

	/** @see #getWidth() */
	String WIDTH__PROP = "width";

	/** @see #getHeight() */
	String HEIGHT__PROP = "height";

	/** Identifier for the property {@link #getX()} in binary format. */
	static final int X__ID = 1;

	/** Identifier for the property {@link #getY()} in binary format. */
	static final int Y__ID = 2;

	/** Identifier for the property {@link #getWidth()} in binary format. */
	static final int WIDTH__ID = 3;

	/** Identifier for the property {@link #getHeight()} in binary format. */
	static final int HEIGHT__ID = 4;

	double getX();

	/**
	 * @see #getX()
	 */
	com.top_logic.graphic.flow.data.Box setX(double value);

	double getY();

	/**
	 * @see #getY()
	 */
	com.top_logic.graphic.flow.data.Box setY(double value);

	double getWidth();

	/**
	 * @see #getWidth()
	 */
	com.top_logic.graphic.flow.data.Box setWidth(double value);

	double getHeight();

	/**
	 * @see #getHeight()
	 */
	com.top_logic.graphic.flow.data.Box setHeight(double value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.Box readBox(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.graphic.flow.data.Box result;
		in.beginArray();
		String type = in.nextString();
		switch (type) {
			case Text.TEXT__TYPE: result = com.top_logic.graphic.flow.data.Text.readText(in); break;
			case Image.IMAGE__TYPE: result = com.top_logic.graphic.flow.data.Image.readImage(in); break;
			case Empty.EMPTY__TYPE: result = com.top_logic.graphic.flow.data.Empty.readEmpty(in); break;
			case CompassLayout.COMPASS_LAYOUT__TYPE: result = com.top_logic.graphic.flow.data.CompassLayout.readCompassLayout(in); break;
			case Align.ALIGN__TYPE: result = com.top_logic.graphic.flow.data.Align.readAlign(in); break;
			case Border.BORDER__TYPE: result = com.top_logic.graphic.flow.data.Border.readBorder(in); break;
			case Fill.FILL__TYPE: result = com.top_logic.graphic.flow.data.Fill.readFill(in); break;
			case Padding.PADDING__TYPE: result = com.top_logic.graphic.flow.data.Padding.readPadding(in); break;
			case GridLayout.GRID_LAYOUT__TYPE: result = com.top_logic.graphic.flow.data.GridLayout.readGridLayout(in); break;
			case HorizontalLayout.HORIZONTAL_LAYOUT__TYPE: result = com.top_logic.graphic.flow.data.HorizontalLayout.readHorizontalLayout(in); break;
			case VerticalLayout.VERTICAL_LAYOUT__TYPE: result = com.top_logic.graphic.flow.data.VerticalLayout.readVerticalLayout(in); break;
			default: in.skipValue(); result = null; break;
		}
		in.endArray();
		return result;
	}

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.Box readBox(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
		in.beginObject();
		int typeField = in.nextName();
		assert typeField == 0;
		int type = in.nextInt();
		com.top_logic.graphic.flow.data.Box result;
		switch (type) {
			case com.top_logic.graphic.flow.data.Text.TEXT__TYPE_ID: result = com.top_logic.graphic.flow.data.impl.Text_Impl.readText_Content(in); break;
			case com.top_logic.graphic.flow.data.Image.IMAGE__TYPE_ID: result = com.top_logic.graphic.flow.data.impl.Image_Impl.readImage_Content(in); break;
			case com.top_logic.graphic.flow.data.Empty.EMPTY__TYPE_ID: result = com.top_logic.graphic.flow.data.impl.Empty_Impl.readEmpty_Content(in); break;
			case com.top_logic.graphic.flow.data.CompassLayout.COMPASS_LAYOUT__TYPE_ID: result = com.top_logic.graphic.flow.data.impl.CompassLayout_Impl.readCompassLayout_Content(in); break;
			case com.top_logic.graphic.flow.data.Align.ALIGN__TYPE_ID: result = com.top_logic.graphic.flow.data.impl.Align_Impl.readAlign_Content(in); break;
			case com.top_logic.graphic.flow.data.Border.BORDER__TYPE_ID: result = com.top_logic.graphic.flow.data.impl.Border_Impl.readBorder_Content(in); break;
			case com.top_logic.graphic.flow.data.Fill.FILL__TYPE_ID: result = com.top_logic.graphic.flow.data.impl.Fill_Impl.readFill_Content(in); break;
			case com.top_logic.graphic.flow.data.Padding.PADDING__TYPE_ID: result = com.top_logic.graphic.flow.data.impl.Padding_Impl.readPadding_Content(in); break;
			case com.top_logic.graphic.flow.data.GridLayout.GRID_LAYOUT__TYPE_ID: result = com.top_logic.graphic.flow.data.impl.GridLayout_Impl.readGridLayout_Content(in); break;
			case com.top_logic.graphic.flow.data.HorizontalLayout.HORIZONTAL_LAYOUT__TYPE_ID: result = com.top_logic.graphic.flow.data.impl.HorizontalLayout_Impl.readHorizontalLayout_Content(in); break;
			case com.top_logic.graphic.flow.data.VerticalLayout.VERTICAL_LAYOUT__TYPE_ID: result = com.top_logic.graphic.flow.data.impl.VerticalLayout_Impl.readVerticalLayout_Content(in); break;
			default: result = null; while (in.hasNext()) {in.skipValue(); }
		}
		in.endObject();
		return result;
	}

	@Override
	default Box self() {
		return this;
	}

	/** Creates a new {@link Box} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	public static Box readBox(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		in.nextTag();
		return com.top_logic.graphic.flow.data.impl.Box_Impl.readBox_XmlContent(in);
	}

	/** Accepts the given visitor. */
	public abstract <R,A,E extends Throwable> R visit(Visitor<R,A,E> v, A arg) throws E;

}
