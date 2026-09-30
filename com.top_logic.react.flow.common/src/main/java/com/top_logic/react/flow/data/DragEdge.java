package com.top_logic.react.flow.data;

/**
 * Edge of a box for resize operations.
 */
public enum DragEdge implements de.haumacher.msgbuf.data.ProtocolEnum {

	/**
	 * Top edge.
	 */
	N("N"),

	/**
	 * Right edge.
	 */
	E("E"),

	/**
	 * Bottom edge.
	 */
	S("S"),

	/**
	 * Left edge.
	 */
	W("W"),

	;

	private final String _protocolName;

	private DragEdge(String protocolName) {
		_protocolName = protocolName;
	}

	/**
	 * The protocol name of a {@link DragEdge} constant.
	 *
	 * @see #valueOfProtocol(String)
	 */
	@Override
	public String protocolName() {
		return _protocolName;
	}

	/** Looks up a {@link DragEdge} constant by it's protocol name. */
	public static DragEdge valueOfProtocol(String protocolName) {
		if (protocolName == null) { return null; }
		switch (protocolName) {
			case "N": return N;
			case "E": return E;
			case "S": return S;
			case "W": return W;
		}
		return N;
	}

	/** Writes this instance to the given output. */
	public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		out.value(protocolName());
	}

	/** Reads a new instance from the given reader. */
	public static DragEdge readDragEdge(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		return valueOfProtocol(in.nextString());
	}
}
