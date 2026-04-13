package com.top_logic.ai.service.tools.model.messages;

/**
 * Response listing available annotation types for a target element.
 */
public class AnnotationSearchResponse extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.service.tools.model.messages.AnnotationSearchResponse} instance.
	 */
	public static com.top_logic.ai.service.tools.model.messages.AnnotationSearchResponse create() {
		return new com.top_logic.ai.service.tools.model.messages.AnnotationSearchResponse();
	}

	/** Identifier for the {@link com.top_logic.ai.service.tools.model.messages.AnnotationSearchResponse} type in JSON format. */
	public static final String ANNOTATION_SEARCH_RESPONSE__TYPE = "AnnotationSearchResponse";

	/** @see #getTargetRef() */
	public static final String TARGET_REF__PROP = "targetRef";

	/** @see #getTargetKind() */
	public static final String TARGET_KIND__PROP = "targetKind";

	/** @see #getTotalMatches() */
	public static final String TOTAL_MATCHES__PROP = "totalMatches";

	/** @see #getResults() */
	public static final String RESULTS__PROP = "results";

	private String _targetRef = "";

	private String _targetKind = "";

	private int _totalMatches = 0;

	private final java.util.List<com.top_logic.ai.service.tools.model.messages.AnnotationSearchResult> _results = new java.util.ArrayList<>();

	/**
	 * Creates a {@link AnnotationSearchResponse} instance.
	 *
	 * @see com.top_logic.ai.service.tools.model.messages.AnnotationSearchResponse#create()
	 */
	protected AnnotationSearchResponse() {
		super();
	}

	/**
	 * The resolved target reference.
	 */
	public final String getTargetRef() {
		return _targetRef;
	}

	/**
	 * @see #getTargetRef()
	 */
	public com.top_logic.ai.service.tools.model.messages.AnnotationSearchResponse setTargetRef(String value) {
		internalSetTargetRef(value);
		return this;
	}

	/** Internal setter for {@link #getTargetRef()} without chain call utility. */
	protected final void internalSetTargetRef(String value) {
		_targetRef = value;
	}

	/**
	 * Kind of the resolved target (e.g. "property", "type", "module").
	 */
	public final String getTargetKind() {
		return _targetKind;
	}

	/**
	 * @see #getTargetKind()
	 */
	public com.top_logic.ai.service.tools.model.messages.AnnotationSearchResponse setTargetKind(String value) {
		internalSetTargetKind(value);
		return this;
	}

	/** Internal setter for {@link #getTargetKind()} without chain call utility. */
	protected final void internalSetTargetKind(String value) {
		_targetKind = value;
	}

	/**
	 * Total number of available annotations.
	 */
	public final int getTotalMatches() {
		return _totalMatches;
	}

	/**
	 * @see #getTotalMatches()
	 */
	public com.top_logic.ai.service.tools.model.messages.AnnotationSearchResponse setTotalMatches(int value) {
		internalSetTotalMatches(value);
		return this;
	}

	/** Internal setter for {@link #getTotalMatches()} without chain call utility. */
	protected final void internalSetTotalMatches(int value) {
		_totalMatches = value;
	}

	/**
	 * List of available annotation types.
	 */
	public final java.util.List<com.top_logic.ai.service.tools.model.messages.AnnotationSearchResult> getResults() {
		return _results;
	}

	/**
	 * @see #getResults()
	 */
	public com.top_logic.ai.service.tools.model.messages.AnnotationSearchResponse setResults(java.util.List<? extends com.top_logic.ai.service.tools.model.messages.AnnotationSearchResult> value) {
		internalSetResults(value);
		return this;
	}

	/** Internal setter for {@link #getResults()} without chain call utility. */
	protected final void internalSetResults(java.util.List<? extends com.top_logic.ai.service.tools.model.messages.AnnotationSearchResult> value) {
		if (value == null) throw new IllegalArgumentException("Property 'results' cannot be null.");
		_results.clear();
		_results.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getResults()} list.
	 */
	public com.top_logic.ai.service.tools.model.messages.AnnotationSearchResponse addResult(com.top_logic.ai.service.tools.model.messages.AnnotationSearchResult value) {
		internalAddResult(value);
		return this;
	}

	/** Implementation of {@link #addResult(com.top_logic.ai.service.tools.model.messages.AnnotationSearchResult)} without chain call utility. */
	protected final void internalAddResult(com.top_logic.ai.service.tools.model.messages.AnnotationSearchResult value) {
		_results.add(value);
	}

	/**
	 * Removes a value from the {@link #getResults()} list.
	 */
	public final void removeResult(com.top_logic.ai.service.tools.model.messages.AnnotationSearchResult value) {
		_results.remove(value);
	}

	@Override
	public String jsonType() {
		return ANNOTATION_SEARCH_RESPONSE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TARGET_REF__PROP, 
			TARGET_KIND__PROP, 
			TOTAL_MATCHES__PROP, 
			RESULTS__PROP);
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
			case TARGET_REF__PROP: return getTargetRef();
			case TARGET_KIND__PROP: return getTargetKind();
			case TOTAL_MATCHES__PROP: return getTotalMatches();
			case RESULTS__PROP: return getResults();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TARGET_REF__PROP: internalSetTargetRef((String) value); break;
			case TARGET_KIND__PROP: internalSetTargetKind((String) value); break;
			case TOTAL_MATCHES__PROP: internalSetTotalMatches((int) value); break;
			case RESULTS__PROP: internalSetResults(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.ai.service.tools.model.messages.AnnotationSearchResult.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.service.tools.model.messages.AnnotationSearchResponse readAnnotationSearchResponse(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.service.tools.model.messages.AnnotationSearchResponse result = new com.top_logic.ai.service.tools.model.messages.AnnotationSearchResponse();
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
		out.name(TARGET_REF__PROP);
		out.value(getTargetRef());
		out.name(TARGET_KIND__PROP);
		out.value(getTargetKind());
		out.name(TOTAL_MATCHES__PROP);
		out.value(getTotalMatches());
		out.name(RESULTS__PROP);
		out.beginArray();
		for (com.top_logic.ai.service.tools.model.messages.AnnotationSearchResult x : getResults()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TARGET_REF__PROP: setTargetRef(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TARGET_KIND__PROP: setTargetKind(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TOTAL_MATCHES__PROP: setTotalMatches(in.nextInt()); break;
			case RESULTS__PROP: {
				java.util.List<com.top_logic.ai.service.tools.model.messages.AnnotationSearchResult> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.ai.service.tools.model.messages.AnnotationSearchResult.readAnnotationSearchResult(in));
				}
				in.endArray();
				setResults(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
