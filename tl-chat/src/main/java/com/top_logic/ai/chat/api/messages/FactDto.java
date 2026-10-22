package com.top_logic.ai.chat.api.messages;

/**
 * One fact a page states about the situation a message is sent in.
 */
public class FactDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.FactDto} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.FactDto create() {
		return new com.top_logic.ai.chat.api.messages.FactDto();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.FactDto} type in JSON format. */
	public static final String FACT_DTO__TYPE = "FactDto";

	/** @see #getName() */
	public static final String NAME__PROP = "name";

	/** @see #getValue() */
	public static final String VALUE__PROP = "value";

	/** @see #isStable() */
	public static final String STABLE__PROP = "stable";

	private String _name = "";

	private String _value = "";

	private boolean _stable = false;

	/**
	 * Creates a {@link FactDto} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.FactDto#create()
	 */
	protected FactDto() {
		super();
	}

	/**
	 * What the fact is called in front of the model, e.g. "Project".
	 */
	public final String getName() {
		return _name;
	}

	/**
	 * @see #getName()
	 */
	public com.top_logic.ai.chat.api.messages.FactDto setName(String value) {
		internalSetName(value);
		return this;
	}

	/** Internal setter for {@link #getName()} without chain call utility. */
	protected final void internalSetName(String value) {
		_name = value;
	}

	/**
	 * The rendered value.
	 */
	public final String getValue() {
		return _value;
	}

	/**
	 * @see #getValue()
	 */
	public com.top_logic.ai.chat.api.messages.FactDto setValue(String value) {
		internalSetValue(value);
		return this;
	}

	/** Internal setter for {@link #getValue()} without chain call utility. */
	protected final void internalSetValue(String value) {
		_value = value;
	}

	/**
	 * Whether the value holds for the whole conversation.
	 *
	 * <p>
	 * Only a stable fact reaches the system prompt, which has to stay byte-identical across the turns
	 * of one conversation or the provider's prompt cache misses on every one of them. A fact that is
	 * not stable goes into the per-turn context block, which travels in the turn itself, beside the
	 * user's text.
	 * </p>
	 */
	public final boolean isStable() {
		return _stable;
	}

	/**
	 * @see #isStable()
	 */
	public com.top_logic.ai.chat.api.messages.FactDto setStable(boolean value) {
		internalSetStable(value);
		return this;
	}

	/** Internal setter for {@link #isStable()} without chain call utility. */
	protected final void internalSetStable(boolean value) {
		_stable = value;
	}

	@Override
	public String jsonType() {
		return FACT_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			NAME__PROP, 
			VALUE__PROP, 
			STABLE__PROP);
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
			case NAME__PROP: return getName();
			case VALUE__PROP: return getValue();
			case STABLE__PROP: return isStable();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case NAME__PROP: internalSetName((String) value); break;
			case VALUE__PROP: internalSetValue((String) value); break;
			case STABLE__PROP: internalSetStable((boolean) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.FactDto readFactDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.FactDto result = new com.top_logic.ai.chat.api.messages.FactDto();
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
		out.name(NAME__PROP);
		out.value(getName());
		out.name(VALUE__PROP);
		out.value(getValue());
		out.name(STABLE__PROP);
		out.value(isStable());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case NAME__PROP: setName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case VALUE__PROP: setValue(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case STABLE__PROP: setStable(in.nextBoolean()); break;
			default: super.readField(in, field);
		}
	}

}
