package com.top_logic.graphic.flow.data;

public interface SelectableBox extends Decoration, com.top_logic.graphic.flow.operations.SelectableBoxOperations {

	/**
	 * Creates a {@link com.top_logic.graphic.flow.data.SelectableBox} instance.
	 */
	static com.top_logic.graphic.flow.data.SelectableBox create() {
		return new com.top_logic.graphic.flow.data.impl.SelectableBox_Impl();
	}

	/** Identifier for the {@link com.top_logic.graphic.flow.data.SelectableBox} type in JSON format. */
	String SELECTABLE_BOX__TYPE = "SelectableBox";

	/** @see #isSelected() */
	String SELECTED__PROP = "selected";

	/**
	 * Whether this element is currently selected.
	 */
	boolean isSelected();

	/**
	 * @see #isSelected()
	 */
	com.top_logic.graphic.flow.data.SelectableBox setSelected(boolean value);

	@Override
	com.top_logic.graphic.flow.data.SelectableBox setContent(com.top_logic.graphic.flow.data.Box value);

	@Override
	com.top_logic.graphic.flow.data.SelectableBox setX(double value);

	@Override
	com.top_logic.graphic.flow.data.SelectableBox setY(double value);

	@Override
	com.top_logic.graphic.flow.data.SelectableBox setWidth(double value);

	@Override
	com.top_logic.graphic.flow.data.SelectableBox setHeight(double value);

	@Override
	com.top_logic.graphic.flow.data.SelectableBox setUserObject(java.lang.Object value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.SelectableBox readSelectableBox(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.graphic.flow.data.SelectableBox) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert SELECTABLE_BOX__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.graphic.flow.data.impl.SelectableBox_Impl result = new com.top_logic.graphic.flow.data.impl.SelectableBox_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

	@Override
	default SelectableBox self() {
		return this;
	}

}
