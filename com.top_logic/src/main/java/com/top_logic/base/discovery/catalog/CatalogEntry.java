package com.top_logic.base.discovery.catalog;

/**
 * One artifact listed in an {@link AiCatalog}.
 *
 * <p>
 * Exactly one of {@link #getUrl()} and {@link #getData()} is set.
 * </p>
 */
public class CatalogEntry extends de.haumacher.msgbuf.data.AbstractDataObject {

	/**
	 * Creates a {@link com.top_logic.base.discovery.catalog.CatalogEntry} instance.
	 */
	public static com.top_logic.base.discovery.catalog.CatalogEntry create() {
		return new com.top_logic.base.discovery.catalog.CatalogEntry();
	}

	/** Identifier for the {@link com.top_logic.base.discovery.catalog.CatalogEntry} type in JSON format. */
	public static final String CATALOG_ENTRY__TYPE = "CatalogEntry";

	/** @see #getIdentifier() */
	private static final String IDENTIFIER__PROP = "identifier";

	/** @see #getType() */
	private static final String TYPE__PROP = "type";

	/** @see #getUrl() */
	private static final String URL__PROP = "url";

	/** @see #getData() */
	private static final String DATA__PROP = "data";

	/** @see #getDisplayName() */
	private static final String DISPLAY_NAME__PROP = "displayName";

	/** @see #getDescription() */
	private static final String DESCRIPTION__PROP = "description";

	/** @see #getTags() */
	private static final String TAGS__PROP = "tags";

	/** @see #getVersion() */
	private static final String VERSION__PROP = "version";

	/** @see #getUpdatedAt() */
	private static final String UPDATED_AT__PROP = "updatedAt";

	private String _identifier = "";

	private String _type = "";

	private String _url = null;

	private Object _data = null;

	private String _displayName = null;

	private String _description = null;

	private final java.util.List<String> _tags = new java.util.ArrayList<>();

	private String _version = null;

	private String _updatedAt = null;

	/**
	 * Creates a {@link CatalogEntry} instance.
	 *
	 * @see com.top_logic.base.discovery.catalog.CatalogEntry#create()
	 */
	protected CatalogEntry() {
		super();
	}

	/**
	 * The stable identifier of the artifact, recommended in the form
	 * <code>urn:air:{publisher}:{namespace}:{name}</code>.
	 */
	public final String getIdentifier() {
		return _identifier;
	}

	/**
	 * @see #getIdentifier()
	 */
	public com.top_logic.base.discovery.catalog.CatalogEntry setIdentifier(String value) {
		internalSetIdentifier(value);
		return this;
	}

	/** Internal setter for {@link #getIdentifier()} without chain call utility. */
	protected final void internalSetIdentifier(String value) {
		_identifier = value;
	}

	/**
	 * The media type of the artifact, e.g. <code>application/mcp-server-card+json</code>.
	 */
	public final String getType() {
		return _type;
	}

	/**
	 * @see #getType()
	 */
	public com.top_logic.base.discovery.catalog.CatalogEntry setType(String value) {
		internalSetType(value);
		return this;
	}

	/** Internal setter for {@link #getType()} without chain call utility. */
	protected final void internalSetType(String value) {
		_type = value;
	}

	/**
	 * The absolute URL the artifact is fetched from.
	 */
	public final String getUrl() {
		return _url;
	}

	/**
	 * @see #getUrl()
	 */
	public com.top_logic.base.discovery.catalog.CatalogEntry setUrl(String value) {
		internalSetUrl(value);
		return this;
	}

	/** Internal setter for {@link #getUrl()} without chain call utility. */
	protected final void internalSetUrl(String value) {
		_url = value;
	}

	/**
	 * Checks, whether {@link #getUrl()} has a value.
	 */
	public final boolean hasUrl() {
		return _url != null;
	}

	/**
	 * The artifact itself, inlined into the catalog.
	 */
	public final Object getData() {
		return _data;
	}

	/**
	 * @see #getData()
	 */
	public com.top_logic.base.discovery.catalog.CatalogEntry setData(Object value) {
		internalSetData(value);
		return this;
	}

	/** Internal setter for {@link #getData()} without chain call utility. */
	protected final void internalSetData(Object value) {
		_data = value;
	}

	/**
	 * Checks, whether {@link #getData()} has a value.
	 */
	public final boolean hasData() {
		return _data != null;
	}

	/**
	 * A human-readable name of the artifact.
	 */
	public final String getDisplayName() {
		return _displayName;
	}

	/**
	 * @see #getDisplayName()
	 */
	public com.top_logic.base.discovery.catalog.CatalogEntry setDisplayName(String value) {
		internalSetDisplayName(value);
		return this;
	}

