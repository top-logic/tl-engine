package com.top_logic.graphic.flow.data;

/**
 * A box that shows a tooltip when the mouse hovers over its contents.
 */
public interface Tooltip extends Decoration, com.top_logic.graphic.flow.operations.TooltipOperations {

	/**
	 * Creates a {@link com.top_logic.graphic.flow.data.Tooltip} instance.
	 */
	static com.top_logic.graphic.flow.data.Tooltip create() {
		return new com.top_logic.graphic.flow.data.impl.Tooltip_Impl();
	}

	/** Identifier for the {@link com.top_logic.graphic.flow.data.Tooltip} type in JSON format. */
	String TOOLTIP__TYPE = "Tooltip";

	/** @see #getText() */
	String TEXT__PROP = "text";

	/**
	 * The text to display as tooltip
	 */
	String getText();

	/**
	 * @see #getText()
	 */
	com.top_logic.graphic.flow.data.Tooltip setText(String value);

	/**
	 * Checks, whether {@link #getText()} has a value.
	 */
	boolean hasText();

	@Override
	com.top_logic.graphic.flow.data.Tooltip setContent(com.top_logic.graphic.flow.data.Box value);

	@Override
	com.top_logic.graphic.flow.data.Tooltip setX(double value);

	@Override
	com.top_logic.graphic.flow.data.Tooltip setY(double value);

	@Override
	com.top_logic.graphic.flow.data.Tooltip setWidth(double value);

	@Override
	com.top_logic.graphic.flow.data.Tooltip setHeight(double value);

	@Override
	com.top_logic.graphic.flow.data.Tooltip setCssClass(String value);

	@Override
	com.top_logic.graphic.flow.data.Tooltip setUserObject(java.lang.Object value);

	@Override
	com.top_logic.graphic.flow.data.Tooltip setClientId(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.Tooltip readTooltip(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.graphic.flow.data.Tooltip) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert TOOLTIP__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.graphic.flow.data.impl.Tooltip_Impl result = new com.top_logic.graphic.flow.data.impl.Tooltip_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

	@Override
	default Tooltip self() {
		return this;
	}

}
