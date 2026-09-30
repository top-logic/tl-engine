package com.top_logic.graphic.flow.data;

/**
 * An alignment instruction.
 */
public enum Alignment implements de.haumacher.msgbuf.data.ProtocolEnum {

	/**
	 * The content is aligned to the start coordinate of the container (left-to-right and top-to-bottom).
	 */
	START("start"),

	/**
	 * The content is centered within the available space of the container.
	 */
	MIDDLE("middle"),

	/**
	 * The content is aligned to the end coordinate of the container (left-to-right and top-to-bottom).
	 */
	STOP("stop"),

	/**
	 * The content is stretched to fit the size of the container.
	 */
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
}
