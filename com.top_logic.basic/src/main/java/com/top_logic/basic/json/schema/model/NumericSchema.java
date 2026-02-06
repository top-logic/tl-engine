package com.top_logic.basic.json.schema.model;

/**
 * Schema for numeric validation (number and integer types).
 *
 * <p>
 * Validates numeric instances with range and multiple-of constraints.
 * </p>
 */
public class NumericSchema extends Schema {

	/**
	 * Creates a {@link com.top_logic.basic.json.schema.model.NumericSchema} instance.
	 */
	public static com.top_logic.basic.json.schema.model.NumericSchema create() {
		return new com.top_logic.basic.json.schema.model.NumericSchema();
	}

	/** Identifier for the {@link com.top_logic.basic.json.schema.model.NumericSchema} type in JSON format. */
	public static final String NUMERIC_SCHEMA__TYPE = "NumericSchema";

	/** @see #getMultipleOf() */
	public static final String MULTIPLE_OF__PROP = "multipleOf";

	/** @see #getMaximum() */
	public static final String MAXIMUM__PROP = "maximum";

	/** @see #getExclusiveMaximum() */
	public static final String EXCLUSIVE_MAXIMUM__PROP = "exclusiveMaximum";

	/** @see #getMinimum() */
	public static final String MINIMUM__PROP = "minimum";

	/** @see #getExclusiveMinimum() */
	public static final String EXCLUSIVE_MINIMUM__PROP = "exclusiveMinimum";

	/** @see #isIntegerOnly() */
	public static final String INTEGER_ONLY__PROP = "integerOnly";

	private Double _multipleOf = null;

	private Double _maximum = null;

	private Double _exclusiveMaximum = null;

	private Double _minimum = null;

	private Double _exclusiveMinimum = null;

	private boolean _integerOnly = false;

