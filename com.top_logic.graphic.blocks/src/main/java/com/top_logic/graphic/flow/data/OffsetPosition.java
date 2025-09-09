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

	/** Writes this instance to the given binary output. */
	public final void writeTo(de.haumacher.msgbuf.binary.DataWriter out) throws java.io.IOException {
		switch (this) {
			case CENTER: out.value(1); break;
			case CENTER_TOP: out.value(2); break;
			case CENTER_LEFT: out.value(3); break;
			case CENTER_BOTTOM: out.value(4); break;
			case CENTER_RIGHT: out.value(5); break;
			case TOP_LEFT: out.value(6); break;
			case TOP_RIGHT: out.value(7); break;
			case BOTTOM_LEFT: out.value(8); break;
			case BOTTOM_RIGHT: out.value(9); break;
			default: out.value(0);
		}
	}

	/** Reads a new instance from the given binary reader. */
	public static OffsetPosition readOffsetPosition(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
		switch (in.nextInt()) {
			case 1: return CENTER;
			case 2: return CENTER_TOP;
			case 3: return CENTER_LEFT;
			case 4: return CENTER_BOTTOM;
			case 5: return CENTER_RIGHT;
			case 6: return TOP_LEFT;
			case 7: return TOP_RIGHT;
			case 8: return BOTTOM_LEFT;
			case 9: return BOTTOM_RIGHT;
			default: return CENTER;
		}
	}
}
