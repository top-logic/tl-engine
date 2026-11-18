package com.top_logic.ai.mcp.server.discovery;

/**
 * An endpoint of a {@link ServerCard} a client connects to over the network.
 */
public class ServerRemote extends de.haumacher.msgbuf.data.AbstractDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.mcp.server.discovery.ServerRemote} instance.
	 */
	public static com.top_logic.ai.mcp.server.discovery.ServerRemote create() {
		return new com.top_logic.ai.mcp.server.discovery.ServerRemote();
	}

	/** Identifier for the {@link com.top_logic.ai.mcp.server.discovery.ServerRemote} type in JSON format. */
	public static final String SERVER_REMOTE__TYPE = "ServerRemote";

	/** @see #getType() */
	private static final String TYPE__PROP = "type";

	/** @see #getUrl() */
	private static final String URL__PROP = "url";

	/** @see #getHeaders() */
	private static final String HEADERS__PROP = "headers";

	private String _type = "";

	private String _url = "";

	private final java.util.List<com.top_logic.ai.mcp.server.discovery.RemoteHeader> _headers = new java.util.ArrayList<>();

	/**
	 * Creates a {@link ServerRemote} instance.
	 *
	 * @see com.top_logic.ai.mcp.server.discovery.ServerRemote#create()
	 */
	protected ServerRemote() {
		super();
	}

	/**
	 * The transport, e.g. <code>streamable-http</code>.
	 */
	public final String getType() {
		return _type;
	}

	/**
	 * @see #getType()
	 */
	public com.top_logic.ai.mcp.server.discovery.ServerRemote setType(String value) {
		internalSetType(value);
		return this;
	}

	/** Internal setter for {@link #getType()} without chain call utility. */
	protected final void internalSetType(String value) {
		_type = value;
	}

	/**
	 * The absolute URL of the endpoint.
	 */
	public final String getUrl() {
		return _url;
	}

	/**
	 * @see #getUrl()
	 */
	public com.top_logic.ai.mcp.server.discovery.ServerRemote setUrl(String value) {
		internalSetUrl(value);
		return this;
	}

	/** Internal setter for {@link #getUrl()} without chain call utility. */
	protected final void internalSetUrl(String value) {
		_url = value;
	}

	/**
	 * The request headers a client sends to the endpoint.
	 */
	public final java.util.List<com.top_logic.ai.mcp.server.discovery.RemoteHeader> getHeaders() {
		return _headers;
	}

	/**
	 * @see #getHeaders()
	 */
	public com.top_logic.ai.mcp.server.discovery.ServerRemote setHeaders(java.util.List<? extends com.top_logic.ai.mcp.server.discovery.RemoteHeader> value) {
		internalSetHeaders(value);
		return this;
	}

	/** Internal setter for {@link #getHeaders()} without chain call utility. */
	protected final void internalSetHeaders(java.util.List<? extends com.top_logic.ai.mcp.server.discovery.RemoteHeader> value) {
		if (value == null) throw new IllegalArgumentException("Property 'headers' cannot be null.");
		_headers.clear();
		_headers.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getHeaders()} list.
	 */
	public com.top_logic.ai.mcp.server.discovery.ServerRemote addHeader(com.top_logic.ai.mcp.server.discovery.RemoteHeader value) {
		internalAddHeader(value);
		return this;
	}

	/** Implementation of {@link #addHeader(com.top_logic.ai.mcp.server.discovery.RemoteHeader)} without chain call utility. */
	protected final void internalAddHeader(com.top_logic.ai.mcp.server.discovery.RemoteHeader value) {
		_headers.add(value);
	}

	/**
	 * Removes a value from the {@link #getHeaders()} list.
	 */
	public final void removeHeader(com.top_logic.ai.mcp.server.discovery.RemoteHeader value) {
		_headers.remove(value);
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.mcp.server.discovery.ServerRemote readServerRemote(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.mcp.server.discovery.ServerRemote result = new com.top_logic.ai.mcp.server.discovery.ServerRemote();
		result.readContent(in);
		return result;
	}

	@Override
	public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		writeContent(out);
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(TYPE__PROP);
		out.value(getType());
		out.name(URL__PROP);
		out.value(getUrl());
		out.name(HEADERS__PROP);
		out.beginArray();
		for (com.top_logic.ai.mcp.server.discovery.RemoteHeader x : getHeaders()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TYPE__PROP: setType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case URL__PROP: setUrl(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case HEADERS__PROP: {
				java.util.List<com.top_logic.ai.mcp.server.discovery.RemoteHeader> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.ai.mcp.server.discovery.RemoteHeader.readRemoteHeader(in));
				}
				in.endArray();
				setHeaders(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
