package com.top_logic.tl.ai.core.tools.web.messages;

/**
 * A single organic result within a {@link TavilySearchResponse}.
 */
public class TavilySearchResult extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.web.messages.TavilySearchResult} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.web.messages.TavilySearchResult create() {
		return new com.top_logic.tl.ai.core.tools.web.messages.TavilySearchResult();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.web.messages.TavilySearchResult} type in JSON format. */
	public static final String TAVILY_SEARCH_RESULT__TYPE = "TavilySearchResult";

	/** @see #getTitle() */
	public static final String TITLE__PROP = "title";

	/** @see #getUrl() */
	public static final String URL__PROP = "url";

	/** @see #getContent() */
	public static final String CONTENT__PROP = "content";

	/** @see #getRawContent() */
	public static final String RAW_CONTENT__PROP = "raw_content";

	private String _title = "";

	private String _url = "";

	private String _content = "";

	private String _rawContent = "";

	/**
	 * Creates a {@link TavilySearchResult} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.web.messages.TavilySearchResult#create()
	 */
	protected TavilySearchResult() {
		super();
	}

	/**
	 * The result title.
	 */
	public final String getTitle() {
		return _title;
	}

	/**
	 * @see #getTitle()
	 */
	public com.top_logic.tl.ai.core.tools.web.messages.TavilySearchResult setTitle(String value) {
		internalSetTitle(value);
		return this;
	}

	/** Internal setter for {@link #getTitle()} without chain call utility. */
	protected final void internalSetTitle(String value) {
		_title = value;
	}

	/**
	 * The result URL.
	 */
	public final String getUrl() {
		return _url;
	}

	/**
	 * @see #getUrl()
	 */
	public com.top_logic.tl.ai.core.tools.web.messages.TavilySearchResult setUrl(String value) {
		internalSetUrl(value);
		return this;
	}

	/** Internal setter for {@link #getUrl()} without chain call utility. */
	protected final void internalSetUrl(String value) {
		_url = value;
	}

	/**
	 * A short, query-relevant content extract (snippet).
	 */
	public final String getContent() {
		return _content;
	}

	/**
	 * @see #getContent()
	 */
	public com.top_logic.tl.ai.core.tools.web.messages.TavilySearchResult setContent(String value) {
		internalSetContent(value);
		return this;
	}

	/** Internal setter for {@link #getContent()} without chain call utility. */
	protected final void internalSetContent(String value) {
		_content = value;
	}

	/**
	 * The full raw page content, present when {@code include_raw_content} was requested.
	 */
	public final String getRawContent() {
		return _rawContent;
	}

	/**
	 * @see #getRawContent()
	 */
	public com.top_logic.tl.ai.core.tools.web.messages.TavilySearchResult setRawContent(String value) {
		internalSetRawContent(value);
		return this;
	}

	/** Internal setter for {@link #getRawContent()} without chain call utility. */
	protected final void internalSetRawContent(String value) {
		_rawContent = value;
	}

	@Override
	public String jsonType() {
		return TAVILY_SEARCH_RESULT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TITLE__PROP, 
			URL__PROP, 
			CONTENT__PROP, 
			RAW_CONTENT__PROP);
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
			case TITLE__PROP: return getTitle();
			case URL__PROP: return getUrl();
			case CONTENT__PROP: return getContent();
			case RAW_CONTENT__PROP: return getRawContent();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TITLE__PROP: internalSetTitle((String) value); break;
			case URL__PROP: internalSetUrl((String) value); break;
			case CONTENT__PROP: internalSetContent((String) value); break;
			case RAW_CONTENT__PROP: internalSetRawContent((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.web.messages.TavilySearchResult readTavilySearchResult(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.web.messages.TavilySearchResult result = new com.top_logic.tl.ai.core.tools.web.messages.TavilySearchResult();
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
		out.name(TITLE__PROP);
		out.value(getTitle());
		out.name(URL__PROP);
		out.value(getUrl());
		out.name(CONTENT__PROP);
		out.value(getContent());
		out.name(RAW_CONTENT__PROP);
		out.value(getRawContent());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TITLE__PROP: setTitle(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case URL__PROP: setUrl(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CONTENT__PROP: setContent(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case RAW_CONTENT__PROP: setRawContent(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
