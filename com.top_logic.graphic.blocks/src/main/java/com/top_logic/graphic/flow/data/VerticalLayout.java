package com.top_logic.graphic.flow.data;

public interface VerticalLayout extends RowLayout, com.top_logic.graphic.flow.model.layout.VerticalLayout {

	/**
	 * Creates a {@link com.top_logic.graphic.flow.data.VerticalLayout} instance.
	 */
	static com.top_logic.graphic.flow.data.VerticalLayout create() {
		return new com.top_logic.graphic.flow.data.impl.VerticalLayout_Impl();
	}

	/** Identifier for the {@link com.top_logic.graphic.flow.data.VerticalLayout} type in JSON format. */
	String VERTICAL_LAYOUT__TYPE = "VerticalLayout";

	/** Identifier for the {@link com.top_logic.graphic.flow.data.VerticalLayout} type in binary format. */
	static final int VERTICAL_LAYOUT__TYPE_ID = 11;

	@Override
	com.top_logic.graphic.flow.data.VerticalLayout setGap(double value);

	@Override
	com.top_logic.graphic.flow.data.VerticalLayout setFill(com.top_logic.graphic.flow.data.SpaceDistribution value);

	@Override
	com.top_logic.graphic.flow.data.VerticalLayout setContents(java.util.List<? extends com.top_logic.graphic.flow.data.Box> value);

	@Override
	com.top_logic.graphic.flow.data.VerticalLayout addContent(com.top_logic.graphic.flow.data.Box value);

	@Override
	com.top_logic.graphic.flow.data.VerticalLayout setX(double value);

	@Override
	com.top_logic.graphic.flow.data.VerticalLayout setY(double value);

	@Override
	com.top_logic.graphic.flow.data.VerticalLayout setWidth(double value);

	@Override
	com.top_logic.graphic.flow.data.VerticalLayout setHeight(double value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.VerticalLayout readVerticalLayout(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.graphic.flow.data.impl.VerticalLayout_Impl result = new com.top_logic.graphic.flow.data.impl.VerticalLayout_Impl();
		result.readContent(in);
		return result;
	}

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.VerticalLayout readVerticalLayout(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
		in.beginObject();
		com.top_logic.graphic.flow.data.VerticalLayout result = com.top_logic.graphic.flow.data.impl.VerticalLayout_Impl.readVerticalLayout_Content(in);
		in.endObject();
		return result;
	}

	@Override
	default VerticalLayout self() {
		return this;
	}

	/** Creates a new {@link VerticalLayout} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	public static VerticalLayout readVerticalLayout(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		in.nextTag();
		return com.top_logic.graphic.flow.data.impl.VerticalLayout_Impl.readVerticalLayout_XmlContent(in);
	}

}
