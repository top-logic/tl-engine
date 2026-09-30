package com.top_logic.graphic.flow.data;

/**
 * A position specification of a canonical point within a box.
 */
public enum OffsetPosition implements de.haumacher.msgbuf.data.ProtocolEnum {

	/**
	 * The center of the box.
	 */
	CENTER("center"),

	/**
	 * The center of the top border.
	 */
	CENTER_TOP("center-top"),

	/**
	 * The center of the left border.
	 */
	CENTER_LEFT("center-left"),

	/**
	 * The center of the bottom border.
	 */
	CENTER_BOTTOM("center-bottom"),

	/**
	 * The center of the right border.
	 */
	CENTER_RIGHT("center-right"),

	/**
	 * The top left corner.
	 */
	TOP_LEFT("top-left"),

	/**
	 * The top right corner.
	 */
	TOP_RIGHT("top-right"),

	/**
	 * The bottom left corner.
	 */
	BOTTOM_LEFT("bottom-left"),

	/**
	 * The bottom right corner.
	 */
	BOTTOM_RIGHT("bottom-right"),

	;

	private final String _protocolName;

	private OffsetPosition(String protocolName) {
		_protocolName = protocolName;
	}

	/**
	 * The protocol name of a {@link OffsetPosition} constant.
	 *
	 * @see #valueOfProtocol(String)
	 */
	@Override
	public String protocolName() {
		return _protocolName;
	}

	/** Looks up a {@link OffsetPosition} constant by it's protocol name. */
	public static OffsetPosition valueOfProtocol(String protocolName) {
		if (protocolName == null) { return null; }
		switch (protocolName) {
			case "center": return CENTER;
			case "center-top": return CENTER_TOP;
			case "center-left": return CENTER_LEFT;
			case "center-bottom": return CENTER_BOTTOM;
			case "center-right": return CENTER_RIGHT;
			case "top-left": return TOP_LEFT;
			case "top-right": return TOP_RIGHT;
			case "bottom-left": return BOTTOM_LEFT;
			case "bottom-right": return BOTTOM_RIGHT;
		}
		return CENTER;
	}

	/** Writes this instance to the given output. */
	public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		out.value(protocolName());
	}

	/** Reads a new instance from the given reader. */
	public static OffsetPosition readOffsetPosition(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		return valueOfProtocol(in.nextString());
	}
}
