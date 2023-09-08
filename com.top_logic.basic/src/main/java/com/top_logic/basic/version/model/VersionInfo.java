package com.top_logic.basic.version.model;

/**
 * Version and dependency information of an application.
 */
public class VersionInfo extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.basic.version.model.VersionInfo} instance.
	 */
	public static com.top_logic.basic.version.model.VersionInfo create() {
		return new com.top_logic.basic.version.model.VersionInfo();
	}

	/** Identifier for the {@link com.top_logic.basic.version.model.VersionInfo} type in JSON format. */
	public static final String VERSION_INFO__TYPE = "VersionInfo";

	/** @see #getGroupId() */
	public static final String GROUP_ID__PROP = "groupId";

	/** @see #getArtifactId() */
	public static final String ARTIFACT_ID__PROP = "artifactId";

	/** @see #getVersion() */
	public static final String VERSION__PROP = "version";

	/** @see #getName() */
	public static final String NAME__PROP = "name";

	/** @see #getDescription() */
	public static final String DESCRIPTION__PROP = "description";

	/** @see #getUrl() */
	public static final String URL__PROP = "url";

	/** @see #getInceptionYear() */
	public static final String INCEPTION_YEAR__PROP = "inceptionYear";

	/** @see #getBuildQualifier() */
	public static final String BUILD_QUALIFIER__PROP = "buildQualifier";

	/** @see #getOrganization() */
	public static final String ORGANIZATION__PROP = "organization";

	/** @see #getContributors() */
	public static final String CONTRIBUTORS__PROP = "contributors";

	/** @see #getDevelopers() */
	public static final String DEVELOPERS__PROP = "developers";

	/** @see #getLicenses() */
	public static final String LICENSES__PROP = "licenses";

	/** @see #getDependencies() */
	public static final String DEPENDENCIES__PROP = "dependencies";

	private String _groupId = "";

	private String _artifactId = "";

	private String _version = "";

	private String _name = null;

	private String _description = null;

	private String _url = null;

	private String _inceptionYear = null;

	private String _buildQualifier = null;

	private com.top_logic.basic.version.model.Organisation _organization = null;

	private final java.util.List<com.top_logic.basic.version.model.Contributor> _contributors = new java.util.ArrayList<>();

	private final java.util.List<com.top_logic.basic.version.model.Contributor> _developers = new java.util.ArrayList<>();

	private final java.util.List<com.top_logic.basic.version.model.License> _licenses = new java.util.ArrayList<>();

	private final java.util.List<com.top_logic.basic.version.model.VersionInfo> _dependencies = new java.util.ArrayList<>();

	/**
	 * Creates a {@link VersionInfo} instance.
	 *
	 * @see com.top_logic.basic.version.model.VersionInfo#create()
	 */
	protected VersionInfo() {
		super();
	}

	/**
	 * The Maven group identifier of the application.
	 */
	public final String getGroupId() {
		return _groupId;
	}

	/**
	 * @see #getGroupId()
	 */
	public com.top_logic.basic.version.model.VersionInfo setGroupId(String value) {
		internalSetGroupId(value);
		return this;
	}

	/** Internal setter for {@link #getGroupId()} without chain call utility. */
	protected final void internalSetGroupId(String value) {
		_groupId = value;
	}

	/**
	 * The Maven artifact identifier of the application.
	 */
	public final String getArtifactId() {
		return _artifactId;
	}

	/**
	 * @see #getArtifactId()
	 */
	public com.top_logic.basic.version.model.VersionInfo setArtifactId(String value) {
		internalSetArtifactId(value);
		return this;
	}

	/** Internal setter for {@link #getArtifactId()} without chain call utility. */
	protected final void internalSetArtifactId(String value) {
		_artifactId = value;
	}

	/**
	 * The application version.
	 */
	public final String getVersion() {
		return _version;
	}

	/**
	 * @see #getVersion()
	 */
	public com.top_logic.basic.version.model.VersionInfo setVersion(String value) {
		internalSetVersion(value);
		return this;
	}

	/** Internal setter for {@link #getVersion()} without chain call utility. */
	protected final void internalSetVersion(String value) {
		_version = value;
	}

	/**
	 * The application name as given in the Maven project model.
	 */
	public final String getName() {
		return _name;
	}

	/**
	 * @see #getName()
	 */
	public com.top_logic.basic.version.model.VersionInfo setName(String value) {
		internalSetName(value);
		return this;
	}

	/** Internal setter for {@link #getName()} without chain call utility. */
	protected final void internalSetName(String value) {
		_name = value;
	}

	/**
	 * Checks, whether {@link #getName()} has a value.
	 */
	public final boolean hasName() {
		return _name != null;
	}

	/**
	 * Description of the application.
	 */
	public final String getDescription() {
		return _description;
	}

	/**
	 * @see #getDescription()
	 */
	public com.top_logic.basic.version.model.VersionInfo setDescription(String value) {
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
	 * URL of the application home page.
	 */
	public final String getUrl() {
		return _url;
	}

	/**
	 * @see #getUrl()
	 */
	public com.top_logic.basic.version.model.VersionInfo setUrl(String value) {
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
	 * Date of the first releas of the software.
	 */
	public final String getInceptionYear() {
		return _inceptionYear;
	}

	/**
	 * @see #getInceptionYear()
	 */
	public com.top_logic.basic.version.model.VersionInfo setInceptionYear(String value) {
		internalSetInceptionYear(value);
		return this;
	}

	/** Internal setter for {@link #getInceptionYear()} without chain call utility. */
	protected final void internalSetInceptionYear(String value) {
		_inceptionYear = value;
	}

	/**
	 * Checks, whether {@link #getInceptionYear()} has a value.
	 */
	public final boolean hasInceptionYear() {
		return _inceptionYear != null;
	}

	/**
	 * Additional qualifier to identify the build of the software.
	 */
	public final String getBuildQualifier() {
		return _buildQualifier;
	}

	/**
	 * @see #getBuildQualifier()
	 */
	public com.top_logic.basic.version.model.VersionInfo setBuildQualifier(String value) {
		internalSetBuildQualifier(value);
		return this;
	}

	/** Internal setter for {@link #getBuildQualifier()} without chain call utility. */
	protected final void internalSetBuildQualifier(String value) {
		_buildQualifier = value;
	}

	/**
	 * Checks, whether {@link #getBuildQualifier()} has a value.
	 */
	public final boolean hasBuildQualifier() {
		return _buildQualifier != null;
	}

	/**
	 * The organization that released the the software.
	 */
	public final com.top_logic.basic.version.model.Organisation getOrganization() {
		return _organization;
	}

	/**
	 * @see #getOrganization()
	 */
	public com.top_logic.basic.version.model.VersionInfo setOrganization(com.top_logic.basic.version.model.Organisation value) {
		internalSetOrganization(value);
		return this;
	}

	/** Internal setter for {@link #getOrganization()} without chain call utility. */
	protected final void internalSetOrganization(com.top_logic.basic.version.model.Organisation value) {
		_organization = value;
	}

	/**
	 * Checks, whether {@link #getOrganization()} has a value.
	 */
	public final boolean hasOrganization() {
		return _organization != null;
	}

	/**
	 * The contributors to the software that are not {@link #getDevelopers()}.
	 */
	public final java.util.List<com.top_logic.basic.version.model.Contributor> getContributors() {
		return _contributors;
	}

	/**
	 * @see #getContributors()
	 */
	public com.top_logic.basic.version.model.VersionInfo setContributors(java.util.List<? extends com.top_logic.basic.version.model.Contributor> value) {
		internalSetContributors(value);
		return this;
	}

	/** Internal setter for {@link #getContributors()} without chain call utility. */
	protected final void internalSetContributors(java.util.List<? extends com.top_logic.basic.version.model.Contributor> value) {
		if (value == null) throw new IllegalArgumentException("Property 'contributors' cannot be null.");
		_contributors.clear();
		_contributors.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getContributors()} list.
	 */
	public com.top_logic.basic.version.model.VersionInfo addContributor(com.top_logic.basic.version.model.Contributor value) {
		internalAddContributor(value);
		return this;
	}

	/** Implementation of {@link #addContributor(com.top_logic.basic.version.model.Contributor)} without chain call utility. */
	protected final void internalAddContributor(com.top_logic.basic.version.model.Contributor value) {
		_contributors.add(value);
	}

	/**
	 * Removes a value from the {@link #getContributors()} list.
	 */
	public final void removeContributor(com.top_logic.basic.version.model.Contributor value) {
		_contributors.remove(value);
	}

	/**
	 * The committers that contributed to the software.
	 */
	public final java.util.List<com.top_logic.basic.version.model.Contributor> getDevelopers() {
		return _developers;
	}

	/**
	 * @see #getDevelopers()
	 */
	public com.top_logic.basic.version.model.VersionInfo setDevelopers(java.util.List<? extends com.top_logic.basic.version.model.Contributor> value) {
		internalSetDevelopers(value);
		return this;
	}

	/** Internal setter for {@link #getDevelopers()} without chain call utility. */
	protected final void internalSetDevelopers(java.util.List<? extends com.top_logic.basic.version.model.Contributor> value) {
		if (value == null) throw new IllegalArgumentException("Property 'developers' cannot be null.");
		_developers.clear();
		_developers.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getDevelopers()} list.
	 */
	public com.top_logic.basic.version.model.VersionInfo addDeveloper(com.top_logic.basic.version.model.Contributor value) {
		internalAddDeveloper(value);
		return this;
	}

	/** Implementation of {@link #addDeveloper(com.top_logic.basic.version.model.Contributor)} without chain call utility. */
	protected final void internalAddDeveloper(com.top_logic.basic.version.model.Contributor value) {
		_developers.add(value);
	}

	/**
	 * Removes a value from the {@link #getDevelopers()} list.
	 */
	public final void removeDeveloper(com.top_logic.basic.version.model.Contributor value) {
		_developers.remove(value);
	}

	/**
	 * Licenses under which the software is released.
	 */
	public final java.util.List<com.top_logic.basic.version.model.License> getLicenses() {
		return _licenses;
	}

	/**
	 * @see #getLicenses()
	 */
	public com.top_logic.basic.version.model.VersionInfo setLicenses(java.util.List<? extends com.top_logic.basic.version.model.License> value) {
		internalSetLicenses(value);
		return this;
	}

	/** Internal setter for {@link #getLicenses()} without chain call utility. */
	protected final void internalSetLicenses(java.util.List<? extends com.top_logic.basic.version.model.License> value) {
		if (value == null) throw new IllegalArgumentException("Property 'licenses' cannot be null.");
		_licenses.clear();
		_licenses.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getLicenses()} list.
	 */
	public com.top_logic.basic.version.model.VersionInfo addLicense(com.top_logic.basic.version.model.License value) {
		internalAddLicense(value);
		return this;
	}

	/** Implementation of {@link #addLicense(com.top_logic.basic.version.model.License)} without chain call utility. */
	protected final void internalAddLicense(com.top_logic.basic.version.model.License value) {
		_licenses.add(value);
	}

	/**
	 * Removes a value from the {@link #getLicenses()} list.
	 */
	public final void removeLicense(com.top_logic.basic.version.model.License value) {
		_licenses.remove(value);
	}

	/**
	 * Description of dependencies this software depends on.
	 */
	public final java.util.List<com.top_logic.basic.version.model.VersionInfo> getDependencies() {
		return _dependencies;
	}

	/**
	 * @see #getDependencies()
	 */
	public com.top_logic.basic.version.model.VersionInfo setDependencies(java.util.List<? extends com.top_logic.basic.version.model.VersionInfo> value) {
		internalSetDependencies(value);
		return this;
	}

	/** Internal setter for {@link #getDependencies()} without chain call utility. */
	protected final void internalSetDependencies(java.util.List<? extends com.top_logic.basic.version.model.VersionInfo> value) {
		if (value == null) throw new IllegalArgumentException("Property 'dependencies' cannot be null.");
		_dependencies.clear();
		_dependencies.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getDependencies()} list.
	 */
	public com.top_logic.basic.version.model.VersionInfo addDependencie(com.top_logic.basic.version.model.VersionInfo value) {
		internalAddDependencie(value);
		return this;
	}

	/** Implementation of {@link #addDependencie(com.top_logic.basic.version.model.VersionInfo)} without chain call utility. */
	protected final void internalAddDependencie(com.top_logic.basic.version.model.VersionInfo value) {
		_dependencies.add(value);
	}

	/**
	 * Removes a value from the {@link #getDependencies()} list.
	 */
	public final void removeDependencie(com.top_logic.basic.version.model.VersionInfo value) {
		_dependencies.remove(value);
	}

	@Override
	public String jsonType() {
		return VERSION_INFO__TYPE;
	}

	private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(
		java.util.Arrays.asList(
			GROUP_ID__PROP, 
			ARTIFACT_ID__PROP, 
			VERSION__PROP, 
			NAME__PROP, 
			DESCRIPTION__PROP, 
			URL__PROP, 
			INCEPTION_YEAR__PROP, 
			BUILD_QUALIFIER__PROP, 
			ORGANIZATION__PROP, 
			CONTRIBUTORS__PROP, 
			DEVELOPERS__PROP, 
			LICENSES__PROP, 
			DEPENDENCIES__PROP));

	@Override
	public java.util.List<String> properties() {
		return PROPERTIES;
	}

	@Override
	public Object get(String field) {
		switch (field) {
			case GROUP_ID__PROP: return getGroupId();
			case ARTIFACT_ID__PROP: return getArtifactId();
			case VERSION__PROP: return getVersion();
			case NAME__PROP: return getName();
			case DESCRIPTION__PROP: return getDescription();
			case URL__PROP: return getUrl();
			case INCEPTION_YEAR__PROP: return getInceptionYear();
			case BUILD_QUALIFIER__PROP: return getBuildQualifier();
			case ORGANIZATION__PROP: return getOrganization();
			case CONTRIBUTORS__PROP: return getContributors();
			case DEVELOPERS__PROP: return getDevelopers();
			case LICENSES__PROP: return getLicenses();
			case DEPENDENCIES__PROP: return getDependencies();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case GROUP_ID__PROP: internalSetGroupId((String) value); break;
			case ARTIFACT_ID__PROP: internalSetArtifactId((String) value); break;
			case VERSION__PROP: internalSetVersion((String) value); break;
			case NAME__PROP: internalSetName((String) value); break;
			case DESCRIPTION__PROP: internalSetDescription((String) value); break;
			case URL__PROP: internalSetUrl((String) value); break;
			case INCEPTION_YEAR__PROP: internalSetInceptionYear((String) value); break;
			case BUILD_QUALIFIER__PROP: internalSetBuildQualifier((String) value); break;
			case ORGANIZATION__PROP: internalSetOrganization((com.top_logic.basic.version.model.Organisation) value); break;
			case CONTRIBUTORS__PROP: internalSetContributors(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.basic.version.model.Contributor.class, value)); break;
			case DEVELOPERS__PROP: internalSetDevelopers(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.basic.version.model.Contributor.class, value)); break;
			case LICENSES__PROP: internalSetLicenses(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.basic.version.model.License.class, value)); break;
			case DEPENDENCIES__PROP: internalSetDependencies(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.basic.version.model.VersionInfo.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.basic.version.model.VersionInfo readVersionInfo(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.basic.version.model.VersionInfo result = new com.top_logic.basic.version.model.VersionInfo();
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
		out.name(GROUP_ID__PROP);
		out.value(getGroupId());
		out.name(ARTIFACT_ID__PROP);
		out.value(getArtifactId());
		out.name(VERSION__PROP);
		out.value(getVersion());
		if (hasName()) {
			out.name(NAME__PROP);
			out.value(getName());
		}
		if (hasDescription()) {
			out.name(DESCRIPTION__PROP);
			out.value(getDescription());
		}
		if (hasUrl()) {
			out.name(URL__PROP);
			out.value(getUrl());
		}
		if (hasInceptionYear()) {
			out.name(INCEPTION_YEAR__PROP);
			out.value(getInceptionYear());
		}
		if (hasBuildQualifier()) {
			out.name(BUILD_QUALIFIER__PROP);
			out.value(getBuildQualifier());
		}
		if (hasOrganization()) {
			out.name(ORGANIZATION__PROP);
			getOrganization().writeTo(out);
		}
		out.name(CONTRIBUTORS__PROP);
		out.beginArray();
		for (com.top_logic.basic.version.model.Contributor x : getContributors()) {
			x.writeTo(out);
		}
		out.endArray();
		out.name(DEVELOPERS__PROP);
		out.beginArray();
		for (com.top_logic.basic.version.model.Contributor x : getDevelopers()) {
			x.writeTo(out);
		}
		out.endArray();
		out.name(LICENSES__PROP);
		out.beginArray();
		for (com.top_logic.basic.version.model.License x : getLicenses()) {
			x.writeTo(out);
		}
		out.endArray();
		out.name(DEPENDENCIES__PROP);
		out.beginArray();
		for (com.top_logic.basic.version.model.VersionInfo x : getDependencies()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case GROUP_ID__PROP: setGroupId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ARTIFACT_ID__PROP: setArtifactId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case VERSION__PROP: setVersion(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case NAME__PROP: setName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DESCRIPTION__PROP: setDescription(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case URL__PROP: setUrl(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case INCEPTION_YEAR__PROP: setInceptionYear(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case BUILD_QUALIFIER__PROP: setBuildQualifier(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ORGANIZATION__PROP: setOrganization(com.top_logic.basic.version.model.Organisation.readOrganisation(in)); break;
			case CONTRIBUTORS__PROP: {
				in.beginArray();
				while (in.hasNext()) {
					addContributor(com.top_logic.basic.version.model.Contributor.readContributor(in));
				}
				in.endArray();
			}
			break;
			case DEVELOPERS__PROP: {
				in.beginArray();
				while (in.hasNext()) {
					addDeveloper(com.top_logic.basic.version.model.Contributor.readContributor(in));
				}
				in.endArray();
			}
			break;
			case LICENSES__PROP: {
				in.beginArray();
				while (in.hasNext()) {
					addLicense(com.top_logic.basic.version.model.License.readLicense(in));
				}
				in.endArray();
			}
			break;
			case DEPENDENCIES__PROP: {
				in.beginArray();
				while (in.hasNext()) {
					addDependencie(com.top_logic.basic.version.model.VersionInfo.readVersionInfo(in));
				}
				in.endArray();
			}
			break;
			default: super.readField(in, field);
		}
	}

}
