package com.top_logic.ai.chat.api.messages;

/**
 * What one run of a conversation cost.
 */
public class RunCostDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.RunCostDto} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.RunCostDto create() {
		return new com.top_logic.ai.chat.api.messages.RunCostDto();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.RunCostDto} type in JSON format. */
	public static final String RUN_COST_DTO__TYPE = "RunCostDto";

	/** @see #getTurnId() */
	public static final String TURN_ID__PROP = "turnId";

	/** @see #getCost() */
	public static final String COST__PROP = "cost";

	/** @see #getCurrency() */
	public static final String CURRENCY__PROP = "currency";

	private String _turnId = "";

	private double _cost = 0.0d;

	private String _currency = "";

	/**
	 * Creates a {@link RunCostDto} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.RunCostDto#create()
	 */
	protected RunCostDto() {
		super();
	}

	/**
	 * ID of the last turn the run wrote, as in <code>turnsJson</code>.
	 */
	public final String getTurnId() {
		return _turnId;
	}

	/**
	 * @see #getTurnId()
	 */
	public com.top_logic.ai.chat.api.messages.RunCostDto setTurnId(String value) {
		internalSetTurnId(value);
		return this;
	}

	/** Internal setter for {@link #getTurnId()} without chain call utility. */
	protected final void internalSetTurnId(String value) {
		_turnId = value;
	}

	/**
	 * The cost in {@link #getCurrency()}.
	 */
	public final double getCost() {
		return _cost;
	}

	/**
	 * @see #getCost()
	 */
	public com.top_logic.ai.chat.api.messages.RunCostDto setCost(double value) {
		internalSetCost(value);
		return this;
	}

	/** Internal setter for {@link #getCost()} without chain call utility. */
	protected final void internalSetCost(double value) {
		_cost = value;
	}

	/**
	 * The ISO 4217 code of the currency of {@link #getCost()}.
	 */
	public final String getCurrency() {
		return _currency;
	}

	/**
	 * @see #getCurrency()
	 */
	public com.top_logic.ai.chat.api.messages.RunCostDto setCurrency(String value) {
		internalSetCurrency(value);
		return this;
	}

	/** Internal setter for {@link #getCurrency()} without chain call utility. */
	protected final void internalSetCurrency(String value) {
		_currency = value;
	}

	@Override
	public String jsonType() {
		return RUN_COST_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TURN_ID__PROP, 
			COST__PROP, 
			CURRENCY__PROP);
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
			case TURN_ID__PROP: return getTurnId();
			case COST__PROP: return getCost();
			case CURRENCY__PROP: return getCurrency();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TURN_ID__PROP: internalSetTurnId((String) value); break;
			case COST__PROP: internalSetCost((double) value); break;
			case CURRENCY__PROP: internalSetCurrency((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.RunCostDto readRunCostDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.RunCostDto result = new com.top_logic.ai.chat.api.messages.RunCostDto();
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
		out.name(TURN_ID__PROP);
		out.value(getTurnId());
		out.name(COST__PROP);
		out.value(getCost());
		out.name(CURRENCY__PROP);
		out.value(getCurrency());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TURN_ID__PROP: setTurnId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case COST__PROP: setCost(in.nextDouble()); break;
			case CURRENCY__PROP: setCurrency(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
