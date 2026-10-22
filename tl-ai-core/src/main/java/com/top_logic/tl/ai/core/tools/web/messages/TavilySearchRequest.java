package com.top_logic.tl.ai.core.tools.web.messages;

/**
 * Request payload for the Tavily Search API ({@code POST /search}).
 */
public class TavilySearchRequest extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.web.messages.TavilySearchRequest} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.web.messages.TavilySearchRequest create() {
		return new com.top_logic.tl.ai.core.tools.web.messages.TavilySearchRequest();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.web.messages.TavilySearchRequest} type in JSON format. */
	public static final String TAVILY_SEARCH_REQUEST__TYPE = "TavilySearchRequest";

	/** @see #getQuery() */
	public static final String QUERY__PROP = "query";

	/** @see #getMaxResults() */
	public static final String MAX_RESULTS__PROP = "max_results";

	/** @see #getSearchDepth() */
	public static final String SEARCH_DEPTH__PROP = "search_depth";

	/** @see #isIncludeAnswer() */
	public static final String INCLUDE_ANSWER__PROP = "include_answer";

	/** @see #isIncludeRawContent() */
	public static final String INCLUDE_RAW_CONTENT__PROP = "include_raw_content";

	/** @see #getIncludeDomains() */
	public static final String INCLUDE_DOMAINS__PROP = "include_domains";

	/** @see #getExcludeDomains() */
	public static final String EXCLUDE_DOMAINS__PROP = "exclude_domains";

	/** @see #getTimeRange() */
	public static final String TIME_RANGE__PROP = "time_range";

	private String _query = "";

	private int _maxResults = 0;

	private String _searchDepth = "";

	private boolean _includeAnswer = false;

	private boolean _includeRawContent = false;

	private final java.util.List<String> _includeDomains = new java.util.ArrayList<>();

	private final java.util.List<String> _excludeDomains = new java.util.ArrayList<>();

	private String _timeRange = null;

	/**
	 * Creates a {@link TavilySearchRequest} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.web.messages.TavilySearchRequest#create()
	 */
	protected TavilySearchRequest() {
		super();
	}

	/**
	 * The natural-language search query.
	 */
	public final String getQuery() {
		return _query;
	}

	/**
	 * @see #getQuery()
	 */
	public com.top_logic.tl.ai.core.tools.web.messages.TavilySearchRequest setQuery(String value) {
		internalSetQuery(value);
		return this;
	}

	/** Internal setter for {@link #getQuery()} without chain call utility. */
	protected final void internalSetQuery(String value) {
		_query = value;
	}

	/**
	 * Maximum number of results to return.
	 */
	public final int getMaxResults() {
		return _maxResults;
	}

	/**
	 * @see #getMaxResults()
	 */
	public com.top_logic.tl.ai.core.tools.web.messages.TavilySearchRequest setMaxResults(int value) {
		internalSetMaxResults(value);
		return this;
	}

	/** Internal setter for {@link #getMaxResults()} without chain call utility. */
	protected final void internalSetMaxResults(int value) {
		_maxResults = value;
	}

	/**
	 * Search depth, either {@code basic} or {@code advanced}.
	 */
	public final String getSearchDepth() {
		return _searchDepth;
	}

	/**
	 * @see #getSearchDepth()
	 */
	public com.top_logic.tl.ai.core.tools.web.messages.TavilySearchRequest setSearchDepth(String value) {
		internalSetSearchDepth(value);
		return this;
	}

	/** Internal setter for {@link #getSearchDepth()} without chain call utility. */
	protected final void internalSetSearchDepth(String value) {
		_searchDepth = value;
	}

	/**
	 * Whether Tavily should include a synthesized answer.
	 */
	public final boolean isIncludeAnswer() {
		return _includeAnswer;
	}

	/**
	 * @see #isIncludeAnswer()
	 */
	public com.top_logic.tl.ai.core.tools.web.messages.TavilySearchRequest setIncludeAnswer(boolean value) {
		internalSetIncludeAnswer(value);
		return this;
	}

	/** Internal setter for {@link #isIncludeAnswer()} without chain call utility. */
	protected final void internalSetIncludeAnswer(boolean value) {
		_includeAnswer = value;
	}

	/**
	 * Whether each result should include the full raw page content.
	 */
	public final boolean isIncludeRawContent() {
		return _includeRawContent;
	}

	/**
	 * @see #isIncludeRawContent()
	 */
	public com.top_logic.tl.ai.core.tools.web.messages.TavilySearchRequest setIncludeRawContent(boolean value) {
		internalSetIncludeRawContent(value);
		return this;
	}

	/** Internal setter for {@link #isIncludeRawContent()} without chain call utility. */
	protected final void internalSetIncludeRawContent(boolean value) {
		_includeRawContent = value;
	}

	/**
	 * Restrict results to these domains (empty means no restriction).
	 */
	public final java.util.List<String> getIncludeDomains() {
		return _includeDomains;
	}

	/**
	 * @see #getIncludeDomains()
	 */
	public com.top_logic.tl.ai.core.tools.web.messages.TavilySearchRequest setIncludeDomains(java.util.List<? extends String> value) {
		internalSetIncludeDomains(value);
		return this;
	}

	/** Internal setter for {@link #getIncludeDomains()} without chain call utility. */
	protected final void internalSetIncludeDomains(java.util.List<? extends String> value) {
		_includeDomains.clear();
		_includeDomains.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getIncludeDomains()} list.
	 */
	public com.top_logic.tl.ai.core.tools.web.messages.TavilySearchRequest addIncludeDomain(String value) {
		internalAddIncludeDomain(value);
		return this;
	}

	/** Implementation of {@link #addIncludeDomain(String)} without chain call utility. */
	protected final void internalAddIncludeDomain(String value) {
		_includeDomains.add(value);
	}

	/**
	 * Removes a value from the {@link #getIncludeDomains()} list.
	 */
	public final void removeIncludeDomain(String value) {
		_includeDomains.remove(value);
	}

	/**
	 * Exclude results from these domains.
	 */
	public final java.util.List<String> getExcludeDomains() {
		return _excludeDomains;
	}

	/**
	 * @see #getExcludeDomains()
	 */
	public com.top_logic.tl.ai.core.tools.web.messages.TavilySearchRequest setExcludeDomains(java.util.List<? extends String> value) {
		internalSetExcludeDomains(value);
		return this;
	}

	/** Internal setter for {@link #getExcludeDomains()} without chain call utility. */
	protected final void internalSetExcludeDomains(java.util.List<? extends String> value) {
		_excludeDomains.clear();
		_excludeDomains.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getExcludeDomains()} list.
	 */
	public com.top_logic.tl.ai.core.tools.web.messages.TavilySearchRequest addExcludeDomain(String value) {
		internalAddExcludeDomain(value);
		return this;
	}

	/** Implementation of {@link #addExcludeDomain(String)} without chain call utility. */
	protected final void internalAddExcludeDomain(String value) {
		_excludeDomains.add(value);
	}

	/**
	 * Removes a value from the {@link #getExcludeDomains()} list.
	 */
	public final void removeExcludeDomain(String value) {
		_excludeDomains.remove(value);
	}

	/**
	 * Only results published within this range: {@code day}, {@code week}, {@code month} or {@code year}; absent for any time.
	 */
	public final String getTimeRange() {
		return _timeRange;
	}

	/**
	 * @see #getTimeRange()
	 */
	public com.top_logic.tl.ai.core.tools.web.messages.TavilySearchRequest setTimeRange(String value) {
		internalSetTimeRange(value);
		return this;
	}

	/** Internal setter for {@link #getTimeRange()} without chain call utility. */
	protected final void internalSetTimeRange(String value) {
		_timeRange = value;
	}

	/**
	 * Checks, whether {@link #getTimeRange()} has a value.
	 */
	public final boolean hasTimeRange() {
		return _timeRange != null;
	}

	@Override
	public String jsonType() {
		return TAVILY_SEARCH_REQUEST__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			QUERY__PROP, 
			MAX_RESULTS__PROP, 
			SEARCH_DEPTH__PROP, 
			INCLUDE_ANSWER__PROP, 
			INCLUDE_RAW_CONTENT__PROP, 
			INCLUDE_DOMAINS__PROP, 
			EXCLUDE_DOMAINS__PROP, 
			TIME_RANGE__PROP);
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
			case QUERY__PROP: return getQuery();
			case MAX_RESULTS__PROP: return getMaxResults();
			case SEARCH_DEPTH__PROP: return getSearchDepth();
			case INCLUDE_ANSWER__PROP: return isIncludeAnswer();
			case INCLUDE_RAW_CONTENT__PROP: return isIncludeRawContent();
			case INCLUDE_DOMAINS__PROP: return getIncludeDomains();
			case EXCLUDE_DOMAINS__PROP: return getExcludeDomains();
			case TIME_RANGE__PROP: return getTimeRange();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case QUERY__PROP: internalSetQuery((String) value); break;
			case MAX_RESULTS__PROP: internalSetMaxResults((int) value); break;
			case SEARCH_DEPTH__PROP: internalSetSearchDepth((String) value); break;
			case INCLUDE_ANSWER__PROP: internalSetIncludeAnswer((boolean) value); break;
			case INCLUDE_RAW_CONTENT__PROP: internalSetIncludeRawContent((boolean) value); break;
			case INCLUDE_DOMAINS__PROP: internalSetIncludeDomains(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
			case EXCLUDE_DOMAINS__PROP: internalSetExcludeDomains(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
			case TIME_RANGE__PROP: internalSetTimeRange((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.web.messages.TavilySearchRequest readTavilySearchRequest(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.web.messages.TavilySearchRequest result = new com.top_logic.tl.ai.core.tools.web.messages.TavilySearchRequest();
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
		out.name(QUERY__PROP);
		out.value(getQuery());
		out.name(MAX_RESULTS__PROP);
		out.value(getMaxResults());
		out.name(SEARCH_DEPTH__PROP);
		out.value(getSearchDepth());
		out.name(INCLUDE_ANSWER__PROP);
		out.value(isIncludeAnswer());
		out.name(INCLUDE_RAW_CONTENT__PROP);
		out.value(isIncludeRawContent());
		out.name(INCLUDE_DOMAINS__PROP);
		out.beginArray();
		for (String x : getIncludeDomains()) {
			out.value(x);
		}
		out.endArray();
		out.name(EXCLUDE_DOMAINS__PROP);
		out.beginArray();
		for (String x : getExcludeDomains()) {
			out.value(x);
		}
		out.endArray();
		if (hasTimeRange()) {
			out.name(TIME_RANGE__PROP);
			out.value(getTimeRange());
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case QUERY__PROP: setQuery(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case MAX_RESULTS__PROP: setMaxResults(in.nextInt()); break;
			case SEARCH_DEPTH__PROP: setSearchDepth(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case INCLUDE_ANSWER__PROP: setIncludeAnswer(in.nextBoolean()); break;
			case INCLUDE_RAW_CONTENT__PROP: setIncludeRawContent(in.nextBoolean()); break;
			case INCLUDE_DOMAINS__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setIncludeDomains(newValue);
			}
			break;
			case EXCLUDE_DOMAINS__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setExcludeDomains(newValue);
			}
			break;
			case TIME_RANGE__PROP: setTimeRange(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
