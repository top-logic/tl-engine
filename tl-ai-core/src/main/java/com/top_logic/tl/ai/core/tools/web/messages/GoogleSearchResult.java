package com.top_logic.tl.ai.core.tools.web.messages;

/**
 * A single organic result within a {@link GoogleSearchResponse}.
 */
public class GoogleSearchResult extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.web.messages.GoogleSearchResult} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.web.messages.GoogleSearchResult create() {
		return new com.top_logic.tl.ai.core.tools.web.messages.GoogleSearchResult();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.web.messages.GoogleSearchResult} type in JSON format. */
	public static final String GOOGLE_SEARCH_RESULT__TYPE = "GoogleSearchResult";

	/** @see #getTitle() */
	public static final String TITLE__PROP = "title";

	/** @see #getLink() */
	public static final String LINK__PROP = "link";

	/** @see #getSnippet() */
	public static final String SNIPPET__PROP = "snippet";

	private String _title = "";

	private String _link = "";

	private String _snippet = "";

	/**
	 * Creates a {@link GoogleSearchResult} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.web.messages.GoogleSearchResult#create()
	 */
	protected GoogleSearchResult() {
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
	public com.top_logic.tl.ai.core.tools.web.messages.GoogleSearchResult setTitle(String value) {
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
	public final String getLink() {
		return _link;
	}

	/**
	 * @see #getLink()
	 */
	public com.top_logic.tl.ai.core.tools.web.messages.GoogleSearchResult setLink(String value) {
		internalSetLink(value);
		return this;
	}

	/** Internal setter for {@link #getLink()} without chain call utility. */
	protected final void internalSetLink(String value) {
		_link = value;
	}

	/**
	 * A short, query-relevant extract (snippet).
	 */
	public final String getSnippet() {
		return _snippet;
	}

	/**
	 * @see #getSnippet()
	 */
	public com.top_logic.tl.ai.core.tools.web.messages.GoogleSearchResult setSnippet(String value) {
		internalSetSnippet(value);
		return this;
	}

	/** Internal setter for {@link #getSnippet()} without chain call utility. */
	protected final void internalSetSnippet(String value) {
		_snippet = value;
	}

	@Override
	public String jsonType() {
		return GOOGLE_SEARCH_RESULT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TITLE__PROP, 
			LINK__PROP, 
			SNIPPET__PROP);
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
			case LINK__PROP: return getLink();
			case SNIPPET__PROP: return getSnippet();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TITLE__PROP: internalSetTitle((String) value); break;
			case LINK__PROP: internalSetLink((String) value); break;
			case SNIPPET__PROP: internalSetSnippet((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.web.messages.GoogleSearchResult readGoogleSearchResult(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.web.messages.GoogleSearchResult result = new com.top_logic.tl.ai.core.tools.web.messages.GoogleSearchResult();
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
		out.name(LINK__PROP);
		out.value(getLink());
		out.name(SNIPPET__PROP);
		out.value(getSnippet());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TITLE__PROP: setTitle(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case LINK__PROP: setLink(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case SNIPPET__PROP: setSnippet(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
