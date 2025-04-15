package com.top_logic.graphic.flow.data;

public interface Image extends Box, com.top_logic.graphic.flow.model.ImageOperations {

	/**
	 * Creates a {@link com.top_logic.graphic.flow.data.Image} instance.
	 */
	static com.top_logic.graphic.flow.data.Image create() {
		return new com.top_logic.graphic.flow.data.impl.Image_Impl();
	}

	/** Identifier for the {@link com.top_logic.graphic.flow.data.Image} type in JSON format. */
	String IMAGE__TYPE = "Image";

	/** @see #getHref() */
	String HREF__PROP = "href";

	/** @see #getImgWidth() */
	String IMG_WIDTH__PROP = "imgWidth";

	/** @see #getImgHeight() */
	String IMG_HEIGHT__PROP = "imgHeight";

	/** @see #getAlign() */
	String ALIGN__PROP = "align";

	/** @see #getScale() */
	String SCALE__PROP = "scale";

	/** Identifier for the {@link com.top_logic.graphic.flow.data.Image} type in binary format. */
	static final int IMAGE__TYPE_ID = 2;

	/** Identifier for the property {@link #getHref()} in binary format. */
	static final int HREF__ID = 5;

	/** Identifier for the property {@link #getImgWidth()} in binary format. */
	static final int IMG_WIDTH__ID = 6;

	/** Identifier for the property {@link #getImgHeight()} in binary format. */
	static final int IMG_HEIGHT__ID = 7;

	/** Identifier for the property {@link #getAlign()} in binary format. */
	static final int ALIGN__ID = 8;

	/** Identifier for the property {@link #getScale()} in binary format. */
	static final int SCALE__ID = 9;

	String getHref();

	/**
	 * @see #getHref()
	 */
	com.top_logic.graphic.flow.data.Image setHref(String value);

	double getImgWidth();

	/**
	 * @see #getImgWidth()
	 */
	com.top_logic.graphic.flow.data.Image setImgWidth(double value);

	double getImgHeight();

	/**
	 * @see #getImgHeight()
	 */
	com.top_logic.graphic.flow.data.Image setImgHeight(double value);

	com.top_logic.graphic.flow.data.ImageAlign getAlign();

	/**
	 * @see #getAlign()
	 */
	com.top_logic.graphic.flow.data.Image setAlign(com.top_logic.graphic.flow.data.ImageAlign value);

	com.top_logic.graphic.flow.data.ImageScale getScale();

	/**
	 * @see #getScale()
	 */
	com.top_logic.graphic.flow.data.Image setScale(com.top_logic.graphic.flow.data.ImageScale value);

	@Override
	com.top_logic.graphic.flow.data.Image setX(double value);

	@Override
	com.top_logic.graphic.flow.data.Image setY(double value);

	@Override
	com.top_logic.graphic.flow.data.Image setWidth(double value);

	@Override
	com.top_logic.graphic.flow.data.Image setHeight(double value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.Image readImage(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.graphic.flow.data.impl.Image_Impl result = new com.top_logic.graphic.flow.data.impl.Image_Impl();
		result.readContent(in);
		return result;
	}

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.Image readImage(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
		in.beginObject();
		com.top_logic.graphic.flow.data.Image result = com.top_logic.graphic.flow.data.impl.Image_Impl.readImage_Content(in);
		in.endObject();
		return result;
	}

	@Override
	default Image self() {
		return this;
	}

	/** Creates a new {@link Image} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	public static Image readImage(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		in.nextTag();
		return com.top_logic.graphic.flow.data.impl.Image_Impl.readImage_XmlContent(in);
	}

}
