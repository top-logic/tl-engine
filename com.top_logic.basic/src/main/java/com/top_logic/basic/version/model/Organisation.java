package com.top_logic.basic.version.model;

/**
 * An organization that releases software.
 */
public class Organisation extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.basic.version.model.Organisation} instance.
	 */
	public static com.top_logic.basic.version.model.Organisation create() {
		return new com.top_logic.basic.version.model.Organisation();
	}

	/** Identifier for the {@link com.top_logic.basic.version.model.Organisation} type in JSON format. */
	public static final String ORGANISATION__TYPE = "Organisation";

	/** @see #getName() */
	public static final String NAME__PROP = "name";

	/** @see #getUrl() */
	public static final String URL__PROP = "url";

	private String _name = "";

	private String _url = null;

	/**
	 * Creates a {@link Organisation} instance.
	 *
	 * @see com.top_logic.basic.version.model.Organisation#create()
	 */
	protected Organisation() {
		super();
	}

	/**
	 * The name of the organization.
	 */
	public final String getName() {
		return _name;
	}

	/**
	 * @see #getName()
	 */
	public com.top_logic.basic.version.model.Organisation setName(String value) {
		internalSetName(value);
		return this;
	}

	/** Internal setter for {@link #getName()} without chain call utility. */
	protected final void internalSetName(String value) {
		_name = value;
	}

	/**
	 * The home page of the organization.
	 */
	public final String getUrl() {
		return _url;
	}

	/**
	 * @see #getUrl()
	 */
	public com.top_logic.basic.version.model.Organisation setUrl(String value) {
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

	@Override
	public String jsonType() {
		return ORGANISATION__TYPE;
	}

	private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(
		java.util.Arrays.asList(
			NAME__PROP, 
			URL__PROP));

	@Override
	public java.util.List<String> properties() {
		return PROPERTIES;
	}

	@Override
	public Object get(String field) {
		switch (field) {
			case NAME__PROP: return getName();
			case URL__PROP: return getUrl();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case NAME__PROP: internalSetName((String) value); break;
			case URL__PROP: internalSetUrl((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.basic.version.model.Organisation readOrganisation(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.basic.version.model.Organisation result = new com.top_logic.basic.version.model.Organisation();
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
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case NAME__PROP: setName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case URL__PROP: setUrl(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
