package com.top_logic.ai.chat.api.messages;

/**
 * One selectable model.
 */
public class ModelOptionDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.ModelOptionDto} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.ModelOptionDto create() {
		return new com.top_logic.ai.chat.api.messages.ModelOptionDto();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.ModelOptionDto} type in JSON format. */
	public static final String MODEL_OPTION_DTO__TYPE = "ModelOptionDto";

	/** @see #getId() */
	public static final String ID__PROP = "id";

	/** @see #isGrantedToRole() */
	public static final String GRANTED_TO_ROLE__PROP = "grantedToRole";

	/** @see #getProvider() */
	public static final String PROVIDER__PROP = "provider";

	/** @see #getContextWindow() */
	public static final String CONTEXT_WINDOW__PROP = "contextWindow";

	/** @see #getMaxOutput() */
	public static final String MAX_OUTPUT__PROP = "maxOutput";

	/** @see #getEffortLevels() */
	public static final String EFFORT_LEVELS__PROP = "effortLevels";

	/** @see #getDefaultEffort() */
	public static final String DEFAULT_EFFORT__PROP = "defaultEffort";

	private String _id = "";

	private boolean _grantedToRole = false;

	private String _provider = "";

	private int _contextWindow = 0;

	private int _maxOutput = 0;

	private final java.util.List<String> _effortLevels = new java.util.ArrayList<>();

	private String _defaultEffort = "";

	/**
	 * Creates a {@link ModelOptionDto} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.ModelOptionDto#create()
	 */
	protected ModelOptionDto() {
		super();
	}

	/**
	 * Model identifier.
	 */
	public final String getId() {
		return _id;
	}

	/**
	 * @see #getId()
	 */
	public com.top_logic.ai.chat.api.messages.ModelOptionDto setId(String value) {
		internalSetId(value);
		return this;
	}

	/** Internal setter for {@link #getId()} without chain call utility. */
	protected final void internalSetId(String value) {
		_id = value;
	}

	/**
	 * Whether the caller may choose this model whatever agent they run.
	 *
	 * False for a model that is listed only because an agent the caller may run is assigned it: that
	 * one is usable with *that* agent and refused with another. A client offering a model list has to
	 * narrow it to this flag plus the selected agent's own model, or it offers a turn that comes back
	 * refused.
	 */
	public final boolean isGrantedToRole() {
		return _grantedToRole;
	}

	/**
	 * @see #isGrantedToRole()
	 */
	public com.top_logic.ai.chat.api.messages.ModelOptionDto setGrantedToRole(boolean value) {
		internalSetGrantedToRole(value);
		return this;
	}

	/** Internal setter for {@link #isGrantedToRole()} without chain call utility. */
	protected final void internalSetGrantedToRole(boolean value) {
		_grantedToRole = value;
	}

	/**
	 * Provider key (anthropic, openai, ...).
	 */
	public final String getProvider() {
		return _provider;
	}

	/**
	 * @see #getProvider()
	 */
	public com.top_logic.ai.chat.api.messages.ModelOptionDto setProvider(String value) {
		internalSetProvider(value);
		return this;
	}

	/** Internal setter for {@link #getProvider()} without chain call utility. */
	protected final void internalSetProvider(String value) {
		_provider = value;
	}

	/**
	 * Context window in tokens.
	 */
	public final int getContextWindow() {
		return _contextWindow;
	}

	/**
	 * @see #getContextWindow()
	 */
	public com.top_logic.ai.chat.api.messages.ModelOptionDto setContextWindow(int value) {
		internalSetContextWindow(value);
		return this;
	}

	/** Internal setter for {@link #getContextWindow()} without chain call utility. */
	protected final void internalSetContextWindow(int value) {
		_contextWindow = value;
	}

	/**
	 * Maximum output tokens.
	 */
	public final int getMaxOutput() {
		return _maxOutput;
	}

	/**
	 * @see #getMaxOutput()
	 */
	public com.top_logic.ai.chat.api.messages.ModelOptionDto setMaxOutput(int value) {
		internalSetMaxOutput(value);
		return this;
	}

	/** Internal setter for {@link #getMaxOutput()} without chain call utility. */
	protected final void internalSetMaxOutput(int value) {
		_maxOutput = value;
	}

	/**
	 * How hard this model may be asked to think, in increasing order.
	 *
	 * <p>
	 * Empty when the model offers no such setting, which is what a UI must check before offering
	 * one: the providers reject a level their model does not know.
	 * </p>
	 */
	public final java.util.List<String> getEffortLevels() {
		return _effortLevels;
	}

	/**
	 * @see #getEffortLevels()
	 */
	public com.top_logic.ai.chat.api.messages.ModelOptionDto setEffortLevels(java.util.List<? extends String> value) {
		internalSetEffortLevels(value);
		return this;
	}

	/** Internal setter for {@link #getEffortLevels()} without chain call utility. */
	protected final void internalSetEffortLevels(java.util.List<? extends String> value) {
		_effortLevels.clear();
		_effortLevels.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getEffortLevels()} list.
	 */
	public com.top_logic.ai.chat.api.messages.ModelOptionDto addEffortLevel(String value) {
		internalAddEffortLevel(value);
		return this;
	}

	/** Implementation of {@link #addEffortLevel(String)} without chain call utility. */
	protected final void internalAddEffortLevel(String value) {
		_effortLevels.add(value);
	}

	/**
	 * Removes a value from the {@link #getEffortLevels()} list.
	 */
	public final void removeEffortLevel(String value) {
		_effortLevels.remove(value);
	}

	/**
	 * The level in force when the caller names none; empty when the model has no levels.
	 */
	public final String getDefaultEffort() {
		return _defaultEffort;
	}

	/**
	 * @see #getDefaultEffort()
	 */
	public com.top_logic.ai.chat.api.messages.ModelOptionDto setDefaultEffort(String value) {
		internalSetDefaultEffort(value);
		return this;
	}

	/** Internal setter for {@link #getDefaultEffort()} without chain call utility. */
	protected final void internalSetDefaultEffort(String value) {
		_defaultEffort = value;
	}

	@Override
	public String jsonType() {
		return MODEL_OPTION_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			ID__PROP, 
			GRANTED_TO_ROLE__PROP, 
			PROVIDER__PROP, 
			CONTEXT_WINDOW__PROP, 
			MAX_OUTPUT__PROP, 
			EFFORT_LEVELS__PROP, 
			DEFAULT_EFFORT__PROP);
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
			case ID__PROP: return getId();
			case GRANTED_TO_ROLE__PROP: return isGrantedToRole();
			case PROVIDER__PROP: return getProvider();
			case CONTEXT_WINDOW__PROP: return getContextWindow();
			case MAX_OUTPUT__PROP: return getMaxOutput();
			case EFFORT_LEVELS__PROP: return getEffortLevels();
			case DEFAULT_EFFORT__PROP: return getDefaultEffort();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ID__PROP: internalSetId((String) value); break;
			case GRANTED_TO_ROLE__PROP: internalSetGrantedToRole((boolean) value); break;
			case PROVIDER__PROP: internalSetProvider((String) value); break;
			case CONTEXT_WINDOW__PROP: internalSetContextWindow((int) value); break;
			case MAX_OUTPUT__PROP: internalSetMaxOutput((int) value); break;
			case EFFORT_LEVELS__PROP: internalSetEffortLevels(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
			case DEFAULT_EFFORT__PROP: internalSetDefaultEffort((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.ModelOptionDto readModelOptionDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.ModelOptionDto result = new com.top_logic.ai.chat.api.messages.ModelOptionDto();
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
		out.name(ID__PROP);
		out.value(getId());
		out.name(GRANTED_TO_ROLE__PROP);
		out.value(isGrantedToRole());
		out.name(PROVIDER__PROP);
		out.value(getProvider());
		out.name(CONTEXT_WINDOW__PROP);
		out.value(getContextWindow());
		out.name(MAX_OUTPUT__PROP);
		out.value(getMaxOutput());
		out.name(EFFORT_LEVELS__PROP);
		out.beginArray();
		for (String x : getEffortLevels()) {
			out.value(x);
		}
		out.endArray();
		out.name(DEFAULT_EFFORT__PROP);
		out.value(getDefaultEffort());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ID__PROP: setId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case GRANTED_TO_ROLE__PROP: setGrantedToRole(in.nextBoolean()); break;
			case PROVIDER__PROP: setProvider(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CONTEXT_WINDOW__PROP: setContextWindow(in.nextInt()); break;
			case MAX_OUTPUT__PROP: setMaxOutput(in.nextInt()); break;
			case EFFORT_LEVELS__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setEffortLevels(newValue);
			}
			break;
			case DEFAULT_EFFORT__PROP: setDefaultEffort(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
