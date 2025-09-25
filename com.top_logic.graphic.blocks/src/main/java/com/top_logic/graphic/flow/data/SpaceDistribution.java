package com.top_logic.graphic.flow.data;

/**
 * Instruction how to use extra space in a row layout.
 */
public enum SpaceDistribution implements de.haumacher.msgbuf.data.ProtocolEnum {

	/**
	 * Do not use extra space, allow to {@link Align} the row within its container.
	 */
	NONE("NONE"),

	/**
	 * Stretch the content boxes but keep the gap constant.
	 */
	STRETCH_CONTENT("STRETCH_CONTENT"),

	/**
	 * Stretch the gap between boxes.
	 */
	STRETCH_GAP("STRETCH_GAP"),

	/**
	 * Evenly distribute extra space among the content and the gap between boxes.
	 */
	STRETCH_ALL("STRETCH_ALL"),

	;

	private final String _protocolName;

	private SpaceDistribution(String protocolName) {
		_protocolName = protocolName;
	}

	/**
	 * The protocol name of a {@link SpaceDistribution} constant.
	 *
	 * @see #valueOfProtocol(String)
	 */
	@Override
	public String protocolName() {
		return _protocolName;
	}

	/** Looks up a {@link SpaceDistribution} constant by it's protocol name. */
	public static SpaceDistribution valueOfProtocol(String protocolName) {
		if (protocolName == null) { return null; }
		switch (protocolName) {
			case "NONE": return NONE;
			case "STRETCH_CONTENT": return STRETCH_CONTENT;
			case "STRETCH_GAP": return STRETCH_GAP;
			case "STRETCH_ALL": return STRETCH_ALL;
		}
		return NONE;
	}

	/** Writes this instance to the given output. */
	public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		out.value(protocolName());
	}

	/** Reads a new instance from the given reader. */
	public static SpaceDistribution readSpaceDistribution(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		return valueOfProtocol(in.nextString());
	}

	/** Writes this instance to the given binary output. */
	public final void writeTo(de.haumacher.msgbuf.binary.DataWriter out) throws java.io.IOException {
		switch (this) {
			case NONE: out.value(1); break;
			case STRETCH_CONTENT: out.value(2); break;
			case STRETCH_GAP: out.value(3); break;
			case STRETCH_ALL: out.value(4); break;
			default: out.value(0);
		}
	}

	/** Reads a new instance from the given binary reader. */
	public static SpaceDistribution readSpaceDistribution(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
		switch (in.nextInt()) {
			case 1: return NONE;
			case 2: return STRETCH_CONTENT;
			case 3: return STRETCH_GAP;
			case 4: return STRETCH_ALL;
			default: return NONE;
		}
	}
}
