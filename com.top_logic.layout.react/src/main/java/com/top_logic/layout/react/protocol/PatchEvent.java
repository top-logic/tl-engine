package com.top_logic.layout.react.protocol;

/**
 * Incremental patch to a React control's state.
 */
public interface PatchEvent extends SSEEvent {

	/**
	 * Creates a {@link com.top_logic.layout.react.protocol.PatchEvent} instance.
	 */
	static com.top_logic.layout.react.protocol.PatchEvent create() {
		return new com.top_logic.layout.react.protocol.impl.PatchEvent_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.protocol.PatchEvent} type in JSON format. */
	String PATCH_EVENT__TYPE = "PatchEvent";

	/** @see #getControlId() */
	String CONTROL_ID__PROP = "controlId";

	/** @see #getPatch() */
	String PATCH__PROP = "patch";

	/**
	 * The control ID this patch applies to.
	 */
	String getControlId();

	/**
	 * @see #getControlId()
	 */
	com.top_logic.layout.react.protocol.PatchEvent setControlId(String value);

	/**
	 * A JSON Merge Patch (RFC 7396) serialized as a string.
	 */
	String getPatch();

	/**
	 * @see #getPatch()
	 */
	com.top_logic.layout.react.protocol.PatchEvent setPatch(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.protocol.PatchEvent readPatchEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.protocol.impl.PatchEvent_Impl result = new com.top_logic.layout.react.protocol.impl.PatchEvent_Impl();
		result.readContent(in);
		return result;
	}

}
