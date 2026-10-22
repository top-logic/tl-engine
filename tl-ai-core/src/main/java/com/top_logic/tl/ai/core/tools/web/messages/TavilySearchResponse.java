package com.top_logic.tl.ai.core.tools.web.messages;

/**
 * Response payload from the Tavily Search API.
 */
public class TavilySearchResponse extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.web.messages.TavilySearchResponse} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.web.messages.TavilySearchResponse create() {
		return new com.top_logic.tl.ai.core.tools.web.messages.TavilySearchResponse();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.web.messages.TavilySearchResponse} type in JSON format. */
	public static final String TAVILY_SEARCH_RESPONSE__TYPE = "TavilySearchResponse";

	/** @see #getAnswer() */
	public static final String ANSWER__PROP = "answer";

	/** @see #getResults() */
	public static final String RESULTS__PROP = "results";

	private String _answer = "";

	private final java.util.List<com.top_logic.tl.ai.core.tools.web.messages.TavilySearchResult> _results = new java.util.ArrayList<>();

	/**
	 * Creates a {@link TavilySearchResponse} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.web.messages.TavilySearchResponse#create()
	 */
	protected TavilySearchResponse() {
		super();
	}

	/**
	 * Synthesized answer, present when {@code include_answer} was requested.
	 */
	public final String getAnswer() {
		return _answer;
	}

	/**
	 * @see #getAnswer()
	 */
	public com.top_logic.tl.ai.core.tools.web.messages.TavilySearchResponse setAnswer(String value) {
		internalSetAnswer(value);
		return this;
	}

	/** Internal setter for {@link #getAnswer()} without chain call utility. */
	protected final void internalSetAnswer(String value) {
		_answer = value;
	}

	/**
	 * The organic search results.
	 */
	public final java.util.List<com.top_logic.tl.ai.core.tools.web.messages.TavilySearchResult> getResults() {
		return _results;
	}

	/**
	 * @see #getResults()
	 */
	public com.top_logic.tl.ai.core.tools.web.messages.TavilySearchResponse setResults(java.util.List<? extends com.top_logic.tl.ai.core.tools.web.messages.TavilySearchResult> value) {
		internalSetResults(value);
		return this;
	}

	/** Internal setter for {@link #getResults()} without chain call utility. */
	protected final void internalSetResults(java.util.List<? extends com.top_logic.tl.ai.core.tools.web.messages.TavilySearchResult> value) {
		if (value == null) throw new IllegalArgumentException("Property 'results' cannot be null.");
		_results.clear();
		_results.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getResults()} list.
	 */
	public com.top_logic.tl.ai.core.tools.web.messages.TavilySearchResponse addResult(com.top_logic.tl.ai.core.tools.web.messages.TavilySearchResult value) {
		internalAddResult(value);
		return this;
	}

	/** Implementation of {@link #addResult(com.top_logic.tl.ai.core.tools.web.messages.TavilySearchResult)} without chain call utility. */
	protected final void internalAddResult(com.top_logic.tl.ai.core.tools.web.messages.TavilySearchResult value) {
		_results.add(value);
	}

	/**
	 * Removes a value from the {@link #getResults()} list.
	 */
	public final void removeResult(com.top_logic.tl.ai.core.tools.web.messages.TavilySearchResult value) {
		_results.remove(value);
	}

	@Override
	public String jsonType() {
		return TAVILY_SEARCH_RESPONSE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			ANSWER__PROP, 
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
			case ANSWER__PROP: return getAnswer();
			case RESULTS__PROP: return getResults();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ANSWER__PROP: internalSetAnswer((String) value); break;
			case RESULTS__PROP: internalSetResults(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.tl.ai.core.tools.web.messages.TavilySearchResult.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.web.messages.TavilySearchResponse readTavilySearchResponse(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.web.messages.TavilySearchResponse result = new com.top_logic.tl.ai.core.tools.web.messages.TavilySearchResponse();
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
		out.name(ANSWER__PROP);
		out.value(getAnswer());
		out.name(RESULTS__PROP);
		out.beginArray();
		for (com.top_logic.tl.ai.core.tools.web.messages.TavilySearchResult x : getResults()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ANSWER__PROP: setAnswer(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case RESULTS__PROP: {
				java.util.List<com.top_logic.tl.ai.core.tools.web.messages.TavilySearchResult> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.tl.ai.core.tools.web.messages.TavilySearchResult.readTavilySearchResult(in));
				}
				in.endArray();
				setResults(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
