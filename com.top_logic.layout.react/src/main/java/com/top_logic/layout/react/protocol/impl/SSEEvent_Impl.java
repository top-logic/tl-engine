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

	protected de.haumacher.msgbuf.observer.Listener _listener = de.haumacher.msgbuf.observer.Listener.NONE;

	@Override
	public com.top_logic.layout.react.protocol.SSEEvent registerListener(de.haumacher.msgbuf.observer.Listener l) {
		internalRegisterListener(l);
		return this;
	}

	protected final void internalRegisterListener(de.haumacher.msgbuf.observer.Listener l) {
		_listener = de.haumacher.msgbuf.observer.Listener.register(_listener, l);
	}

	@Override
	public com.top_logic.layout.react.protocol.SSEEvent unregisterListener(de.haumacher.msgbuf.observer.Listener l) {
		internalUnregisterListener(l);
		return this;
	}

	protected final void internalUnregisterListener(de.haumacher.msgbuf.observer.Listener l) {
		_listener = de.haumacher.msgbuf.observer.Listener.unregister(_listener, l);
	}

	@Override
	public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		out.beginArray();
		out.value(jsonType());
		writeContent(out);
		out.endArray();
	}

}
