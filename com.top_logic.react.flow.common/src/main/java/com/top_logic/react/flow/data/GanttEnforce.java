package com.top_logic.react.flow.data;

/**
 * Constraint enforcement mode for a Gantt edge.
 */
public enum GanttEnforce implements de.haumacher.msgbuf.data.ProtocolEnum {

	/**
	 * No constraint enforcement; the edge is purely visual. Violated style is still
	 *  applied if defined (based on endpoint position comparison).
	 */
	NONE("NONE"),

	/**
	 * Drag operations are blocked when the constraint would be violated.
	 */
	STRICT("STRICT"),

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
			case "NONE": return NONE;
			case "STRICT": return STRICT;
		}
		return NONE;
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
			case NONE: out.value(1); break;
			case STRICT: out.value(2); break;
			default: out.value(0);
		}
	}

	/** Reads a new instance from the given binary reader. */
	public static GanttEnforce readGanttEnforce(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
		switch (in.nextInt()) {
			case 1: return NONE;
			case 2: return STRICT;
			default: return NONE;
		}
	}
}
