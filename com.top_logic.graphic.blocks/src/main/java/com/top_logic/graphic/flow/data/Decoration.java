package com.top_logic.graphic.flow.data;

public interface Decoration extends Box, com.top_logic.graphic.flow.model.DecorationOperations {

	/** Visitor interface for the {@link com.top_logic.graphic.flow.data.Decoration} hierarchy.*/
	public interface Visitor<R,A,E extends Throwable> {

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

	/** Identifier for the property {@link #getContent()} in binary format. */
	static final int CONTENT__ID = 5;

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

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.Decoration readDecoration(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.graphic.flow.data.Decoration result;
		in.beginArray();
		String type = in.nextString();
		switch (type) {
			case Align.ALIGN__TYPE: result = com.top_logic.graphic.flow.data.Align.readAlign(in); break;
			case Border.BORDER__TYPE: result = com.top_logic.graphic.flow.data.Border.readBorder(in); break;
			case Fill.FILL__TYPE: result = com.top_logic.graphic.flow.data.Fill.readFill(in); break;
			case Padding.PADDING__TYPE: result = com.top_logic.graphic.flow.data.Padding.readPadding(in); break;
			default: in.skipValue(); result = null; break;
		}
		in.endArray();
		return result;
	}

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.Decoration readDecoration(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
		in.beginObject();
		int typeField = in.nextName();
		assert typeField == 0;
		int type = in.nextInt();
		com.top_logic.graphic.flow.data.Decoration result;
		switch (type) {
			case com.top_logic.graphic.flow.data.Align.ALIGN__TYPE_ID: result = com.top_logic.graphic.flow.data.impl.Align_Impl.readAlign_Content(in); break;
			case com.top_logic.graphic.flow.data.Border.BORDER__TYPE_ID: result = com.top_logic.graphic.flow.data.impl.Border_Impl.readBorder_Content(in); break;
			case com.top_logic.graphic.flow.data.Fill.FILL__TYPE_ID: result = com.top_logic.graphic.flow.data.impl.Fill_Impl.readFill_Content(in); break;
			case com.top_logic.graphic.flow.data.Padding.PADDING__TYPE_ID: result = com.top_logic.graphic.flow.data.impl.Padding_Impl.readPadding_Content(in); break;
			default: result = null; while (in.hasNext()) {in.skipValue(); }
		}
		in.endObject();
		return result;
	}

	@Override
	default Decoration self() {
		return this;
	}

	/** Creates a new {@link Decoration} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	public static Decoration readDecoration(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		in.nextTag();
		return com.top_logic.graphic.flow.data.impl.Decoration_Impl.readDecoration_XmlContent(in);
	}

	/** Accepts the given visitor. */
	public abstract <R,A,E extends Throwable> R visit(Visitor<R,A,E> v, A arg) throws E;

}
