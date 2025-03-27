package com.top_logic.graphic.flow.data;

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

	/** Identifier for the property {@link #getGap()} in binary format. */
	static final int GAP__ID = 6;

	/** Identifier for the property {@link #getFill()} in binary format. */
	static final int FILL__ID = 7;

	double getGap();

	/**
	 * @see #getGap()
	 */
	com.top_logic.graphic.flow.data.RowLayout setGap(double value);

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

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.RowLayout readRowLayout(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.graphic.flow.data.RowLayout result;
		in.beginArray();
		String type = in.nextString();
		switch (type) {
			case HorizontalLayout.HORIZONTAL_LAYOUT__TYPE: result = com.top_logic.graphic.flow.data.HorizontalLayout.readHorizontalLayout(in); break;
			case VerticalLayout.VERTICAL_LAYOUT__TYPE: result = com.top_logic.graphic.flow.data.VerticalLayout.readVerticalLayout(in); break;
			default: in.skipValue(); result = null; break;
		}
		in.endArray();
		return result;
	}

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.RowLayout readRowLayout(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
		in.beginObject();
		int typeField = in.nextName();
		assert typeField == 0;
		int type = in.nextInt();
		com.top_logic.graphic.flow.data.RowLayout result;
		switch (type) {
			case com.top_logic.graphic.flow.data.HorizontalLayout.HORIZONTAL_LAYOUT__TYPE_ID: result = com.top_logic.graphic.flow.data.impl.HorizontalLayout_Impl.readHorizontalLayout_Content(in); break;
			case com.top_logic.graphic.flow.data.VerticalLayout.VERTICAL_LAYOUT__TYPE_ID: result = com.top_logic.graphic.flow.data.impl.VerticalLayout_Impl.readVerticalLayout_Content(in); break;
			default: result = null; while (in.hasNext()) {in.skipValue(); }
		}
		in.endObject();
		return result;
	}

	/** Creates a new {@link RowLayout} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	public static RowLayout readRowLayout(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		in.nextTag();
		return com.top_logic.graphic.flow.data.impl.RowLayout_Impl.readRowLayout_XmlContent(in);
	}

	/** Accepts the given visitor. */
	public abstract <R,A,E extends Throwable> R visit(Visitor<R,A,E> v, A arg) throws E;

}
