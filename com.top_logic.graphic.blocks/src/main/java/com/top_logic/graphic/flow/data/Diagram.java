package com.top_logic.graphic.flow.data;

/**
 * Top-level element that can be rendered.
 */
public interface Diagram extends Widget, com.top_logic.graphic.flow.operations.DiagramOperations {

	/**
	 * Creates a {@link com.top_logic.graphic.flow.data.Diagram} instance.
	 */
	static com.top_logic.graphic.flow.data.Diagram create() {
		return new com.top_logic.graphic.flow.data.impl.Diagram_Impl();
	}

	/** Identifier for the {@link com.top_logic.graphic.flow.data.Diagram} type in JSON format. */
	String DIAGRAM__TYPE = "Diagram";

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.Diagram readDiagram(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.graphic.flow.data.Diagram) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert DIAGRAM__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.graphic.flow.data.impl.Diagram_Impl result = new com.top_logic.graphic.flow.data.impl.Diagram_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

	@Override
	default Diagram self() {
		return this;
	}

	/** Creates a new {@link Diagram} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	public static Diagram readDiagram(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		in.nextTag();
		return com.top_logic.graphic.flow.data.impl.Diagram_Impl.readDiagram_XmlContent(in);
	}

}
