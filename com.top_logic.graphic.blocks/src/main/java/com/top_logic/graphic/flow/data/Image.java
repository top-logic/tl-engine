package com.top_logic.graphic.flow.data;

/**
 * An embedded image.
 */
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

	/** @see #getOrientation() */
	String ORIENTATION__PROP = "orientation";

	/**
	 * The URL where the image data resides.
	 */
	String getHref();

	/**
	 * @see #getHref()
	 */
	com.top_logic.graphic.flow.data.Image setHref(String value);

	/**
	 * The width of the image data.
	 */
	double getImgWidth();

	/**
	 * @see #getImgWidth()
	 */
	com.top_logic.graphic.flow.data.Image setImgWidth(double value);

	/**
	 * The height of the image data.
	 */
	double getImgHeight();

	/**
	 * @see #getImgHeight()
	 */
	com.top_logic.graphic.flow.data.Image setImgHeight(double value);

	/**
	 * The alignment of the image data to the displayed image box.
	 */
	com.top_logic.graphic.flow.data.ImageAlign getAlign();

	/**
	 * @see #getAlign()
	 */
	com.top_logic.graphic.flow.data.Image setAlign(com.top_logic.graphic.flow.data.ImageAlign value);

	/**
	 * The scaling of the image data within the image box.
	 */
	com.top_logic.graphic.flow.data.ImageScale getScale();

	/**
	 * @see #getScale()
	 */
	com.top_logic.graphic.flow.data.Image setScale(com.top_logic.graphic.flow.data.ImageScale value);

	/**
	 * The image transformation before display.
	 */
	com.top_logic.graphic.flow.data.ImageOrientation getOrientation();

	/**
	 * @see #getOrientation()
	 */
	com.top_logic.graphic.flow.data.Image setOrientation(com.top_logic.graphic.flow.data.ImageOrientation value);

	@Override
	com.top_logic.graphic.flow.data.Image setX(double value);

	@Override
	com.top_logic.graphic.flow.data.Image setY(double value);

	@Override
	com.top_logic.graphic.flow.data.Image setWidth(double value);

	@Override
	com.top_logic.graphic.flow.data.Image setHeight(double value);

	@Override
	com.top_logic.graphic.flow.data.Image setCssClass(String value);

	@Override
	com.top_logic.graphic.flow.data.Image setUserObject(java.lang.Object value);

	@Override
	com.top_logic.graphic.flow.data.Image setClientId(String value);

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

}
