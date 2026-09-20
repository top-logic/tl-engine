package com.top_logic.tl.ai.core.tools.model.messages;

/**
 * Model search response.
 */
public class SearchResponse extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.model.messages.SearchResponse} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.model.messages.SearchResponse create() {
		return new com.top_logic.tl.ai.core.tools.model.messages.SearchResponse();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.model.messages.SearchResponse} type in JSON format. */
	public static final String SEARCH_RESPONSE__TYPE = "SearchResponse";

	/** @see #getResultCount() */
	public static final String RESULT_COUNT__PROP = "resultCount";

	/** @see #getResults() */
	public static final String RESULTS__PROP = "results";

	/** @see #getError() */
	public static final String ERROR__PROP = "error";

	private int _resultCount = 0;

	private final java.util.List<com.top_logic.tl.ai.core.tools.model.messages.SearchResult> _results = new java.util.ArrayList<>();

	private String _error = "";

	/**
	 * Creates a {@link SearchResponse} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.model.messages.SearchResponse#create()
	 */
	protected SearchResponse() {
		super();
	}

	/**
	 * Number of results.
	 */
	public final int getResultCount() {
		return _resultCount;
	}

	/**
	 * @see #getResultCount()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.SearchResponse setResultCount(int value) {
		internalSetResultCount(value);
		return this;
	}

	/** Internal setter for {@link #getResultCount()} without chain call utility. */
	protected final void internalSetResultCount(int value) {
		_resultCount = value;
	}

	/**
	 * List of search results.
	 */
	public final java.util.List<com.top_logic.tl.ai.core.tools.model.messages.SearchResult> getResults() {
		return _results;
	}

	/**
	 * @see #getResults()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.SearchResponse setResults(java.util.List<? extends com.top_logic.tl.ai.core.tools.model.messages.SearchResult> value) {
		internalSetResults(value);
		return this;
	}

	/** Internal setter for {@link #getResults()} without chain call utility. */
	protected final void internalSetResults(java.util.List<? extends com.top_logic.tl.ai.core.tools.model.messages.SearchResult> value) {
		if (value == null) throw new IllegalArgumentException("Property 'results' cannot be null.");
		_results.clear();
		_results.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getResults()} list.
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.SearchResponse addResult(com.top_logic.tl.ai.core.tools.model.messages.SearchResult value) {
		internalAddResult(value);
		return this;
	}

	/** Implementation of {@link #addResult(com.top_logic.tl.ai.core.tools.model.messages.SearchResult)} without chain call utility. */
	protected final void internalAddResult(com.top_logic.tl.ai.core.tools.model.messages.SearchResult value) {
		_results.add(value);
	}

	/**
	 * Removes a value from the {@link #getResults()} list.
	 */
	public final void removeResult(com.top_logic.tl.ai.core.tools.model.messages.SearchResult value) {
		_results.remove(value);
	}

	/**
	 * Error message, empty if the search ran. A search that found nothing is not an error, so this
	 * is the only thing that says the search failed.
	 */
	public final String getError() {
		return _error;
	}

	/**
	 * @see #getError()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.SearchResponse setError(String value) {
		internalSetError(value);
		return this;
	}

	/** Internal setter for {@link #getError()} without chain call utility. */
	protected final void internalSetError(String value) {
		_error = value;
	}

	@Override
	public String jsonType() {
		return SEARCH_RESPONSE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			RESULT_COUNT__PROP, 
			RESULTS__PROP, 
			ERROR__PROP);
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
			case RESULT_COUNT__PROP: return getResultCount();
			case RESULTS__PROP: return getResults();
			case ERROR__PROP: return getError();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case RESULT_COUNT__PROP: internalSetResultCount((int) value); break;
			case RESULTS__PROP: internalSetResults(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.tl.ai.core.tools.model.messages.SearchResult.class, value)); break;
			case ERROR__PROP: internalSetError((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.model.messages.SearchResponse readSearchResponse(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.model.messages.SearchResponse result = new com.top_logic.tl.ai.core.tools.model.messages.SearchResponse();
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
		out.name(RESULT_COUNT__PROP);
		out.value(getResultCount());
		out.name(RESULTS__PROP);
		out.beginArray();
		for (com.top_logic.tl.ai.core.tools.model.messages.SearchResult x : getResults()) {
			x.writeTo(out);
		}
		out.endArray();
		out.name(ERROR__PROP);
		out.value(getError());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case RESULT_COUNT__PROP: setResultCount(in.nextInt()); break;
			case RESULTS__PROP: {
				java.util.List<com.top_logic.tl.ai.core.tools.model.messages.SearchResult> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.tl.ai.core.tools.model.messages.SearchResult.readSearchResult(in));
				}
				in.endArray();
				setResults(newValue);
			}
			break;
			case ERROR__PROP: setError(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
