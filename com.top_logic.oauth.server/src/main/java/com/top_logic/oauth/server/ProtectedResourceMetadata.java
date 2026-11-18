package com.top_logic.oauth.server;

/**
 * The metadata of a protected resource (RFC 9728): the document a client reads to learn which
 * authorization server issues the access tokens a resource accepts.
 *
 * <p>
 * Served by the <code>OAuthServerServlet</code> for every resource guarded by the
 * <code>OAuthAccessTokenAuthentication</code>.
 * </p>
 */
public class ProtectedResourceMetadata extends de.haumacher.msgbuf.data.AbstractDataObject {

	/**
	 * Creates a {@link com.top_logic.oauth.server.ProtectedResourceMetadata} instance.
	 */
	public static com.top_logic.oauth.server.ProtectedResourceMetadata create() {
		return new com.top_logic.oauth.server.ProtectedResourceMetadata();
	}

	/** Identifier for the {@link com.top_logic.oauth.server.ProtectedResourceMetadata} type in JSON format. */
	public static final String PROTECTED_RESOURCE_METADATA__TYPE = "ProtectedResourceMetadata";

	/** @see #getResource() */
	private static final String RESOURCE__PROP = "resource";

	/** @see #getAuthorizationServers() */
	private static final String AUTHORIZATION_SERVERS__PROP = "authorization_servers";

	/** @see #getBearerMethodsSupported() */
	private static final String BEARER_METHODS_SUPPORTED__PROP = "bearer_methods_supported";

	private String _resource = "";

	private final java.util.List<String> _authorizationServers = new java.util.ArrayList<>();

	private final java.util.List<String> _bearerMethodsSupported = new java.util.ArrayList<>();

	/**
	 * Creates a {@link ProtectedResourceMetadata} instance.
	 *
	 * @see com.top_logic.oauth.server.ProtectedResourceMetadata#create()
	 */
	protected ProtectedResourceMetadata() {
		super();
	}

	/**
	 * The resource identifier: the absolute URL of the protected endpoint.
	 */
	public final String getResource() {
		return _resource;
	}

	/**
	 * @see #getResource()
	 */
	public com.top_logic.oauth.server.ProtectedResourceMetadata setResource(String value) {
		internalSetResource(value);
		return this;
	}

	/** Internal setter for {@link #getResource()} without chain call utility. */
	protected final void internalSetResource(String value) {
		_resource = value;
	}

	/**
	 * The issuer identifiers of the authorization servers that issue tokens for the resource.
	 */
	public final java.util.List<String> getAuthorizationServers() {
		return _authorizationServers;
	}

	/**
	 * @see #getAuthorizationServers()
	 */
	public com.top_logic.oauth.server.ProtectedResourceMetadata setAuthorizationServers(java.util.List<? extends String> value) {
		internalSetAuthorizationServers(value);
		return this;
	}

	/** Internal setter for {@link #getAuthorizationServers()} without chain call utility. */
	protected final void internalSetAuthorizationServers(java.util.List<? extends String> value) {
		_authorizationServers.clear();
		_authorizationServers.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getAuthorizationServers()} list.
	 */
	public com.top_logic.oauth.server.ProtectedResourceMetadata addAuthorizationServer(String value) {
		internalAddAuthorizationServer(value);
		return this;
	}

	/** Implementation of {@link #addAuthorizationServer(String)} without chain call utility. */
	protected final void internalAddAuthorizationServer(String value) {
		_authorizationServers.add(value);
	}

	/**
	 * Removes a value from the {@link #getAuthorizationServers()} list.
	 */
	public final void removeAuthorizationServer(String value) {
		_authorizationServers.remove(value);
	}

	/**
	 * How a token is presented to the resource, here always <code>header</code>.
	 */
	public final java.util.List<String> getBearerMethodsSupported() {
		return _bearerMethodsSupported;
	}

	/**
	 * @see #getBearerMethodsSupported()
	 */
	public com.top_logic.oauth.server.ProtectedResourceMetadata setBearerMethodsSupported(java.util.List<? extends String> value) {
		internalSetBearerMethodsSupported(value);
		return this;
	}

	/** Internal setter for {@link #getBearerMethodsSupported()} without chain call utility. */
	protected final void internalSetBearerMethodsSupported(java.util.List<? extends String> value) {
		_bearerMethodsSupported.clear();
		_bearerMethodsSupported.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getBearerMethodsSupported()} list.
	 */
	public com.top_logic.oauth.server.ProtectedResourceMetadata addBearerMethodsSupported(String value) {
		internalAddBearerMethodsSupported(value);
		return this;
	}

	/** Implementation of {@link #addBearerMethodsSupported(String)} without chain call utility. */
	protected final void internalAddBearerMethodsSupported(String value) {
		_bearerMethodsSupported.add(value);
	}

	/**
	 * Removes a value from the {@link #getBearerMethodsSupported()} list.
	 */
	public final void removeBearerMethodsSupported(String value) {
		_bearerMethodsSupported.remove(value);
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.oauth.server.ProtectedResourceMetadata readProtectedResourceMetadata(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.oauth.server.ProtectedResourceMetadata result = new com.top_logic.oauth.server.ProtectedResourceMetadata();
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
		out.name(RESOURCE__PROP);
		out.value(getResource());
		out.name(AUTHORIZATION_SERVERS__PROP);
		out.beginArray();
		for (String x : getAuthorizationServers()) {
			out.value(x);
		}
		out.endArray();
		out.name(BEARER_METHODS_SUPPORTED__PROP);
		out.beginArray();
		for (String x : getBearerMethodsSupported()) {
			out.value(x);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case RESOURCE__PROP: setResource(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case AUTHORIZATION_SERVERS__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setAuthorizationServers(newValue);
			}
			break;
			case BEARER_METHODS_SUPPORTED__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setBearerMethodsSupported(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