	/**
	 * Creates a {@link NumericSchema} instance.
	 *
	 * @see com.top_logic.basic.json.schema.model.NumericSchema#create()
	 */
	protected NumericSchema() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.NUMERIC_SCHEMA;
	}

	/**
	 * Multiple-of constraint.
	 *
	 * <p>
	 * Valid only if division by this value results in an integer.
	 * </p>
	 */
	public final Double getMultipleOf() {
		return _multipleOf;
	}

	/**
	 * @see #getMultipleOf()
	 */
	public com.top_logic.basic.json.schema.model.NumericSchema setMultipleOf(Double value) {
		internalSetMultipleOf(value);
		return this;
	}

	/** Internal setter for {@link #getMultipleOf()} without chain call utility. */
	protected final void internalSetMultipleOf(Double value) {
		_multipleOf = value;
	}

	/**
	 * Checks, whether {@link #getMultipleOf()} has a value.
	 */
	public final boolean hasMultipleOf() {
		return _multipleOf != null;
	}

	/**
	 * Maximum value (inclusive).
	 */
	public final Double getMaximum() {
		return _maximum;
	}

	/**
	 * @see #getMaximum()
	 */
	public com.top_logic.basic.json.schema.model.NumericSchema setMaximum(Double value) {
		internalSetMaximum(value);
		return this;
	}

	/** Internal setter for {@link #getMaximum()} without chain call utility. */
	protected final void internalSetMaximum(Double value) {
		_maximum = value;
	}

	/**
	 * Checks, whether {@link #getMaximum()} has a value.
	 */
	public final boolean hasMaximum() {
		return _maximum != null;
	}

	/**
	 * Exclusive maximum value (strictly less than).
	 */
	public final Double getExclusiveMaximum() {
		return _exclusiveMaximum;
	}

	/**
	 * @see #getExclusiveMaximum()
	 */
	public com.top_logic.basic.json.schema.model.NumericSchema setExclusiveMaximum(Double value) {
		internalSetExclusiveMaximum(value);
		return this;
	}

	/** Internal setter for {@link #getExclusiveMaximum()} without chain call utility. */
	protected final void internalSetExclusiveMaximum(Double value) {
		_exclusiveMaximum = value;
	}

	/**
	 * Checks, whether {@link #getExclusiveMaximum()} has a value.
	 */
	public final boolean hasExclusiveMaximum() {
		return _exclusiveMaximum != null;
	}

	/**
	 * Minimum value (inclusive).
	 */
	public final Double getMinimum() {
		return _minimum;
	}

	/**
	 * @see #getMinimum()
	 */
	public com.top_logic.basic.json.schema.model.NumericSchema setMinimum(Double value) {
		internalSetMinimum(value);
		return this;
	}

	/** Internal setter for {@link #getMinimum()} without chain call utility. */
	protected final void internalSetMinimum(Double value) {
		_minimum = value;
	}

	/**
	 * Checks, whether {@link #getMinimum()} has a value.
	 */
	public final boolean hasMinimum() {
		return _minimum != null;
	}

	/**
	 * Exclusive minimum value (strictly greater than).
	 */
	public final Double getExclusiveMinimum() {
		return _exclusiveMinimum;
	}

	/**
	 * @see #getExclusiveMinimum()
	 */
	public com.top_logic.basic.json.schema.model.NumericSchema setExclusiveMinimum(Double value) {
		internalSetExclusiveMinimum(value);
		return this;
	}

	/** Internal setter for {@link #getExclusiveMinimum()} without chain call utility. */
	protected final void internalSetExclusiveMinimum(Double value) {
		_exclusiveMinimum = value;
	}

	/**
	 * Checks, whether {@link #getExclusiveMinimum()} has a value.
	 */
	public final boolean hasExclusiveMinimum() {
		return _exclusiveMinimum != null;
	}

	/**
	 * Whether this is an integer schema (vs. number).
	 */
	public final boolean isIntegerOnly() {
		return _integerOnly;
	}

	/**
	 * @see #isIntegerOnly()
	 */
	public com.top_logic.basic.json.schema.model.NumericSchema setIntegerOnly(boolean value) {
		internalSetIntegerOnly(value);
		return this;
	}

	/** Internal setter for {@link #isIntegerOnly()} without chain call utility. */
	protected final void internalSetIntegerOnly(boolean value) {
		_integerOnly = value;
	}

	@Override
	public com.top_logic.basic.json.schema.model.NumericSchema setId(String value) {
		internalSetId(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.NumericSchema setAnchor(String value) {
		internalSetAnchor(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.NumericSchema setDynamicAnchor(String value) {
		internalSetDynamicAnchor(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.NumericSchema setComment(String value) {
		internalSetComment(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.NumericSchema setDefinitions(java.util.Map<String, com.top_logic.basic.json.schema.model.Schema> value) {
		internalSetDefinitions(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.NumericSchema putDefinition(String key, com.top_logic.basic.json.schema.model.Schema value) {
		internalPutDefinition(key, value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.NumericSchema setTitle(String value) {
		internalSetTitle(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.NumericSchema setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.NumericSchema setDefaultValue(String value) {
		internalSetDefaultValue(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.NumericSchema setDeprecated(boolean value) {
		internalSetDeprecated(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.NumericSchema setReadOnly(boolean value) {
		internalSetReadOnly(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.NumericSchema setWriteOnly(boolean value) {
		internalSetWriteOnly(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.NumericSchema setExamples(java.util.List<? extends String> value) {
		internalSetExamples(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.NumericSchema addExample(String value) {
		internalAddExample(value);
		return this;
	}

	@Override
	public String jsonType() {
		return NUMERIC_SCHEMA__TYPE;
	}

	@SuppressWarnings("hiding")
	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			MULTIPLE_OF__PROP, 
			MAXIMUM__PROP, 
			EXCLUSIVE_MAXIMUM__PROP, 
			MINIMUM__PROP, 
			EXCLUSIVE_MINIMUM__PROP, 
			INTEGER_ONLY__PROP);
		java.util.List<String> tmp = new java.util.ArrayList<>();
		tmp.addAll(com.top_logic.basic.json.schema.model.Schema.PROPERTIES);
		tmp.addAll(local);
		PROPERTIES = java.util.Collections.unmodifiableList(tmp);
	}

	@SuppressWarnings("hiding")
	static final java.util.Set<String> TRANSIENT_PROPERTIES;
	static {
		java.util.HashSet<String> tmp = new java.util.HashSet<>();
		tmp.addAll(com.top_logic.basic.json.schema.model.Schema.TRANSIENT_PROPERTIES);
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
			case MULTIPLE_OF__PROP: return getMultipleOf();
			case MAXIMUM__PROP: return getMaximum();
			case EXCLUSIVE_MAXIMUM__PROP: return getExclusiveMaximum();
			case MINIMUM__PROP: return getMinimum();
			case EXCLUSIVE_MINIMUM__PROP: return getExclusiveMinimum();
			case INTEGER_ONLY__PROP: return isIntegerOnly();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case MULTIPLE_OF__PROP: internalSetMultipleOf((Double) value); break;
			case MAXIMUM__PROP: internalSetMaximum((Double) value); break;
			case EXCLUSIVE_MAXIMUM__PROP: internalSetExclusiveMaximum((Double) value); break;
			case MINIMUM__PROP: internalSetMinimum((Double) value); break;
			case EXCLUSIVE_MINIMUM__PROP: internalSetExclusiveMinimum((Double) value); break;
			case INTEGER_ONLY__PROP: internalSetIntegerOnly((boolean) value); break;
			default: super.set(field, value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.basic.json.schema.model.NumericSchema readNumericSchema(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.basic.json.schema.model.NumericSchema result = new com.top_logic.basic.json.schema.model.NumericSchema();
		result.readContent(in);
		return result;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		if (hasMultipleOf()) {
			out.name(MULTIPLE_OF__PROP);
			out.value(getMultipleOf());
		}
		if (hasMaximum()) {
			out.name(MAXIMUM__PROP);
			out.value(getMaximum());
		}
		if (hasExclusiveMaximum()) {
			out.name(EXCLUSIVE_MAXIMUM__PROP);
			out.value(getExclusiveMaximum());
		}
		if (hasMinimum()) {
			out.name(MINIMUM__PROP);
			out.value(getMinimum());
		}
		if (hasExclusiveMinimum()) {
			out.name(EXCLUSIVE_MINIMUM__PROP);
			out.value(getExclusiveMinimum());
		}
		out.name(INTEGER_ONLY__PROP);
		out.value(isIntegerOnly());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case MULTIPLE_OF__PROP: setMultipleOf(in.nextDouble()); break;
			case MAXIMUM__PROP: setMaximum(in.nextDouble()); break;
			case EXCLUSIVE_MAXIMUM__PROP: setExclusiveMaximum(in.nextDouble()); break;
			case MINIMUM__PROP: setMinimum(in.nextDouble()); break;
			case EXCLUSIVE_MINIMUM__PROP: setExclusiveMinimum(in.nextDouble()); break;
			case INTEGER_ONLY__PROP: setIntegerOnly(in.nextBoolean()); break;
			default: super.readField(in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.basic.json.schema.model.Schema.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
