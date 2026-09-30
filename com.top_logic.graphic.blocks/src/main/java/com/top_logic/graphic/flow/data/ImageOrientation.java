package com.top_logic.graphic.flow.data;

/**
 * Image transformation.
 */
public enum ImageOrientation implements de.haumacher.msgbuf.data.ProtocolEnum {

	/**
	 * The image is displayed as encoded in the image data.
	 */
	NORMAL("normal"),

	/**
	 * The image is flipped horizontally.
	 */
	FLIP_H("flipH"),

	/**
	 * The image is rotated by 180°.
	 */
	ROTATE_180("rotate180"),

	/**
	 * The image is flipped vertically.
	 */
	FLIP_V("flipV"),

	/**
	 * The image is flipped horizontally, and rotated by 270° counter-clockwise.
	 */
	FLIP_H_ROTATE_270("flipH_rotate270"),

	/**
	 * The image is rotated by 90° counter-clockwise.
	 */
	ROTATE_90("rotate90"),

	/**
	 * The image is flipped horizontally and rotated by 90° counter-clockwise.
	 */
	FLIP_H_ROTATE_90("flipH_rotate90"),

	/**
	 * The image is rotated by 270° counter-clockwise.
	 */
	ROTATE_270("rotate270"),

	;

	private final String _protocolName;

	private ImageOrientation(String protocolName) {
		_protocolName = protocolName;
	}

	/**
	 * The protocol name of a {@link ImageOrientation} constant.
	 *
	 * @see #valueOfProtocol(String)
	 */
	@Override
	public String protocolName() {
		return _protocolName;
	}

	/** Looks up a {@link ImageOrientation} constant by it's protocol name. */
	public static ImageOrientation valueOfProtocol(String protocolName) {
		if (protocolName == null) { return null; }
		switch (protocolName) {
			case "normal": return NORMAL;
			case "flipH": return FLIP_H;
			case "rotate180": return ROTATE_180;
			case "flipV": return FLIP_V;
			case "flipH_rotate270": return FLIP_H_ROTATE_270;
			case "rotate90": return ROTATE_90;
			case "flipH_rotate90": return FLIP_H_ROTATE_90;
			case "rotate270": return ROTATE_270;
		}
		return NORMAL;
	}

	/** Writes this instance to the given output. */
	public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		out.value(protocolName());
	}

	/** Reads a new instance from the given reader. */
	public static ImageOrientation readImageOrientation(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		return valueOfProtocol(in.nextString());
	}
}
