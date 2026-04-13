package com.top_logic.ai.service.tools.model.messages;

/**
 * Information about an annotation instance.
 */
public class AnnotationInfo extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.service.tools.model.messages.AnnotationInfo} instance.
	 */
	public static com.top_logic.ai.service.tools.model.messages.AnnotationInfo create() {
		return new com.top_logic.ai.service.tools.model.messages.AnnotationInfo();
	}

	/** Identifier for the {@link com.top_logic.ai.service.tools.model.messages.AnnotationInfo} type in JSON format. */
	public static final String ANNOTATION_INFO__TYPE = "AnnotationInfo";

	/** @see #getTag() */
	public static final String TAG__PROP = "tag";

	/** @see #getConfigInterface() */
	public static final String CONFIG_INTERFACE__PROP = "configInterface";

	/** @see #isInherited() */
	public static final String INHERITED__PROP = "inherited";

	/** @see #getDefinedIn() */
	public static final String DEFINED_IN__PROP = "definedIn";

	/** @see #getConfigJson() */
	public static final String CONFIG_JSON__PROP = "configJson";

	private String _tag = "";

	private String _configInterface = "";

	private boolean _inherited = false;

	private String _definedIn = "";

	private String _configJson = "";

	/**
	 * Creates a {@link AnnotationInfo} instance.
	 *
	 * @see com.top_logic.ai.service.tools.model.messages.AnnotationInfo#create()
	 */
	protected AnnotationInfo() {
		super();
	}

	/**
	 * Annotation tag name.
	 */
	public final String getTag() {
		return _tag;
	}

	/**
	 * @see #getTag()
	 */
	public com.top_logic.ai.service.tools.model.messages.AnnotationInfo setTag(String value) {
		internalSetTag(value);
		return this;
	}

	/** Internal setter for {@link #getTag()} without chain call utility. */
	protected final void internalSetTag(String value) {
		_tag = value;
	}

	/**
	 * Configuration interface class name.
	 */
	public final String getConfigInterface() {
		return _configInterface;
	}

	/**
	 * @see #getConfigInterface()
	 */
	public com.top_logic.ai.service.tools.model.messages.AnnotationInfo setConfigInterface(String value) {
		internalSetConfigInterface(value);
		return this;
	}

	/** Internal setter for {@link #getConfigInterface()} without chain call utility. */
	protected final void internalSetConfigInterface(String value) {
		_configInterface = value;
	}

	/**
	 * Whether this annotation is inherited.
	 */
	public final boolean isInherited() {
		return _inherited;
	}

	/**
	 * @see #isInherited()
	 */
	public com.top_logic.ai.service.tools.model.messages.AnnotationInfo setInherited(boolean value) {
		internalSetInherited(value);
		return this;
	}

	/** Internal setter for {@link #isInherited()} without chain call utility. */
	protected final void internalSetInherited(boolean value) {
		_inherited = value;
	}

	/**
	 * Qualified name where the annotation is defined (for inherited annotations).
	 */
	public final String getDefinedIn() {
		return _definedIn;
	}

	/**
	 * @see #getDefinedIn()
	 */
	public com.top_logic.ai.service.tools.model.messages.AnnotationInfo setDefinedIn(String value) {
		internalSetDefinedIn(value);
		return this;
	}

	/** Internal setter for {@link #getDefinedIn()} without chain call utility. */
	protected final void internalSetDefinedIn(String value) {
		_definedIn = value;
	}

	/**
	 * Annotation configuration as JSON (serialized via {@code JsonConfigurationWriter} with schema references).
	 */
	public final String getConfigJson() {
		return _configJson;
	}

	/**
	 * @see #getConfigJson()
	 */
	public com.top_logic.ai.service.tools.model.messages.AnnotationInfo setConfigJson(String value) {
		internalSetConfigJson(value);
		return this;
	}

	/** Internal setter for {@link #getConfigJson()} without chain call utility. */
	protected final void internalSetConfigJson(String value) {
		_configJson = value;
	}

	@Override
	public String jsonType() {
		return ANNOTATION_INFO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TAG__PROP, 
			CONFIG_INTERFACE__PROP, 
			INHERITED__PROP, 
			DEFINED_IN__PROP, 
			CONFIG_JSON__PROP);
		PROPERTIES = java.util.Collections.unmodifiableList(local);
	}

	static final java.util.Set<String> TRANSIENT_PROPERTIES;
	static {
		java.util.HashSet<String> tmp = new java.util.HashSet<>();
		tmp.addAll(java.util.Arrays.asList(
				));
		TRANSIENT_PROPERTIES = java.util.Collections.unmodifiableSet(tmp);
	}

	@Override
	public java.util.List<String> properties() {
		return PROPERTIES;
	}

	@Override
	public java.util.Set<String> transientProperties() {
		return TRANSIENT_PROPERTIES;
	}

	@Override
	public Object get(String field) {
		switch (field) {
			case TAG__PROP: return getTag();
			case CONFIG_INTERFACE__PROP: return getConfigInterface();
			case INHERITED__PROP: return isInherited();
			case DEFINED_IN__PROP: return getDefinedIn();
			case CONFIG_JSON__PROP: return getConfigJson();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TAG__PROP: internalSetTag((String) value); break;
			case CONFIG_INTERFACE__PROP: internalSetConfigInterface((String) value); break;
			case INHERITED__PROP: internalSetInherited((boolean) value); break;
			case DEFINED_IN__PROP: internalSetDefinedIn((String) value); break;
			case CONFIG_JSON__PROP: internalSetConfigJson((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.service.tools.model.messages.AnnotationInfo readAnnotationInfo(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.service.tools.model.messages.AnnotationInfo result = new com.top_logic.ai.service.tools.model.messages.AnnotationInfo();
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
		out.name(TAG__PROP);
		out.value(getTag());
		out.name(CONFIG_INTERFACE__PROP);
		out.value(getConfigInterface());
		out.name(INHERITED__PROP);
		out.value(isInherited());
		out.name(DEFINED_IN__PROP);
		out.value(getDefinedIn());
		out.name(CONFIG_JSON__PROP);
		out.value(getConfigJson());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TAG__PROP: setTag(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CONFIG_INTERFACE__PROP: setConfigInterface(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case INHERITED__PROP: setInherited(in.nextBoolean()); break;
			case DEFINED_IN__PROP: setDefinedIn(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CONFIG_JSON__PROP: setConfigJson(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
