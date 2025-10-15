package com.top_logic.graphic.flow.data;

/**
 * An alignment of some box within a potentially larger content box.
 */
public interface Align extends Decoration, com.top_logic.graphic.flow.operations.AlignOperations {

	/**
	 * Creates a {@link com.top_logic.graphic.flow.data.Align} instance.
	 */
	static com.top_logic.graphic.flow.data.Align create() {
		return new com.top_logic.graphic.flow.data.impl.Align_Impl();
	}

	/** Identifier for the {@link com.top_logic.graphic.flow.data.Align} type in JSON format. */
	String ALIGN__TYPE = "Align";

	/** @see #getXAlign() */
	String X_ALIGN__PROP = "xAlign";

	/** @see #getYAlign() */
	String Y_ALIGN__PROP = "yAlign";

	/**
	 * Instruction for horizontal alignment.
	 */
	com.top_logic.graphic.flow.data.Alignment getXAlign();

	/**
	 * @see #getXAlign()
	 */
	com.top_logic.graphic.flow.data.Align setXAlign(com.top_logic.graphic.flow.data.Alignment value);

	/**
	 * Instruction for vertical alignment.
	 */
	com.top_logic.graphic.flow.data.Alignment getYAlign();

	/**
	 * @see #getYAlign()
	 */
	com.top_logic.graphic.flow.data.Align setYAlign(com.top_logic.graphic.flow.data.Alignment value);

	@Override
	com.top_logic.graphic.flow.data.Align setContent(com.top_logic.graphic.flow.data.Box value);

	@Override
	com.top_logic.graphic.flow.data.Align setX(double value);

	@Override
	com.top_logic.graphic.flow.data.Align setY(double value);

	@Override
	com.top_logic.graphic.flow.data.Align setWidth(double value);

	@Override
	com.top_logic.graphic.flow.data.Align setHeight(double value);

	@Override
	com.top_logic.graphic.flow.data.Align setCssClass(String value);

	@Override
	com.top_logic.graphic.flow.data.Align setUserObject(java.lang.Object value);

	@Override
	com.top_logic.graphic.flow.data.Align setClientId(String value);

	@Override
	com.top_logic.graphic.flow.data.Align setRenderInfo(java.lang.Object value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.Align readAlign(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.graphic.flow.data.Align) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert ALIGN__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.graphic.flow.data.impl.Align_Impl result = new com.top_logic.graphic.flow.data.impl.Align_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

	@Override
	default Align self() {
		return this;
	}

}
