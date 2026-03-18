package com.top_logic.ai.service.tools.model.messages;

/**
 * A single available annotation type for a model element.
 */
public class AnnotationSearchResult extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.service.tools.model.messages.AnnotationSearchResult} instance.
	 */
	public static com.top_logic.ai.service.tools.model.messages.AnnotationSearchResult create() {
		return new com.top_logic.ai.service.tools.model.messages.AnnotationSearchResult();
	}

	/** Identifier for the {@link com.top_logic.ai.service.tools.model.messages.AnnotationSearchResult} type in JSON format. */
	public static final String ANNOTATION_SEARCH_RESULT__TYPE = "AnnotationSearchResult";

	/** @see #getTagName() */
	public static final String TAG_NAME__PROP = "tagName";

	/** @see #getConfigInterface() */
	public static final String CONFIG_INTERFACE__PROP = "configInterface";

	/** @see #getDescription() */
	public static final String DESCRIPTION__PROP = "description";

	/** @see #getApplicableTo() */
	public static final String APPLICABLE_TO__PROP = "applicableTo";

	/** @see #getImplementationOptions() */
	public static final String IMPLEMENTATION_OPTIONS__PROP = "implementationOptions";

	private String _tagName = "";

	private String _configInterface = "";

	private String _description = "";

	private final java.util.List<String> _applicableTo = new java.util.ArrayList<>();

	private final java.util.List<com.top_logic.ai.service.tools.model.messages.AnnotationPropertyInfo> _implementationOptions = new java.util.ArrayList<>();

	/**
	 * Creates a {@link AnnotationSearchResult} instance.
	 *
	 * @see com.top_logic.ai.service.tools.model.messages.AnnotationSearchResult#create()
	 */
	protected AnnotationSearchResult() {
		super();
	}

	/**
	 * XML tag name from @TagName annotation.
	 */
	public final String getTagName() {
		return _tagName;
	}

	/**
	 * @see #getTagName()
	 */
	public com.top_logic.ai.service.tools.model.messages.AnnotationSearchResult setTagName(String value) {
		internalSetTagName(value);
		return this;
	}

	/** Internal setter for {@link #getTagName()} without chain call utility. */
	protected final void internalSetTagName(String value) {
		_tagName = value;
	}

	/**
	 * Fully qualified configuration interface class name.
	 */
	public final String getConfigInterface() {
		return _configInterface;
	}

	/**
	 * @see #getConfigInterface()
	 */
	public com.top_logic.ai.service.tools.model.messages.AnnotationSearchResult setConfigInterface(String value) {
		internalSetConfigInterface(value);
		return this;
	}

	/** Internal setter for {@link #getConfigInterface()} without chain call utility. */
	protected final void internalSetConfigInterface(String value) {
		_configInterface = value;
	}

	/**
	 * Human-readable description from i18n resources.
	 */
	public final String getDescription() {
		return _description;
	}

	/**
	 * @see #getDescription()
	 */
	public com.top_logic.ai.service.tools.model.messages.AnnotationSearchResult setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	/** Internal setter for {@link #getDescription()} without chain call utility. */
	protected final void internalSetDescription(String value) {
		_description = value;
	}

	/**
	 * Element kinds this annotation applies to (e.g. "attribute", "type", "module", "classifier").
	 */
	public final java.util.List<String> getApplicableTo() {
		return _applicableTo;
	}

	/**
	 * @see #getApplicableTo()
	 */
	public com.top_logic.ai.service.tools.model.messages.AnnotationSearchResult setApplicableTo(java.util.List<? extends String> value) {
		internalSetApplicableTo(value);
		return this;
	}

	/** Internal setter for {@link #getApplicableTo()} without chain call utility. */
	protected final void internalSetApplicableTo(java.util.List<? extends String> value) {
		_applicableTo.clear();
		_applicableTo.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getApplicableTo()} list.
	 */
	public com.top_logic.ai.service.tools.model.messages.AnnotationSearchResult addApplicableTo(String value) {
		internalAddApplicableTo(value);
		return this;
	}

	/** Implementation of {@link #addApplicableTo(String)} without chain call utility. */
	protected final void internalAddApplicableTo(String value) {
		_applicableTo.add(value);
	}

	/**
	 * Removes a value from the {@link #getApplicableTo()} list.
	 */
	public final void removeApplicableTo(String value) {
		_applicableTo.remove(value);
	}

	/**
	 * Concrete implementation options for polymorphic configuration properties.
	 * Empty if the annotation can be used directly without choosing an implementation.
	 */
	public final java.util.List<com.top_logic.ai.service.tools.model.messages.AnnotationPropertyInfo> getImplementationOptions() {
		return _implementationOptions;
	}

	/**
	 * @see #getImplementationOptions()
	 */
	public com.top_logic.ai.service.tools.model.messages.AnnotationSearchResult setImplementationOptions(java.util.List<? extends com.top_logic.ai.service.tools.model.messages.AnnotationPropertyInfo> value) {
		internalSetImplementationOptions(value);
		return this;
	}

	/** Internal setter for {@link #getImplementationOptions()} without chain call utility. */
	protected final void internalSetImplementationOptions(java.util.List<? extends com.top_logic.ai.service.tools.model.messages.AnnotationPropertyInfo> value) {
		if (value == null) throw new IllegalArgumentException("Property 'implementationOptions' cannot be null.");
		_implementationOptions.clear();
		_implementationOptions.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getImplementationOptions()} list.
	 */
	public com.top_logic.ai.service.tools.model.messages.AnnotationSearchResult addImplementationOption(com.top_logic.ai.service.tools.model.messages.AnnotationPropertyInfo value) {
		internalAddImplementationOption(value);
		return this;
	}

	/** Implementation of {@link #addImplementationOption(com.top_logic.ai.service.tools.model.messages.AnnotationPropertyInfo)} without chain call utility. */
	protected final void internalAddImplementationOption(com.top_logic.ai.service.tools.model.messages.AnnotationPropertyInfo value) {
		_implementationOptions.add(value);
	}

	/**
	 * Removes a value from the {@link #getImplementationOptions()} list.
	 */
	public final void removeImplementationOption(com.top_logic.ai.service.tools.model.messages.AnnotationPropertyInfo value) {
		_implementationOptions.remove(value);
	}

	@Override
	public String jsonType() {
		return ANNOTATION_SEARCH_RESULT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TAG_NAME__PROP, 
			CONFIG_INTERFACE__PROP, 
			DESCRIPTION__PROP, 
			APPLICABLE_TO__PROP, 
			IMPLEMENTATION_OPTIONS__PROP);
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
			case TAG_NAME__PROP: return getTagName();
			case CONFIG_INTERFACE__PROP: return getConfigInterface();
			case DESCRIPTION__PROP: return getDescription();
			case APPLICABLE_TO__PROP: return getApplicableTo();
			case IMPLEMENTATION_OPTIONS__PROP: return getImplementationOptions();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TAG_NAME__PROP: internalSetTagName((String) value); break;
			case CONFIG_INTERFACE__PROP: internalSetConfigInterface((String) value); break;
			case DESCRIPTION__PROP: internalSetDescription((String) value); break;
			case APPLICABLE_TO__PROP: internalSetApplicableTo(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
			case IMPLEMENTATION_OPTIONS__PROP: internalSetImplementationOptions(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.ai.service.tools.model.messages.AnnotationPropertyInfo.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.service.tools.model.messages.AnnotationSearchResult readAnnotationSearchResult(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.service.tools.model.messages.AnnotationSearchResult result = new com.top_logic.ai.service.tools.model.messages.AnnotationSearchResult();
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
		out.name(TAG_NAME__PROP);
		out.value(getTagName());
		out.name(CONFIG_INTERFACE__PROP);
		out.value(getConfigInterface());
		out.name(DESCRIPTION__PROP);
		out.value(getDescription());
		out.name(APPLICABLE_TO__PROP);
		out.beginArray();
		for (String x : getApplicableTo()) {
			out.value(x);
		}
		out.endArray();
		out.name(IMPLEMENTATION_OPTIONS__PROP);
		out.beginArray();
		for (com.top_logic.ai.service.tools.model.messages.AnnotationPropertyInfo x : getImplementationOptions()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TAG_NAME__PROP: setTagName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CONFIG_INTERFACE__PROP: setConfigInterface(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DESCRIPTION__PROP: setDescription(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case APPLICABLE_TO__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setApplicableTo(newValue);
			}
			break;
			case IMPLEMENTATION_OPTIONS__PROP: {
				java.util.List<com.top_logic.ai.service.tools.model.messages.AnnotationPropertyInfo> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.ai.service.tools.model.messages.AnnotationPropertyInfo.readAnnotationPropertyInfo(in));
				}
				in.endArray();
				setImplementationOptions(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
