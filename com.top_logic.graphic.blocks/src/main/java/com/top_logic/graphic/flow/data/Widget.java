package com.top_logic.graphic.flow.data;

/**
 * Common base class for all diagram elements that can be rendered to SVG.
 */
public interface Widget extends de.haumacher.msgbuf.graph.SharedGraphNode, com.top_logic.graphic.flow.operations.WidgetOperations {

	/** Type codes for the {@link com.top_logic.graphic.flow.data.Widget} hierarchy. */
	public enum TypeKind {

		/** Type literal for {@link com.top_logic.graphic.flow.data.Diagram}. */
		DIAGRAM,

		/** Type literal for {@link com.top_logic.graphic.flow.data.FloatingLayout}. */
		FLOATING_LAYOUT,

		/** Type literal for {@link com.top_logic.graphic.flow.data.TreeLayout}. */
		TREE_LAYOUT,

		/** Type literal for {@link com.top_logic.graphic.flow.data.Text}. */
		TEXT,

		/** Type literal for {@link com.top_logic.graphic.flow.data.Image}. */
		IMAGE,

		/** Type literal for {@link com.top_logic.graphic.flow.data.Empty}. */
		EMPTY,

		/** Type literal for {@link com.top_logic.graphic.flow.data.Stack}. */
		STACK,

		/** Type literal for {@link com.top_logic.graphic.flow.data.SelectableBox}. */
		SELECTABLE_BOX,

		/** Type literal for {@link com.top_logic.graphic.flow.data.ClickTarget}. */
		CLICK_TARGET,

		/** Type literal for {@link com.top_logic.graphic.flow.data.Tooltip}. */
		TOOLTIP,

		/** Type literal for {@link com.top_logic.graphic.flow.data.Align}. */
		ALIGN,

		/** Type literal for {@link com.top_logic.graphic.flow.data.Border}. */
		BORDER,

		/** Type literal for {@link com.top_logic.graphic.flow.data.Fill}. */
		FILL,

		/** Type literal for {@link com.top_logic.graphic.flow.data.Padding}. */
		PADDING,

		/** Type literal for {@link com.top_logic.graphic.flow.data.CompassLayout}. */
		COMPASS_LAYOUT,

		/** Type literal for {@link com.top_logic.graphic.flow.data.GridLayout}. */
		GRID_LAYOUT,

		/** Type literal for {@link com.top_logic.graphic.flow.data.HorizontalLayout}. */
		HORIZONTAL_LAYOUT,

		/** Type literal for {@link com.top_logic.graphic.flow.data.VerticalLayout}. */
		VERTICAL_LAYOUT,

		/** Type literal for {@link com.top_logic.graphic.flow.data.TreeConnection}. */
		TREE_CONNECTION,

		/** Type literal for {@link com.top_logic.graphic.flow.data.TreeConnector}. */
		TREE_CONNECTOR,
		;

	}

	/** Visitor interface for the {@link com.top_logic.graphic.flow.data.Widget} hierarchy.*/
	public interface Visitor<R,A,E extends Throwable> extends com.top_logic.graphic.flow.data.Box.Visitor<R,A,E> {

		/** Visit case for {@link com.top_logic.graphic.flow.data.Diagram}.*/
		R visit(com.top_logic.graphic.flow.data.Diagram self, A arg) throws E;

		/** Visit case for {@link com.top_logic.graphic.flow.data.TreeConnection}.*/
		R visit(com.top_logic.graphic.flow.data.TreeConnection self, A arg) throws E;

		/** Visit case for {@link com.top_logic.graphic.flow.data.TreeConnector}.*/
		R visit(com.top_logic.graphic.flow.data.TreeConnector self, A arg) throws E;

	}

	/** @see #getCssClass() */
	String CSS_CLASS__PROP = "cssClass";

	/** @see #getUserObject() */
	String USER_OBJECT__PROP = "userObject";

	/** @see #getClientId() */
	String CLIENT_ID__PROP = "clientId";

	/** The type code of this instance. */
	TypeKind kind();

	/**
	 * Custom CSS class to add to the SVG element.
	 */
	String getCssClass();

