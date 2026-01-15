package com.top_logic.ai.service.jsonschema.model;

/**
 * Schema for array validation.
 *
 * <p>
 * Validates array instances with size, uniqueness, and item constraints.
 * </p>
 */
public class ArraySchema extends Schema {

	/**
	 * Creates a {@link com.top_logic.ai.service.jsonschema.model.ArraySchema} instance.
	 */
	public static com.top_logic.ai.service.jsonschema.model.ArraySchema create() {
		return new com.top_logic.ai.service.jsonschema.model.ArraySchema();
	}

	/** Identifier for the {@link com.top_logic.ai.service.jsonschema.model.ArraySchema} type in JSON format. */
	public static final String ARRAY_SCHEMA__TYPE = "ArraySchema";

	/** @see #getMaxItems() */
	public static final String MAX_ITEMS__PROP = "maxItems";

	/** @see #getMinItems() */
	public static final String MIN_ITEMS__PROP = "minItems";

	/** @see #isUniqueItems() */
	public static final String UNIQUE_ITEMS__PROP = "uniqueItems";

	/** @see #getPrefixItems() */
	public static final String PREFIX_ITEMS__PROP = "prefixItems";

	/** @see #getItems() */
	public static final String ITEMS__PROP = "items";

	/** @see #getContains() */
	public static final String CONTAINS__PROP = "contains";

	/** @see #getMinContains() */
	public static final String MIN_CONTAINS__PROP = "minContains";

	/** @see #getMaxContains() */
	public static final String MAX_CONTAINS__PROP = "maxContains";

	private int _maxItems = 0;

	private int _minItems = 0;

	private boolean _uniqueItems = false;

	private final java.util.List<com.top_logic.ai.service.jsonschema.model.Schema> _prefixItems = new java.util.ArrayList<>();

	private com.top_logic.ai.service.jsonschema.model.Schema _items = null;

	private com.top_logic.ai.service.jsonschema.model.Schema _contains = null;

	private int _minContains = 0;

	private int _maxContains = 0;

