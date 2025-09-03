package com.top_logic.graphic.flow.data;

/**
 * A box that accepts drop events by invoking a server callback.
 */
public interface DropRegion extends Decoration, com.top_logic.graphic.flow.operations.DropRegionOperations {

	/**
	 * Creates a {@link com.top_logic.graphic.flow.data.DropRegion} instance.
	 */
	static com.top_logic.graphic.flow.data.DropRegion create() {
		return new com.top_logic.graphic.flow.data.impl.DropRegion_Impl();
	}

	/** Identifier for the {@link com.top_logic.graphic.flow.data.DropRegion} type in JSON format. */
	String DROP_REGION__TYPE = "DropRegion";

	/** @see #getDropHandler() */
	String DROP_HANDLER__PROP = "dropHandler";

	/** @see #getHandlerRegistration() */
	String HANDLER_REGISTRATION__PROP = "handlerRegistration";

	/**
	 * Server-side-only field to store the callback to invoke when a drop occurs.
	 */
	com.top_logic.graphic.flow.callback.DropHandler getDropHandler();

	/**
	 * @see #getDropHandler()
	 */
	com.top_logic.graphic.flow.data.DropRegion setDropHandler(com.top_logic.graphic.flow.callback.DropHandler value);

	/**
	 * Checks, whether {@link #getDropHandler()} has a value.
	 */
	boolean hasDropHandler();

	/**
	 * Internal field to store the drop handler registration (client-side only).
	 */
	com.top_logic.graphic.blocks.svg.event.Registration getHandlerRegistration();

	/**
	 * @see #getHandlerRegistration()
	 */
	com.top_logic.graphic.flow.data.DropRegion setHandlerRegistration(com.top_logic.graphic.blocks.svg.event.Registration value);

	/**
	 * Checks, whether {@link #getHandlerRegistration()} has a value.
	 */
	boolean hasHandlerRegistration();

	@Override
	com.top_logic.graphic.flow.data.DropRegion setContent(com.top_logic.graphic.flow.data.Box value);

	@Override
	com.top_logic.graphic.flow.data.DropRegion setX(double value);

	@Override
	com.top_logic.graphic.flow.data.DropRegion setY(double value);

	@Override
	com.top_logic.graphic.flow.data.DropRegion setWidth(double value);

	@Override
	com.top_logic.graphic.flow.data.DropRegion setHeight(double value);

	@Override
	com.top_logic.graphic.flow.data.DropRegion setCssClass(String value);

	@Override
	com.top_logic.graphic.flow.data.DropRegion setUserObject(java.lang.Object value);

	@Override
	com.top_logic.graphic.flow.data.DropRegion setClientId(String value);

	@Override
	com.top_logic.graphic.flow.data.DropRegion setRenderInfo(java.lang.Object value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.DropRegion readDropRegion(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.graphic.flow.data.DropRegion) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert DROP_REGION__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.graphic.flow.data.impl.DropRegion_Impl result = new com.top_logic.graphic.flow.data.impl.DropRegion_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

	@Override
	default DropRegion self() {
		return this;
	}

}
