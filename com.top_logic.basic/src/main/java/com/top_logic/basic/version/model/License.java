package com.top_logic.basic.version.model;

/**
 * Description of a license associated to a software product.
 */
public class License extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.basic.version.model.License} instance.
	 */
	public static com.top_logic.basic.version.model.License create() {
		return new com.top_logic.basic.version.model.License();
	}

	/** Identifier for the {@link com.top_logic.basic.version.model.License} type in JSON format. */
	public static final String LICENSE__TYPE = "License";

	/** @see #getName() */
	public static final String NAME__PROP = "name";

	/** @see #getUrl() */
	public static final String URL__PROP = "url";

	/** @see #getComments() */
	public static final String COMMENTS__PROP = "comments";

	private String _name = "";

	private String _url = null;

	private String _comments = null;

	/**
	 * Creates a {@link License} instance.
	 *
	 * @see com.top_logic.basic.version.model.License#create()
	 */
	protected License() {
		super();
	}

	/**
	 * Official name of the license, preferably an SPDX identifier.
	 */
	public final String getName() {
		return _name;
	}

	/**
	 * @see #getName()
	 */
	public com.top_logic.basic.version.model.License setName(String value) {
		internalSetName(value);
		return this;
	}

	/** Internal setter for {@link #getName()} without chain call utility. */
	protected final void internalSetName(String value) {
		_name = value;
	}

	/**
	 * The URL where the license text can be downloade.
	 */
	public final String getUrl() {
		return _url;
	}

	/**
	 * @see #getUrl()
	 */
	public com.top_logic.basic.version.model.License setUrl(String value) {
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
	 * Additional comments to the license.
	 */
	public final String getComments() {
		return _comments;
	}

	/**
	 * @see #getComments()
	 */
	public com.top_logic.basic.version.model.License setComments(String value) {
		internalSetComments(value);
		return this;
	}

	/** Internal setter for {@link #getComments()} without chain call utility. */
	protected final void internalSetComments(String value) {
		_comments = value;
	}

	/**
	 * Checks, whether {@link #getComments()} has a value.
	 */
	public final boolean hasComments() {
		return _comments != null;
	}

	@Override
	public String jsonType() {
		return LICENSE__TYPE;
	}

	private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(
		java.util.Arrays.asList(
			NAME__PROP, 
			URL__PROP, 
			COMMENTS__PROP));

	@Override
	public java.util.List<String> properties() {
		return PROPERTIES;
	}

	@Override
	public Object get(String field) {
		switch (field) {
			case NAME__PROP: return getName();
			case URL__PROP: return getUrl();
			case COMMENTS__PROP: return getComments();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case NAME__PROP: internalSetName((String) value); break;
			case URL__PROP: internalSetUrl((String) value); break;
			case COMMENTS__PROP: internalSetComments((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.basic.version.model.License readLicense(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.basic.version.model.License result = new com.top_logic.basic.version.model.License();
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
		out.name(NAME__PROP);
		out.value(getName());
		if (hasUrl()) {
			out.name(URL__PROP);
			out.value(getUrl());
		}
		if (hasComments()) {
			out.name(COMMENTS__PROP);
			out.value(getComments());
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case NAME__PROP: setName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case URL__PROP: setUrl(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case COMMENTS__PROP: setComments(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
