package com.top_logic.tl.ai.core.tools.model.messages;

/**
 * Paginated list of instances.
 */
public class InstanceListResponse extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.model.messages.InstanceListResponse} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.model.messages.InstanceListResponse create() {
		return new com.top_logic.tl.ai.core.tools.model.messages.InstanceListResponse();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.model.messages.InstanceListResponse} type in JSON format. */
	public static final String INSTANCE_LIST_RESPONSE__TYPE = "InstanceListResponse";

	/** @see #getModule() */
	public static final String MODULE__PROP = "module";

	/** @see #getType() */
	public static final String TYPE__PROP = "type";

	/** @see #getTotalCount() */
	public static final String TOTAL_COUNT__PROP = "totalCount";

	/** @see #getLimit() */
	public static final String LIMIT__PROP = "limit";

	/** @see #getOffset() */
	public static final String OFFSET__PROP = "offset";

	/** @see #getInstances() */
	public static final String INSTANCES__PROP = "instances";

	/** @see #getNextCursor() */
	public static final String NEXT_CURSOR__PROP = "nextCursor";

	/** @see #isHasMore() */
	public static final String HAS_MORE__PROP = "hasMore";

	/** @see #getHint() */
	public static final String HINT__PROP = "hint";

	private String _module = "";

	private String _type = "";

	private int _totalCount = 0;

	private int _limit = 0;

	private int _offset = 0;

	private final java.util.List<com.top_logic.tl.ai.core.tools.model.messages.InstanceEntry> _instances = new java.util.ArrayList<>();

	private String _nextCursor = "";

	private boolean _hasMore = false;

	private String _hint = "";

	/**
	 * Creates a {@link InstanceListResponse} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.model.messages.InstanceListResponse#create()
	 */
	protected InstanceListResponse() {
		super();
	}

	/**
	 * Module name.
	 */
	public final String getModule() {
		return _module;
	}

	/**
	 * @see #getModule()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.InstanceListResponse setModule(String value) {
		internalSetModule(value);
		return this;
	}

	/** Internal setter for {@link #getModule()} without chain call utility. */
	protected final void internalSetModule(String value) {
		_module = value;
	}

	/**
	 * Type name.
	 */
	public final String getType() {
		return _type;
	}

	/**
	 * @see #getType()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.InstanceListResponse setType(String value) {
		internalSetType(value);
		return this;
	}

	/** Internal setter for {@link #getType()} without chain call utility. */
	protected final void internalSetType(String value) {
		_type = value;
	}

	/**
	 * Total number of instances.
	 */
	public final int getTotalCount() {
		return _totalCount;
	}

	/**
	 * @see #getTotalCount()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.InstanceListResponse setTotalCount(int value) {
		internalSetTotalCount(value);
		return this;
	}

	/** Internal setter for {@link #getTotalCount()} without chain call utility. */
	protected final void internalSetTotalCount(int value) {
		_totalCount = value;
	}

	/**
	 * Requested page size limit.
	 */
	public final int getLimit() {
		return _limit;
	}

	/**
	 * @see #getLimit()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.InstanceListResponse setLimit(int value) {
		internalSetLimit(value);
		return this;
	}

	/** Internal setter for {@link #getLimit()} without chain call utility. */
	protected final void internalSetLimit(int value) {
		_limit = value;
	}

	/**
	 * Current offset in the result set.
	 */
	public final int getOffset() {
		return _offset;
	}

	/**
	 * @see #getOffset()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.InstanceListResponse setOffset(int value) {
		internalSetOffset(value);
		return this;
	}

	/** Internal setter for {@link #getOffset()} without chain call utility. */
	protected final void internalSetOffset(int value) {
		_offset = value;
	}

	/**
	 * The instances of this page.
	 */
	public final java.util.List<com.top_logic.tl.ai.core.tools.model.messages.InstanceEntry> getInstances() {
		return _instances;
	}

	/**
	 * @see #getInstances()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.InstanceListResponse setInstances(java.util.List<? extends com.top_logic.tl.ai.core.tools.model.messages.InstanceEntry> value) {
		internalSetInstances(value);
		return this;
	}

	/** Internal setter for {@link #getInstances()} without chain call utility. */
	protected final void internalSetInstances(java.util.List<? extends com.top_logic.tl.ai.core.tools.model.messages.InstanceEntry> value) {
		if (value == null) throw new IllegalArgumentException("Property 'instances' cannot be null.");
		_instances.clear();
		_instances.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getInstances()} list.
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.InstanceListResponse addInstance(com.top_logic.tl.ai.core.tools.model.messages.InstanceEntry value) {
		internalAddInstance(value);
		return this;
	}

	/** Implementation of {@link #addInstance(com.top_logic.tl.ai.core.tools.model.messages.InstanceEntry)} without chain call utility. */
	protected final void internalAddInstance(com.top_logic.tl.ai.core.tools.model.messages.InstanceEntry value) {
		_instances.add(value);
	}

	/**
	 * Removes a value from the {@link #getInstances()} list.
	 */
	public final void removeInstance(com.top_logic.tl.ai.core.tools.model.messages.InstanceEntry value) {
		_instances.remove(value);
	}

	/**
	 * Cursor for the next page (null if no more pages).
	 */
	public final String getNextCursor() {
		return _nextCursor;
	}

	/**
	 * @see #getNextCursor()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.InstanceListResponse setNextCursor(String value) {
		internalSetNextCursor(value);
		return this;
	}

	/** Internal setter for {@link #getNextCursor()} without chain call utility. */
	protected final void internalSetNextCursor(String value) {
		_nextCursor = value;
	}

	/**
	 * Whether there are more instances beyond this page.
	 */
	public final boolean isHasMore() {
		return _hasMore;
	}

	/**
	 * @see #isHasMore()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.InstanceListResponse setHasMore(boolean value) {
		internalSetHasMore(value);
		return this;
	}

	/** Internal setter for {@link #isHasMore()} without chain call utility. */
	protected final void internalSetHasMore(boolean value) {
		_hasMore = value;
	}

	/**
	 * Set when there are more instances than one page: says to select, sort or aggregate with a
	 * query rather than paging through all of them.
	 */
	public final String getHint() {
		return _hint;
	}

	/**
	 * @see #getHint()
	 */
	public com.top_logic.tl.ai.core.tools.model.messages.InstanceListResponse setHint(String value) {
		internalSetHint(value);
		return this;
	}

	/** Internal setter for {@link #getHint()} without chain call utility. */
	protected final void internalSetHint(String value) {
		_hint = value;
	}

	@Override
	public String jsonType() {
		return INSTANCE_LIST_RESPONSE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			MODULE__PROP, 
			TYPE__PROP, 
			TOTAL_COUNT__PROP, 
			LIMIT__PROP, 
			OFFSET__PROP, 
			INSTANCES__PROP, 
			NEXT_CURSOR__PROP, 
			HAS_MORE__PROP, 
			HINT__PROP);
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
			case MODULE__PROP: return getModule();
			case TYPE__PROP: return getType();
			case TOTAL_COUNT__PROP: return getTotalCount();
			case LIMIT__PROP: return getLimit();
			case OFFSET__PROP: return getOffset();
			case INSTANCES__PROP: return getInstances();
			case NEXT_CURSOR__PROP: return getNextCursor();
			case HAS_MORE__PROP: return isHasMore();
			case HINT__PROP: return getHint();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case MODULE__PROP: internalSetModule((String) value); break;
			case TYPE__PROP: internalSetType((String) value); break;
			case TOTAL_COUNT__PROP: internalSetTotalCount((int) value); break;
			case LIMIT__PROP: internalSetLimit((int) value); break;
			case OFFSET__PROP: internalSetOffset((int) value); break;
			case INSTANCES__PROP: internalSetInstances(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.tl.ai.core.tools.model.messages.InstanceEntry.class, value)); break;
			case NEXT_CURSOR__PROP: internalSetNextCursor((String) value); break;
			case HAS_MORE__PROP: internalSetHasMore((boolean) value); break;
			case HINT__PROP: internalSetHint((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.model.messages.InstanceListResponse readInstanceListResponse(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.model.messages.InstanceListResponse result = new com.top_logic.tl.ai.core.tools.model.messages.InstanceListResponse();
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
		out.name(MODULE__PROP);
		out.value(getModule());
		out.name(TYPE__PROP);
		out.value(getType());
		out.name(TOTAL_COUNT__PROP);
		out.value(getTotalCount());
		out.name(LIMIT__PROP);
		out.value(getLimit());
		out.name(OFFSET__PROP);
		out.value(getOffset());
		out.name(INSTANCES__PROP);
		out.beginArray();
		for (com.top_logic.tl.ai.core.tools.model.messages.InstanceEntry x : getInstances()) {
			x.writeTo(out);
		}
		out.endArray();
		out.name(NEXT_CURSOR__PROP);
		out.value(getNextCursor());
		out.name(HAS_MORE__PROP);
		out.value(isHasMore());
		out.name(HINT__PROP);
		out.value(getHint());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case MODULE__PROP: setModule(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TYPE__PROP: setType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TOTAL_COUNT__PROP: setTotalCount(in.nextInt()); break;
			case LIMIT__PROP: setLimit(in.nextInt()); break;
			case OFFSET__PROP: setOffset(in.nextInt()); break;
			case INSTANCES__PROP: {
				java.util.List<com.top_logic.tl.ai.core.tools.model.messages.InstanceEntry> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.tl.ai.core.tools.model.messages.InstanceEntry.readInstanceEntry(in));
				}
				in.endArray();
				setInstances(newValue);
			}
			break;
			case NEXT_CURSOR__PROP: setNextCursor(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case HAS_MORE__PROP: setHasMore(in.nextBoolean()); break;
			case HINT__PROP: setHint(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
