package com.top_logic.tl.ai.core.tools.web.messages;

/**
 * Response payload from the Google Custom Search JSON API ({@code GET /customsearch/v1}).
 *
 * <p>
 * The request is sent as URL query parameters, so no request message is modelled.
 * </p>
 */
public class GoogleSearchResponse extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.web.messages.GoogleSearchResponse} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.web.messages.GoogleSearchResponse create() {
		return new com.top_logic.tl.ai.core.tools.web.messages.GoogleSearchResponse();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.web.messages.GoogleSearchResponse} type in JSON format. */
	public static final String GOOGLE_SEARCH_RESPONSE__TYPE = "GoogleSearchResponse";

	/** @see #getItems() */
	public static final String ITEMS__PROP = "items";

	private final java.util.List<com.top_logic.tl.ai.core.tools.web.messages.GoogleSearchResult> _items = new java.util.ArrayList<>();

	/**
	 * Creates a {@link GoogleSearchResponse} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.web.messages.GoogleSearchResponse#create()
	 */
	protected GoogleSearchResponse() {
		super();
	}

	/**
	 * The organic search results; absent when the search yields no matches.
	 */
	public final java.util.List<com.top_logic.tl.ai.core.tools.web.messages.GoogleSearchResult> getItems() {
		return _items;
	}

	/**
	 * @see #getItems()
	 */
	public com.top_logic.tl.ai.core.tools.web.messages.GoogleSearchResponse setItems(java.util.List<? extends com.top_logic.tl.ai.core.tools.web.messages.GoogleSearchResult> value) {
		internalSetItems(value);
		return this;
	}

	/** Internal setter for {@link #getItems()} without chain call utility. */
	protected final void internalSetItems(java.util.List<? extends com.top_logic.tl.ai.core.tools.web.messages.GoogleSearchResult> value) {
		if (value == null) throw new IllegalArgumentException("Property 'items' cannot be null.");
		_items.clear();
		_items.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getItems()} list.
	 */
	public com.top_logic.tl.ai.core.tools.web.messages.GoogleSearchResponse addItem(com.top_logic.tl.ai.core.tools.web.messages.GoogleSearchResult value) {
		internalAddItem(value);
		return this;
	}

	/** Implementation of {@link #addItem(com.top_logic.tl.ai.core.tools.web.messages.GoogleSearchResult)} without chain call utility. */
	protected final void internalAddItem(com.top_logic.tl.ai.core.tools.web.messages.GoogleSearchResult value) {
		_items.add(value);
	}

	/**
	 * Removes a value from the {@link #getItems()} list.
	 */
	public final void removeItem(com.top_logic.tl.ai.core.tools.web.messages.GoogleSearchResult value) {
		_items.remove(value);
	}

	@Override
	public String jsonType() {
		return GOOGLE_SEARCH_RESPONSE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			ITEMS__PROP);
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
			case ITEMS__PROP: return getItems();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ITEMS__PROP: internalSetItems(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.tl.ai.core.tools.web.messages.GoogleSearchResult.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.web.messages.GoogleSearchResponse readGoogleSearchResponse(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.web.messages.GoogleSearchResponse result = new com.top_logic.tl.ai.core.tools.web.messages.GoogleSearchResponse();
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
		out.name(ITEMS__PROP);
		out.beginArray();
		for (com.top_logic.tl.ai.core.tools.web.messages.GoogleSearchResult x : getItems()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ITEMS__PROP: {
				java.util.List<com.top_logic.tl.ai.core.tools.web.messages.GoogleSearchResult> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.tl.ai.core.tools.web.messages.GoogleSearchResult.readGoogleSearchResult(in));
				}
				in.endArray();
				setItems(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
