package com.top_logic.tl.ai.core.tools.model.messages;

/**
 * List of types response.
 */
public class TypeListResponse extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.model.messages.TypeListResponse} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.model.messages.TypeListResponse create() {
		return new com.top_logic.tl.ai.core.tools.model.messages.TypeListResponse();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.model.messages.TypeListResponse} type in JSON format. */
	public static final String TYPE_LIST_RESPONSE__TYPE = "TypeListResponse";

	/** @see #getTypes() */
	public static final String TYPES__PROP = "types";

	private final java.util.List<com.top_logic.tl.ai.core.tools.model.messages.TypeInfo> _types = new java.util.ArrayList<>();

	/**
	 * Creates a {@link TypeListResponse} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.model.messages.TypeListResponse#create()
	 */
	protected TypeListResponse() {
		super();
	}

	/**
	 * Array of type information.
	 */
	public final java.util.List<com.top_logic.tl.ai.core.tools.model.messages.TypeInfo> getTypes() {
		return _types;
	}

	/**
	 * @see #getTypes()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.TypeListResponse setTypes(java.util.List<? extends com.top_logic.tl.ai.core.tools.model.messages.TypeInfo> value) {
		internalSetTypes(value);
		return this;
	}

	/** Internal setter for {@link #getTypes()} without chain call utility. */
	protected final void internalSetTypes(java.util.List<? extends com.top_logic.tl.ai.core.tools.model.messages.TypeInfo> value) {
		if (value == null) throw new IllegalArgumentException("Property 'types' cannot be null.");
		_types.clear();
		_types.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getTypes()} list.
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.TypeListResponse addType(com.top_logic.tl.ai.core.tools.model.messages.TypeInfo value) {
		internalAddType(value);
		return this;
	}

	/** Implementation of {@link #addType(com.top_logic.tl.ai.core.tools.model.messages.TypeInfo)} without chain call utility. */
	protected final void internalAddType(com.top_logic.tl.ai.core.tools.model.messages.TypeInfo value) {
		_types.add(value);
	}

	/**
	 * Removes a value from the {@link #getTypes()} list.
	 */
	public final void removeType(com.top_logic.tl.ai.core.tools.model.messages.TypeInfo value) {
		_types.remove(value);
	}

	@Override
	public String jsonType() {
		return TYPE_LIST_RESPONSE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TYPES__PROP);
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
			case TYPES__PROP: return getTypes();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TYPES__PROP: internalSetTypes(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.tl.ai.core.tools.model.messages.TypeInfo.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.model.messages.TypeListResponse readTypeListResponse(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.model.messages.TypeListResponse result = new com.top_logic.tl.ai.core.tools.model.messages.TypeListResponse();
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
		out.name(TYPES__PROP);
		out.beginArray();
		for (com.top_logic.tl.ai.core.tools.model.messages.TypeInfo x : getTypes()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TYPES__PROP: {
				java.util.List<com.top_logic.tl.ai.core.tools.model.messages.TypeInfo> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.tl.ai.core.tools.model.messages.TypeInfo.readTypeInfo(in));
				}
				in.endArray();
				setTypes(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
