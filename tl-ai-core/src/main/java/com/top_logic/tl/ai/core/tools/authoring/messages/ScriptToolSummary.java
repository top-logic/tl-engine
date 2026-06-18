package com.top_logic.tl.ai.core.tools.authoring.messages;

/**
 * Short TL-Script tool entry for listings.
 */
public class ScriptToolSummary extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolSummary} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolSummary create() {
		return new com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolSummary();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolSummary} type in JSON format. */
	public static final String SCRIPT_TOOL_SUMMARY__TYPE = "ScriptToolSummary";

	/** @see #getName() */
	public static final String NAME__PROP = "name";

	/** @see #getDescription() */
	public static final String DESCRIPTION__PROP = "description";

	/** @see #getMode() */
	public static final String MODE__PROP = "mode";

	private String _name = "";

	private String _description = "";

	private String _mode = "";

	/**
	 * Creates a {@link ScriptToolSummary} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolSummary#create()
	 */
	protected ScriptToolSummary() {
		super();
	}

	/**
	 * Tool name (unqualified; referenced as custom.{name}).
	 */
	public final String getName() {
		return _name;
	}

	/**
	 * @see #getName()
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolSummary setName(String value) {
		internalSetName(value);
		return this;
	}

	/** Internal setter for {@link #getName()} without chain call utility. */
	protected final void internalSetName(String value) {
		_name = value;
	}

	/**
	 * Tool description.
	 */
	public final String getDescription() {
		return _description;
	}

	/**
	 * @see #getDescription()
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolSummary setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	/** Internal setter for {@link #getDescription()} without chain call utility. */
	protected final void internalSetDescription(String value) {
		_description = value;
	}

	/**
	 * Tool mode: READ, USER_SCOPED, DATA_WRITE, or STRUCTURAL.
	 */
	public final String getMode() {
		return _mode;
	}

	/**
	 * @see #getMode()
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolSummary setMode(String value) {
		internalSetMode(value);
		return this;
	}

	/** Internal setter for {@link #getMode()} without chain call utility. */
	protected final void internalSetMode(String value) {
		_mode = value;
	}

	@Override
	public String jsonType() {
		return SCRIPT_TOOL_SUMMARY__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			NAME__PROP, 
			DESCRIPTION__PROP, 
			MODE__PROP);
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
			case MODE__PROP: return getMode();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case NAME__PROP: internalSetName((String) value); break;
			case DESCRIPTION__PROP: internalSetDescription((String) value); break;
			case MODE__PROP: internalSetMode((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolSummary readScriptToolSummary(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolSummary result = new com.top_logic.tl.ai.core.tools.authoring.messages.ScriptToolSummary();
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
		out.name(MODE__PROP);
		out.value(getMode());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case NAME__PROP: setName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DESCRIPTION__PROP: setDescription(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case MODE__PROP: setMode(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
