package com.top_logic.tl.ai.core.tools.model.messages;

/**
 * Enumeration details response.
 */
public class EnumerationResponse extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.model.messages.EnumerationResponse} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.model.messages.EnumerationResponse create() {
		return new com.top_logic.tl.ai.core.tools.model.messages.EnumerationResponse();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.model.messages.EnumerationResponse} type in JSON format. */
	public static final String ENUMERATION_RESPONSE__TYPE = "EnumerationResponse";

	/** @see #getEnumeration() */
	public static final String ENUMERATION__PROP = "enumeration";

	/** @see #getModule() */
	public static final String MODULE__PROP = "module";

	/** @see #getName() */
	public static final String NAME__PROP = "name";

	/** @see #getClassifierCount() */
	public static final String CLASSIFIER_COUNT__PROP = "classifierCount";

	/** @see #isUnordered() */
	public static final String UNORDERED__PROP = "unordered";

	/** @see #getLabel() */
	public static final String LABEL__PROP = "label";

	/** @see #getDescription() */
	public static final String DESCRIPTION__PROP = "description";

	/** @see #getClassifiers() */
	public static final String CLASSIFIERS__PROP = "classifiers";

	private String _enumeration = "";

	private String _module = "";

	private String _name = "";

	private int _classifierCount = 0;

	private boolean _unordered = false;

	private String _label = "";

	private String _description = "";

	private final java.util.List<com.top_logic.tl.ai.core.tools.model.messages.ClassifierInfo> _classifiers = new java.util.ArrayList<>();

	/**
	 * Creates a {@link EnumerationResponse} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.model.messages.EnumerationResponse#create()
	 */
	protected EnumerationResponse() {
		super();
	}

	/**
	 * Qualified enumeration name.
	 */
	public final String getEnumeration() {
		return _enumeration;
	}

	/**
	 * @see #getEnumeration()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.EnumerationResponse setEnumeration(String value) {
		internalSetEnumeration(value);
		return this;
	}

	/** Internal setter for {@link #getEnumeration()} without chain call utility. */
	protected final void internalSetEnumeration(String value) {
		_enumeration = value;
	}

	/**
	 * Module name.
	 */
	public final String getModule() {
		return _module;
	}

	/**
	 * @see #getModule()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.EnumerationResponse setModule(String value) {
		internalSetModule(value);
		return this;
	}

	/** Internal setter for {@link #getModule()} without chain call utility. */
	protected final void internalSetModule(String value) {
		_module = value;
	}

	/**
	 * Enumeration name.
	 */
	public final String getName() {
		return _name;
	}

	/**
	 * @see #getName()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.EnumerationResponse setName(String value) {
		internalSetName(value);
		return this;
	}

	/** Internal setter for {@link #getName()} without chain call utility. */
	protected final void internalSetName(String value) {
		_name = value;
	}

	/**
	 * Number of classifiers in the enumeration.
	 */
	public final int getClassifierCount() {
		return _classifierCount;
	}

	/**
	 * @see #getClassifierCount()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.EnumerationResponse setClassifierCount(int value) {
		internalSetClassifierCount(value);
		return this;
	}

	/** Internal setter for {@link #getClassifierCount()} without chain call utility. */
	protected final void internalSetClassifierCount(int value) {
		_classifierCount = value;
	}

	/**
	 * Whether the enumeration is unordered.
	 */
	public final boolean isUnordered() {
		return _unordered;
	}

	/**
	 * @see #isUnordered()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.EnumerationResponse setUnordered(boolean value) {
		internalSetUnordered(value);
		return this;
	}

	/** Internal setter for {@link #isUnordered()} without chain call utility. */
	protected final void internalSetUnordered(boolean value) {
		_unordered = value;
	}

	/**
	 * Localized label for the enumeration.
	 */
	public final String getLabel() {
		return _label;
	}

	/**
	 * @see #getLabel()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.EnumerationResponse setLabel(String value) {
		internalSetLabel(value);
		return this;
	}

	/** Internal setter for {@link #getLabel()} without chain call utility. */
	protected final void internalSetLabel(String value) {
		_label = value;
	}

	/**
	 * Localized description of the enumeration.
	 */
	public final String getDescription() {
		return _description;
	}

	/**
	 * @see #getDescription()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.EnumerationResponse setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	/** Internal setter for {@link #getDescription()} without chain call utility. */
	protected final void internalSetDescription(String value) {
		_description = value;
	}

	/**
	 * List of classifiers in the enumeration.
	 */
	public final java.util.List<com.top_logic.tl.ai.core.tools.model.messages.ClassifierInfo> getClassifiers() {
		return _classifiers;
	}

	/**
	 * @see #getClassifiers()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.EnumerationResponse setClassifiers(java.util.List<? extends com.top_logic.tl.ai.core.tools.model.messages.ClassifierInfo> value) {
		internalSetClassifiers(value);
		return this;
	}

	/** Internal setter for {@link #getClassifiers()} without chain call utility. */
	protected final void internalSetClassifiers(java.util.List<? extends com.top_logic.tl.ai.core.tools.model.messages.ClassifierInfo> value) {
		if (value == null) throw new IllegalArgumentException("Property 'classifiers' cannot be null.");
		_classifiers.clear();
		_classifiers.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getClassifiers()} list.
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.EnumerationResponse addClassifier(com.top_logic.tl.ai.core.tools.model.messages.ClassifierInfo value) {
		internalAddClassifier(value);
		return this;
	}

	/** Implementation of {@link #addClassifier(com.top_logic.tl.ai.core.tools.model.messages.ClassifierInfo)} without chain call utility. */
	protected final void internalAddClassifier(com.top_logic.tl.ai.core.tools.model.messages.ClassifierInfo value) {
		_classifiers.add(value);
	}

	/**
	 * Removes a value from the {@link #getClassifiers()} list.
	 */
	public final void removeClassifier(com.top_logic.tl.ai.core.tools.model.messages.ClassifierInfo value) {
		_classifiers.remove(value);
	}

	@Override
	public String jsonType() {
		return ENUMERATION_RESPONSE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			ENUMERATION__PROP, 
			MODULE__PROP, 
			NAME__PROP, 
			CLASSIFIER_COUNT__PROP, 
			UNORDERED__PROP, 
			LABEL__PROP, 
			DESCRIPTION__PROP, 
			CLASSIFIERS__PROP);
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
			case ENUMERATION__PROP: return getEnumeration();
			case MODULE__PROP: return getModule();
			case NAME__PROP: return getName();
			case CLASSIFIER_COUNT__PROP: return getClassifierCount();
			case UNORDERED__PROP: return isUnordered();
			case LABEL__PROP: return getLabel();
			case DESCRIPTION__PROP: return getDescription();
			case CLASSIFIERS__PROP: return getClassifiers();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ENUMERATION__PROP: internalSetEnumeration((String) value); break;
			case MODULE__PROP: internalSetModule((String) value); break;
			case NAME__PROP: internalSetName((String) value); break;
			case CLASSIFIER_COUNT__PROP: internalSetClassifierCount((int) value); break;
			case UNORDERED__PROP: internalSetUnordered((boolean) value); break;
			case LABEL__PROP: internalSetLabel((String) value); break;
			case DESCRIPTION__PROP: internalSetDescription((String) value); break;
			case CLASSIFIERS__PROP: internalSetClassifiers(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.tl.ai.core.tools.model.messages.ClassifierInfo.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.model.messages.EnumerationResponse readEnumerationResponse(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.model.messages.EnumerationResponse result = new com.top_logic.tl.ai.core.tools.model.messages.EnumerationResponse();
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
		out.name(ENUMERATION__PROP);
		out.value(getEnumeration());
		out.name(MODULE__PROP);
		out.value(getModule());
		out.name(NAME__PROP);
		out.value(getName());
		out.name(CLASSIFIER_COUNT__PROP);
		out.value(getClassifierCount());
		out.name(UNORDERED__PROP);
		out.value(isUnordered());
		out.name(LABEL__PROP);
		out.value(getLabel());
		out.name(DESCRIPTION__PROP);
		out.value(getDescription());
		out.name(CLASSIFIERS__PROP);
		out.beginArray();
		for (com.top_logic.tl.ai.core.tools.model.messages.ClassifierInfo x : getClassifiers()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ENUMERATION__PROP: setEnumeration(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case MODULE__PROP: setModule(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case NAME__PROP: setName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CLASSIFIER_COUNT__PROP: setClassifierCount(in.nextInt()); break;
			case UNORDERED__PROP: setUnordered(in.nextBoolean()); break;
			case LABEL__PROP: setLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DESCRIPTION__PROP: setDescription(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CLASSIFIERS__PROP: {
				java.util.List<com.top_logic.tl.ai.core.tools.model.messages.ClassifierInfo> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.tl.ai.core.tools.model.messages.ClassifierInfo.readClassifierInfo(in));
				}
				in.endArray();
				setClassifiers(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
