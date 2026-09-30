package com.top_logic.graphic.flow.data;

/**
 * A layout direction for a structured diagram.
 */
public enum DiagramDirection implements de.haumacher.msgbuf.data.ProtocolEnum {

	/**
	 * Diagram contents is layouted from left to right.
	 */
	LTR("ltr"),

	/**
	 * Diagram contents is layouted from right to left.
	 */
	RTL("rtl"),

	/**
	 * Diagram contents is layouted from top to bottom.
	 */
	TOPDOWN("topdown"),

	/**
	 * Diagram contents is layouted from bottom to top.
	 */
	BOTTOMUP("bottomup"),

	;

	private final String _protocolName;

	private DiagramDirection(String protocolName) {
		_protocolName = protocolName;
	}

	/**
	 * The protocol name of a {@link DiagramDirection} constant.
	 *
	 * @see #valueOfProtocol(String)
	 */
	@Override
	public String protocolName() {
		return _protocolName;
	}

	/** Looks up a {@link DiagramDirection} constant by it's protocol name. */
	public static DiagramDirection valueOfProtocol(String protocolName) {
		if (protocolName == null) { return null; }
		switch (protocolName) {
			case "ltr": return LTR;
			case "rtl": return RTL;
			case "topdown": return TOPDOWN;
			case "bottomup": return BOTTOMUP;
		}
		return LTR;
	}

	/** Writes this instance to the given output. */
	public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		out.value(protocolName());
	}

	/** Reads a new instance from the given reader. */
	public static DiagramDirection readDiagramDirection(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		return valueOfProtocol(in.nextString());
	}
}
