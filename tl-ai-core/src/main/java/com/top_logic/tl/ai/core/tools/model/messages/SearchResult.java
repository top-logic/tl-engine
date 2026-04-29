package com.top_logic.tl.ai.core.tools.model.messages;

/**
 * Search result for a model element.
 */
public class SearchResult extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.model.messages.SearchResult} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.model.messages.SearchResult create() {
		return new com.top_logic.tl.ai.core.tools.model.messages.SearchResult();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.model.messages.SearchResult} type in JSON format. */
	public static final String SEARCH_RESULT__TYPE = "SearchResult";

	/** @see #getKind() */
	public static final String KIND__PROP = "kind";

	/** @see #getName() */
	public static final String NAME__PROP = "name";

	/** @see #getQualifiedName() */
	public static final String QUALIFIED_NAME__PROP = "qualifiedName";

	/** @see #getScore() */
	public static final String SCORE__PROP = "score";

	/** @see #getMatchedField() */
	public static final String MATCHED_FIELD__PROP = "matchedField";

	/** @see #getContext() */
	public static final String CONTEXT__PROP = "context";

	/** @see #getLabel() */
	public static final String LABEL__PROP = "label";

	/** @see #getDescription() */
	public static final String DESCRIPTION__PROP = "description";

	private String _kind = "";

	private String _name = "";

	private String _qualifiedName = "";

	private int _score = 0;

	private String _matchedField = "";

	private final java.util.Map<String, String> _context = new java.util.LinkedHashMap<>();

	private String _label = "";

	private String _description = "";

	/**
	 * Creates a {@link SearchResult} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.model.messages.SearchResult#create()
	 */
	protected SearchResult() {
		super();
	}

	/**
	 * Kind of element (module, class, enumeration, datatype, property, reference).
	 */
	public final String getKind() {
		return _kind;
	}

	/**
	 * @see #getKind()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.SearchResult setKind(String value) {
		internalSetKind(value);
		return this;
	}

	/** Internal setter for {@link #getKind()} without chain call utility. */
	protected final void internalSetKind(String value) {
		_kind = value;
	}

	/**
	 * Element name.
	 */
	public final String getName() {
		return _name;
	}

	/**
	 * @see #getName()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.SearchResult setName(String value) {
		internalSetName(value);
		return this;
	}

	/** Internal setter for {@link #getName()} without chain call utility. */
	protected final void internalSetName(String value) {
		_name = value;
	}

	/**
	 * Qualified name (for types).
	 */
	public final String getQualifiedName() {
		return _qualifiedName;
	}

	/**
	 * @see #getQualifiedName()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.SearchResult setQualifiedName(String value) {
		internalSetQualifiedName(value);
		return this;
	}

	/** Internal setter for {@link #getQualifiedName()} without chain call utility. */
	protected final void internalSetQualifiedName(String value) {
		_qualifiedName = value;
	}

	/**
	 * Relevance score.
	 */
	public final int getScore() {
		return _score;
	}

	/**
	 * @see #getScore()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.SearchResult setScore(int value) {
		internalSetScore(value);
		return this;
	}

	/** Internal setter for {@link #getScore()} without chain call utility. */
	protected final void internalSetScore(int value) {
		_score = value;
	}

	/**
	 * Which field was matched (name, <code>qualifiedName</code>, label, description).
	 */
	public final String getMatchedField() {
		return _matchedField;
	}

	/**
	 * @see #getMatchedField()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.SearchResult setMatchedField(String value) {
		internalSetMatchedField(value);
		return this;
	}

	/** Internal setter for {@link #getMatchedField()} without chain call utility. */
	protected final void internalSetMatchedField(String value) {
		_matchedField = value;
	}

	/**
	 * Contextual information (module, type, counts, etc.).
	 *
	 * Note: Values are serialized as JSON and need to be parsed.
	 */
	public final java.util.Map<String, String> getContext() {
		return _context;
	}

	/**
	 * @see #getContext()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.SearchResult setContext(java.util.Map<String, String> value) {
		internalSetContext(value);
		return this;
	}

	/** Internal setter for {@link #getContext()} without chain call utility. */
	protected final void internalSetContext(java.util.Map<String, String> value) {
		if (value == null) throw new IllegalArgumentException("Property 'context' cannot be null.");
		_context.clear();
		_context.putAll(value);
	}

	/**
	 * Adds a key value pair to the {@link #getContext()} map.
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.SearchResult putContext(String key, String value) {
		internalPutContext(key, value);
		return this;
	}

	/** Implementation of {@link #putContext(String, String)} without chain call utility. */
	protected final void  internalPutContext(String key, String value) {
		if (_context.containsKey(key)) {
			throw new IllegalArgumentException("Property 'context' already contains a value for key '" + key + "'.");
		}
		_context.put(key, value);
	}

	/**
	 * Removes a key from the {@link #getContext()} map.
	 */
	public final void removeContext(String key) {
		_context.remove(key);
	}

	/**
	 * Localized label.
	 */
	public final String getLabel() {
		return _label;
	}

	/**
	 * @see #getLabel()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.SearchResult setLabel(String value) {
		internalSetLabel(value);
		return this;
	}

	/** Internal setter for {@link #getLabel()} without chain call utility. */
	protected final void internalSetLabel(String value) {
		_label = value;
	}

	/**
	 * Localized description.
	 */
	public final String getDescription() {
		return _description;
	}

	/**
	 * @see #getDescription()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.SearchResult setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	/** Internal setter for {@link #getDescription()} without chain call utility. */
	protected final void internalSetDescription(String value) {
		_description = value;
	}

	@Override
	public String jsonType() {
		return SEARCH_RESULT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			KIND__PROP, 
			NAME__PROP, 
			QUALIFIED_NAME__PROP, 
			SCORE__PROP, 
			MATCHED_FIELD__PROP, 
			CONTEXT__PROP, 
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
			case KIND__PROP: return getKind();
			case NAME__PROP: return getName();
			case QUALIFIED_NAME__PROP: return getQualifiedName();
			case SCORE__PROP: return getScore();
			case MATCHED_FIELD__PROP: return getMatchedField();
			case CONTEXT__PROP: return getContext();
			case LABEL__PROP: return getLabel();
			case DESCRIPTION__PROP: return getDescription();
			default: return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void set(String field, Object value) {
		switch (field) {
			case KIND__PROP: internalSetKind((String) value); break;
			case NAME__PROP: internalSetName((String) value); break;
			case QUALIFIED_NAME__PROP: internalSetQualifiedName((String) value); break;
			case SCORE__PROP: internalSetScore((int) value); break;
			case MATCHED_FIELD__PROP: internalSetMatchedField((String) value); break;
			case CONTEXT__PROP: internalSetContext((java.util.Map<String, String>) value); break;
			case LABEL__PROP: internalSetLabel((String) value); break;
			case DESCRIPTION__PROP: internalSetDescription((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.model.messages.SearchResult readSearchResult(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.model.messages.SearchResult result = new com.top_logic.tl.ai.core.tools.model.messages.SearchResult();
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
		out.name(KIND__PROP);
		out.value(getKind());
		out.name(NAME__PROP);
		out.value(getName());
		out.name(QUALIFIED_NAME__PROP);
		out.value(getQualifiedName());
		out.name(SCORE__PROP);
		out.value(getScore());
		out.name(MATCHED_FIELD__PROP);
		out.value(getMatchedField());
		out.name(CONTEXT__PROP);
		out.beginObject();
		for (java.util.Map.Entry<String,String> entry : getContext().entrySet()) {
			out.name(entry.getKey());
			out.value(entry.getValue());
		}
		out.endObject();
		out.name(LABEL__PROP);
		out.value(getLabel());
		out.name(DESCRIPTION__PROP);
		out.value(getDescription());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case KIND__PROP: setKind(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case NAME__PROP: setName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case QUALIFIED_NAME__PROP: setQualifiedName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case SCORE__PROP: setScore(in.nextInt()); break;
			case MATCHED_FIELD__PROP: setMatchedField(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CONTEXT__PROP: {
				java.util.Map<String, String> newValue = new java.util.LinkedHashMap<>();
				in.beginObject();
				while (in.hasNext()) {
					newValue.put(in.nextName(), de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endObject();
				setContext(newValue);
				break;
			}
			case LABEL__PROP: setLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DESCRIPTION__PROP: setDescription(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
