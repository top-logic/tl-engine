package com.top_logic.layout.react.protocol.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.protocol.SSEEvent}.
 */
public abstract class SSEEvent_Impl extends de.haumacher.msgbuf.data.AbstractDataObject implements com.top_logic.layout.react.protocol.SSEEvent {

	/**
	 * Creates a {@link SSEEvent_Impl} instance.
	 */
	public SSEEvent_Impl() {
		super();
	}

	@Override
	public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		out.beginArray();
		out.value(jsonType());
		writeContent(out);
		out.endArray();
	}

}