	/**
	 * Creates a {@link ArraySchema} instance.
	 *
	 * @see com.top_logic.ai.service.jsonschema.model.ArraySchema#create()
	 */
	protected ArraySchema() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.ARRAY_SCHEMA;
	}

	/**
	 * Maximum array size.
	 */
	public final int getMaxItems() {
		return _maxItems;
	}

	/**
	 * @see #getMaxItems()
	 */
	public com.top_logic.ai.service.jsonschema.model.ArraySchema setMaxItems(int value) {
		internalSetMaxItems(value);
		return this;
	}

	/** Internal setter for {@link #getMaxItems()} without chain call utility. */
	protected final void internalSetMaxItems(int value) {
		_maxItems = value;
	}

	/**
	 * Minimum array size.
	 */
	public final int getMinItems() {
		return _minItems;
	}

	/**
	 * @see #getMinItems()
	 */
	public com.top_logic.ai.service.jsonschema.model.ArraySchema setMinItems(int value) {
		internalSetMinItems(value);
		return this;
	}

	/** Internal setter for {@link #getMinItems()} without chain call utility. */
	protected final void internalSetMinItems(int value) {
		_minItems = value;
	}

	/**
	 * Unique items constraint.
	 *
	 * <p>
	 * If true, all array elements must be distinct.
	 * </p>
	 */
	public final boolean isUniqueItems() {
		return _uniqueItems;
	}

	/**
	 * @see #isUniqueItems()
	 */
	public com.top_logic.ai.service.jsonschema.model.ArraySchema setUniqueItems(boolean value) {
		internalSetUniqueItems(value);
		return this;
	}

	/** Internal setter for {@link #isUniqueItems()} without chain call utility. */
	protected final void internalSetUniqueItems(boolean value) {
		_uniqueItems = value;
	}

	/**
	 * Positional schemas for array items.
	 *
	 * <p>
	 * Applies schemas to array elements by position.
	 * </p>
	 */
	public final java.util.List<com.top_logic.ai.service.jsonschema.model.Schema> getPrefixItems() {
		return _prefixItems;
	}

	/**
	 * @see #getPrefixItems()
	 */
	public com.top_logic.ai.service.jsonschema.model.ArraySchema setPrefixItems(java.util.List<? extends com.top_logic.ai.service.jsonschema.model.Schema> value) {
		internalSetPrefixItems(value);
		return this;
	}

	/** Internal setter for {@link #getPrefixItems()} without chain call utility. */
	protected final void internalSetPrefixItems(java.util.List<? extends com.top_logic.ai.service.jsonschema.model.Schema> value) {
		if (value == null) throw new IllegalArgumentException("Property 'prefixItems' cannot be null.");
		_prefixItems.clear();
		_prefixItems.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getPrefixItems()} list.
	 */
	public com.top_logic.ai.service.jsonschema.model.ArraySchema addPrefixItem(com.top_logic.ai.service.jsonschema.model.Schema value) {
		internalAddPrefixItem(value);
		return this;
	}

	/** Implementation of {@link #addPrefixItem(com.top_logic.ai.service.jsonschema.model.Schema)} without chain call utility. */
	protected final void internalAddPrefixItem(com.top_logic.ai.service.jsonschema.model.Schema value) {
		_prefixItems.add(value);
	}

	/**
	 * Removes a value from the {@link #getPrefixItems()} list.
	 */
	public final void removePrefixItem(com.top_logic.ai.service.jsonschema.model.Schema value) {
		_prefixItems.remove(value);
	}

	/**
	 * Schema for array items.
	 *
	 * <p>
	 * For arrays without prefixItems: validates all items.
	 * For arrays with prefixItems: validates items beyond prefixItems positions.
	 * </p>
	 */
	public final com.top_logic.ai.service.jsonschema.model.Schema getItems() {
		return _items;
	}

	/**
	 * @see #getItems()
	 */
	public com.top_logic.ai.service.jsonschema.model.ArraySchema setItems(com.top_logic.ai.service.jsonschema.model.Schema value) {
		internalSetItems(value);
		return this;
	}

	/** Internal setter for {@link #getItems()} without chain call utility. */
	protected final void internalSetItems(com.top_logic.ai.service.jsonschema.model.Schema value) {
		_items = value;
	}

	/**
	 * Checks, whether {@link #getItems()} has a value.
	 */
	public final boolean hasItems() {
		return _items != null;
	}

	/**
	 * Contains constraint schema.
	 *
	 * <p>
	 * Array must contain at least one item matching this schema.
	 * </p>
	 */
	public final com.top_logic.ai.service.jsonschema.model.Schema getContains() {
		return _contains;
	}

	/**
	 * @see #getContains()
	 */
	public com.top_logic.ai.service.jsonschema.model.ArraySchema setContains(com.top_logic.ai.service.jsonschema.model.Schema value) {
		internalSetContains(value);
		return this;
	}

	/** Internal setter for {@link #getContains()} without chain call utility. */
	protected final void internalSetContains(com.top_logic.ai.service.jsonschema.model.Schema value) {
		_contains = value;
	}

	/**
	 * Checks, whether {@link #getContains()} has a value.
	 */
	public final boolean hasContains() {
		return _contains != null;
	}

	/**
	 * Minimum number of items matching contains schema.
	 */
	public final int getMinContains() {
		return _minContains;
	}

	/**
	 * @see #getMinContains()
	 */
	public com.top_logic.ai.service.jsonschema.model.ArraySchema setMinContains(int value) {
		internalSetMinContains(value);
		return this;
	}

	/** Internal setter for {@link #getMinContains()} without chain call utility. */
	protected final void internalSetMinContains(int value) {
		_minContains = value;
	}

	/**
	 * Maximum number of items matching contains schema.
	 */
	public final int getMaxContains() {
		return _maxContains;
	}

	/**
	 * @see #getMaxContains()
	 */
	public com.top_logic.ai.service.jsonschema.model.ArraySchema setMaxContains(int value) {
		internalSetMaxContains(value);
		return this;
	}

	/** Internal setter for {@link #getMaxContains()} without chain call utility. */
	protected final void internalSetMaxContains(int value) {
		_maxContains = value;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.ArraySchema setId(String value) {
		internalSetId(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.ArraySchema setAnchor(String value) {
		internalSetAnchor(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.ArraySchema setDynamicAnchor(String value) {
		internalSetDynamicAnchor(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.ArraySchema setComment(String value) {
		internalSetComment(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.ArraySchema setDefinitions(java.util.Map<String, com.top_logic.ai.service.jsonschema.model.Schema> value) {
		internalSetDefinitions(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.ArraySchema putDefinition(String key, com.top_logic.ai.service.jsonschema.model.Schema value) {
		internalPutDefinition(key, value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.ArraySchema setTitle(String value) {
		internalSetTitle(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.ArraySchema setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.ArraySchema setDefaultValue(String value) {
		internalSetDefaultValue(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.ArraySchema setDeprecated(boolean value) {
		internalSetDeprecated(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.ArraySchema setReadOnly(boolean value) {
		internalSetReadOnly(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.ArraySchema setWriteOnly(boolean value) {
		internalSetWriteOnly(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.ArraySchema setExamples(java.util.List<? extends String> value) {
		internalSetExamples(value);
		return this;
	}

	@Override
	public com.top_logic.ai.service.jsonschema.model.ArraySchema addExample(String value) {
		internalAddExample(value);
		return this;
	}

	@Override
	public String jsonType() {
		return ARRAY_SCHEMA__TYPE;
	}

	@SuppressWarnings("hiding")
	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			MAX_ITEMS__PROP, 
			MIN_ITEMS__PROP, 
			UNIQUE_ITEMS__PROP, 
			PREFIX_ITEMS__PROP, 
			ITEMS__PROP, 
			CONTAINS__PROP, 
			MIN_CONTAINS__PROP, 
			MAX_CONTAINS__PROP);
		java.util.List<String> tmp = new java.util.ArrayList<>();
		tmp.addAll(com.top_logic.ai.service.jsonschema.model.Schema.PROPERTIES);
		tmp.addAll(local);
		PROPERTIES = java.util.Collections.unmodifiableList(tmp);
	}

	@SuppressWarnings("hiding")
	static final java.util.Set<String> TRANSIENT_PROPERTIES;
	static {
		java.util.HashSet<String> tmp = new java.util.HashSet<>();
		tmp.addAll(com.top_logic.ai.service.jsonschema.model.Schema.TRANSIENT_PROPERTIES);
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
			case MAX_ITEMS__PROP: return getMaxItems();
			case MIN_ITEMS__PROP: return getMinItems();
			case UNIQUE_ITEMS__PROP: return isUniqueItems();
			case PREFIX_ITEMS__PROP: return getPrefixItems();
			case ITEMS__PROP: return getItems();
			case CONTAINS__PROP: return getContains();
			case MIN_CONTAINS__PROP: return getMinContains();
			case MAX_CONTAINS__PROP: return getMaxContains();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case MAX_ITEMS__PROP: internalSetMaxItems((int) value); break;
			case MIN_ITEMS__PROP: internalSetMinItems((int) value); break;
			case UNIQUE_ITEMS__PROP: internalSetUniqueItems((boolean) value); break;
			case PREFIX_ITEMS__PROP: internalSetPrefixItems(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.ai.service.jsonschema.model.Schema.class, value)); break;
			case ITEMS__PROP: internalSetItems((com.top_logic.ai.service.jsonschema.model.Schema) value); break;
			case CONTAINS__PROP: internalSetContains((com.top_logic.ai.service.jsonschema.model.Schema) value); break;
			case MIN_CONTAINS__PROP: internalSetMinContains((int) value); break;
			case MAX_CONTAINS__PROP: internalSetMaxContains((int) value); break;
			default: super.set(field, value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.service.jsonschema.model.ArraySchema readArraySchema(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.service.jsonschema.model.ArraySchema result = new com.top_logic.ai.service.jsonschema.model.ArraySchema();
		result.readContent(in);
		return result;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(MAX_ITEMS__PROP);
		out.value(getMaxItems());
		out.name(MIN_ITEMS__PROP);
		out.value(getMinItems());
		out.name(UNIQUE_ITEMS__PROP);
		out.value(isUniqueItems());
		out.name(PREFIX_ITEMS__PROP);
		out.beginArray();
		for (com.top_logic.ai.service.jsonschema.model.Schema x : getPrefixItems()) {
			x.writeTo(out);
		}
		out.endArray();
		if (hasItems()) {
			out.name(ITEMS__PROP);
			getItems().writeTo(out);
		}
		if (hasContains()) {
			out.name(CONTAINS__PROP);
			getContains().writeTo(out);
		}
		out.name(MIN_CONTAINS__PROP);
		out.value(getMinContains());
		out.name(MAX_CONTAINS__PROP);
		out.value(getMaxContains());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case MAX_ITEMS__PROP: setMaxItems(in.nextInt()); break;
			case MIN_ITEMS__PROP: setMinItems(in.nextInt()); break;
			case UNIQUE_ITEMS__PROP: setUniqueItems(in.nextBoolean()); break;
			case PREFIX_ITEMS__PROP: {
				java.util.List<com.top_logic.ai.service.jsonschema.model.Schema> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.ai.service.jsonschema.model.Schema.readSchema(in));
				}
				in.endArray();
				setPrefixItems(newValue);
			}
			break;
			case ITEMS__PROP: setItems(com.top_logic.ai.service.jsonschema.model.Schema.readSchema(in)); break;
			case CONTAINS__PROP: setContains(com.top_logic.ai.service.jsonschema.model.Schema.readSchema(in)); break;
			case MIN_CONTAINS__PROP: setMinContains(in.nextInt()); break;
			case MAX_CONTAINS__PROP: setMaxContains(in.nextInt()); break;
			default: super.readField(in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.ai.service.jsonschema.model.Schema.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
