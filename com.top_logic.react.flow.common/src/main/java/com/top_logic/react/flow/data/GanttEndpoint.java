package com.top_logic.react.flow.data;

/**
 * Which end of an item a Gantt edge attaches to.
 */
public enum GanttEndpoint implements de.haumacher.msgbuf.data.ProtocolEnum {

	START("START"),

	END("END"),

	;

	private final String _protocolName;

	private GanttEndpoint(String protocolName) {
		_protocolName = protocolName;
	}

	/**
	 * The protocol name of a {@link GanttEndpoint} constant.
	 *
	 * @see #valueOfProtocol(String)
	 */
	@Override
	public String protocolName() {
		return _protocolName;
	}

	/** Looks up a {@link GanttEndpoint} constant by it's protocol name. */
	public static GanttEndpoint valueOfProtocol(String protocolName) {
		if (protocolName == null) { return null; }
		switch (protocolName) {
			case "START": return START;
			case "END": return END;
		}
		return START;
	}

	/** Writes this instance to the given output. */
	public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		out.value(protocolName());
	}

	/** Reads a new instance from the given reader. */
	public static GanttEndpoint readGanttEndpoint(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		return valueOfProtocol(in.nextString());
	}

	/** Writes this instance to the given binary output. */
	public final void writeTo(de.haumacher.msgbuf.binary.DataWriter out) throws java.io.IOException {
		switch (this) {
			case START: out.value(1); break;
			case END: out.value(2); break;
			default: out.value(0);
		}
	}

	/** Reads a new instance from the given binary reader. */
	public static GanttEndpoint readGanttEndpoint(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
		switch (in.nextInt()) {
			case 1: return START;
			case 2: return END;
			default: return START;
		}
	}
}
