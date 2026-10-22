package com.top_logic.tl.ai.core.tools.web.messages;

/**
 * Response payload from the Brave Web Search API ({@code GET /res/v1/web/search}).
 *
 * <p>
 * The request is sent as URL query parameters, so no request message is modelled.
 * </p>
 */
public class BraveSearchResponse extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.web.messages.BraveSearchResponse} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.web.messages.BraveSearchResponse create() {
		return new com.top_logic.tl.ai.core.tools.web.messages.BraveSearchResponse();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.web.messages.BraveSearchResponse} type in JSON format. */
	public static final String BRAVE_SEARCH_RESPONSE__TYPE = "BraveSearchResponse";

	/** @see #getWeb() */
	public static final String WEB__PROP = "web";

	private com.top_logic.tl.ai.core.tools.web.messages.BraveWebResults _web = null;

	/**
	 * Creates a {@link BraveSearchResponse} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.web.messages.BraveSearchResponse#create()
	 */
	protected BraveSearchResponse() {
		super();
	}

	/**
	 * The web results section; absent when the response carries no web results.
	 */
	public final com.top_logic.tl.ai.core.tools.web.messages.BraveWebResults getWeb() {
		return _web;
	}

	/**
	 * @see #getWeb()
	 */
	public com.top_logic.tl.ai.core.tools.web.messages.BraveSearchResponse setWeb(com.top_logic.tl.ai.core.tools.web.messages.BraveWebResults value) {
		internalSetWeb(value);
		return this;
	}

	/** Internal setter for {@link #getWeb()} without chain call utility. */
	protected final void internalSetWeb(com.top_logic.tl.ai.core.tools.web.messages.BraveWebResults value) {
		_web = value;
	}

	/**
	 * Checks, whether {@link #getWeb()} has a value.
	 */
	public final boolean hasWeb() {
		return _web != null;
	}

	@Override
	public String jsonType() {
		return BRAVE_SEARCH_RESPONSE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			WEB__PROP);
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
			case WEB__PROP: return getWeb();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case WEB__PROP: internalSetWeb((com.top_logic.tl.ai.core.tools.web.messages.BraveWebResults) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.web.messages.BraveSearchResponse readBraveSearchResponse(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.web.messages.BraveSearchResponse result = new com.top_logic.tl.ai.core.tools.web.messages.BraveSearchResponse();
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
		if (hasWeb()) {
			out.name(WEB__PROP);
			getWeb().writeTo(out);
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case WEB__PROP: setWeb(com.top_logic.tl.ai.core.tools.web.messages.BraveWebResults.readBraveWebResults(in)); break;
			default: super.readField(in, field);
		}
	}

}
