package com.top_logic.tl.ai.core.tools.refusal.messages;

/**
 * The application refused what a tool was asked to do, so none of it took effect.
 *
 * <p>
 * The field names are identifiers. The only text in it is the application's own - the
 * {@link Reason#getText()} of each reason - in the language of the run's user.
 * </p>
 */
public class ApplicationRefusal extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.refusal.messages.ApplicationRefusal} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.refusal.messages.ApplicationRefusal create() {
		return new com.top_logic.tl.ai.core.tools.refusal.messages.ApplicationRefusal();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.refusal.messages.ApplicationRefusal} type in JSON format. */
	public static final String APPLICATION_REFUSAL__TYPE = "ApplicationRefusal";

	/** @see #isRefused() */
	public static final String REFUSED__PROP = "refused";

	/** @see #getCommand() */
	public static final String COMMAND__PROP = "command";

	/** @see #getInput() */
	public static final String INPUT__PROP = "input";

	/** @see #getObject() */
	public static final String OBJECT__PROP = "object";

	/** @see #getType() */
	public static final String TYPE__PROP = "type";

	/** @see #getReasons() */
	public static final String REASONS__PROP = "reasons";

	/** @see #getMandatory() */
	public static final String MANDATORY__PROP = "mandatory";

	private boolean _refused = false;

	private String _command = null;

	private String _input = null;

	private String _object = null;

	private String _type = null;

	private final java.util.List<com.top_logic.tl.ai.core.tools.refusal.messages.Reason> _reasons = new java.util.ArrayList<>();

	private final java.util.List<com.top_logic.tl.ai.core.tools.refusal.messages.MandatoryAttributes> _mandatory = new java.util.ArrayList<>();

	/**
	 * Creates a {@link ApplicationRefusal} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.refusal.messages.ApplicationRefusal#create()
	 */
	protected ApplicationRefusal() {
		super();
	}

	/**
	 * Always <code>true</code>: marks the answer as the application's refusal.
	 */
	public final boolean isRefused() {
		return _refused;
	}

	/**
	 * @see #isRefused()
	 */
	public com.top_logic.tl.ai.core.tools.refusal.messages.ApplicationRefusal setRefused(boolean value) {
		internalSetRefused(value);
		return this;
	}

	/** Internal setter for {@link #isRefused()} without chain call utility. */
	protected final void internalSetRefused(boolean value) {
		_refused = value;
	}

	/**
	 * The name of the page command the application refused to run, if a page command was refused.
	 */
	public final String getCommand() {
		return _command;
	}

	/**
	 * @see #getCommand()
	 */
	public com.top_logic.tl.ai.core.tools.refusal.messages.ApplicationRefusal setCommand(String value) {
		internalSetCommand(value);
		return this;
	}

	/** Internal setter for {@link #getCommand()} without chain call utility. */
	protected final void internalSetCommand(String value) {
		_command = value;
	}

	/**
	 * Checks, whether {@link #getCommand()} has a value.
	 */
	public final boolean hasCommand() {
		return _command != null;
	}

	/**
	 * The id of the object the agent asked the refused page command to run for, if it passed one.
	 */
	public final String getInput() {
		return _input;
	}

	/**
	 * @see #getInput()
	 */
	public com.top_logic.tl.ai.core.tools.refusal.messages.ApplicationRefusal setInput(String value) {
		internalSetInput(value);
		return this;
	}

	/** Internal setter for {@link #getInput()} without chain call utility. */
	protected final void internalSetInput(String value) {
		_input = value;
	}

	/**
	 * Checks, whether {@link #getInput()} has a value.
	 */
	public final boolean hasInput() {
		return _input != null;
	}

	/**
	 * The object the refused operation was about, as <code>label [id:TID]</code>, if it names one.
	 */
	public final String getObject() {
		return _object;
	}

	/**
	 * @see #getObject()
	 */
	public com.top_logic.tl.ai.core.tools.refusal.messages.ApplicationRefusal setObject(String value) {
		internalSetObject(value);
		return this;
	}

	/** Internal setter for {@link #getObject()} without chain call utility. */
	protected final void internalSetObject(String value) {
		_object = value;
	}

	/**
	 * Checks, whether {@link #getObject()} has a value.
	 */
	public final boolean hasObject() {
		return _object != null;
	}

	/**
	 * The qualified name of the type of the object that was not created, if a creation was refused.
	 */
	public final String getType() {
		return _type;
	}

	/**
	 * @see #getType()
	 */
	public com.top_logic.tl.ai.core.tools.refusal.messages.ApplicationRefusal setType(String value) {
		internalSetType(value);
		return this;
	}

	/** Internal setter for {@link #getType()} without chain call utility. */
	protected final void internalSetType(String value) {
		_type = value;
	}

	/**
	 * Checks, whether {@link #getType()} has a value.
	 */
	public final boolean hasType() {
		return _type != null;
	}

	/**
	 * Why the application refused; empty if it gave no reason.
	 */
	public final java.util.List<com.top_logic.tl.ai.core.tools.refusal.messages.Reason> getReasons() {
		return _reasons;
	}

	/**
	 * @see #getReasons()
	 */
	public com.top_logic.tl.ai.core.tools.refusal.messages.ApplicationRefusal setReasons(java.util.List<? extends com.top_logic.tl.ai.core.tools.refusal.messages.Reason> value) {
		internalSetReasons(value);
		return this;
	}

	/** Internal setter for {@link #getReasons()} without chain call utility. */
	protected final void internalSetReasons(java.util.List<? extends com.top_logic.tl.ai.core.tools.refusal.messages.Reason> value) {
		if (value == null) throw new IllegalArgumentException("Property 'reasons' cannot be null.");
		_reasons.clear();
		_reasons.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getReasons()} list.
	 */
	public com.top_logic.tl.ai.core.tools.refusal.messages.ApplicationRefusal addReason(com.top_logic.tl.ai.core.tools.refusal.messages.Reason value) {
		internalAddReason(value);
		return this;
	}

	/** Implementation of {@link #addReason(com.top_logic.tl.ai.core.tools.refusal.messages.Reason)} without chain call utility. */
	protected final void internalAddReason(com.top_logic.tl.ai.core.tools.refusal.messages.Reason value) {
		_reasons.add(value);
	}

	/**
	 * Removes a value from the {@link #getReasons()} list.
	 */
	public final void removeReason(com.top_logic.tl.ai.core.tools.refusal.messages.Reason value) {
		_reasons.remove(value);
	}

	/**
	 * The mandatory attributes of the types of the objects that were not created.
	 */
	public final java.util.List<com.top_logic.tl.ai.core.tools.refusal.messages.MandatoryAttributes> getMandatory() {
		return _mandatory;
	}

	/**
	 * @see #getMandatory()
	 */
	public com.top_logic.tl.ai.core.tools.refusal.messages.ApplicationRefusal setMandatory(java.util.List<? extends com.top_logic.tl.ai.core.tools.refusal.messages.MandatoryAttributes> value) {
		internalSetMandatory(value);
		return this;
	}

	/** Internal setter for {@link #getMandatory()} without chain call utility. */
	protected final void internalSetMandatory(java.util.List<? extends com.top_logic.tl.ai.core.tools.refusal.messages.MandatoryAttributes> value) {
		if (value == null) throw new IllegalArgumentException("Property 'mandatory' cannot be null.");
		_mandatory.clear();
		_mandatory.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getMandatory()} list.
	 */
	public com.top_logic.tl.ai.core.tools.refusal.messages.ApplicationRefusal addMandatory(com.top_logic.tl.ai.core.tools.refusal.messages.MandatoryAttributes value) {
		internalAddMandatory(value);
		return this;
	}

	/** Implementation of {@link #addMandatory(com.top_logic.tl.ai.core.tools.refusal.messages.MandatoryAttributes)} without chain call utility. */
	protected final void internalAddMandatory(com.top_logic.tl.ai.core.tools.refusal.messages.MandatoryAttributes value) {
		_mandatory.add(value);
	}

	/**
	 * Removes a value from the {@link #getMandatory()} list.
	 */
	public final void removeMandatory(com.top_logic.tl.ai.core.tools.refusal.messages.MandatoryAttributes value) {
		_mandatory.remove(value);
	}

	@Override
	public String jsonType() {
		return APPLICATION_REFUSAL__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			REFUSED__PROP, 
			COMMAND__PROP, 
			INPUT__PROP, 
			OBJECT__PROP, 
			TYPE__PROP, 
			REASONS__PROP, 
			MANDATORY__PROP);
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
			case REFUSED__PROP: return isRefused();
			case COMMAND__PROP: return getCommand();
			case INPUT__PROP: return getInput();
			case OBJECT__PROP: return getObject();
			case TYPE__PROP: return getType();
			case REASONS__PROP: return getReasons();
			case MANDATORY__PROP: return getMandatory();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case REFUSED__PROP: internalSetRefused((boolean) value); break;
			case COMMAND__PROP: internalSetCommand((String) value); break;
			case INPUT__PROP: internalSetInput((String) value); break;
			case OBJECT__PROP: internalSetObject((String) value); break;
			case TYPE__PROP: internalSetType((String) value); break;
			case REASONS__PROP: internalSetReasons(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.tl.ai.core.tools.refusal.messages.Reason.class, value)); break;
			case MANDATORY__PROP: internalSetMandatory(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.tl.ai.core.tools.refusal.messages.MandatoryAttributes.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.refusal.messages.ApplicationRefusal readApplicationRefusal(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.refusal.messages.ApplicationRefusal result = new com.top_logic.tl.ai.core.tools.refusal.messages.ApplicationRefusal();
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
		out.name(REFUSED__PROP);
		out.value(isRefused());
		if (hasCommand()) {
			out.name(COMMAND__PROP);
			out.value(getCommand());
		}
		if (hasInput()) {
			out.name(INPUT__PROP);
			out.value(getInput());
		}
		if (hasObject()) {
			out.name(OBJECT__PROP);
			out.value(getObject());
		}
		if (hasType()) {
			out.name(TYPE__PROP);
			out.value(getType());
		}
		out.name(REASONS__PROP);
		out.beginArray();
		for (com.top_logic.tl.ai.core.tools.refusal.messages.Reason x : getReasons()) {
			x.writeTo(out);
		}
		out.endArray();
		out.name(MANDATORY__PROP);
		out.beginArray();
		for (com.top_logic.tl.ai.core.tools.refusal.messages.MandatoryAttributes x : getMandatory()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case REFUSED__PROP: setRefused(in.nextBoolean()); break;
			case COMMAND__PROP: setCommand(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case INPUT__PROP: setInput(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case OBJECT__PROP: setObject(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TYPE__PROP: setType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case REASONS__PROP: {
				java.util.List<com.top_logic.tl.ai.core.tools.refusal.messages.Reason> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.tl.ai.core.tools.refusal.messages.Reason.readReason(in));
				}
				in.endArray();
				setReasons(newValue);
			}
			break;
			case MANDATORY__PROP: {
				java.util.List<com.top_logic.tl.ai.core.tools.refusal.messages.MandatoryAttributes> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.tl.ai.core.tools.refusal.messages.MandatoryAttributes.readMandatoryAttributes(in));
				}
				in.endArray();
				setMandatory(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
