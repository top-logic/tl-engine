package com.top_logic.tl.ai.core.tools.authoring.messages;

/**
 * Short skill entry for listings.
 */
public class SkillSummary extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.authoring.messages.SkillSummary} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.authoring.messages.SkillSummary create() {
		return new com.top_logic.tl.ai.core.tools.authoring.messages.SkillSummary();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.authoring.messages.SkillSummary} type in JSON format. */
	public static final String SKILL_SUMMARY__TYPE = "SkillSummary";

	/** @see #getName() */
	public static final String NAME__PROP = "name";

	/** @see #getDescription() */
	public static final String DESCRIPTION__PROP = "description";

	private String _name = "";

	private String _description = "";

	/**
	 * Creates a {@link SkillSummary} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.authoring.messages.SkillSummary#create()
	 */
	protected SkillSummary() {
		super();
	}

	/**
	 * Skill name.
	 */
	public final String getName() {
		return _name;
	}

	/**
	 * @see #getName()
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.SkillSummary setName(String value) {
		internalSetName(value);
		return this;
	}

	/** Internal setter for {@link #getName()} without chain call utility. */
	protected final void internalSetName(String value) {
		_name = value;
	}

	/**
	 * Skill description.
	 */
	public final String getDescription() {
		return _description;
	}

	/**
	 * @see #getDescription()
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.SkillSummary setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	/** Internal setter for {@link #getDescription()} without chain call utility. */
	protected final void internalSetDescription(String value) {
		_description = value;
	}

	@Override
	public String jsonType() {
		return SKILL_SUMMARY__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			NAME__PROP, 
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
			case DESCRIPTION__PROP: return getDescription();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case NAME__PROP: internalSetName((String) value); break;
			case DESCRIPTION__PROP: internalSetDescription((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.authoring.messages.SkillSummary readSkillSummary(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.authoring.messages.SkillSummary result = new com.top_logic.tl.ai.core.tools.authoring.messages.SkillSummary();
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
		out.name(DESCRIPTION__PROP);
		out.value(getDescription());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case NAME__PROP: setName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DESCRIPTION__PROP: setDescription(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
