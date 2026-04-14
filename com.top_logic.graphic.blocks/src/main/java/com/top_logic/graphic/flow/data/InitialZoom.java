package com.top_logic.graphic.flow.data;

/**
 * Initial zoom policy applied when a {@link Diagram} is (re-)displayed.
 *
 * <p>
 * The fixed variants set an absolute zoom factor. The fit variants scale the diagram such that
 * it fits into the available viewport; by default fit variants never enlarge above 100%.
 * The <code>_ENLARGE</code> variants allow scaling beyond 100% to fill the viewport.
 * </p>
 */
public enum InitialZoom implements de.haumacher.msgbuf.data.ProtocolEnum {

	/**
	 * Fixed zoom at 100% (1:1). Default.
	 */
	FIXED_100("FIXED_100"),

	/**
	 * Fixed zoom at 50%.
	 */
	FIXED_50("FIXED_50"),

	/**
	 * Fixed zoom at 75%.
	 */
	FIXED_75("FIXED_75"),

	/**
	 * Fixed zoom at 150%.
	 */
	FIXED_150("FIXED_150"),

	/**
	 * Fixed zoom at 200%.
	 */
	FIXED_200("FIXED_200"),

	/**
	 * Scale to fit the entire diagram into the viewport, but never enlarge above 100%.
	 */
	FIT_TO_PAGE("FIT_TO_PAGE"),

	/**
	 * Scale to fit the diagram width into the viewport, but never enlarge above 100%.
	 */
	FIT_TO_WIDTH("FIT_TO_WIDTH"),

	/**
	 * Scale to fit the diagram height into the viewport, but never enlarge above 100%.
	 */
	FIT_TO_HEIGHT("FIT_TO_HEIGHT"),

	/**
	 * Scale to fit the entire diagram into the viewport, enlarging above 100% if needed.
	 */
	FIT_TO_PAGE_ENLARGE("FIT_TO_PAGE_ENLARGE"),

	/**
	 * Scale to fit the diagram width into the viewport, enlarging above 100% if needed.
	 */
	FIT_TO_WIDTH_ENLARGE("FIT_TO_WIDTH_ENLARGE"),

	/**
	 * Scale to fit the diagram height into the viewport, enlarging above 100% if needed.
	 */
	FIT_TO_HEIGHT_ENLARGE("FIT_TO_HEIGHT_ENLARGE"),

	;

	private final String _protocolName;

	private InitialZoom(String protocolName) {
		_protocolName = protocolName;
	}

	/**
	 * The protocol name of a {@link InitialZoom} constant.
	 *
	 * @see #valueOfProtocol(String)
	 */
	@Override
	public String protocolName() {
		return _protocolName;
	}

	/** Looks up a {@link InitialZoom} constant by it's protocol name. */
	public static InitialZoom valueOfProtocol(String protocolName) {
		if (protocolName == null) { return null; }
		switch (protocolName) {
			case "FIXED_100": return FIXED_100;
			case "FIXED_50": return FIXED_50;
			case "FIXED_75": return FIXED_75;
			case "FIXED_150": return FIXED_150;
			case "FIXED_200": return FIXED_200;
			case "FIT_TO_PAGE": return FIT_TO_PAGE;
			case "FIT_TO_WIDTH": return FIT_TO_WIDTH;
			case "FIT_TO_HEIGHT": return FIT_TO_HEIGHT;
			case "FIT_TO_PAGE_ENLARGE": return FIT_TO_PAGE_ENLARGE;
			case "FIT_TO_WIDTH_ENLARGE": return FIT_TO_WIDTH_ENLARGE;
			case "FIT_TO_HEIGHT_ENLARGE": return FIT_TO_HEIGHT_ENLARGE;
		}
		return FIXED_100;
	}

	/** Writes this instance to the given output. */
	public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		out.value(protocolName());
	}

	/** Reads a new instance from the given reader. */
	public static InitialZoom readInitialZoom(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		return valueOfProtocol(in.nextString());
	}

	/** Writes this instance to the given binary output. */
	public final void writeTo(de.haumacher.msgbuf.binary.DataWriter out) throws java.io.IOException {
		switch (this) {
			case FIXED_100: out.value(1); break;
			case FIXED_50: out.value(2); break;
			case FIXED_75: out.value(3); break;
			case FIXED_150: out.value(4); break;
			case FIXED_200: out.value(5); break;
			case FIT_TO_PAGE: out.value(6); break;
			case FIT_TO_WIDTH: out.value(7); break;
			case FIT_TO_HEIGHT: out.value(8); break;
			case FIT_TO_PAGE_ENLARGE: out.value(9); break;
			case FIT_TO_WIDTH_ENLARGE: out.value(10); break;
			case FIT_TO_HEIGHT_ENLARGE: out.value(11); break;
			default: out.value(0);
		}
	}

	/** Reads a new instance from the given binary reader. */
	public static InitialZoom readInitialZoom(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
		switch (in.nextInt()) {
			case 1: return FIXED_100;
			case 2: return FIXED_50;
			case 3: return FIXED_75;
			case 4: return FIXED_150;
			case 5: return FIXED_200;
			case 6: return FIT_TO_PAGE;
			case 7: return FIT_TO_WIDTH;
			case 8: return FIT_TO_HEIGHT;
			case 9: return FIT_TO_PAGE_ENLARGE;
			case 10: return FIT_TO_WIDTH_ENLARGE;
			case 11: return FIT_TO_HEIGHT_ENLARGE;
			default: return FIXED_100;
		}
	}
}
