package com.top_logic.graphic.flow.data;

/**
 * Mouse buttons that can be used to interact with diagram elements.
 */
public enum MouseButton implements de.haumacher.msgbuf.data.ProtocolEnum {

	/**
	 * The left mouse button.
	 */
	LEFT("LEFT"),

	/**
	 * The right mouse button.
	 */
	RIGHT("RIGHT"),

	/**
	 * The middle mouse button.
	 */
	MIDDLE("MIDDLE"),

	;

	private final String _protocolName;

	private MouseButton(String protocolName) {
		_protocolName = protocolName;
	}

	/**
	 * The protocol name of a {@link MouseButton} constant.
	 *
	 * @see #valueOfProtocol(String)
	 */
	@Override
	public String protocolName() {
		return _protocolName;
	}

	/** Looks up a {@link MouseButton} constant by it's protocol name. */
	public static MouseButton valueOfProtocol(String protocolName) {
		if (protocolName == null) { return null; }
		switch (protocolName) {
			case "LEFT": return LEFT;
			case "RIGHT": return RIGHT;
			case "MIDDLE": return MIDDLE;
		}
		return LEFT;
	}

	/** Writes this instance to the given output. */
	public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		out.value(protocolName());
	}

	/** Reads a new instance from the given reader. */
	public static MouseButton readMouseButton(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		return valueOfProtocol(in.nextString());
	}

	/** Writes this instance to the given binary output. */
	public final void writeTo(de.haumacher.msgbuf.binary.DataWriter out) throws java.io.IOException {
		switch (this) {
			case LEFT: out.value(1); break;
			case RIGHT: out.value(2); break;
			case MIDDLE: out.value(3); break;
			default: out.value(0);
		}
	}

	/** Reads a new instance from the given binary reader. */
	public static MouseButton readMouseButton(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
		switch (in.nextInt()) {
			case 1: return LEFT;
			case 2: return RIGHT;
			case 3: return MIDDLE;
			default: return LEFT;
		}
	}
}
