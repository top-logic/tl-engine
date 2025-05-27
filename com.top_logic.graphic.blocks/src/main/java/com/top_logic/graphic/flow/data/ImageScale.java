package com.top_logic.graphic.flow.data;

/**
 * Scaling instruction for an image within a box.
 */
public enum ImageScale implements de.haumacher.msgbuf.data.ProtocolEnum {

	/**
	 * Scales the graphic such that:
	 * <ul>
	 * <li>The aspect ratio is preserved.</li>
	 * <li>The entire <code>viewBox</code> is visible within the viewport.</li>
	 * <li>The <code>viewBox</code> is scaled up as much as possible, while still meeting the other
	 * criteria.</li>
	 * </ul>
	 *
	 * <p>
	 * In this case, if the aspect ratio of the graphic does not match the viewport, some of the
	 * viewport will extend beyond the bounds of the <code>viewBox</code> (i.e., the area into which the <code>viewBox</code>
	 * will draw will be smaller than the viewport). meet
	 * </p>
	 */
	MEET("meet"),

	/**
	 * Scales the graphic such that:
	 *
	 * <ul>
	 * <li>The aspect ratio is preserved.</li>
	 * <li>The entire viewport is covered by the <code>viewBox</code>.</li>
	 * <li>The <code>viewBox</code> is scaled down as much as possible, while still meeting the other
	 * criteria.</li>
	 * </ul>
	 *
	 * <p>
	 * In this case, if the aspect ratio of the <code>viewBox</code> does not match the viewport, some of the
	 * <code>viewBox</code> will extend beyond the bounds of the viewport (i.e., the area into which the <code>viewBox</code>
	 * will draw is larger than the viewport). slice
	 * </p>
	 */
	SLICE("slice"),

	;

	private final String _protocolName;

	private ImageScale(String protocolName) {
		_protocolName = protocolName;
	}

	/**
	 * The protocol name of a {@link ImageScale} constant.
	 *
	 * @see #valueOfProtocol(String)
	 */
	@Override
	public String protocolName() {
		return _protocolName;
	}

	/** Looks up a {@link ImageScale} constant by it's protocol name. */
	public static ImageScale valueOfProtocol(String protocolName) {
		if (protocolName == null) { return null; }
		switch (protocolName) {
			case "meet": return MEET;
			case "slice": return SLICE;
		}
		return MEET;
	}

	/** Writes this instance to the given output. */
	public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		out.value(protocolName());
	}

	/** Reads a new instance from the given reader. */
	public static ImageScale readImageScale(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		return valueOfProtocol(in.nextString());
	}

	/** Writes this instance to the given binary output. */
	public final void writeTo(de.haumacher.msgbuf.binary.DataWriter out) throws java.io.IOException {
		switch (this) {
			case MEET: out.value(1); break;
			case SLICE: out.value(2); break;
			default: out.value(0);
		}
	}

	/** Reads a new instance from the given binary reader. */
	public static ImageScale readImageScale(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
		switch (in.nextInt()) {
			case 1: return MEET;
			case 2: return SLICE;
			default: return MEET;
		}
	}
}
