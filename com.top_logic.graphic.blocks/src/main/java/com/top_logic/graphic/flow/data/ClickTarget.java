package com.top_logic.graphic.flow.data;

/**
 * A box that reacts on interactive mouse click events invoking a callback.
 */
public interface ClickTarget extends Decoration, com.top_logic.graphic.flow.operations.ClickTargetOperations {

	/**
	 * Creates a {@link com.top_logic.graphic.flow.data.ClickTarget} instance.
	 */
	static com.top_logic.graphic.flow.data.ClickTarget create() {
		return new com.top_logic.graphic.flow.data.impl.ClickTarget_Impl();
	}

	/** Identifier for the {@link com.top_logic.graphic.flow.data.ClickTarget} type in JSON format. */
	String CLICK_TARGET__TYPE = "ClickTarget";

	/** @see #getButtons() */
	String BUTTONS__PROP = "buttons";

	/** @see #getClickHandler() */
	String CLICK_HANDLER__PROP = "clickHandler";

	/** @see #getHandlerRegistration() */
	String HANDLER_REGISTRATION__PROP = "handlerRegistration";

	/**
	 * The mouse buttons to react on.
	 */
	java.util.List<com.top_logic.graphic.flow.data.MouseButton> getButtons();

	/**
	 * @see #getButtons()
	 */
	com.top_logic.graphic.flow.data.ClickTarget setButtons(java.util.List<? extends com.top_logic.graphic.flow.data.MouseButton> value);

	/**
	 * Adds a value to the {@link #getButtons()} list.
	 */
	com.top_logic.graphic.flow.data.ClickTarget addButton(com.top_logic.graphic.flow.data.MouseButton value);

	/**
	 * Removes a value from the {@link #getButtons()} list.
	 */
	void removeButton(com.top_logic.graphic.flow.data.MouseButton value);

	/**
	 * Server-side-only field to store the callback to invoke when the element is clicked.
	 */
	com.top_logic.graphic.flow.callback.ClickHandler getClickHandler();

	/**
	 * @see #getClickHandler()
	 */
	com.top_logic.graphic.flow.data.ClickTarget setClickHandler(com.top_logic.graphic.flow.callback.ClickHandler value);

	/**
	 * Checks, whether {@link #getClickHandler()} has a value.
	 */
	boolean hasClickHandler();

	/**
	 * Internal field to store the click handler registration (client-side only).
	 */
	com.top_logic.graphic.blocks.svg.event.Registration getHandlerRegistration();

	/**
	 * @see #getHandlerRegistration()
	 */
	com.top_logic.graphic.flow.data.ClickTarget setHandlerRegistration(com.top_logic.graphic.blocks.svg.event.Registration value);

	/**
	 * Checks, whether {@link #getHandlerRegistration()} has a value.
	 */
	boolean hasHandlerRegistration();

	@Override
	com.top_logic.graphic.flow.data.ClickTarget setContent(com.top_logic.graphic.flow.data.Box value);

	@Override
	com.top_logic.graphic.flow.data.ClickTarget setX(double value);

	@Override
	com.top_logic.graphic.flow.data.ClickTarget setY(double value);

	@Override
	com.top_logic.graphic.flow.data.ClickTarget setWidth(double value);

	@Override
	com.top_logic.graphic.flow.data.ClickTarget setHeight(double value);

	@Override
	com.top_logic.graphic.flow.data.ClickTarget setCssClass(String value);

	@Override
	com.top_logic.graphic.flow.data.ClickTarget setUserObject(java.lang.Object value);

	@Override
	com.top_logic.graphic.flow.data.ClickTarget setClientId(String value);

	@Override
	com.top_logic.graphic.flow.data.ClickTarget setRenderInfo(java.lang.Object value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.ClickTarget readClickTarget(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.graphic.flow.data.ClickTarget) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert CLICK_TARGET__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.graphic.flow.data.impl.ClickTarget_Impl result = new com.top_logic.graphic.flow.data.impl.ClickTarget_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

	@Override
	default ClickTarget self() {
		return this;
	}

}
