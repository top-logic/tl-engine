package com.top_logic.graphic.flow.data;

public interface HorizontalLayout extends RowLayout, com.top_logic.graphic.flow.model.layout.HorizontalLayout {

	/**
	 * Creates a {@link com.top_logic.graphic.flow.data.HorizontalLayout} instance.
	 */
	static com.top_logic.graphic.flow.data.HorizontalLayout create() {
		return new com.top_logic.graphic.flow.data.impl.HorizontalLayout_Impl();
	}

	/** Identifier for the {@link com.top_logic.graphic.flow.data.HorizontalLayout} type in JSON format. */
	String HORIZONTAL_LAYOUT__TYPE = "HorizontalLayout";

	/** Identifier for the {@link com.top_logic.graphic.flow.data.HorizontalLayout} type in binary format. */
	static final int HORIZONTAL_LAYOUT__TYPE_ID = 10;

	@Override
	com.top_logic.graphic.flow.data.HorizontalLayout setGap(double value);

	@Override
	com.top_logic.graphic.flow.data.HorizontalLayout setFill(com.top_logic.graphic.flow.data.SpaceDistribution value);

	@Override
	com.top_logic.graphic.flow.data.HorizontalLayout setContents(java.util.List<? extends com.top_logic.graphic.flow.data.Box> value);

	@Override
	com.top_logic.graphic.flow.data.HorizontalLayout addContent(com.top_logic.graphic.flow.data.Box value);

	@Override
	com.top_logic.graphic.flow.data.HorizontalLayout setX(double value);

	@Override
	com.top_logic.graphic.flow.data.HorizontalLayout setY(double value);

	@Override
	com.top_logic.graphic.flow.data.HorizontalLayout setWidth(double value);

	@Override
	com.top_logic.graphic.flow.data.HorizontalLayout setHeight(double value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.HorizontalLayout readHorizontalLayout(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.graphic.flow.data.impl.HorizontalLayout_Impl result = new com.top_logic.graphic.flow.data.impl.HorizontalLayout_Impl();
		result.readContent(in);
		return result;
	}

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.HorizontalLayout readHorizontalLayout(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
		in.beginObject();
		com.top_logic.graphic.flow.data.HorizontalLayout result = com.top_logic.graphic.flow.data.impl.HorizontalLayout_Impl.readHorizontalLayout_Content(in);
		in.endObject();
		return result;
	}

	@Override
	default HorizontalLayout self() {
		return this;
	}

	/** Creates a new {@link HorizontalLayout} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	public static HorizontalLayout readHorizontalLayout(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		in.nextTag();
		return com.top_logic.graphic.flow.data.impl.HorizontalLayout_Impl.readHorizontalLayout_XmlContent(in);
	}

}
