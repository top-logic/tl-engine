package com.top_logic.graphic.flow.data;

public interface Layout extends Box {

	/** Visitor interface for the {@link com.top_logic.graphic.flow.data.Layout} hierarchy.*/
	public interface Visitor<R,A,E extends Throwable> extends com.top_logic.graphic.flow.data.RowLayout.Visitor<R,A,E> {

		/** Visit case for {@link com.top_logic.graphic.flow.data.GridLayout}.*/
		R visit(com.top_logic.graphic.flow.data.GridLayout self, A arg) throws E;

	}

	/** @see #getContents() */
	String CONTENTS__PROP = "contents";

	/** Identifier for the property {@link #getContents()} in binary format. */
	static final int CONTENTS__ID = 5;

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

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.Layout readLayout(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.graphic.flow.data.Layout result;
		in.beginArray();
		String type = in.nextString();
		switch (type) {
			case GridLayout.GRID_LAYOUT__TYPE: result = com.top_logic.graphic.flow.data.GridLayout.readGridLayout(in); break;
			case HorizontalLayout.HORIZONTAL_LAYOUT__TYPE: result = com.top_logic.graphic.flow.data.HorizontalLayout.readHorizontalLayout(in); break;
			case VerticalLayout.VERTICAL_LAYOUT__TYPE: result = com.top_logic.graphic.flow.data.VerticalLayout.readVerticalLayout(in); break;
			default: in.skipValue(); result = null; break;
		}
		in.endArray();
		return result;
	}

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.Layout readLayout(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
		in.beginObject();
		int typeField = in.nextName();
		assert typeField == 0;
		int type = in.nextInt();
		com.top_logic.graphic.flow.data.Layout result;
		switch (type) {
			case com.top_logic.graphic.flow.data.GridLayout.GRID_LAYOUT__TYPE_ID: result = com.top_logic.graphic.flow.data.impl.GridLayout_Impl.readGridLayout_Content(in); break;
			case com.top_logic.graphic.flow.data.HorizontalLayout.HORIZONTAL_LAYOUT__TYPE_ID: result = com.top_logic.graphic.flow.data.impl.HorizontalLayout_Impl.readHorizontalLayout_Content(in); break;
			case com.top_logic.graphic.flow.data.VerticalLayout.VERTICAL_LAYOUT__TYPE_ID: result = com.top_logic.graphic.flow.data.impl.VerticalLayout_Impl.readVerticalLayout_Content(in); break;
			default: result = null; while (in.hasNext()) {in.skipValue(); }
		}
		in.endObject();
		return result;
	}

	/** Creates a new {@link Layout} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	public static Layout readLayout(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		in.nextTag();
		return com.top_logic.graphic.flow.data.impl.Layout_Impl.readLayout_XmlContent(in);
	}

	/** Accepts the given visitor. */
	public abstract <R,A,E extends Throwable> R visit(Visitor<R,A,E> v, A arg) throws E;

}
