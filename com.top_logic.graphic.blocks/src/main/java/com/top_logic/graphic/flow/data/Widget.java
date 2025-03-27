package com.top_logic.graphic.flow.data;

public interface Widget extends de.haumacher.msgbuf.data.DataObject, de.haumacher.msgbuf.binary.BinaryDataObject, de.haumacher.msgbuf.observer.Observable, de.haumacher.msgbuf.xml.XmlSerializable {

	/** Type codes for the {@link com.top_logic.graphic.flow.data.Widget} hierarchy. */
	public enum TypeKind {

		/** Type literal for {@link com.top_logic.graphic.flow.data.Text}. */
		TEXT,

		/** Type literal for {@link com.top_logic.graphic.flow.data.Image}. */
		IMAGE,

		/** Type literal for {@link com.top_logic.graphic.flow.data.Empty}. */
		EMPTY,

		/** Type literal for {@link com.top_logic.graphic.flow.data.Align}. */
		ALIGN,

		/** Type literal for {@link com.top_logic.graphic.flow.data.Border}. */
		BORDER,

		/** Type literal for {@link com.top_logic.graphic.flow.data.Fill}. */
		FILL,

		/** Type literal for {@link com.top_logic.graphic.flow.data.Padding}. */
		PADDING,

		/** Type literal for {@link com.top_logic.graphic.flow.data.CompassLayout}. */
		COMPASS_LAYOUT,

		/** Type literal for {@link com.top_logic.graphic.flow.data.GridLayout}. */
		GRID_LAYOUT,

		/** Type literal for {@link com.top_logic.graphic.flow.data.HorizontalLayout}. */
		HORIZONTAL_LAYOUT,

		/** Type literal for {@link com.top_logic.graphic.flow.data.VerticalLayout}. */
		VERTICAL_LAYOUT,
		;

	}

	/** Visitor interface for the {@link com.top_logic.graphic.flow.data.Widget} hierarchy.*/
	public interface Visitor<R,A,E extends Throwable> extends com.top_logic.graphic.flow.data.Box.Visitor<R,A,E> {

		// Pure sum interface.

	}

	/** The type code of this instance. */
	TypeKind kind();

	@Override
	public com.top_logic.graphic.flow.data.Widget registerListener(de.haumacher.msgbuf.observer.Listener l);

	@Override
	public com.top_logic.graphic.flow.data.Widget unregisterListener(de.haumacher.msgbuf.observer.Listener l);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.Widget readWidget(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.graphic.flow.data.Widget result;
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

	/** The binary identifier for this concrete type in the polymorphic {@link com.top_logic.graphic.flow.data.Widget} hierarchy. */
	abstract int typeId();

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.Widget readWidget(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
		in.beginObject();
		int typeField = in.nextName();
		assert typeField == 0;
		int type = in.nextInt();
		com.top_logic.graphic.flow.data.Widget result;
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

	/** Creates a new {@link Widget} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	public static Widget readWidget(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		in.nextTag();
		return com.top_logic.graphic.flow.data.impl.Widget_Impl.readWidget_XmlContent(in);
	}

	/** Accepts the given visitor. */
	public abstract <R,A,E extends Throwable> R visit(Visitor<R,A,E> v, A arg) throws E;

}
