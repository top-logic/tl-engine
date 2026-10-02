package com.top_logic.tl.ai.core.tools.model.messages;

/**
 * One object of a listing: what it is called, what it is, how to name it again, and the
 * attribute values the caller asked for.
 */
public class InstanceEntry extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.model.messages.InstanceEntry} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.model.messages.InstanceEntry create() {
		return new com.top_logic.tl.ai.core.tools.model.messages.InstanceEntry();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.model.messages.InstanceEntry} type in JSON format. */
	public static final String INSTANCE_ENTRY__TYPE = "InstanceEntry";

	/** @see #getId() */
	public static final String ID__PROP = "id";

	/** @see #getLabel() */
	public static final String LABEL__PROP = "label";

	/** @see #getType() */
	public static final String TYPE__PROP = "type";

	/** @see #getAttributes() */
	public static final String ATTRIBUTES__PROP = "attributes";

	private String _id = "";

	private String _label = "";

	private String _type = "";

	private final java.util.Map<String, String> _attributes = new java.util.LinkedHashMap<>();

	/**
	 * Creates a {@link InstanceEntry} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.model.messages.InstanceEntry#create()
	 */
	protected InstanceEntry() {
		super();
	}

	/**
	 * The identifier a later call names the object by (`getInstance`, `present`, cite).
	 */
	public final String getId() {
		return _id;
	}

	/**
	 * @see #getId()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.InstanceEntry setId(String value) {
		internalSetId(value);
		return this;
	}

	/** Internal setter for {@link #getId()} without chain call utility. */
	protected final void internalSetId(String value) {
		_id = value;
	}

	/**
	 * What the object is called.
	 */
	public final String getLabel() {
		return _label;
	}

	/**
	 * @see #getLabel()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.InstanceEntry setLabel(String value) {
		internalSetLabel(value);
		return this;
	}

	/** Internal setter for {@link #getLabel()} without chain call utility. */
	protected final void internalSetLabel(String value) {
		_label = value;
	}

	/**
	 * The qualified name of the object's own type, which may be a subtype of the listed one.
	 */
	public final String getType() {
		return _type;
	}

	/**
	 * @see #getType()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.InstanceEntry setType(String value) {
		internalSetType(value);
		return this;
	}

	/** Internal setter for {@link #getType()} without chain call utility. */
	protected final void internalSetType(String value) {
		_type = value;
	}

	/**
	 * The requested attributes the acting account may read, by name, each serialized as JSON the way
	 * {@code InstanceResponse.attributes} is.
	 */
	public final java.util.Map<String, String> getAttributes() {
		return _attributes;
	}

	/**
	 * @see #getAttributes()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.InstanceEntry setAttributes(java.util.Map<String, String> value) {
		internalSetAttributes(value);
		return this;
	}

	/** Internal setter for {@link #getAttributes()} without chain call utility. */
	protected final void internalSetAttributes(java.util.Map<String, String> value) {
		if (value == null) throw new IllegalArgumentException("Property 'attributes' cannot be null.");
		_attributes.clear();
		_attributes.putAll(value);
	}

	/**
	 * Adds a key value pair to the {@link #getAttributes()} map.
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.InstanceEntry putAttribute(String key, String value) {
		internalPutAttribute(key, value);
		return this;
	}

	/** Implementation of {@link #putAttribute(String, String)} without chain call utility. */
	protected final void  internalPutAttribute(String key, String value) {
		if (_attributes.containsKey(key)) {
			throw new IllegalArgumentException("Property 'attributes' already contains a value for key '" + key + "'.");
		}
		_attributes.put(key, value);
	}

	/**
	 * Removes a key from the {@link #getAttributes()} map.
	 */
	public final void removeAttribute(String key) {
		_attributes.remove(key);
	}

	@Override
	public String jsonType() {
		return INSTANCE_ENTRY__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			ID__PROP, 
			LABEL__PROP, 
			TYPE__PROP, 
			ATTRIBUTES__PROP);
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
			case ID__PROP: return getId();
			case LABEL__PROP: return getLabel();
			case TYPE__PROP: return getType();
			case ATTRIBUTES__PROP: return getAttributes();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ID__PROP: internalSetId((String) value); break;
			case LABEL__PROP: internalSetLabel((String) value); break;
			case TYPE__PROP: internalSetType((String) value); break;
			case ATTRIBUTES__PROP: internalSetAttributes((java.util.Map<String, String>) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.model.messages.InstanceEntry readInstanceEntry(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.model.messages.InstanceEntry result = new com.top_logic.tl.ai.core.tools.model.messages.InstanceEntry();
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
		out.name(ID__PROP);
		out.value(getId());
		out.name(LABEL__PROP);
		out.value(getLabel());
		out.name(TYPE__PROP);
		out.value(getType());
		out.name(ATTRIBUTES__PROP);
		out.beginObject();
		for (java.util.Map.Entry<String,String> entry : getAttributes().entrySet()) {
			out.name(entry.getKey());
			out.value(entry.getValue());
		}
		out.endObject();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ID__PROP: setId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case LABEL__PROP: setLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TYPE__PROP: setType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ATTRIBUTES__PROP: {
				java.util.Map<String, String> newValue = new java.util.LinkedHashMap<>();
				in.beginObject();
				while (in.hasNext()) {
					newValue.put(in.nextName(), de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endObject();
				setAttributes(newValue);
				break;
			}
			default: super.readField(in, field);
		}
	}

}
