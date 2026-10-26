package com.top_logic.ai.chat.api.messages;

/**
 * What the history of a conversation knows about one of its runs.
 */
public class RecordedRunDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.RecordedRunDto} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.RecordedRunDto create() {
		return new com.top_logic.ai.chat.api.messages.RecordedRunDto();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.RecordedRunDto} type in JSON format. */
	public static final String RECORDED_RUN_DTO__TYPE = "RecordedRunDto";

	/** @see #getTurnId() */
	public static final String TURN_ID__PROP = "turnId";

	/** @see #getStartedAt() */
	public static final String STARTED_AT__PROP = "startedAt";

	/** @see #getEndedAt() */
	public static final String ENDED_AT__PROP = "endedAt";

	/** @see #getCost() */
	public static final String COST__PROP = "cost";

	/** @see #getCurrency() */
	public static final String CURRENCY__PROP = "currency";

	private String _turnId = "";

	private long _startedAt = 0L;

	private long _endedAt = 0L;

	private double _cost = 0.0d;

	private String _currency = "";

	/**
	 * Creates a {@link RecordedRunDto} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.RecordedRunDto#create()
	 */
	protected RecordedRunDto() {
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
	public com.top_logic.ai.chat.api.messages.RecordedRunDto setTurnId(String value) {
		internalSetTurnId(value);
		return this;
	}

	/** Internal setter for {@link #getTurnId()} without chain call utility. */
	protected final void internalSetTurnId(String value) {
		_turnId = value;
	}

	/**
	 * When the run started, in milliseconds since the epoch; zero if not recorded.
	 *
	 * <p>
	 * The moment the run reported its start, which is what a live chat measures the run from: a
	 * turn starts with the user's message, possibly before the process answering it is running.
	 * </p>
	 */
	public final long getStartedAt() {
		return _startedAt;
	}

	/**
	 * @see #getStartedAt()
	 */
	public com.top_logic.ai.chat.api.messages.RecordedRunDto setStartedAt(long value) {
		internalSetStartedAt(value);
		return this;
	}

	/** Internal setter for {@link #getStartedAt()} without chain call utility. */
	protected final void internalSetStartedAt(long value) {
		_startedAt = value;
	}

	/**
	 * When the run ended, in milliseconds since the epoch; zero while it has not ended.
	 */
	public final long getEndedAt() {
		return _endedAt;
	}

	/**
	 * @see #getEndedAt()
	 */
	public com.top_logic.ai.chat.api.messages.RecordedRunDto setEndedAt(long value) {
		internalSetEndedAt(value);
		return this;
	}

	/** Internal setter for {@link #getEndedAt()} without chain call utility. */
	protected final void internalSetEndedAt(long value) {
		_endedAt = value;
	}

	/**
	 * The cost in {@link #getCurrency()}; zero without a {@link #getCurrency()}.
	 */
	public final double getCost() {
		return _cost;
	}

	/**
	 * @see #getCost()
	 */
	public com.top_logic.ai.chat.api.messages.RecordedRunDto setCost(double value) {
		internalSetCost(value);
		return this;
	}

	/** Internal setter for {@link #getCost()} without chain call utility. */
	protected final void internalSetCost(double value) {
		_cost = value;
	}

	/**
	 * The ISO 4217 code of the currency of {@link #getCost()}; empty if the run reported no cost.
	 */
	public final String getCurrency() {
		return _currency;
	}

	/**
	 * @see #getCurrency()
	 */
	public com.top_logic.ai.chat.api.messages.RecordedRunDto setCurrency(String value) {
		internalSetCurrency(value);
		return this;
	}

	/** Internal setter for {@link #getCurrency()} without chain call utility. */
	protected final void internalSetCurrency(String value) {
		_currency = value;
	}

	@Override
	public String jsonType() {
		return RECORDED_RUN_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TURN_ID__PROP, 
			STARTED_AT__PROP, 
			ENDED_AT__PROP, 
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
			case STARTED_AT__PROP: return getStartedAt();
			case ENDED_AT__PROP: return getEndedAt();
			case COST__PROP: return getCost();
			case CURRENCY__PROP: return getCurrency();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TURN_ID__PROP: internalSetTurnId((String) value); break;
			case STARTED_AT__PROP: internalSetStartedAt((long) value); break;
			case ENDED_AT__PROP: internalSetEndedAt((long) value); break;
			case COST__PROP: internalSetCost((double) value); break;
			case CURRENCY__PROP: internalSetCurrency((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.RecordedRunDto readRecordedRunDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.RecordedRunDto result = new com.top_logic.ai.chat.api.messages.RecordedRunDto();
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
		out.name(STARTED_AT__PROP);
		out.value(getStartedAt());
		out.name(ENDED_AT__PROP);
		out.value(getEndedAt());
		out.name(COST__PROP);
		out.value(getCost());
		out.name(CURRENCY__PROP);
		out.value(getCurrency());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TURN_ID__PROP: setTurnId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case STARTED_AT__PROP: setStartedAt(in.nextLong()); break;
			case ENDED_AT__PROP: setEndedAt(in.nextLong()); break;
			case COST__PROP: setCost(in.nextDouble()); break;
			case CURRENCY__PROP: setCurrency(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
