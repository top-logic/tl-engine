package com.top_logic.graphic.flow.data;

/**
 * Element with explicit given width and height.
 */
public interface Sized extends Decoration, com.top_logic.graphic.flow.operations.SizedOperations {

	/**
	 * Creates a {@link com.top_logic.graphic.flow.data.Sized} instance.
	 */
	static com.top_logic.graphic.flow.data.Sized create() {
		return new com.top_logic.graphic.flow.data.impl.Sized_Impl();
	}

	/** Identifier for the {@link com.top_logic.graphic.flow.data.Sized} type in JSON format. */
	String SIZED__TYPE = "Sized";

	/** @see #getMinWidth() */
	String MIN_WIDTH__PROP = "minWidth";

	/** @see #getMaxWidth() */
	String MAX_WIDTH__PROP = "maxWidth";

	/** @see #getMinHeight() */
	String MIN_HEIGHT__PROP = "minHeight";

	/** @see #getMaxHeight() */
	String MAX_HEIGHT__PROP = "maxHeight";

	/** @see #getDesiredX() */
	String DESIRED_X__PROP = "desiredX";

	/** @see #getDesiredY() */
	String DESIRED_Y__PROP = "desiredY";

	/** @see #isPreserveAspectRatio() */
	String PRESERVE_ASPECT_RATIO__PROP = "preserveAspectRatio";

	/**
	 * Minimum width of content.
	 */
	Double getMinWidth();

	/**
	 * @see #getMinWidth()
	 */
	com.top_logic.graphic.flow.data.Sized setMinWidth(Double value);

	/**
	 * Checks, whether {@link #getMinWidth()} has a value.
	 */
	boolean hasMinWidth();

	/**
	 * Maximum width of content.
	 */
	Double getMaxWidth();

	/**
	 * @see #getMaxWidth()
	 */
	com.top_logic.graphic.flow.data.Sized setMaxWidth(Double value);

	/**
	 * Checks, whether {@link #getMaxWidth()} has a value.
	 */
	boolean hasMaxWidth();

	/**
	 * Minimum height of content.
	 */
	Double getMinHeight();

	/**
	 * @see #getMinHeight()
	 */
	com.top_logic.graphic.flow.data.Sized setMinHeight(Double value);

	/**
	 * Checks, whether {@link #getMinHeight()} has a value.
	 */
	boolean hasMinHeight();

	/**
	 * Maximum height of content.
	 */
	Double getMaxHeight();

	/**
	 * @see #getMaxHeight()
	 */
	com.top_logic.graphic.flow.data.Sized setMaxHeight(Double value);

	/**
	 * Checks, whether {@link #getMaxHeight()} has a value.
	 */
	boolean hasMaxHeight();

	/**
	 * The desired X coordinate of the top-left edge of the rectangular region of this box.
	 */
	Double getDesiredX();

	/**
	 * @see #getDesiredX()
	 */
	com.top_logic.graphic.flow.data.Sized setDesiredX(Double value);

	/**
	 * Checks, whether {@link #getDesiredX()} has a value.
	 */
	boolean hasDesiredX();

	/**
	 * The desired Y coordinate of the top-left edge of the rectangular region of this box.
	 */
	Double getDesiredY();

	/**
	 * @see #getDesiredY()
	 */
	com.top_logic.graphic.flow.data.Sized setDesiredY(Double value);

	/**
	 * Checks, whether {@link #getDesiredY()} has a value.
	 */
	boolean hasDesiredY();

	/**
	 * When content size is adjusted, the aspect ratio of the content is kept.
	 */
	boolean isPreserveAspectRatio();

	/**
	 * @see #isPreserveAspectRatio()
	 */
	com.top_logic.graphic.flow.data.Sized setPreserveAspectRatio(boolean value);

	@Override
	com.top_logic.graphic.flow.data.Sized setContent(com.top_logic.graphic.flow.data.Box value);

	@Override
	com.top_logic.graphic.flow.data.Sized setX(double value);

	@Override
	com.top_logic.graphic.flow.data.Sized setY(double value);

	@Override
	com.top_logic.graphic.flow.data.Sized setWidth(double value);

	@Override
	com.top_logic.graphic.flow.data.Sized setHeight(double value);

	@Override
	com.top_logic.graphic.flow.data.Sized setCssClass(String value);

	@Override
	com.top_logic.graphic.flow.data.Sized setUserObject(java.lang.Object value);

	@Override
	com.top_logic.graphic.flow.data.Sized setClientId(String value);

	@Override
	com.top_logic.graphic.flow.data.Sized setRenderInfo(java.lang.Object value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.Sized readSized(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.graphic.flow.data.Sized) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert SIZED__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.graphic.flow.data.impl.Sized_Impl result = new com.top_logic.graphic.flow.data.impl.Sized_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

	@Override
	default Sized self() {
		return this;
	}

}
