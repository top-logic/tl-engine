package com.top_logic.base.discovery.catalog;

/**
 * An AI Catalog: the machine-readable list of the agent-facing artifacts (MCP server cards, A2A
 * agent cards, ...) an application publishes, served at <code>/.well-known/ai-catalog.json</code>
 * with the media type <code>application/ai-catalog+json</code>.
 */
public class AiCatalog extends de.haumacher.msgbuf.data.AbstractDataObject {

	/**
	 * Creates a {@link com.top_logic.base.discovery.catalog.AiCatalog} instance.
	 */
	public static com.top_logic.base.discovery.catalog.AiCatalog create() {
		return new com.top_logic.base.discovery.catalog.AiCatalog();
	}

	/** Identifier for the {@link com.top_logic.base.discovery.catalog.AiCatalog} type in JSON format. */
	public static final String AI_CATALOG__TYPE = "AiCatalog";

	/** @see #getSpecVersion() */
	private static final String SPEC_VERSION__PROP = "specVersion";

	/** @see #getHost() */
	private static final String HOST__PROP = "host";

	/** @see #getEntries() */
	private static final String ENTRIES__PROP = "entries";

	private String _specVersion = "";

	private com.top_logic.base.discovery.catalog.CatalogHost _host = null;

	private final java.util.List<com.top_logic.base.discovery.catalog.CatalogEntry> _entries = new java.util.ArrayList<>();

	/**
	 * Creates a {@link AiCatalog} instance.
	 *
	 * @see com.top_logic.base.discovery.catalog.AiCatalog#create()
	 */
	protected AiCatalog() {
		super();
	}

	/**
	 * The version of the AI Catalog specification this document follows.
	 */
	public final String getSpecVersion() {
		return _specVersion;
	}

	/**
	 * @see #getSpecVersion()
	 */
	public com.top_logic.base.discovery.catalog.AiCatalog setSpecVersion(String value) {
		internalSetSpecVersion(value);
		return this;
	}

	/** Internal setter for {@link #getSpecVersion()} without chain call utility. */
	protected final void internalSetSpecVersion(String value) {
		_specVersion = value;
	}

	/**
	 * The publisher of the catalog.
	 */
	public final com.top_logic.base.discovery.catalog.CatalogHost getHost() {
		return _host;
	}

	/**
	 * @see #getHost()
	 */
	public com.top_logic.base.discovery.catalog.AiCatalog setHost(com.top_logic.base.discovery.catalog.CatalogHost value) {
		internalSetHost(value);
		return this;
	}

	/** Internal setter for {@link #getHost()} without chain call utility. */
	protected final void internalSetHost(com.top_logic.base.discovery.catalog.CatalogHost value) {
		_host = value;
	}

	/**
	 * Checks, whether {@link #getHost()} has a value.
	 */
	public final boolean hasHost() {
		return _host != null;
	}

	/**
	 * The published artifacts.
	 */
	public final java.util.List<com.top_logic.base.discovery.catalog.CatalogEntry> getEntries() {
		return _entries;
	}

	/**
	 * @see #getEntries()
	 */
	public com.top_logic.base.discovery.catalog.AiCatalog setEntries(java.util.List<? extends com.top_logic.base.discovery.catalog.CatalogEntry> value) {
		internalSetEntries(value);
		return this;
	}

	/** Internal setter for {@link #getEntries()} without chain call utility. */
	protected final void internalSetEntries(java.util.List<? extends com.top_logic.base.discovery.catalog.CatalogEntry> value) {
		if (value == null) throw new IllegalArgumentException("Property 'entries' cannot be null.");
		_entries.clear();
		_entries.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getEntries()} list.
	 */
	public com.top_logic.base.discovery.catalog.AiCatalog addEntry(com.top_logic.base.discovery.catalog.CatalogEntry value) {
		internalAddEntry(value);
		return this;
	}

	/** Implementation of {@link #addEntry(com.top_logic.base.discovery.catalog.CatalogEntry)} without chain call utility. */
	protected final void internalAddEntry(com.top_logic.base.discovery.catalog.CatalogEntry value) {
		_entries.add(value);
	}

	/**
	 * Removes a value from the {@link #getEntries()} list.
	 */
	public final void removeEntry(com.top_logic.base.discovery.catalog.CatalogEntry value) {
		_entries.remove(value);
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.base.discovery.catalog.AiCatalog readAiCatalog(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.base.discovery.catalog.AiCatalog result = new com.top_logic.base.discovery.catalog.AiCatalog();
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
		out.name(SPEC_VERSION__PROP);
		out.value(getSpecVersion());
		if (hasHost()) {
			out.name(HOST__PROP);
			getHost().writeTo(out);
		}
		out.name(ENTRIES__PROP);
		out.beginArray();
		for (com.top_logic.base.discovery.catalog.CatalogEntry x : getEntries()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case SPEC_VERSION__PROP: setSpecVersion(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case HOST__PROP: setHost(com.top_logic.base.discovery.catalog.CatalogHost.readCatalogHost(in)); break;
			case ENTRIES__PROP: {
				java.util.List<com.top_logic.base.discovery.catalog.CatalogEntry> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.base.discovery.catalog.CatalogEntry.readCatalogEntry(in));
				}
				in.endArray();
				setEntries(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
