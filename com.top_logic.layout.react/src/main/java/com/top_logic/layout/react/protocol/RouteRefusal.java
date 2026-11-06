package com.top_logic.layout.react.protocol;

/**
 * Reply to the global command the client sends for a URL the browser moved to by itself, when the
 * display does not take up the URL.
 *
 * <p>
 * The client-side counterparts of the field names are {@code FIELD_REFUSED} and
 * {@code FIELD_CURRENT_URL} in {@code route-sync.ts}, which answers a refusal by taking the
 * browser back to the history entry the display belongs to.
 * </p>
 */
public class RouteRefusal extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.layout.react.protocol.RouteRefusal} instance.
	 */
	public static com.top_logic.layout.react.protocol.RouteRefusal create() {
		return new com.top_logic.layout.react.protocol.RouteRefusal();
	}

	/** Identifier for the {@link com.top_logic.layout.react.protocol.RouteRefusal} type in JSON format. */
	public static final String ROUTE_REFUSAL__TYPE = "RouteRefusal";

	/** @see #isRefused() */
	public static final String REFUSED__PROP = "refused";

	/** @see #getCurrentUrl() */
	public static final String CURRENT_URL__PROP = "currentUrl";

	private boolean _refused = false;

	private String _currentUrl = "";

	/**
	 * Creates a {@link RouteRefusal} instance.
	 *
	 * @see com.top_logic.layout.react.protocol.RouteRefusal#create()
	 */
	protected RouteRefusal() {
		super();
	}

	/**
	 * Whether the display does not take up the URL.
	 */
	public final boolean isRefused() {
		return _refused;
	}

	/**
	 * @see #isRefused()
	 */
	public com.top_logic.layout.react.protocol.RouteRefusal setRefused(boolean value) {
		internalSetRefused(value);
		return this;
	}

	/** Internal setter for {@link #isRefused()} without chain call utility. */
	protected final void internalSetRefused(boolean value) {
		_refused = value;
	}

	/**
	 * The URL of the page the display is left on.
	 */
	public final String getCurrentUrl() {
		return _currentUrl;
	}

	/**
	 * @see #getCurrentUrl()
	 */
	public com.top_logic.layout.react.protocol.RouteRefusal setCurrentUrl(String value) {
		internalSetCurrentUrl(value);
		return this;
	}

	/** Internal setter for {@link #getCurrentUrl()} without chain call utility. */
	protected final void internalSetCurrentUrl(String value) {
		_currentUrl = value;
	}

	@Override
	public String jsonType() {
		return ROUTE_REFUSAL__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			REFUSED__PROP, 
			CURRENT_URL__PROP);
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
			case REFUSED__PROP: return isRefused();
			case CURRENT_URL__PROP: return getCurrentUrl();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case REFUSED__PROP: internalSetRefused((boolean) value); break;
			case CURRENT_URL__PROP: internalSetCurrentUrl((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.layout.react.protocol.RouteRefusal readRouteRefusal(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.protocol.RouteRefusal result = new com.top_logic.layout.react.protocol.RouteRefusal();
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
		out.name(REFUSED__PROP);
		out.value(isRefused());
		out.name(CURRENT_URL__PROP);
		out.value(getCurrentUrl());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case REFUSED__PROP: setRefused(in.nextBoolean()); break;
			case CURRENT_URL__PROP: setCurrentUrl(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