	/** Internal setter for {@link #getDisplayName()} without chain call utility. */
	protected final void internalSetDisplayName(String value) {
		_displayName = value;
	}

	/**
	 * Checks, whether {@link #getDisplayName()} has a value.
	 */
	public final boolean hasDisplayName() {
		return _displayName != null;
	}

	/**
	 * A short description of the artifact.
	 */
	public final String getDescription() {
		return _description;
	}

	/**
	 * @see #getDescription()
	 */
	public com.top_logic.base.discovery.catalog.CatalogEntry setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	/** Internal setter for {@link #getDescription()} without chain call utility. */
	protected final void internalSetDescription(String value) {
		_description = value;
	}

	/**
	 * Checks, whether {@link #getDescription()} has a value.
	 */
	public final boolean hasDescription() {
		return _description != null;
	}

	/**
	 * Keywords classifying the artifact.
	 */
	public final java.util.List<String> getTags() {
		return _tags;
	}

	/**
	 * @see #getTags()
	 */
	public com.top_logic.base.discovery.catalog.CatalogEntry setTags(java.util.List<? extends String> value) {
		internalSetTags(value);
		return this;
	}

	/** Internal setter for {@link #getTags()} without chain call utility. */
	protected final void internalSetTags(java.util.List<? extends String> value) {
		_tags.clear();
		_tags.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getTags()} list.
	 */
	public com.top_logic.base.discovery.catalog.CatalogEntry addTag(String value) {
		internalAddTag(value);
		return this;
	}

	/** Implementation of {@link #addTag(String)} without chain call utility. */
	protected final void internalAddTag(String value) {
		_tags.add(value);
	}

	/**
	 * Removes a value from the {@link #getTags()} list.
	 */
	public final void removeTag(String value) {
		_tags.remove(value);
	}

	/**
	 * The version of the artifact.
	 */
	public final String getVersion() {
		return _version;
	}

	/**
	 * @see #getVersion()
	 */
	public com.top_logic.base.discovery.catalog.CatalogEntry setVersion(String value) {
		internalSetVersion(value);
		return this;
	}

	/** Internal setter for {@link #getVersion()} without chain call utility. */
	protected final void internalSetVersion(String value) {
		_version = value;
	}

	/**
	 * Checks, whether {@link #getVersion()} has a value.
	 */
	public final boolean hasVersion() {
		return _version != null;
	}

	/**
	 * The time of the last change of the artifact as ISO 8601 date-time.
	 */
	public final String getUpdatedAt() {
		return _updatedAt;
	}

	/**
	 * @see #getUpdatedAt()
	 */
	public com.top_logic.base.discovery.catalog.CatalogEntry setUpdatedAt(String value) {
		internalSetUpdatedAt(value);
		return this;
	}

	/** Internal setter for {@link #getUpdatedAt()} without chain call utility. */
	protected final void internalSetUpdatedAt(String value) {
		_updatedAt = value;
	}

	/**
	 * Checks, whether {@link #getUpdatedAt()} has a value.
	 */
	public final boolean hasUpdatedAt() {
		return _updatedAt != null;
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.base.discovery.catalog.CatalogEntry readCatalogEntry(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.base.discovery.catalog.CatalogEntry result = new com.top_logic.base.discovery.catalog.CatalogEntry();
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
		out.name(IDENTIFIER__PROP);
		out.value(getIdentifier());
		out.name(TYPE__PROP);
		out.value(getType());
		if (hasUrl()) {
			out.name(URL__PROP);
			out.value(getUrl());
		}
		if (hasData()) {
			out.name(DATA__PROP);
			de.haumacher.msgbuf.json.JsonUtil.writeJsonValue(out, getData());
		}
		if (hasDisplayName()) {
			out.name(DISPLAY_NAME__PROP);
			out.value(getDisplayName());
		}
		if (hasDescription()) {
			out.name(DESCRIPTION__PROP);
			out.value(getDescription());
		}
		out.name(TAGS__PROP);
		out.beginArray();
		for (String x : getTags()) {
			out.value(x);
		}
		out.endArray();
		if (hasVersion()) {
			out.name(VERSION__PROP);
			out.value(getVersion());
		}
		if (hasUpdatedAt()) {
			out.name(UPDATED_AT__PROP);
			out.value(getUpdatedAt());
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case IDENTIFIER__PROP: setIdentifier(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TYPE__PROP: setType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case URL__PROP: setUrl(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DATA__PROP: setData(de.haumacher.msgbuf.json.JsonUtil.nextJsonValue(in)); break;
			case DISPLAY_NAME__PROP: setDisplayName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DESCRIPTION__PROP: setDescription(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TAGS__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setTags(newValue);
			}
			break;
			case VERSION__PROP: setVersion(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case UPDATED_AT__PROP: setUpdatedAt(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
