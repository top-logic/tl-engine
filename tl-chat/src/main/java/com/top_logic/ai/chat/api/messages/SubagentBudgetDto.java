package com.top_logic.ai.chat.api.messages;

/**
 * The budget a turn's subagents spend from: the allowance of the run that started them, which they
 * share, as its headroom when that run started.
 */
public class SubagentBudgetDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.SubagentBudgetDto} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.SubagentBudgetDto create() {
		return new com.top_logic.ai.chat.api.messages.SubagentBudgetDto();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.SubagentBudgetDto} type in JSON format. */
	public static final String SUBAGENT_BUDGET_DTO__TYPE = "SubagentBudgetDto";

	/** @see #getTokens() */
	public static final String TOKENS__PROP = "tokens";

	/** @see #getMoney() */
	public static final String MONEY__PROP = "money";

	/** @see #getCurrency() */
	public static final String CURRENCY__PROP = "currency";

	private long _tokens = 0L;

	private double _money = 0.0d;

	private String _currency = "";

	/**
	 * Creates a {@link SubagentBudgetDto} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.SubagentBudgetDto#create()
	 */
	protected SubagentBudgetDto() {
		super();
	}

	/**
	 * Token headroom; -1 when the run bounds no tokens.
	 */
	public final long getTokens() {
		return _tokens;
	}

	/**
	 * @see #getTokens()
	 */
	public com.top_logic.ai.chat.api.messages.SubagentBudgetDto setTokens(long value) {
		internalSetTokens(value);
		return this;
	}

	/** Internal setter for {@link #getTokens()} without chain call utility. */
	protected final void internalSetTokens(long value) {
		_tokens = value;
	}

	/**
	 * Money headroom, in {@link #getCurrency()}; -1 when the run bounds no money.
	 */
	public final double getMoney() {
		return _money;
	}

	/**
	 * @see #getMoney()
	 */
	public com.top_logic.ai.chat.api.messages.SubagentBudgetDto setMoney(double value) {
		internalSetMoney(value);
		return this;
	}

	/** Internal setter for {@link #getMoney()} without chain call utility. */
	protected final void internalSetMoney(double value) {
		_money = value;
	}

	/**
	 * The ISO 4217 code of {@link #getMoney()}; empty when the run bounds no money.
	 */
	public final String getCurrency() {
		return _currency;
	}

	/**
	 * @see #getCurrency()
	 */
	public com.top_logic.ai.chat.api.messages.SubagentBudgetDto setCurrency(String value) {
		internalSetCurrency(value);
		return this;
	}

	/** Internal setter for {@link #getCurrency()} without chain call utility. */
	protected final void internalSetCurrency(String value) {
		_currency = value;
	}

	@Override
	public String jsonType() {
		return SUBAGENT_BUDGET_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TOKENS__PROP, 
			MONEY__PROP, 
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
			case TOKENS__PROP: return getTokens();
			case MONEY__PROP: return getMoney();
			case CURRENCY__PROP: return getCurrency();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TOKENS__PROP: internalSetTokens((long) value); break;
			case MONEY__PROP: internalSetMoney((double) value); break;
			case CURRENCY__PROP: internalSetCurrency((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.SubagentBudgetDto readSubagentBudgetDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.SubagentBudgetDto result = new com.top_logic.ai.chat.api.messages.SubagentBudgetDto();
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
		out.name(TOKENS__PROP);
		out.value(getTokens());
		out.name(MONEY__PROP);
		out.value(getMoney());
		out.name(CURRENCY__PROP);
		out.value(getCurrency());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TOKENS__PROP: setTokens(in.nextLong()); break;
			case MONEY__PROP: setMoney(in.nextDouble()); break;
			case CURRENCY__PROP: setCurrency(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
