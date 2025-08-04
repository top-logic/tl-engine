package com.top_logic.graphic.flow.data;

/**
 * A box whose content is clipped.
 */
public interface ClipBox extends Decoration, com.top_logic.graphic.flow.operations.ClipBoxOperations {

	/**
	 * Creates a {@link com.top_logic.graphic.flow.data.ClipBox} instance.
	 */
	static com.top_logic.graphic.flow.data.ClipBox create() {
		return new com.top_logic.graphic.flow.data.impl.ClipBox_Impl();
	}

	/** Identifier for the {@link com.top_logic.graphic.flow.data.ClipBox} type in JSON format. */
	String CLIP_BOX__TYPE = "ClipBox";

	@Override
	com.top_logic.graphic.flow.data.ClipBox setContent(com.top_logic.graphic.flow.data.Box value);

	@Override
	com.top_logic.graphic.flow.data.ClipBox setX(double value);

	@Override
	com.top_logic.graphic.flow.data.ClipBox setY(double value);

	@Override
	com.top_logic.graphic.flow.data.ClipBox setWidth(double value);

	@Override
	com.top_logic.graphic.flow.data.ClipBox setHeight(double value);

	@Override
	com.top_logic.graphic.flow.data.ClipBox setCssClass(String value);

	@Override
	com.top_logic.graphic.flow.data.ClipBox setUserObject(java.lang.Object value);

	@Override
	com.top_logic.graphic.flow.data.ClipBox setClientId(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.ClipBox readClipBox(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.graphic.flow.data.ClipBox) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert CLIP_BOX__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.graphic.flow.data.impl.ClipBox_Impl result = new com.top_logic.graphic.flow.data.impl.ClipBox_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

	@Override
	default ClipBox self() {
		return this;
	}

}
