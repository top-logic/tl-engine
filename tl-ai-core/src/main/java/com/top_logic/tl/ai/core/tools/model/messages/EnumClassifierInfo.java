package com.top_logic.tl.ai.core.tools.model.messages;

/**
 * Classifier info used in enumeration responses.
 */
public class EnumClassifierInfo extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.model.messages.EnumClassifierInfo} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.model.messages.EnumClassifierInfo create() {
		return new com.top_logic.tl.ai.core.tools.model.messages.EnumClassifierInfo();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.model.messages.EnumClassifierInfo} type in JSON format. */
	public static final String ENUM_CLASSIFIER_INFO__TYPE = "EnumClassifierInfo";

	/** @see #getName() */
	public static final String NAME__PROP = "name";

	/** @see #isDefault() */
	public static final String DEFAULT__PROP = "default";

	private String _name = "";

	private boolean _default = false;

	/**
	 * Creates a {@link EnumClassifierInfo} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.model.messages.EnumClassifierInfo#create()
	 */
	protected EnumClassifierInfo() {
		super();
	}

	/**
	 * Classifier name.
	 */
	public final String getName() {
		return _name;
	}

	/**
	 * @see #getName()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.EnumClassifierInfo setName(String value) {
		internalSetName(value);
		return this;
	}

	/** Internal setter for {@link #getName()} without chain call utility. */
	protected final void internalSetName(String value) {
		_name = value;
	}

	/**
	 * Whether this is the default classifier.
	 */
	public final boolean isDefault() {
		return _default;
	}

	/**
	 * @see #isDefault()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.EnumClassifierInfo setDefault(boolean value) {
		internalSetDefault(value);
		return this;
	}

	/** Internal setter for {@link #isDefault()} without chain call utility. */
	protected final void internalSetDefault(boolean value) {
		_default = value;
	}

	@Override
	public String jsonType() {
		return ENUM_CLASSIFIER_INFO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			NAME__PROP, 
			DEFAULT__PROP);
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
			case DEFAULT__PROP: return isDefault();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case NAME__PROP: internalSetName((String) value); break;
			case DEFAULT__PROP: internalSetDefault((boolean) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.model.messages.EnumClassifierInfo readEnumClassifierInfo(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.model.messages.EnumClassifierInfo result = new com.top_logic.tl.ai.core.tools.model.messages.EnumClassifierInfo();
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
		out.name(DEFAULT__PROP);
		out.value(isDefault());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case NAME__PROP: setName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DEFAULT__PROP: setDefault(in.nextBoolean()); break;
			default: super.readField(in, field);
		}
	}

}
