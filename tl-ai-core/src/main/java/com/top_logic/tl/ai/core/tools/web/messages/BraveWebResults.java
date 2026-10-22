package com.top_logic.tl.ai.core.tools.web.messages;

/**
 * The {@code web} section of a {@link BraveSearchResponse}.
 */
public class BraveWebResults extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.web.messages.BraveWebResults} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.web.messages.BraveWebResults create() {
		return new com.top_logic.tl.ai.core.tools.web.messages.BraveWebResults();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.web.messages.BraveWebResults} type in JSON format. */
	public static final String BRAVE_WEB_RESULTS__TYPE = "BraveWebResults";

	/** @see #getResults() */
	public static final String RESULTS__PROP = "results";

	private final java.util.List<com.top_logic.tl.ai.core.tools.web.messages.BraveSearchResult> _results = new java.util.ArrayList<>();

	/**
	 * Creates a {@link BraveWebResults} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.web.messages.BraveWebResults#create()
	 */
	protected BraveWebResults() {
		super();
	}

	/**
	 * The organic web results.
	 */
	public final java.util.List<com.top_logic.tl.ai.core.tools.web.messages.BraveSearchResult> getResults() {
		return _results;
	}

	/**
	 * @see #getResults()
	 */
	public com.top_logic.tl.ai.core.tools.web.messages.BraveWebResults setResults(java.util.List<? extends com.top_logic.tl.ai.core.tools.web.messages.BraveSearchResult> value) {
		internalSetResults(value);
		return this;
	}

	/** Internal setter for {@link #getResults()} without chain call utility. */
	protected final void internalSetResults(java.util.List<? extends com.top_logic.tl.ai.core.tools.web.messages.BraveSearchResult> value) {
		if (value == null) throw new IllegalArgumentException("Property 'results' cannot be null.");
		_results.clear();
		_results.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getResults()} list.
	 */
	public com.top_logic.tl.ai.core.tools.web.messages.BraveWebResults addResult(com.top_logic.tl.ai.core.tools.web.messages.BraveSearchResult value) {
		internalAddResult(value);
		return this;
	}

	/** Implementation of {@link #addResult(com.top_logic.tl.ai.core.tools.web.messages.BraveSearchResult)} without chain call utility. */
	protected final void internalAddResult(com.top_logic.tl.ai.core.tools.web.messages.BraveSearchResult value) {
		_results.add(value);
	}

	/**
	 * Removes a value from the {@link #getResults()} list.
	 */
	public final void removeResult(com.top_logic.tl.ai.core.tools.web.messages.BraveSearchResult value) {
		_results.remove(value);
	}

	@Override
	public String jsonType() {
		return BRAVE_WEB_RESULTS__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
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
			case RESULTS__PROP: return getResults();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case RESULTS__PROP: internalSetResults(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.tl.ai.core.tools.web.messages.BraveSearchResult.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.web.messages.BraveWebResults readBraveWebResults(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.web.messages.BraveWebResults result = new com.top_logic.tl.ai.core.tools.web.messages.BraveWebResults();
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
		out.name(RESULTS__PROP);
		out.beginArray();
		for (com.top_logic.tl.ai.core.tools.web.messages.BraveSearchResult x : getResults()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case RESULTS__PROP: {
				java.util.List<com.top_logic.tl.ai.core.tools.web.messages.BraveSearchResult> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.tl.ai.core.tools.web.messages.BraveSearchResult.readBraveSearchResult(in));
				}
				in.endArray();
				setResults(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
