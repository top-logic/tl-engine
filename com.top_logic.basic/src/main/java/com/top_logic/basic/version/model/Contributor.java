package com.top_logic.basic.version.model;

/**
 * A contributor to a software module.
 */
public class Contributor extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.basic.version.model.Contributor} instance.
	 */
	public static com.top_logic.basic.version.model.Contributor create() {
		return new com.top_logic.basic.version.model.Contributor();
	}

	/** Identifier for the {@link com.top_logic.basic.version.model.Contributor} type in JSON format. */
	public static final String CONTRIBUTOR__TYPE = "Contributor";

	/** @see #getName() */
	public static final String NAME__PROP = "name";

	/** @see #getEmail() */
	public static final String EMAIL__PROP = "email";

	/** @see #getUrl() */
	public static final String URL__PROP = "url";

	/** @see #getOrganization() */
	public static final String ORGANIZATION__PROP = "organization";

	/** @see #getOrganizationUrl() */
	public static final String ORGANIZATION_URL__PROP = "organizationUrl";

	private String _name = "";

	private String _email = null;

	private String _url = null;

	private String _organization = null;

	private String _organizationUrl = null;

	/**
	 * Creates a {@link Contributor} instance.
	 *
	 * @see com.top_logic.basic.version.model.Contributor#create()
	 */
	protected Contributor() {
		super();
	}

	/**
	 * The name of the contributor.
	 */
	public final String getName() {
		return _name;
	}

	/**
	 * @see #getName()
	 */
	public com.top_logic.basic.version.model.Contributor setName(String value) {
		internalSetName(value);
		return this;
	}

	/** Internal setter for {@link #getName()} without chain call utility. */
	protected final void internalSetName(String value) {
		_name = value;
	}

	/**
	 * The contributor's e-mail address.
	 */
	public final String getEmail() {
		return _email;
	}

	/**
	 * @see #getEmail()
	 */
	public com.top_logic.basic.version.model.Contributor setEmail(String value) {
		internalSetEmail(value);
		return this;
	}

	/** Internal setter for {@link #getEmail()} without chain call utility. */
	protected final void internalSetEmail(String value) {
		_email = value;
	}

	/**
	 * Checks, whether {@link #getEmail()} has a value.
	 */
	public final boolean hasEmail() {
		return _email != null;
	}

	/**
	 * The contributor's home page.
	 */
	public final String getUrl() {
		return _url;
	}

	/**
	 * @see #getUrl()
	 */
	public com.top_logic.basic.version.model.Contributor setUrl(String value) {
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
	 * The name of the organization, the contributor is working for.
	 */
	public final String getOrganization() {
		return _organization;
	}

	/**
	 * @see #getOrganization()
	 */
	public com.top_logic.basic.version.model.Contributor setOrganization(String value) {
		internalSetOrganization(value);
		return this;
	}

	/** Internal setter for {@link #getOrganization()} without chain call utility. */
	protected final void internalSetOrganization(String value) {
		_organization = value;
	}

	/**
	 * Checks, whether {@link #getOrganization()} has a value.
	 */
	public final boolean hasOrganization() {
		return _organization != null;
	}

	/**
	 * The home page of the organization, the contributor is working for.
	 */
	public final String getOrganizationUrl() {
		return _organizationUrl;
	}

	/**
	 * @see #getOrganizationUrl()
	 */
	public com.top_logic.basic.version.model.Contributor setOrganizationUrl(String value) {
		internalSetOrganizationUrl(value);
		return this;
	}

	/** Internal setter for {@link #getOrganizationUrl()} without chain call utility. */
	protected final void internalSetOrganizationUrl(String value) {
		_organizationUrl = value;
	}

	/**
	 * Checks, whether {@link #getOrganizationUrl()} has a value.
	 */
	public final boolean hasOrganizationUrl() {
		return _organizationUrl != null;
	}

	@Override
	public String jsonType() {
		return CONTRIBUTOR__TYPE;
	}

	private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(
		java.util.Arrays.asList(
			NAME__PROP, 
			EMAIL__PROP, 
			URL__PROP, 
			ORGANIZATION__PROP, 
			ORGANIZATION_URL__PROP));

	@Override
	public java.util.List<String> properties() {
		return PROPERTIES;
	}

	@Override
	public Object get(String field) {
		switch (field) {
			case NAME__PROP: return getName();
			case EMAIL__PROP: return getEmail();
			case URL__PROP: return getUrl();
			case ORGANIZATION__PROP: return getOrganization();
			case ORGANIZATION_URL__PROP: return getOrganizationUrl();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case NAME__PROP: internalSetName((String) value); break;
			case EMAIL__PROP: internalSetEmail((String) value); break;
			case URL__PROP: internalSetUrl((String) value); break;
			case ORGANIZATION__PROP: internalSetOrganization((String) value); break;
			case ORGANIZATION_URL__PROP: internalSetOrganizationUrl((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.basic.version.model.Contributor readContributor(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.basic.version.model.Contributor result = new com.top_logic.basic.version.model.Contributor();
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
		if (hasEmail()) {
			out.name(EMAIL__PROP);
			out.value(getEmail());
		}
		if (hasUrl()) {
			out.name(URL__PROP);
			out.value(getUrl());
		}
		if (hasOrganization()) {
			out.name(ORGANIZATION__PROP);
			out.value(getOrganization());
		}
		if (hasOrganizationUrl()) {
			out.name(ORGANIZATION_URL__PROP);
			out.value(getOrganizationUrl());
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case NAME__PROP: setName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case EMAIL__PROP: setEmail(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case URL__PROP: setUrl(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ORGANIZATION__PROP: setOrganization(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ORGANIZATION_URL__PROP: setOrganizationUrl(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
