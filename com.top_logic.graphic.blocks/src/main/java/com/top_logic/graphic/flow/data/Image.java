package com.top_logic.graphic.flow.data;

public interface Image extends Box, com.top_logic.graphic.flow.operations.ImageOperations {

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
	static com.top_logic.graphic.flow.data.Image readImage(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.graphic.flow.data.Image) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert IMAGE__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.graphic.flow.data.impl.Image_Impl result = new com.top_logic.graphic.flow.data.impl.Image_Impl();
		scope.readData(result, id, in);
		in.endArray();
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