	/**
	 * @see #getCssClass()
	 */
	com.top_logic.graphic.flow.data.Widget setCssClass(String value);

	/**
	 * Checks, whether {@link #getCssClass()} has a value.
	 */
	boolean hasCssClass();

	/**
	 * An arbitrary object that can be associated with this graphics elements.
	 */
	java.lang.Object getUserObject();

	/**
	 * @see #getUserObject()
	 */
	com.top_logic.graphic.flow.data.Widget setUserObject(java.lang.Object value);

	/**
	 * Checks, whether {@link #getUserObject()} has a value.
	 */
	boolean hasUserObject();

	/**
	 * Identifier to associate this graphics element to the client DOM.
	 */
	String getClientId();

	/**
	 * @see #getClientId()
	 */
	com.top_logic.graphic.flow.data.Widget setClientId(String value);

	/**
	 * Checks, whether {@link #getClientId()} has a value.
	 */
	boolean hasClientId();

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.Widget readWidget(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.graphic.flow.data.Widget) scope.resolveOrFail(in.nextInt());
		}
		com.top_logic.graphic.flow.data.Widget result;
		in.beginArray();
		String type = in.nextString();
		int id = in.nextInt();
		switch (type) {
			case Diagram.DIAGRAM__TYPE: result = com.top_logic.graphic.flow.data.Diagram.create(); break;
			case TreeConnection.TREE_CONNECTION__TYPE: result = com.top_logic.graphic.flow.data.TreeConnection.create(); break;
			case TreeConnector.TREE_CONNECTOR__TYPE: result = com.top_logic.graphic.flow.data.TreeConnector.create(); break;
			case FloatingLayout.FLOATING_LAYOUT__TYPE: result = com.top_logic.graphic.flow.data.FloatingLayout.create(); break;
			case Text.TEXT__TYPE: result = com.top_logic.graphic.flow.data.Text.create(); break;
			case Image.IMAGE__TYPE: result = com.top_logic.graphic.flow.data.Image.create(); break;
			case Empty.EMPTY__TYPE: result = com.top_logic.graphic.flow.data.Empty.create(); break;
			case Stack.STACK__TYPE: result = com.top_logic.graphic.flow.data.Stack.create(); break;
			case CompassLayout.COMPASS_LAYOUT__TYPE: result = com.top_logic.graphic.flow.data.CompassLayout.create(); break;
			case TreeLayout.TREE_LAYOUT__TYPE: result = com.top_logic.graphic.flow.data.TreeLayout.create(); break;
			case SelectableBox.SELECTABLE_BOX__TYPE: result = com.top_logic.graphic.flow.data.SelectableBox.create(); break;
			case ClickTarget.CLICK_TARGET__TYPE: result = com.top_logic.graphic.flow.data.ClickTarget.create(); break;
			case Tooltip.TOOLTIP__TYPE: result = com.top_logic.graphic.flow.data.Tooltip.create(); break;
			case Align.ALIGN__TYPE: result = com.top_logic.graphic.flow.data.Align.create(); break;
			case Border.BORDER__TYPE: result = com.top_logic.graphic.flow.data.Border.create(); break;
			case Fill.FILL__TYPE: result = com.top_logic.graphic.flow.data.Fill.create(); break;
			case Padding.PADDING__TYPE: result = com.top_logic.graphic.flow.data.Padding.create(); break;
			case GridLayout.GRID_LAYOUT__TYPE: result = com.top_logic.graphic.flow.data.GridLayout.create(); break;
			case HorizontalLayout.HORIZONTAL_LAYOUT__TYPE: result = com.top_logic.graphic.flow.data.HorizontalLayout.create(); break;
			case VerticalLayout.VERTICAL_LAYOUT__TYPE: result = com.top_logic.graphic.flow.data.VerticalLayout.create(); break;
			default: in.skipValue(); result = null; break;
		}
		if (result != null) {
			scope.readData(result, id, in);
		}
		in.endArray();
		return result;
	}

	@Override
	default Widget self() {
		return this;
	}

	/** Accepts the given visitor. */
	public abstract <R,A,E extends Throwable> R visit(Visitor<R,A,E> v, A arg) throws E;

}
