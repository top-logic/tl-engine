package com.top_logic.ai.service.tools.model.messages;

/**
 * Information about a type (class, enumeration, datatype).
 */
public class TypeInfo extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.service.tools.model.messages.TypeInfo} instance.
	 */
	public static com.top_logic.ai.service.tools.model.messages.TypeInfo create() {
		return new com.top_logic.ai.service.tools.model.messages.TypeInfo();
	}

	/** Identifier for the {@link com.top_logic.ai.service.tools.model.messages.TypeInfo} type in JSON format. */
	public static final String TYPE_INFO__TYPE = "TypeInfo";

	/** @see #getName() */
	public static final String NAME__PROP = "name";

	/** @see #getQualifiedName() */
	public static final String QUALIFIED_NAME__PROP = "qualifiedName";

	/** @see #getMetaType() */
	public static final String META_TYPE__PROP = "metaType";

	/** @see #getClassifierCount() */
	public static final String CLASSIFIER_COUNT__PROP = "classifierCount";

	/** @see #getPartCount() */
	public static final String PART_COUNT__PROP = "partCount";

	/** @see #getGeneralizations() */
	public static final String GENERALIZATIONS__PROP = "generalizations";

	/** @see #getSpecializations() */
	public static final String SPECIALIZATIONS__PROP = "specializations";

	/** @see #isAbstract() */
	public static final String ABSTRACT__PROP = "abstract";

	/** @see #isFinal() */
	public static final String FINAL__PROP = "final";

	/** @see #getLabel() */
	public static final String LABEL__PROP = "label";

	/** @see #getDescription() */
	public static final String DESCRIPTION__PROP = "description";

	private String _name = "";

	private String _qualifiedName = "";

	private String _metaType = "";

	private int _classifierCount = 0;

	private int _partCount = 0;

	private final java.util.List<String> _generalizations = new java.util.ArrayList<>();

	private final java.util.List<String> _specializations = new java.util.ArrayList<>();

	private boolean _abstract = false;

	private boolean _final = false;

	private String _label = "";

	private String _description = "";

	/**
	 * Creates a {@link TypeInfo} instance.
	 *
	 * @see com.top_logic.ai.service.tools.model.messages.TypeInfo#create()
	 */
	protected TypeInfo() {
		super();
	}

	/**
	 * Type name.
	 */
	public final String getName() {
		return _name;
	}

	/**
	 * @see #getName()
	 */
	public com.top_logic.ai.service.tools.model.messages.TypeInfo setName(String value) {
		internalSetName(value);
		return this;
	}

	/** Internal setter for {@link #getName()} without chain call utility. */
	protected final void internalSetName(String value) {
		_name = value;
	}

	/**
	 * Qualified type name (module:TypeName).
	 */
	public final String getQualifiedName() {
		return _qualifiedName;
	}

	/**
	 * @see #getQualifiedName()
	 */
	public com.top_logic.ai.service.tools.model.messages.TypeInfo setQualifiedName(String value) {
		internalSetQualifiedName(value);
		return this;
	}

	/** Internal setter for {@link #getQualifiedName()} without chain call utility. */
	protected final void internalSetQualifiedName(String value) {
		_qualifiedName = value;
	}

	/**
	 * Meta-type: class, enumeration, or datatype.
	 */
	public final String getMetaType() {
		return _metaType;
	}

	/**
	 * @see #getMetaType()
	 */
	public com.top_logic.ai.service.tools.model.messages.TypeInfo setMetaType(String value) {
		internalSetMetaType(value);
		return this;
	}

	/** Internal setter for {@link #getMetaType()} without chain call utility. */
	protected final void internalSetMetaType(String value) {
		_metaType = value;
	}

	/**
	 * Number of classifiers (for enumerations only).
	 */
	public final int getClassifierCount() {
		return _classifierCount;
	}

	/**
	 * @see #getClassifierCount()
	 */
	public com.top_logic.ai.service.tools.model.messages.TypeInfo setClassifierCount(int value) {
		internalSetClassifierCount(value);
		return this;
	}

	/** Internal setter for {@link #getClassifierCount()} without chain call utility. */
	protected final void internalSetClassifierCount(int value) {
		_classifierCount = value;
	}

	/**
	 * Number of local parts (for structured types).
	 */
	public final int getPartCount() {
		return _partCount;
	}

	/**
	 * @see #getPartCount()
	 */
	public com.top_logic.ai.service.tools.model.messages.TypeInfo setPartCount(int value) {
		internalSetPartCount(value);
		return this;
	}

	/** Internal setter for {@link #getPartCount()} without chain call utility. */
	protected final void internalSetPartCount(int value) {
		_partCount = value;
	}

	/**
	 * List of qualified names of supertypes (for classes).
	 */
	public final java.util.List<String> getGeneralizations() {
		return _generalizations;
	}

	/**
	 * @see #getGeneralizations()
	 */
	public com.top_logic.ai.service.tools.model.messages.TypeInfo setGeneralizations(java.util.List<? extends String> value) {
		internalSetGeneralizations(value);
		return this;
	}

	/** Internal setter for {@link #getGeneralizations()} without chain call utility. */
	protected final void internalSetGeneralizations(java.util.List<? extends String> value) {
		_generalizations.clear();
		_generalizations.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getGeneralizations()} list.
	 */
	public com.top_logic.ai.service.tools.model.messages.TypeInfo addGeneralization(String value) {
		internalAddGeneralization(value);
		return this;
	}

	/** Implementation of {@link #addGeneralization(String)} without chain call utility. */
	protected final void internalAddGeneralization(String value) {
		_generalizations.add(value);
	}

	/**
	 * Removes a value from the {@link #getGeneralizations()} list.
	 */
	public final void removeGeneralization(String value) {
		_generalizations.remove(value);
	}

	/**
	 * List of qualified names of subtypes (for classes).
	 */
	public final java.util.List<String> getSpecializations() {
		return _specializations;
	}

	/**
	 * @see #getSpecializations()
	 */
	public com.top_logic.ai.service.tools.model.messages.TypeInfo setSpecializations(java.util.List<? extends String> value) {
		internalSetSpecializations(value);
		return this;
	}

	/** Internal setter for {@link #getSpecializations()} without chain call utility. */
	protected final void internalSetSpecializations(java.util.List<? extends String> value) {
		_specializations.clear();
		_specializations.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getSpecializations()} list.
	 */
	public com.top_logic.ai.service.tools.model.messages.TypeInfo addSpecialization(String value) {
		internalAddSpecialization(value);
		return this;
	}

	/** Implementation of {@link #addSpecialization(String)} without chain call utility. */
	protected final void internalAddSpecialization(String value) {
		_specializations.add(value);
	}

	/**
	 * Removes a value from the {@link #getSpecializations()} list.
	 */
	public final void removeSpecialization(String value) {
		_specializations.remove(value);
	}

	/**
	 * Whether the class is abstract (cannot be instantiated directly). Only set for classes.
	 */
	public final boolean isAbstract() {
		return _abstract;
	}

	/**
	 * @see #isAbstract()
	 */
	public com.top_logic.ai.service.tools.model.messages.TypeInfo setAbstract(boolean value) {
		internalSetAbstract(value);
		return this;
	}

	/** Internal setter for {@link #isAbstract()} without chain call utility. */
	protected final void internalSetAbstract(boolean value) {
		_abstract = value;
	}

	/**
	 * Whether the class is final (cannot be specialized). Only set for classes.
	 */
	public final boolean isFinal() {
		return _final;
	}

	/**
	 * @see #isFinal()
	 */
	public com.top_logic.ai.service.tools.model.messages.TypeInfo setFinal(boolean value) {
		internalSetFinal(value);
		return this;
	}

	/** Internal setter for {@link #isFinal()} without chain call utility. */
	protected final void internalSetFinal(boolean value) {
		_final = value;
	}

	/**
	 * Localized label for the type.
	 */
	public final String getLabel() {
		return _label;
	}

	/**
	 * @see #getLabel()
	 */
	public com.top_logic.ai.service.tools.model.messages.TypeInfo setLabel(String value) {
		internalSetLabel(value);
		return this;
	}

	/** Internal setter for {@link #getLabel()} without chain call utility. */
	protected final void internalSetLabel(String value) {
		_label = value;
	}

	/**
	 * Localized description of the type.
	 */
	public final String getDescription() {
		return _description;
	}

	/**
	 * @see #getDescription()
	 */
	public com.top_logic.ai.service.tools.model.messages.TypeInfo setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	/** Internal setter for {@link #getDescription()} without chain call utility. */
	protected final void internalSetDescription(String value) {
		_description = value;
	}

	@Override
	public String jsonType() {
		return TYPE_INFO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			NAME__PROP, 
			QUALIFIED_NAME__PROP, 
			META_TYPE__PROP, 
			CLASSIFIER_COUNT__PROP, 
			PART_COUNT__PROP, 
			GENERALIZATIONS__PROP, 
			SPECIALIZATIONS__PROP, 
			ABSTRACT__PROP, 
			FINAL__PROP, 
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
			case QUALIFIED_NAME__PROP: return getQualifiedName();
			case META_TYPE__PROP: return getMetaType();
			case CLASSIFIER_COUNT__PROP: return getClassifierCount();
			case PART_COUNT__PROP: return getPartCount();
			case GENERALIZATIONS__PROP: return getGeneralizations();
			case SPECIALIZATIONS__PROP: return getSpecializations();
			case ABSTRACT__PROP: return isAbstract();
			case FINAL__PROP: return isFinal();
			case LABEL__PROP: return getLabel();
			case DESCRIPTION__PROP: return getDescription();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case NAME__PROP: internalSetName((String) value); break;
			case QUALIFIED_NAME__PROP: internalSetQualifiedName((String) value); break;
			case META_TYPE__PROP: internalSetMetaType((String) value); break;
			case CLASSIFIER_COUNT__PROP: internalSetClassifierCount((int) value); break;
			case PART_COUNT__PROP: internalSetPartCount((int) value); break;
			case GENERALIZATIONS__PROP: internalSetGeneralizations(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
			case SPECIALIZATIONS__PROP: internalSetSpecializations(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
			case ABSTRACT__PROP: internalSetAbstract((boolean) value); break;
			case FINAL__PROP: internalSetFinal((boolean) value); break;
			case LABEL__PROP: internalSetLabel((String) value); break;
			case DESCRIPTION__PROP: internalSetDescription((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.service.tools.model.messages.TypeInfo readTypeInfo(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.service.tools.model.messages.TypeInfo result = new com.top_logic.ai.service.tools.model.messages.TypeInfo();
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
		out.name(QUALIFIED_NAME__PROP);
		out.value(getQualifiedName());
		out.name(META_TYPE__PROP);
		out.value(getMetaType());
		out.name(CLASSIFIER_COUNT__PROP);
		out.value(getClassifierCount());
		out.name(PART_COUNT__PROP);
		out.value(getPartCount());
		out.name(GENERALIZATIONS__PROP);
		out.beginArray();
		for (String x : getGeneralizations()) {
			out.value(x);
		}
		out.endArray();
		out.name(SPECIALIZATIONS__PROP);
		out.beginArray();
		for (String x : getSpecializations()) {
			out.value(x);
		}
		out.endArray();
		out.name(ABSTRACT__PROP);
		out.value(isAbstract());
		out.name(FINAL__PROP);
		out.value(isFinal());
		out.name(LABEL__PROP);
		out.value(getLabel());
		out.name(DESCRIPTION__PROP);
		out.value(getDescription());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case NAME__PROP: setName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case QUALIFIED_NAME__PROP: setQualifiedName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case META_TYPE__PROP: setMetaType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CLASSIFIER_COUNT__PROP: setClassifierCount(in.nextInt()); break;
			case PART_COUNT__PROP: setPartCount(in.nextInt()); break;
			case GENERALIZATIONS__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setGeneralizations(newValue);
			}
			break;
			case SPECIALIZATIONS__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setSpecializations(newValue);
			}
			break;
			case ABSTRACT__PROP: setAbstract(in.nextBoolean()); break;
			case FINAL__PROP: setFinal(in.nextBoolean()); break;
			case LABEL__PROP: setLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DESCRIPTION__PROP: setDescription(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
