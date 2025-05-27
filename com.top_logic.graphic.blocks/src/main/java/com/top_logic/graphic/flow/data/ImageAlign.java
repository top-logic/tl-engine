package com.top_logic.graphic.flow.data;

/**
 * Alignment of an image within a box.
 */
public enum ImageAlign implements de.haumacher.msgbuf.data.ProtocolEnum {

	/**
	 * Forces uniform scaling. Align the midpoint X value of the element's <code>viewBox</code> with the midpoint
	 * X value of the viewport. Align the midpoint Y value of the element's <code>viewBox</code> with the
	 * midpoint Y value of the viewport. This is the default value.
	 */
	X_MID_YMID("xMidYMid"),

	/**
	 * Forces uniform scaling. Align the <code>min-x</code> of the element's <code>viewBox</code> with the
	 * smallest X value of the viewport. Align the <code>min-y</code> of the element's <code>viewBox</code> with
	 * the smallest Y value of the viewport.
	 */
	X_MIN_YMIN("xMinYMin"),

	/**
	 * Forces uniform scaling. Align the midpoint X value of the element's <code>viewBox</code> with the midpoint
	 * X value of the viewport. Align the <code>min-y</code> of the element's <code>viewBox</code> with the
	 * smallest Y value of the viewport.
	 */
	X_MID_YMIN("xMidYMin"),

	/**
	 * Forces uniform scaling. Align the <code>min-x</code>+<code>width</code> of the element's
	 * <code>viewBox</code> with the maximum X value of the viewport. Align the <code>min-y</code> of the
	 * element's <code>viewBox</code> with the smallest Y value of the viewport.
	 */
	X_MAX_YMIN("xMaxYMin"),

	/**
	 * Forces uniform scaling. Align the <code>min-x</code> of the element's <code>viewBox</code> with the
	 * smallest X value of the viewport. Align the midpoint Y value of the element's <code>viewBox</code> with
	 * the midpoint Y value of the viewport.
	 */
	X_MIN_YMID("xMinYMid"),

	/**
	 * Forces uniform scaling. Align the <code>min-x</code>+<code>width</code> of the element's
	 * <code>viewBox</code> with the maximum X value of the viewport. Align the midpoint Y value of the element's
	 * <code>viewBox</code> with the midpoint Y value of the viewport.
	 */
	X_MAX_YMID("xMaxYMid"),

	/**
	 * Forces uniform scaling. Align the <code>min-x</code> of the element's <code>viewBox</code> with the
	 * smallest X value of the viewport. Align the <code>min-y</code>+<code>height</code> of the
	 * element's <code>viewBox</code> with the maximum Y value of the viewport.
	 */
	X_MIN_YMAX("xMinYMax"),

	/**
	 * Forces uniform scaling. Align the midpoint X value of the element's <code>viewBox</code> with the midpoint
	 * X value of the viewport. Align the <code>min-y</code>+<code>height</code> of the element's
	 * <code>viewBox</code> with the maximum Y value of the viewport.
	 */
	X_MID_YMAX("xMidYMax"),

	/**
	 * Forces uniform scaling. Align the <code>min-x</code>+<code>width</code> of the element's
	 * <code>viewBox</code> with the maximum X value of the viewport. Align the
	 * <code>min-y</code>+<code>height</code> of the element's <code>viewBox</code> with the maximum Y value of
	 * the viewport.
	 */
	X_MAX_YMAX("xMaxYMax"),

	/**
	 * Does not force uniform scaling. Scale the graphic content of the given element non-uniformly
	 * if necessary such that the element's bounding box exactly matches the viewport rectangle.
	 * Note that if <code>align</code> is none, then the optional <code>meetOrSlice</code> value is
	 * ignored.
	 */
	NONE("none"),

	;

	private final String _protocolName;

	private ImageAlign(String protocolName) {
		_protocolName = protocolName;
	}

	/**
	 * The protocol name of a {@link ImageAlign} constant.
	 *
	 * @see #valueOfProtocol(String)
	 */
	@Override
	public String protocolName() {
		return _protocolName;
	}

	/** Looks up a {@link ImageAlign} constant by it's protocol name. */
	public static ImageAlign valueOfProtocol(String protocolName) {
		if (protocolName == null) { return null; }
		switch (protocolName) {
			case "xMidYMid": return X_MID_YMID;
			case "xMinYMin": return X_MIN_YMIN;
			case "xMidYMin": return X_MID_YMIN;
			case "xMaxYMin": return X_MAX_YMIN;
			case "xMinYMid": return X_MIN_YMID;
			case "xMaxYMid": return X_MAX_YMID;
			case "xMinYMax": return X_MIN_YMAX;
			case "xMidYMax": return X_MID_YMAX;
			case "xMaxYMax": return X_MAX_YMAX;
			case "none": return NONE;
		}
		return X_MID_YMID;
	}

	/** Writes this instance to the given output. */
	public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		out.value(protocolName());
	}

	/** Reads a new instance from the given reader. */
	public static ImageAlign readImageAlign(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		return valueOfProtocol(in.nextString());
	}

	/** Writes this instance to the given binary output. */
	public final void writeTo(de.haumacher.msgbuf.binary.DataWriter out) throws java.io.IOException {
		switch (this) {
			case X_MID_YMID: out.value(1); break;
			case X_MIN_YMIN: out.value(2); break;
			case X_MID_YMIN: out.value(3); break;
			case X_MAX_YMIN: out.value(4); break;
			case X_MIN_YMID: out.value(5); break;
			case X_MAX_YMID: out.value(6); break;
			case X_MIN_YMAX: out.value(7); break;
			case X_MID_YMAX: out.value(8); break;
			case X_MAX_YMAX: out.value(9); break;
			case NONE: out.value(10); break;
			default: out.value(0);
		}
	}

	/** Reads a new instance from the given binary reader. */
	public static ImageAlign readImageAlign(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
		switch (in.nextInt()) {
			case 1: return X_MID_YMID;
			case 2: return X_MIN_YMIN;
			case 3: return X_MID_YMIN;
			case 4: return X_MAX_YMIN;
			case 5: return X_MIN_YMID;
			case 6: return X_MAX_YMID;
			case 7: return X_MIN_YMAX;
			case 8: return X_MID_YMAX;
			case 9: return X_MAX_YMAX;
			case 10: return NONE;
			default: return X_MID_YMID;
		}
	}
}
