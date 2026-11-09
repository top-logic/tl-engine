package com.top_logic.react.flow.data;

/**
 * The part of a {@link GanttLayout} dragged by a pan gesture.
 */
public enum GanttPanTarget implements de.haumacher.msgbuf.data.ProtocolEnum {

	/**
	 * The content is dragged, it moves with the pointer.
	 */
	CONTENT("CONTENT"),

	/**
	 * The thumb of the vertical scrollbar is dragged, it moves with the pointer.
	 */
	VERTICAL_THUMB("VERTICAL_THUMB"),

	/**
	 * The thumb of the horizontal scrollbar is dragged, it moves with the pointer.
	 */
	HORIZONTAL_THUMB("HORIZONTAL_THUMB"),

	;

	private final String _protocolName;

	private GanttPanTarget(String protocolName) {
		_protocolName = protocolName;
	}

	/**
	 * The protocol name of a {@link GanttPanTarget} constant.
	 *
	 * @see #valueOfProtocol(String)
	 */
	@Override
	public String protocolName() {
		return _protocolName;
	}

	/** Looks up a {@link GanttPanTarget} constant by it's protocol name. */
	public static GanttPanTarget valueOfProtocol(String protocolName) {
		if (protocolName == null) { return null; }
		switch (protocolName) {
			case "CONTENT": return CONTENT;
			case "VERTICAL_THUMB": return VERTICAL_THUMB;
			case "HORIZONTAL_THUMB": return HORIZONTAL_THUMB;
		}
		return CONTENT;
	}

	/** Writes this instance to the given output. */
	public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		out.value(protocolName());
	}

	/** Reads a new instance from the given reader. */
	public static GanttPanTarget readGanttPanTarget(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		return valueOfProtocol(in.nextString());
	}
}
