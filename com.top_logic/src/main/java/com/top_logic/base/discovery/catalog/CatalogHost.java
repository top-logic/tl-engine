package com.top_logic.base.discovery.catalog;

/**
 * The publisher of an {@link AiCatalog}.
 */
public class CatalogHost extends de.haumacher.msgbuf.data.AbstractDataObject {

	/**
	 * Creates a {@link com.top_logic.base.discovery.catalog.CatalogHost} instance.
	 */
	public static com.top_logic.base.discovery.catalog.CatalogHost create() {
		return new com.top_logic.base.discovery.catalog.CatalogHost();
	}

	/** Identifier for the {@link com.top_logic.base.discovery.catalog.CatalogHost} type in JSON format. */
	public static final String CATALOG_HOST__TYPE = "CatalogHost";

	/** @see #getDisplayName() */
	private static final String DISPLAY_NAME__PROP = "displayName";

	/** @see #getIdentifier() */
	private static final String IDENTIFIER__PROP = "identifier";

	/** @see #getDocumentationUrl() */
	private static final String DOCUMENTATION_URL__PROP = "documentationUrl";

	/** @see #getLogoUrl() */
	private static final String LOGO_URL__PROP = "logoUrl";

	private String _displayName = "";

	private String _identifier = null;

	private String _documentationUrl = null;

	private String _logoUrl = null;

	/**
	 * Creates a {@link CatalogHost} instance.
	 *
	 * @see com.top_logic.base.discovery.catalog.CatalogHost#create()
	 */
	protected CatalogHost() {
		super();
	}

	/**
	 * The human-readable name of the publisher.
	 */
	public final String getDisplayName() {
		return _displayName;
	}

	/**
	 * @see #getDisplayName()
	 */
	public com.top_logic.base.discovery.catalog.CatalogHost setDisplayName(String value) {
		internalSetDisplayName(value);
		return this;
	}

	/** Internal setter for {@link #getDisplayName()} without chain call utility. */
	protected final void internalSetDisplayName(String value) {
		_displayName = value;
	}

	/**
	 * A stable identifier of the publisher.
	 */
	public final String getIdentifier() {
		return _identifier;
	}

	/**
	 * @see #getIdentifier()
	 */
	public com.top_logic.base.discovery.catalog.CatalogHost setIdentifier(String value) {
		internalSetIdentifier(value);
		return this;
	}

	/** Internal setter for {@link #getIdentifier()} without chain call utility. */
	protected final void internalSetIdentifier(String value) {
		_identifier = value;
	}

	/**
	 * Checks, whether {@link #getIdentifier()} has a value.
	 */
	public final boolean hasIdentifier() {
		return _identifier != null;
	}

	/**
	 * A URL of human- or model-readable documentation of the publisher.
	 */
	public final String getDocumentationUrl() {
		return _documentationUrl;
	}

	/**
	 * @see #getDocumentationUrl()
	 */
	public com.top_logic.base.discovery.catalog.CatalogHost setDocumentationUrl(String value) {
		internalSetDocumentationUrl(value);
		return this;
	}

	/** Internal setter for {@link #getDocumentationUrl()} without chain call utility. */
	protected final void internalSetDocumentationUrl(String value) {
		_documentationUrl = value;
	}

	/**
	 * Checks, whether {@link #getDocumentationUrl()} has a value.
	 */
	public final boolean hasDocumentationUrl() {
		return _documentationUrl != null;
	}

	/**
	 * A URL of the publisher's logo.
	 */
	public final String getLogoUrl() {
		return _logoUrl;
	}

	/**
	 * @see #getLogoUrl()
	 */
	public com.top_logic.base.discovery.catalog.CatalogHost setLogoUrl(String value) {
		internalSetLogoUrl(value);
		return this;
	}

	/** Internal setter for {@link #getLogoUrl()} without chain call utility. */
	protected final void internalSetLogoUrl(String value) {
		_logoUrl = value;
	}

	/**
	 * Checks, whether {@link #getLogoUrl()} has a value.
	 */
	public final boolean hasLogoUrl() {
		return _logoUrl != null;
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.base.discovery.catalog.CatalogHost readCatalogHost(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.base.discovery.catalog.CatalogHost result = new com.top_logic.base.discovery.catalog.CatalogHost();
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
		out.name(DISPLAY_NAME__PROP);
		out.value(getDisplayName());
		if (hasIdentifier()) {
			out.name(IDENTIFIER__PROP);
			out.value(getIdentifier());
		}
		if (hasDocumentationUrl()) {
			out.name(DOCUMENTATION_URL__PROP);
			out.value(getDocumentationUrl());
		}
		if (hasLogoUrl()) {
			out.name(LOGO_URL__PROP);
			out.value(getLogoUrl());
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case DISPLAY_NAME__PROP: setDisplayName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case IDENTIFIER__PROP: setIdentifier(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DOCUMENTATION_URL__PROP: setDocumentationUrl(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case LOGO_URL__PROP: setLogoUrl(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
