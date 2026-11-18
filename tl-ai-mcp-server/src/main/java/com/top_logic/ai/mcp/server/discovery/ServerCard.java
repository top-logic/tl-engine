package com.top_logic.ai.mcp.server.discovery;

/**
 * An MCP server card (SEP-2127): the description of an MCP server a client reads before it
 * connects, served at <code>/mcp/server-card</code> with the media type
 * <code>application/mcp-server-card+json</code>.
 *
 * <p>
 * A card never contains a credential, nor the tools or resources of the server: a client learns
 * those from the server after it connected.
 * </p>
 */
public class ServerCard extends de.haumacher.msgbuf.data.AbstractDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.mcp.server.discovery.ServerCard} instance.
	 */
	public static com.top_logic.ai.mcp.server.discovery.ServerCard create() {
		return new com.top_logic.ai.mcp.server.discovery.ServerCard();
	}

	/** Identifier for the {@link com.top_logic.ai.mcp.server.discovery.ServerCard} type in JSON format. */
	public static final String SERVER_CARD__TYPE = "ServerCard";

	/** @see #getSchema() */
	private static final String SCHEMA__PROP = "$schema";

	/** @see #getName() */
	private static final String NAME__PROP = "name";

	/** @see #getVersion() */
	private static final String VERSION__PROP = "version";

	/** @see #getDescription() */
	private static final String DESCRIPTION__PROP = "description";

	/** @see #getTitle() */
	private static final String TITLE__PROP = "title";

	/** @see #getWebsiteUrl() */
	private static final String WEBSITE_URL__PROP = "websiteUrl";

	/** @see #getRemotes() */
	private static final String REMOTES__PROP = "remotes";

	private String _schema = "";

	private String _name = "";

	private String _version = "";

	private String _description = "";

	private String _title = null;

	private String _websiteUrl = null;

	private final java.util.List<com.top_logic.ai.mcp.server.discovery.ServerRemote> _remotes = new java.util.ArrayList<>();

	/**
	 * Creates a {@link ServerCard} instance.
	 *
	 * @see com.top_logic.ai.mcp.server.discovery.ServerCard#create()
	 */
	protected ServerCard() {
		super();
	}

	/**
	 * The URL of the JSON schema the card follows.
	 */
	public final String getSchema() {
		return _schema;
	}

	/**
	 * @see #getSchema()
	 */
	public com.top_logic.ai.mcp.server.discovery.ServerCard setSchema(String value) {
		internalSetSchema(value);
		return this;
	}

	/** Internal setter for {@link #getSchema()} without chain call utility. */
	protected final void internalSetSchema(String value) {
		_schema = value;
	}

	/**
	 * The name of the server in reverse-DNS form, e.g. <code>com.example/orders</code>, matching
	 * <code>^[a-zA-Z0-9.-]+/[a-zA-Z0-9._-]+$</code>.
	 */
	public final String getName() {
		return _name;
	}

	/**
	 * @see #getName()
	 */
	public com.top_logic.ai.mcp.server.discovery.ServerCard setName(String value) {
		internalSetName(value);
		return this;
	}

	/** Internal setter for {@link #getName()} without chain call utility. */
	protected final void internalSetName(String value) {
		_name = value;
	}

	/**
	 * The version of the server.
	 */
	public final String getVersion() {
		return _version;
	}

	/**
	 * @see #getVersion()
	 */
	public com.top_logic.ai.mcp.server.discovery.ServerCard setVersion(String value) {
		internalSetVersion(value);
		return this;
	}

	/** Internal setter for {@link #getVersion()} without chain call utility. */
	protected final void internalSetVersion(String value) {
		_version = value;
	}

	/**
	 * What the server offers, at most 100 characters.
	 */
	public final String getDescription() {
		return _description;
	}

	/**
	 * @see #getDescription()
	 */
	public com.top_logic.ai.mcp.server.discovery.ServerCard setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	/** Internal setter for {@link #getDescription()} without chain call utility. */
	protected final void internalSetDescription(String value) {
		_description = value;
	}

	/**
	 * The human-readable name of the server, at most 100 characters.
	 */
	public final String getTitle() {
		return _title;
	}

	/**
	 * @see #getTitle()
	 */
	public com.top_logic.ai.mcp.server.discovery.ServerCard setTitle(String value) {
		internalSetTitle(value);
		return this;
	}

	/** Internal setter for {@link #getTitle()} without chain call utility. */
	protected final void internalSetTitle(String value) {
		_title = value;
	}

	/**
	 * Checks, whether {@link #getTitle()} has a value.
	 */
	public final boolean hasTitle() {
		return _title != null;
	}

	/**
	 * The URL of the website of the server, here the application.
	 */
	public final String getWebsiteUrl() {
		return _websiteUrl;
	}

	/**
	 * @see #getWebsiteUrl()
	 */
	public com.top_logic.ai.mcp.server.discovery.ServerCard setWebsiteUrl(String value) {
		internalSetWebsiteUrl(value);
		return this;
	}

	/** Internal setter for {@link #getWebsiteUrl()} without chain call utility. */
	protected final void internalSetWebsiteUrl(String value) {
		_websiteUrl = value;
	}

	/**
	 * Checks, whether {@link #getWebsiteUrl()} has a value.
	 */
	public final boolean hasWebsiteUrl() {
		return _websiteUrl != null;
	}

	/**
	 * The endpoints a client connects to.
	 */
	public final java.util.List<com.top_logic.ai.mcp.server.discovery.ServerRemote> getRemotes() {
		return _remotes;
	}

	/**
	 * @see #getRemotes()
	 */
	public com.top_logic.ai.mcp.server.discovery.ServerCard setRemotes(java.util.List<? extends com.top_logic.ai.mcp.server.discovery.ServerRemote> value) {
		internalSetRemotes(value);
		return this;
	}

	/** Internal setter for {@link #getRemotes()} without chain call utility. */
	protected final void internalSetRemotes(java.util.List<? extends com.top_logic.ai.mcp.server.discovery.ServerRemote> value) {
		if (value == null) throw new IllegalArgumentException("Property 'remotes' cannot be null.");
		_remotes.clear();
		_remotes.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getRemotes()} list.
	 */
	public com.top_logic.ai.mcp.server.discovery.ServerCard addRemote(com.top_logic.ai.mcp.server.discovery.ServerRemote value) {
		internalAddRemote(value);
		return this;
	}

	/** Implementation of {@link #addRemote(com.top_logic.ai.mcp.server.discovery.ServerRemote)} without chain call utility. */
	protected final void internalAddRemote(com.top_logic.ai.mcp.server.discovery.ServerRemote value) {
		_remotes.add(value);
	}

	/**
	 * Removes a value from the {@link #getRemotes()} list.
	 */
	public final void removeRemote(com.top_logic.ai.mcp.server.discovery.ServerRemote value) {
		_remotes.remove(value);
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.mcp.server.discovery.ServerCard readServerCard(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.mcp.server.discovery.ServerCard result = new com.top_logic.ai.mcp.server.discovery.ServerCard();
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
		out.name(SCHEMA__PROP);
		out.value(getSchema());
		out.name(NAME__PROP);
		out.value(getName());
		out.name(VERSION__PROP);
		out.value(getVersion());
		out.name(DESCRIPTION__PROP);
		out.value(getDescription());
		if (hasTitle()) {
			out.name(TITLE__PROP);
			out.value(getTitle());
		}
		if (hasWebsiteUrl()) {
			out.name(WEBSITE_URL__PROP);
			out.value(getWebsiteUrl());
		}
		out.name(REMOTES__PROP);
		out.beginArray();
		for (com.top_logic.ai.mcp.server.discovery.ServerRemote x : getRemotes()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case SCHEMA__PROP: setSchema(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case NAME__PROP: setName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case VERSION__PROP: setVersion(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DESCRIPTION__PROP: setDescription(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TITLE__PROP: setTitle(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case WEBSITE_URL__PROP: setWebsiteUrl(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case REMOTES__PROP: {
				java.util.List<com.top_logic.ai.mcp.server.discovery.ServerRemote> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.ai.mcp.server.discovery.ServerRemote.readServerRemote(in));
				}
				in.endArray();
				setRemotes(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
