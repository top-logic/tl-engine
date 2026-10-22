package com.top_logic.tl.ai.core.tools.model.messages;

/**
 * Information about an enumeration classifier.
 */
public class ClassifierInfo extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.model.messages.ClassifierInfo} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.model.messages.ClassifierInfo create() {
		return new com.top_logic.tl.ai.core.tools.model.messages.ClassifierInfo();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.model.messages.ClassifierInfo} type in JSON format. */
	public static final String CLASSIFIER_INFO__TYPE = "ClassifierInfo";

	/** @see #getName() */
	public static final String NAME__PROP = "name";

	/** @see #isDefault() */
	public static final String DEFAULT__PROP = "default";

	/** @see #getIndex() */
	public static final String INDEX__PROP = "index";

	/** @see #getCustomI18NKey() */
	public static final String CUSTOM_I_18_NKEY__PROP = "customI18NKey";

	/** @see #getLabel() */
	public static final String LABEL__PROP = "label";

	/** @see #getDescription() */
	public static final String DESCRIPTION__PROP = "description";

	private String _name = "";

	private boolean _default = false;

	private int _index = 0;

	private String _customI18NKey = "";

	private String _label = "";

	private String _description = "";

	/**
	 * Creates a {@link ClassifierInfo} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.model.messages.ClassifierInfo#create()
	 */
	protected ClassifierInfo() {
		super();
	}

	/**
	 * Classifier name.
	 */
	public final String getName() {
		return _name;
	}

	/**
	 * @see #getName()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.ClassifierInfo setName(String value) {
		internalSetName(value);
		return this;
	}

	/** Internal setter for {@link #getName()} without chain call utility. */
	protected final void internalSetName(String value) {
		_name = value;
	}

	/**
	 * Whether this is the default classifier.
	 */
	public final boolean isDefault() {
		return _default;
	}

	/**
	 * @see #isDefault()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.ClassifierInfo setDefault(boolean value) {
		internalSetDefault(value);
		return this;
	}

	/** Internal setter for {@link #isDefault()} without chain call utility. */
	protected final void internalSetDefault(boolean value) {
		_default = value;
	}

	/**
	 * Index of the classifier in the enumeration.
	 */
	public final int getIndex() {
		return _index;
	}

	/**
	 * @see #getIndex()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.ClassifierInfo setIndex(int value) {
		internalSetIndex(value);
		return this;
	}

	/** Internal setter for {@link #getIndex()} without chain call utility. */
	protected final void internalSetIndex(int value) {
		_index = value;
	}

	/**
	 * Custom I18N key (if annotated with TLI18NKey).
	 */
	public final String getCustomI18NKey() {
		return _customI18NKey;
	}

	/**
	 * @see #getCustomI18NKey()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.ClassifierInfo setCustomI18NKey(String value) {
		internalSetCustomI18NKey(value);
		return this;
	}

	/** Internal setter for {@link #getCustomI18NKey()} without chain call utility. */
	protected final void internalSetCustomI18NKey(String value) {
		_customI18NKey = value;
	}

	/**
	 * Localized label for the classifier.
	 */
	public final String getLabel() {
		return _label;
	}

	/**
	 * @see #getLabel()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.ClassifierInfo setLabel(String value) {
		internalSetLabel(value);
		return this;
	}

	/** Internal setter for {@link #getLabel()} without chain call utility. */
	protected final void internalSetLabel(String value) {
		_label = value;
	}

	/**
	 * Localized description of the classifier.
	 */
	public final String getDescription() {
		return _description;
	}

	/**
	 * @see #getDescription()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.ClassifierInfo setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	/** Internal setter for {@link #getDescription()} without chain call utility. */
	protected final void internalSetDescription(String value) {
		_description = value;
	}

	@Override
	public String jsonType() {
		return CLASSIFIER_INFO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			NAME__PROP, 
			DEFAULT__PROP, 
			INDEX__PROP, 
			CUSTOM_I_18_NKEY__PROP, 
			LABEL__PROP, 
			DESCRIPTION__PROP);
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
			case NAME__PROP: return getName();
			case DEFAULT__PROP: return isDefault();
			case INDEX__PROP: return getIndex();
			case CUSTOM_I_18_NKEY__PROP: return getCustomI18NKey();
			case LABEL__PROP: return getLabel();
			case DESCRIPTION__PROP: return getDescription();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case NAME__PROP: internalSetName((String) value); break;
			case DEFAULT__PROP: internalSetDefault((boolean) value); break;
			case INDEX__PROP: internalSetIndex((int) value); break;
			case CUSTOM_I_18_NKEY__PROP: internalSetCustomI18NKey((String) value); break;
			case LABEL__PROP: internalSetLabel((String) value); break;
			case DESCRIPTION__PROP: internalSetDescription((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.model.messages.ClassifierInfo readClassifierInfo(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.model.messages.ClassifierInfo result = new com.top_logic.tl.ai.core.tools.model.messages.ClassifierInfo();
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
		out.name(DEFAULT__PROP);
		out.value(isDefault());
		out.name(INDEX__PROP);
		out.value(getIndex());
		out.name(CUSTOM_I_18_NKEY__PROP);
		out.value(getCustomI18NKey());
		out.name(LABEL__PROP);
		out.value(getLabel());
		out.name(DESCRIPTION__PROP);
		out.value(getDescription());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case NAME__PROP: setName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DEFAULT__PROP: setDefault(in.nextBoolean()); break;
			case INDEX__PROP: setIndex(in.nextInt()); break;
			case CUSTOM_I_18_NKEY__PROP: setCustomI18NKey(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case LABEL__PROP: setLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DESCRIPTION__PROP: setDescription(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
