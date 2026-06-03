package com.top_logic.tl.ai.core.tools.authoring.messages;

/**
 * Result of listing knowledge-base skills.
 */
public class SkillListResult extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.authoring.messages.SkillListResult} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.authoring.messages.SkillListResult create() {
		return new com.top_logic.tl.ai.core.tools.authoring.messages.SkillListResult();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.authoring.messages.SkillListResult} type in JSON format. */
	public static final String SKILL_LIST_RESULT__TYPE = "SkillListResult";

	/** @see #getSkills() */
	public static final String SKILLS__PROP = "skills";

	private final java.util.List<com.top_logic.tl.ai.core.tools.authoring.messages.SkillSummary> _skills = new java.util.ArrayList<>();

	/**
	 * Creates a {@link SkillListResult} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.authoring.messages.SkillListResult#create()
	 */
	protected SkillListResult() {
		super();
	}

	/**
	 * The knowledge-base skills.
	 */
	public final java.util.List<com.top_logic.tl.ai.core.tools.authoring.messages.SkillSummary> getSkills() {
		return _skills;
	}

	/**
	 * @see #getSkills()
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.SkillListResult setSkills(java.util.List<? extends com.top_logic.tl.ai.core.tools.authoring.messages.SkillSummary> value) {
		internalSetSkills(value);
		return this;
	}

	/** Internal setter for {@link #getSkills()} without chain call utility. */
	protected final void internalSetSkills(java.util.List<? extends com.top_logic.tl.ai.core.tools.authoring.messages.SkillSummary> value) {
		if (value == null) throw new IllegalArgumentException("Property 'skills' cannot be null.");
		_skills.clear();
		_skills.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getSkills()} list.
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.SkillListResult addSkill(com.top_logic.tl.ai.core.tools.authoring.messages.SkillSummary value) {
		internalAddSkill(value);
		return this;
	}

	/** Implementation of {@link #addSkill(com.top_logic.tl.ai.core.tools.authoring.messages.SkillSummary)} without chain call utility. */
	protected final void internalAddSkill(com.top_logic.tl.ai.core.tools.authoring.messages.SkillSummary value) {
		_skills.add(value);
	}

	/**
	 * Removes a value from the {@link #getSkills()} list.
	 */
	public final void removeSkill(com.top_logic.tl.ai.core.tools.authoring.messages.SkillSummary value) {
		_skills.remove(value);
	}

	@Override
	public String jsonType() {
		return SKILL_LIST_RESULT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			SKILLS__PROP);
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
			case SKILLS__PROP: return getSkills();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case SKILLS__PROP: internalSetSkills(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.tl.ai.core.tools.authoring.messages.SkillSummary.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.authoring.messages.SkillListResult readSkillListResult(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.authoring.messages.SkillListResult result = new com.top_logic.tl.ai.core.tools.authoring.messages.SkillListResult();
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
		out.name(SKILLS__PROP);
		out.beginArray();
		for (com.top_logic.tl.ai.core.tools.authoring.messages.SkillSummary x : getSkills()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case SKILLS__PROP: {
				java.util.List<com.top_logic.tl.ai.core.tools.authoring.messages.SkillSummary> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.tl.ai.core.tools.authoring.messages.SkillSummary.readSkillSummary(in));
				}
				in.endArray();
				setSkills(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
