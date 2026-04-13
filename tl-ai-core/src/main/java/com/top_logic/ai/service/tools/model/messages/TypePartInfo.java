package com.top_logic.ai.service.tools.model.messages;

/**
 * Information about a type part (property or reference).
 */
public class TypePartInfo extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.service.tools.model.messages.TypePartInfo} instance.
	 */
	public static com.top_logic.ai.service.tools.model.messages.TypePartInfo create() {
		return new com.top_logic.ai.service.tools.model.messages.TypePartInfo();
	}

	/** Identifier for the {@link com.top_logic.ai.service.tools.model.messages.TypePartInfo} type in JSON format. */
	public static final String TYPE_PART_INFO__TYPE = "TypePartInfo";

	/** @see #getName() */
	public static final String NAME__PROP = "name";

	/** @see #getKind() */
	public static final String KIND__PROP = "kind";

	/** @see #getDefinedIn() */
	public static final String DEFINED_IN__PROP = "definedIn";

	/** @see #isInherited() */
	public static final String INHERITED__PROP = "inherited";

	/** @see #getType() */
	public static final String TYPE__PROP = "type";

	/** @see #isMandatory() */
	public static final String MANDATORY__PROP = "mandatory";

	/** @see #isMultiple() */
	public static final String MULTIPLE__PROP = "multiple";

	/** @see #isOrdered() */
	public static final String ORDERED__PROP = "ordered";

	/** @see #isBag() */
	public static final String BAG__PROP = "bag";

	/** @see #isAbstract() */
	public static final String ABSTRACT__PROP = "abstract";

	/** @see #isDerived() */
	public static final String DERIVED__PROP = "derived";

	/** @see #isOverride() */
	public static final String OVERRIDE__PROP = "override";

	/** @see #getLabel() */
	public static final String LABEL__PROP = "label";

	/** @see #getDescription() */
	public static final String DESCRIPTION__PROP = "description";

	private String _name = "";

	private String _kind = "";

	private String _definedIn = "";

	private boolean _inherited = false;

	private String _type = "";

	private boolean _mandatory = false;

	private boolean _multiple = false;

	private boolean _ordered = false;

	private boolean _bag = false;

	private boolean _abstract = false;

	private boolean _derived = false;

	private boolean _override = false;

	private String _label = "";

	private String _description = "";

	/**
	 * Creates a {@link TypePartInfo} instance.
	 *
	 * @see com.top_logic.ai.service.tools.model.messages.TypePartInfo#create()
	 */
	protected TypePartInfo() {
		super();
	}

	/**
	 * Part name.
	 */
	public final String getName() {
		return _name;
	}

	/**
	 * @see #getName()
	 */
	public com.top_logic.ai.service.tools.model.messages.TypePartInfo setName(String value) {
		internalSetName(value);
		return this;
	}

	/** Internal setter for {@link #getName()} without chain call utility. */
	protected final void internalSetName(String value) {
		_name = value;
	}

	/**
	 * Part kind: property or reference.
	 */
	public final String getKind() {
		return _kind;
	}

	/**
	 * @see #getKind()
	 */
	public com.top_logic.ai.service.tools.model.messages.TypePartInfo setKind(String value) {
		internalSetKind(value);
		return this;
	}

	/** Internal setter for {@link #getKind()} without chain call utility. */
	protected final void internalSetKind(String value) {
		_kind = value;
	}

	/**
	 * Qualified name of the type that defines this part.
	 */
	public final String getDefinedIn() {
		return _definedIn;
	}

	/**
	 * @see #getDefinedIn()
	 */
	public com.top_logic.ai.service.tools.model.messages.TypePartInfo setDefinedIn(String value) {
		internalSetDefinedIn(value);
		return this;
	}

	/** Internal setter for {@link #getDefinedIn()} without chain call utility. */
	protected final void internalSetDefinedIn(String value) {
		_definedIn = value;
	}

	/**
	 * Whether this part is inherited from a supertype.
	 */
	public final boolean isInherited() {
		return _inherited;
	}

	/**
	 * @see #isInherited()
	 */
	public com.top_logic.ai.service.tools.model.messages.TypePartInfo setInherited(boolean value) {
		internalSetInherited(value);
		return this;
	}

	/** Internal setter for {@link #isInherited()} without chain call utility. */
	protected final void internalSetInherited(boolean value) {
		_inherited = value;
	}

	/**
	 * Qualified name of the part's type.
	 */
	public final String getType() {
		return _type;
	}

	/**
	 * @see #getType()
	 */
	public com.top_logic.ai.service.tools.model.messages.TypePartInfo setType(String value) {
		internalSetType(value);
		return this;
	}

	/** Internal setter for {@link #getType()} without chain call utility. */
	protected final void internalSetType(String value) {
		_type = value;
	}

	/**
	 * Whether the part is mandatory.
	 */
	public final boolean isMandatory() {
		return _mandatory;
	}

	/**
	 * @see #isMandatory()
	 */
	public com.top_logic.ai.service.tools.model.messages.TypePartInfo setMandatory(boolean value) {
		internalSetMandatory(value);
		return this;
	}

	/** Internal setter for {@link #isMandatory()} without chain call utility. */
	protected final void internalSetMandatory(boolean value) {
		_mandatory = value;
	}

	/**
	 * Whether the part can have multiple values.
	 */
	public final boolean isMultiple() {
		return _multiple;
	}

	/**
	 * @see #isMultiple()
	 */
	public com.top_logic.ai.service.tools.model.messages.TypePartInfo setMultiple(boolean value) {
		internalSetMultiple(value);
		return this;
	}

	/** Internal setter for {@link #isMultiple()} without chain call utility. */
	protected final void internalSetMultiple(boolean value) {
		_multiple = value;
	}

	/**
	 * Whether multiple values are ordered (only for multiple parts).
	 */
	public final boolean isOrdered() {
		return _ordered;
	}

	/**
	 * @see #isOrdered()
	 */
	public com.top_logic.ai.service.tools.model.messages.TypePartInfo setOrdered(boolean value) {
		internalSetOrdered(value);
		return this;
	}

	/** Internal setter for {@link #isOrdered()} without chain call utility. */
	protected final void internalSetOrdered(boolean value) {
		_ordered = value;
	}

	/**
	 * Whether the part is a bag (unordered, allows duplicates).
	 */
	public final boolean isBag() {
		return _bag;
	}

	/**
	 * @see #isBag()
	 */
	public com.top_logic.ai.service.tools.model.messages.TypePartInfo setBag(boolean value) {
		internalSetBag(value);
		return this;
	}

	/** Internal setter for {@link #isBag()} without chain call utility. */
	protected final void internalSetBag(boolean value) {
		_bag = value;
	}

	/**
	 * Whether the part is abstract.
	 */
	public final boolean isAbstract() {
		return _abstract;
	}

	/**
	 * @see #isAbstract()
	 */
	public com.top_logic.ai.service.tools.model.messages.TypePartInfo setAbstract(boolean value) {
		internalSetAbstract(value);
		return this;
	}

	/** Internal setter for {@link #isAbstract()} without chain call utility. */
	protected final void internalSetAbstract(boolean value) {
		_abstract = value;
	}

	/**
	 * Whether the part is derived (computed).
	 */
	public final boolean isDerived() {
		return _derived;
	}

	/**
	 * @see #isDerived()
	 */
	public com.top_logic.ai.service.tools.model.messages.TypePartInfo setDerived(boolean value) {
		internalSetDerived(value);
		return this;
	}

	/** Internal setter for {@link #isDerived()} without chain call utility. */
	protected final void internalSetDerived(boolean value) {
		_derived = value;
	}

	/**
	 * Whether the part overrides a part from a supertype.
	 */
	public final boolean isOverride() {
		return _override;
	}

	/**
	 * @see #isOverride()
	 */
	public com.top_logic.ai.service.tools.model.messages.TypePartInfo setOverride(boolean value) {
		internalSetOverride(value);
		return this;
	}

	/** Internal setter for {@link #isOverride()} without chain call utility. */
	protected final void internalSetOverride(boolean value) {
		_override = value;
	}

	/**
	 * Localized label for the part.
	 */
	public final String getLabel() {
		return _label;
	}

	/**
	 * @see #getLabel()
	 */
	public com.top_logic.ai.service.tools.model.messages.TypePartInfo setLabel(String value) {
		internalSetLabel(value);
		return this;
	}

	/** Internal setter for {@link #getLabel()} without chain call utility. */
	protected final void internalSetLabel(String value) {
		_label = value;
	}

	/**
	 * Localized description of the part.
	 */
	public final String getDescription() {
		return _description;
	}

	/**
	 * @see #getDescription()
	 */
	public com.top_logic.ai.service.tools.model.messages.TypePartInfo setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	/** Internal setter for {@link #getDescription()} without chain call utility. */
	protected final void internalSetDescription(String value) {
		_description = value;
	}

	@Override
	public String jsonType() {
		return TYPE_PART_INFO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			NAME__PROP, 
			KIND__PROP, 
			DEFINED_IN__PROP, 
			INHERITED__PROP, 
			TYPE__PROP, 
			MANDATORY__PROP, 
			MULTIPLE__PROP, 
			ORDERED__PROP, 
			BAG__PROP, 
			ABSTRACT__PROP, 
			DERIVED__PROP, 
			OVERRIDE__PROP, 
			LABEL__PROP, 
			DESCRIPTION__PROP);
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
			case KIND__PROP: return getKind();
			case DEFINED_IN__PROP: return getDefinedIn();
			case INHERITED__PROP: return isInherited();
			case TYPE__PROP: return getType();
			case MANDATORY__PROP: return isMandatory();
			case MULTIPLE__PROP: return isMultiple();
			case ORDERED__PROP: return isOrdered();
			case BAG__PROP: return isBag();
			case ABSTRACT__PROP: return isAbstract();
			case DERIVED__PROP: return isDerived();
			case OVERRIDE__PROP: return isOverride();
			case LABEL__PROP: return getLabel();
			case DESCRIPTION__PROP: return getDescription();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case NAME__PROP: internalSetName((String) value); break;
			case KIND__PROP: internalSetKind((String) value); break;
			case DEFINED_IN__PROP: internalSetDefinedIn((String) value); break;
			case INHERITED__PROP: internalSetInherited((boolean) value); break;
			case TYPE__PROP: internalSetType((String) value); break;
			case MANDATORY__PROP: internalSetMandatory((boolean) value); break;
			case MULTIPLE__PROP: internalSetMultiple((boolean) value); break;
			case ORDERED__PROP: internalSetOrdered((boolean) value); break;
			case BAG__PROP: internalSetBag((boolean) value); break;
			case ABSTRACT__PROP: internalSetAbstract((boolean) value); break;
			case DERIVED__PROP: internalSetDerived((boolean) value); break;
			case OVERRIDE__PROP: internalSetOverride((boolean) value); break;
			case LABEL__PROP: internalSetLabel((String) value); break;
			case DESCRIPTION__PROP: internalSetDescription((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.service.tools.model.messages.TypePartInfo readTypePartInfo(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.service.tools.model.messages.TypePartInfo result = new com.top_logic.ai.service.tools.model.messages.TypePartInfo();
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
		out.name(KIND__PROP);
		out.value(getKind());
		out.name(DEFINED_IN__PROP);
		out.value(getDefinedIn());
		out.name(INHERITED__PROP);
		out.value(isInherited());
		out.name(TYPE__PROP);
		out.value(getType());
		out.name(MANDATORY__PROP);
		out.value(isMandatory());
		out.name(MULTIPLE__PROP);
		out.value(isMultiple());
		out.name(ORDERED__PROP);
		out.value(isOrdered());
		out.name(BAG__PROP);
		out.value(isBag());
		out.name(ABSTRACT__PROP);
		out.value(isAbstract());
		out.name(DERIVED__PROP);
		out.value(isDerived());
		out.name(OVERRIDE__PROP);
		out.value(isOverride());
		out.name(LABEL__PROP);
		out.value(getLabel());
		out.name(DESCRIPTION__PROP);
		out.value(getDescription());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case NAME__PROP: setName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case KIND__PROP: setKind(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DEFINED_IN__PROP: setDefinedIn(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case INHERITED__PROP: setInherited(in.nextBoolean()); break;
			case TYPE__PROP: setType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case MANDATORY__PROP: setMandatory(in.nextBoolean()); break;
			case MULTIPLE__PROP: setMultiple(in.nextBoolean()); break;
			case ORDERED__PROP: setOrdered(in.nextBoolean()); break;
			case BAG__PROP: setBag(in.nextBoolean()); break;
			case ABSTRACT__PROP: setAbstract(in.nextBoolean()); break;
			case DERIVED__PROP: setDerived(in.nextBoolean()); break;
			case OVERRIDE__PROP: setOverride(in.nextBoolean()); break;
			case LABEL__PROP: setLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DESCRIPTION__PROP: setDescription(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
