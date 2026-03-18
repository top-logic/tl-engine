package com.top_logic.ai.service.tools.model.messages;

/**
 * A single annotation type matching a search query.
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

	/** @see #getTargetTypes() */
	public static final String TARGET_TYPES__PROP = "targetTypes";

	/** @see #getScore() */
	public static final String SCORE__PROP = "score";

	/** @see #getMatchedField() */
	public static final String MATCHED_FIELD__PROP = "matchedField";

	private String _tagName = "";

	private String _configInterface = "";

	private String _description = "";

	private final java.util.List<String> _applicableTo = new java.util.ArrayList<>();

	private final java.util.List<String> _targetTypes = new java.util.ArrayList<>();

	private int _score = 0;

	private String _matchedField = "";

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
	 * TLTypeKind values from @TargetType (e.g. "STRING", "INT", "DATE"). Empty if unrestricted.
	 */
	public final java.util.List<String> getTargetTypes() {
		return _targetTypes;
	}

	/**
	 * @see #getTargetTypes()
	 */
	public com.top_logic.ai.service.tools.model.messages.AnnotationSearchResult setTargetTypes(java.util.List<? extends String> value) {
		internalSetTargetTypes(value);
		return this;
	}

	/** Internal setter for {@link #getTargetTypes()} without chain call utility. */
	protected final void internalSetTargetTypes(java.util.List<? extends String> value) {
		_targetTypes.clear();
		_targetTypes.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getTargetTypes()} list.
	 */
	public com.top_logic.ai.service.tools.model.messages.AnnotationSearchResult addTargetType(String value) {
		internalAddTargetType(value);
		return this;
	}

	/** Implementation of {@link #addTargetType(String)} without chain call utility. */
	protected final void internalAddTargetType(String value) {
		_targetTypes.add(value);
	}

	/**
	 * Removes a value from the {@link #getTargetTypes()} list.
	 */
	public final void removeTargetType(String value) {
		_targetTypes.remove(value);
	}

	/**
	 * Keyword relevance score.
	 */
	public final int getScore() {
		return _score;
	}

	/**
	 * @see #getScore()
	 */
	public com.top_logic.ai.service.tools.model.messages.AnnotationSearchResult setScore(int value) {
		internalSetScore(value);
		return this;
	}

	/** Internal setter for {@link #getScore()} without chain call utility. */
	protected final void internalSetScore(int value) {
		_score = value;
	}

	/**
	 * Which field was matched by the query.
	 */
	public final String getMatchedField() {
		return _matchedField;
	}

	/**
	 * @see #getMatchedField()
	 */
	public com.top_logic.ai.service.tools.model.messages.AnnotationSearchResult setMatchedField(String value) {
		internalSetMatchedField(value);
		return this;
	}

	/** Internal setter for {@link #getMatchedField()} without chain call utility. */
	protected final void internalSetMatchedField(String value) {
		_matchedField = value;
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
			TARGET_TYPES__PROP, 
			SCORE__PROP, 
			MATCHED_FIELD__PROP);
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
			case TARGET_TYPES__PROP: return getTargetTypes();
			case SCORE__PROP: return getScore();
			case MATCHED_FIELD__PROP: return getMatchedField();
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
			case TARGET_TYPES__PROP: internalSetTargetTypes(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
			case SCORE__PROP: internalSetScore((int) value); break;
			case MATCHED_FIELD__PROP: internalSetMatchedField((String) value); break;
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
		out.name(TARGET_TYPES__PROP);
		out.beginArray();
		for (String x : getTargetTypes()) {
			out.value(x);
		}
		out.endArray();
		out.name(SCORE__PROP);
		out.value(getScore());
		out.name(MATCHED_FIELD__PROP);
		out.value(getMatchedField());
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
			case TARGET_TYPES__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setTargetTypes(newValue);
			}
			break;
			case SCORE__PROP: setScore(in.nextInt()); break;
			case MATCHED_FIELD__PROP: setMatchedField(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
