package com.top_logic.graphic.flow.data;

/**
 * Description of connector symbols.
 */
public enum ConnectorSymbol implements de.haumacher.msgbuf.data.ProtocolEnum {

	/**
	 * Display no connector symbol.
	 */
	NONE("none"),

	/**
	 * An open arrow.
	 */
	ARROW("arrow"),

	/**
	 * A closed (triangular) arrow.
	 */
	CLOSED_ARROW("closedArrow"),

	/**
	 * A filled (triangular) arrow.
	 */
	FILLED_ARROW("filledArrow"),

	/**
	 * A diamond.
	 */
	DIAMOND("diamond"),

	/**
	 * A filled diamond.
	 */
	FILLED_DIAMOND("filledDiamond"),

	;

	private final String _protocolName;

	private ConnectorSymbol(String protocolName) {
		_protocolName = protocolName;
	}

	/**
	 * The protocol name of a {@link ConnectorSymbol} constant.
	 *
	 * @see #valueOfProtocol(String)
	 */
	@Override
	public String protocolName() {
		return _protocolName;
	}

	/** Looks up a {@link ConnectorSymbol} constant by it's protocol name. */
	public static ConnectorSymbol valueOfProtocol(String protocolName) {
		if (protocolName == null) { return null; }
		switch (protocolName) {
			case "none": return NONE;
			case "arrow": return ARROW;
			case "closedArrow": return CLOSED_ARROW;
			case "filledArrow": return FILLED_ARROW;
			case "diamond": return DIAMOND;
			case "filledDiamond": return FILLED_DIAMOND;
		}
		return NONE;
	}

	/** Writes this instance to the given output. */
	public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		out.value(protocolName());
	}

	/** Reads a new instance from the given reader. */
	public static ConnectorSymbol readConnectorSymbol(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		return valueOfProtocol(in.nextString());
	}
}
