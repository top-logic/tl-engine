package com.top_logic.graphic.flow.data;

public enum Alignment implements de.haumacher.msgbuf.data.ProtocolEnum {

	START("start"),

	MIDDLE("middle"),

	STOP("stop"),

	STRECH("strech"),

	;

	private final String _protocolName;

	private Alignment(String protocolName) {
		_protocolName = protocolName;
	}

	/**
	 * The protocol name of a {@link Alignment} constant.
	 *
	 * @see #valueOfProtocol(String)
	 */
	@Override
	public String protocolName() {
		return _protocolName;
	}

	/** Looks up a {@link Alignment} constant by it's protocol name. */
	public static Alignment valueOfProtocol(String protocolName) {
		if (protocolName == null) { return null; }
		switch (protocolName) {
			case "start": return START;
			case "middle": return MIDDLE;
			case "stop": return STOP;
			case "strech": return STRECH;
		}
		return START;
	}

	/** Writes this instance to the given output. */
	public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		out.value(protocolName());
	}

	/** Reads a new instance from the given reader. */
	public static Alignment readAlignment(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		return valueOfProtocol(in.nextString());
	}

	/** Writes this instance to the given binary output. */
	public final void writeTo(de.haumacher.msgbuf.binary.DataWriter out) throws java.io.IOException {
		switch (this) {
			case START: out.value(1); break;
			case MIDDLE: out.value(2); break;
			case STOP: out.value(3); break;
			case STRECH: out.value(4); break;
			default: out.value(0);
		}
	}

	/** Reads a new instance from the given binary reader. */
	public static Alignment readAlignment(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
		switch (in.nextInt()) {
			case 1: return START;
			case 2: return MIDDLE;
			case 3: return STOP;
			case 4: return STRECH;
			default: return START;
		}
	}
}
