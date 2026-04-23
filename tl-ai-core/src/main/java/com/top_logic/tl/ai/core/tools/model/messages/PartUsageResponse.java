package com.top_logic.tl.ai.core.tools.model.messages;

/**
 * Part usage analysis response.
 */
public class PartUsageResponse extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.model.messages.PartUsageResponse} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.model.messages.PartUsageResponse create() {
		return new com.top_logic.tl.ai.core.tools.model.messages.PartUsageResponse();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.model.messages.PartUsageResponse} type in JSON format. */
	public static final String PART_USAGE_RESPONSE__TYPE = "PartUsageResponse";

	/** @see #getOwnerType() */
	public static final String OWNER_TYPE__PROP = "ownerType";

	/** @see #getPartName() */
	public static final String PART_NAME__PROP = "partName";

	/** @see #getPartKind() */
	public static final String PART_KIND__PROP = "partKind";

	/** @see #getDefinedIn() */
	public static final String DEFINED_IN__PROP = "definedIn";

	/** @see #getInheritedBy() */
	public static final String INHERITED_BY__PROP = "inheritedBy";

	private String _ownerType = "";

	private String _partName = "";

	private String _partKind = "";

	private String _definedIn = "";

	private final java.util.List<String> _inheritedBy = new java.util.ArrayList<>();

	/**
	 * Creates a {@link PartUsageResponse} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.model.messages.PartUsageResponse#create()
	 */
	protected PartUsageResponse() {
		super();
	}

	/**
	 * Qualified name of the owner type.
	 */
	public final String getOwnerType() {
		return _ownerType;
	}

	/**
	 * @see #getOwnerType()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.PartUsageResponse setOwnerType(String value) {
		internalSetOwnerType(value);
		return this;
	}

	/** Internal setter for {@link #getOwnerType()} without chain call utility. */
	protected final void internalSetOwnerType(String value) {
		_ownerType = value;
	}

	/**
	 * Part name.
	 */
	public final String getPartName() {
		return _partName;
	}

	/**
	 * @see #getPartName()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.PartUsageResponse setPartName(String value) {
		internalSetPartName(value);
		return this;
	}

	/** Internal setter for {@link #getPartName()} without chain call utility. */
	protected final void internalSetPartName(String value) {
		_partName = value;
	}

	/**
	 * Part kind (property or reference).
	 */
	public final String getPartKind() {
		return _partKind;
	}

	/**
	 * @see #getPartKind()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.PartUsageResponse setPartKind(String value) {
		internalSetPartKind(value);
		return this;
	}

	/** Internal setter for {@link #getPartKind()} without chain call utility. */
	protected final void internalSetPartKind(String value) {
		_partKind = value;
	}

	/**
	 * Qualified name where the part is defined.
	 */
	public final String getDefinedIn() {
		return _definedIn;
	}

	/**
	 * @see #getDefinedIn()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.PartUsageResponse setDefinedIn(String value) {
		internalSetDefinedIn(value);
		return this;
	}

	/** Internal setter for {@link #getDefinedIn()} without chain call utility. */
	protected final void internalSetDefinedIn(String value) {
		_definedIn = value;
	}

	/**
	 * List of qualified names of subclasses that inherit this part.
	 */
	public final java.util.List<String> getInheritedBy() {
		return _inheritedBy;
	}

	/**
	 * @see #getInheritedBy()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.PartUsageResponse setInheritedBy(java.util.List<? extends String> value) {
		internalSetInheritedBy(value);
		return this;
	}

	/** Internal setter for {@link #getInheritedBy()} without chain call utility. */
	protected final void internalSetInheritedBy(java.util.List<? extends String> value) {
		_inheritedBy.clear();
		_inheritedBy.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getInheritedBy()} list.
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.PartUsageResponse addInheritedBy(String value) {
		internalAddInheritedBy(value);
		return this;
	}

	/** Implementation of {@link #addInheritedBy(String)} without chain call utility. */
	protected final void internalAddInheritedBy(String value) {
		_inheritedBy.add(value);
	}

	/**
	 * Removes a value from the {@link #getInheritedBy()} list.
	 */
	public final void removeInheritedBy(String value) {
		_inheritedBy.remove(value);
	}

	@Override
	public String jsonType() {
		return PART_USAGE_RESPONSE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			OWNER_TYPE__PROP, 
			PART_NAME__PROP, 
			PART_KIND__PROP, 
			DEFINED_IN__PROP, 
			INHERITED_BY__PROP);
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
			case OWNER_TYPE__PROP: return getOwnerType();
			case PART_NAME__PROP: return getPartName();
			case PART_KIND__PROP: return getPartKind();
			case DEFINED_IN__PROP: return getDefinedIn();
			case INHERITED_BY__PROP: return getInheritedBy();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case OWNER_TYPE__PROP: internalSetOwnerType((String) value); break;
			case PART_NAME__PROP: internalSetPartName((String) value); break;
			case PART_KIND__PROP: internalSetPartKind((String) value); break;
			case DEFINED_IN__PROP: internalSetDefinedIn((String) value); break;
			case INHERITED_BY__PROP: internalSetInheritedBy(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.model.messages.PartUsageResponse readPartUsageResponse(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.model.messages.PartUsageResponse result = new com.top_logic.tl.ai.core.tools.model.messages.PartUsageResponse();
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
		out.name(OWNER_TYPE__PROP);
		out.value(getOwnerType());
		out.name(PART_NAME__PROP);
		out.value(getPartName());
		out.name(PART_KIND__PROP);
		out.value(getPartKind());
		out.name(DEFINED_IN__PROP);
		out.value(getDefinedIn());
		out.name(INHERITED_BY__PROP);
		out.beginArray();
		for (String x : getInheritedBy()) {
			out.value(x);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case OWNER_TYPE__PROP: setOwnerType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case PART_NAME__PROP: setPartName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case PART_KIND__PROP: setPartKind(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DEFINED_IN__PROP: setDefinedIn(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case INHERITED_BY__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setInheritedBy(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
