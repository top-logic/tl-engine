package com.top_logic.tl.ai.core.tools.refusal.messages;

/**
 * The mandatory attributes of one type.
 */
public class MandatoryAttributes extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.refusal.messages.MandatoryAttributes} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.refusal.messages.MandatoryAttributes create() {
		return new com.top_logic.tl.ai.core.tools.refusal.messages.MandatoryAttributes();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.refusal.messages.MandatoryAttributes} type in JSON format. */
	public static final String MANDATORY_ATTRIBUTES__TYPE = "MandatoryAttributes";

	/** @see #getType() */
	public static final String TYPE__PROP = "type";

	/** @see #getAttributes() */
	public static final String ATTRIBUTES__PROP = "attributes";

	private String _type = "";

	private final java.util.List<String> _attributes = new java.util.ArrayList<>();

	/**
	 * Creates a {@link MandatoryAttributes} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.refusal.messages.MandatoryAttributes#create()
	 */
	protected MandatoryAttributes() {
		super();
	}

	/**
	 * The qualified name of the type.
	 */
	public final String getType() {
		return _type;
	}

	/**
	 * @see #getType()
	 */
	public com.top_logic.tl.ai.core.tools.refusal.messages.MandatoryAttributes setType(String value) {
		internalSetType(value);
		return this;
	}

	/** Internal setter for {@link #getType()} without chain call utility. */
	protected final void internalSetType(String value) {
		_type = value;
	}

	/**
	 * The names of its mandatory attributes.
	 */
	public final java.util.List<String> getAttributes() {
		return _attributes;
	}

	/**
	 * @see #getAttributes()
	 */
	public com.top_logic.tl.ai.core.tools.refusal.messages.MandatoryAttributes setAttributes(java.util.List<? extends String> value) {
		internalSetAttributes(value);
		return this;
	}

	/** Internal setter for {@link #getAttributes()} without chain call utility. */
	protected final void internalSetAttributes(java.util.List<? extends String> value) {
		_attributes.clear();
		_attributes.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getAttributes()} list.
	 */
	public com.top_logic.tl.ai.core.tools.refusal.messages.MandatoryAttributes addAttribute(String value) {
		internalAddAttribute(value);
		return this;
	}

	/** Implementation of {@link #addAttribute(String)} without chain call utility. */
	protected final void internalAddAttribute(String value) {
		_attributes.add(value);
	}

	/**
	 * Removes a value from the {@link #getAttributes()} list.
	 */
	public final void removeAttribute(String value) {
		_attributes.remove(value);
	}

	@Override
	public String jsonType() {
		return MANDATORY_ATTRIBUTES__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
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
			case TYPE__PROP: return getType();
			case ATTRIBUTES__PROP: return getAttributes();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TYPE__PROP: internalSetType((String) value); break;
			case ATTRIBUTES__PROP: internalSetAttributes(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.refusal.messages.MandatoryAttributes readMandatoryAttributes(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.refusal.messages.MandatoryAttributes result = new com.top_logic.tl.ai.core.tools.refusal.messages.MandatoryAttributes();
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
		out.name(TYPE__PROP);
		out.value(getType());
		out.name(ATTRIBUTES__PROP);
		out.beginArray();
		for (String x : getAttributes()) {
			out.value(x);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TYPE__PROP: setType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ATTRIBUTES__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setAttributes(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
