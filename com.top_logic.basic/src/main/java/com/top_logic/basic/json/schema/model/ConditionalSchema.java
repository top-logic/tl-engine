package com.top_logic.basic.json.schema.model;

/**
 * Conditional if/then/else schema.
 *
 * <p>
 * If instance validates against 'if', 'then' is applied, otherwise 'else'.
 * </p>
 */
public class ConditionalSchema extends Schema {

	/**
	 * Creates a {@link com.top_logic.basic.json.schema.model.ConditionalSchema} instance.
	 */
	public static com.top_logic.basic.json.schema.model.ConditionalSchema create() {
		return new com.top_logic.basic.json.schema.model.ConditionalSchema();
	}

	/** Identifier for the {@link com.top_logic.basic.json.schema.model.ConditionalSchema} type in JSON format. */
	public static final String CONDITIONAL_SCHEMA__TYPE = "ConditionalSchema";

	/** @see #getCondition() */
	public static final String CONDITION__PROP = "if";

	/** @see #getThenSchema() */
	public static final String THEN_SCHEMA__PROP = "then";

	/** @see #getElseSchema() */
	public static final String ELSE_SCHEMA__PROP = "else";

	private com.top_logic.basic.json.schema.model.Schema _condition = null;

	private com.top_logic.basic.json.schema.model.Schema _thenSchema = null;

	private com.top_logic.basic.json.schema.model.Schema _elseSchema = null;

	/**
	 * Creates a {@link ConditionalSchema} instance.
	 *
	 * @see com.top_logic.basic.json.schema.model.ConditionalSchema#create()
	 */
	protected ConditionalSchema() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.CONDITIONAL_SCHEMA;
	}

	/**
	 * Condition schema.
	 */
	public final com.top_logic.basic.json.schema.model.Schema getCondition() {
		return _condition;
	}

	/**
	 * @see #getCondition()
	 */
	public com.top_logic.basic.json.schema.model.ConditionalSchema setCondition(com.top_logic.basic.json.schema.model.Schema value) {
		internalSetCondition(value);
		return this;
	}

	/** Internal setter for {@link #getCondition()} without chain call utility. */
	protected final void internalSetCondition(com.top_logic.basic.json.schema.model.Schema value) {
		_condition = value;
	}

	/**
	 * Checks, whether {@link #getCondition()} has a value.
	 */
	public final boolean hasCondition() {
		return _condition != null;
	}

	/**
	 * Schema applied when 'if' validates successfully.
	 */
	public final com.top_logic.basic.json.schema.model.Schema getThenSchema() {
		return _thenSchema;
	}

	/**
	 * @see #getThenSchema()
	 */
	public com.top_logic.basic.json.schema.model.ConditionalSchema setThenSchema(com.top_logic.basic.json.schema.model.Schema value) {
		internalSetThenSchema(value);
		return this;
	}

	/** Internal setter for {@link #getThenSchema()} without chain call utility. */
	protected final void internalSetThenSchema(com.top_logic.basic.json.schema.model.Schema value) {
		_thenSchema = value;
	}

	/**
	 * Checks, whether {@link #getThenSchema()} has a value.
	 */
	public final boolean hasThenSchema() {
		return _thenSchema != null;
	}

	/**
	 * Schema applied when 'if' validation fails.
	 */
	public final com.top_logic.basic.json.schema.model.Schema getElseSchema() {
		return _elseSchema;
	}

	/**
	 * @see #getElseSchema()
	 */
	public com.top_logic.basic.json.schema.model.ConditionalSchema setElseSchema(com.top_logic.basic.json.schema.model.Schema value) {
		internalSetElseSchema(value);
		return this;
	}

	/** Internal setter for {@link #getElseSchema()} without chain call utility. */
	protected final void internalSetElseSchema(com.top_logic.basic.json.schema.model.Schema value) {
		_elseSchema = value;
	}

	/**
	 * Checks, whether {@link #getElseSchema()} has a value.
	 */
	public final boolean hasElseSchema() {
		return _elseSchema != null;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ConditionalSchema setId(String value) {
		internalSetId(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ConditionalSchema setAnchor(String value) {
		internalSetAnchor(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ConditionalSchema setDynamicAnchor(String value) {
		internalSetDynamicAnchor(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ConditionalSchema setComment(String value) {
		internalSetComment(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ConditionalSchema setDefinitions(java.util.Map<String, com.top_logic.basic.json.schema.model.Schema> value) {
		internalSetDefinitions(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ConditionalSchema putDefinition(String key, com.top_logic.basic.json.schema.model.Schema value) {
		internalPutDefinition(key, value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ConditionalSchema setTitle(String value) {
		internalSetTitle(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ConditionalSchema setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ConditionalSchema setDefaultValue(String value) {
		internalSetDefaultValue(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ConditionalSchema setDeprecated(boolean value) {
		internalSetDeprecated(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ConditionalSchema setReadOnly(boolean value) {
		internalSetReadOnly(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ConditionalSchema setWriteOnly(boolean value) {
		internalSetWriteOnly(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ConditionalSchema setExamples(java.util.List<? extends String> value) {
		internalSetExamples(value);
		return this;
	}

	@Override
	public com.top_logic.basic.json.schema.model.ConditionalSchema addExample(String value) {
		internalAddExample(value);
		return this;
	}

	@Override
	public String jsonType() {
		return CONDITIONAL_SCHEMA__TYPE;
	}

	@SuppressWarnings("hiding")
	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			CONDITION__PROP, 
			THEN_SCHEMA__PROP, 
			ELSE_SCHEMA__PROP);
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
			case CONDITION__PROP: return getCondition();
			case THEN_SCHEMA__PROP: return getThenSchema();
			case ELSE_SCHEMA__PROP: return getElseSchema();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case CONDITION__PROP: internalSetCondition((com.top_logic.basic.json.schema.model.Schema) value); break;
			case THEN_SCHEMA__PROP: internalSetThenSchema((com.top_logic.basic.json.schema.model.Schema) value); break;
			case ELSE_SCHEMA__PROP: internalSetElseSchema((com.top_logic.basic.json.schema.model.Schema) value); break;
			default: super.set(field, value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.basic.json.schema.model.ConditionalSchema readConditionalSchema(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.basic.json.schema.model.ConditionalSchema result = new com.top_logic.basic.json.schema.model.ConditionalSchema();
		result.readContent(in);
		return result;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		if (hasCondition()) {
			out.name(CONDITION__PROP);
			getCondition().writeTo(out);
		}
		if (hasThenSchema()) {
			out.name(THEN_SCHEMA__PROP);
			getThenSchema().writeTo(out);
		}
		if (hasElseSchema()) {
			out.name(ELSE_SCHEMA__PROP);
			getElseSchema().writeTo(out);
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case CONDITION__PROP: setCondition(com.top_logic.basic.json.schema.model.Schema.readSchema(in)); break;
			case THEN_SCHEMA__PROP: setThenSchema(com.top_logic.basic.json.schema.model.Schema.readSchema(in)); break;
			case ELSE_SCHEMA__PROP: setElseSchema(com.top_logic.basic.json.schema.model.Schema.readSchema(in)); break;
			default: super.readField(in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.basic.json.schema.model.Schema.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
