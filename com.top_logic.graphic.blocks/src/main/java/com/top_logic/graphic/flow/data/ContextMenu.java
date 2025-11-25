package com.top_logic.graphic.flow.data;

/**
 * A box defines a custom context menu region.
 */
public interface ContextMenu extends Decoration, com.top_logic.graphic.flow.operations.ContextMenuOperations {

	/**
	 * Creates a {@link com.top_logic.graphic.flow.data.ContextMenu} instance.
	 */
	static com.top_logic.graphic.flow.data.ContextMenu create() {
		return new com.top_logic.graphic.flow.data.impl.ContextMenu_Impl();
	}

	/** Identifier for the {@link com.top_logic.graphic.flow.data.ContextMenu} type in JSON format. */
	String CONTEXT_MENU__TYPE = "ContextMenu";

	/** @see #getMenuProvider() */
	String MENU_PROVIDER__PROP = "menuProvider";

	/**
	 * Internal field to store the commands that should be displayed in the context menu.
	 */
	com.top_logic.graphic.flow.callback.DiagramContextMenuProvider getMenuProvider();

	/**
	 * @see #getMenuProvider()
	 */
	com.top_logic.graphic.flow.data.ContextMenu setMenuProvider(com.top_logic.graphic.flow.callback.DiagramContextMenuProvider value);

	/**
	 * Checks, whether {@link #getMenuProvider()} has a value.
	 */
	boolean hasMenuProvider();

	@Override
	com.top_logic.graphic.flow.data.ContextMenu setContent(com.top_logic.graphic.flow.data.Box value);

	@Override
	com.top_logic.graphic.flow.data.ContextMenu setX(double value);

	@Override
	com.top_logic.graphic.flow.data.ContextMenu setY(double value);

	@Override
	com.top_logic.graphic.flow.data.ContextMenu setWidth(double value);

	@Override
	com.top_logic.graphic.flow.data.ContextMenu setHeight(double value);

	@Override
	com.top_logic.graphic.flow.data.ContextMenu setCssClass(String value);

	@Override
	com.top_logic.graphic.flow.data.ContextMenu setUserObject(java.lang.Object value);

	@Override
	com.top_logic.graphic.flow.data.ContextMenu setClientId(String value);

	@Override
	com.top_logic.graphic.flow.data.ContextMenu setRenderInfo(java.lang.Object value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.ContextMenu readContextMenu(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.graphic.flow.data.ContextMenu) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert CONTEXT_MENU__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.graphic.flow.data.impl.ContextMenu_Impl result = new com.top_logic.graphic.flow.data.impl.ContextMenu_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

	@Override
	default ContextMenu self() {
		return this;
	}

}
