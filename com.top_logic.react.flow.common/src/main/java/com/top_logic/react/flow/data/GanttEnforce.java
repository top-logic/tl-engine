package com.top_logic.react.flow.data;

/**
 * Semantic of a Gantt edge.
 */
public enum GanttEnforce implements de.haumacher.msgbuf.data.ProtocolEnum {

	/**
	 * Operation is blocked when the constraint would be violated.
	 */
	STRICT("STRICT"),

	/**
	 * Operation is allowed; violation is marked visually (red edge).
	 */
	WARN("WARN"),

	/**
	 * Purely decorative relation; no time constraint.
	 */
	NONE("NONE"),

	;

	private final String _protocolName;

	private GanttEnforce(String protocolName) {
		_protocolName = protocolName;
	}

	/**
	 * The protocol name of a {@link GanttEnforce} constant.
	 *
	 * @see #valueOfProtocol(String)
	 */
	@Override
	public String protocolName() {
		return _protocolName;
	}

	/** Looks up a {@link GanttEnforce} constant by it's protocol name. */
	public static GanttEnforce valueOfProtocol(String protocolName) {
		if (protocolName == null) { return null; }
		switch (protocolName) {
			case "STRICT": return STRICT;
			case "WARN": return WARN;
			case "NONE": return NONE;
		}
		return STRICT;
	}

	/** Writes this instance to the given output. */
	public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		out.value(protocolName());
	}

	/** Reads a new instance from the given reader. */
	public static GanttEnforce readGanttEnforce(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		return valueOfProtocol(in.nextString());
	}

	/** Writes this instance to the given binary output. */
	public final void writeTo(de.haumacher.msgbuf.binary.DataWriter out) throws java.io.IOException {
		switch (this) {
			case STRICT: out.value(1); break;
			case WARN: out.value(2); break;
			case NONE: out.value(3); break;
			default: out.value(0);
		}
	}

	/** Reads a new instance from the given binary reader. */
	public static GanttEnforce readGanttEnforce(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
		switch (in.nextInt()) {
			case 1: return STRICT;
			case 2: return WARN;
			case 3: return NONE;
			default: return STRICT;
		}
	}
}
