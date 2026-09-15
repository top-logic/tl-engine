package com.top_logic.ai.chat.api.messages;

/**
 * Per-user layout of the right rail and artefact pane.
 */
public class ChatPreferencesDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.ChatPreferencesDto} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.ChatPreferencesDto create() {
		return new com.top_logic.ai.chat.api.messages.ChatPreferencesDto();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.ChatPreferencesDto} type in JSON format. */
	public static final String CHAT_PREFERENCES_DTO__TYPE = "ChatPreferencesDto";

	/** @see #isRailOpen() */
	public static final String RAIL_OPEN__PROP = "railOpen";

	/** @see #getPaneWidth() */
	public static final String PANE_WIDTH__PROP = "paneWidth";

	/** @see #getBadgeSeen() */
	public static final String BADGE_SEEN__PROP = "badgeSeen";

	private boolean _railOpen = false;

	private int _paneWidth = 0;

	private int _badgeSeen = 0;

	/**
	 * Creates a {@link ChatPreferencesDto} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.ChatPreferencesDto#create()
	 */
	protected ChatPreferencesDto() {
		super();
	}

	/**
	 * Whether the right rail is open.
	 */
	public final boolean isRailOpen() {
		return _railOpen;
	}

	/**
	 * @see #isRailOpen()
	 */
	public com.top_logic.ai.chat.api.messages.ChatPreferencesDto setRailOpen(boolean value) {
		internalSetRailOpen(value);
		return this;
	}

	/** Internal setter for {@link #isRailOpen()} without chain call utility. */
	protected final void internalSetRailOpen(boolean value) {
		_railOpen = value;
	}

	/**
	 * Width of the artefact pane in pixels; 0 means the default.
	 */
	public final int getPaneWidth() {
		return _paneWidth;
	}

	/**
	 * @see #getPaneWidth()
	 */
	public com.top_logic.ai.chat.api.messages.ChatPreferencesDto setPaneWidth(int value) {
		internalSetPaneWidth(value);
		return this;
	}

	/** Internal setter for {@link #getPaneWidth()} without chain call utility. */
	protected final void internalSetPaneWidth(int value) {
		_paneWidth = value;
	}

	/**
	 * Sum of section badges last stored when the rail was opened.
	 */
	public final int getBadgeSeen() {
		return _badgeSeen;
	}

	/**
	 * @see #getBadgeSeen()
	 */
	public com.top_logic.ai.chat.api.messages.ChatPreferencesDto setBadgeSeen(int value) {
		internalSetBadgeSeen(value);
		return this;
	}

	/** Internal setter for {@link #getBadgeSeen()} without chain call utility. */
	protected final void internalSetBadgeSeen(int value) {
		_badgeSeen = value;
	}

	@Override
	public String jsonType() {
		return CHAT_PREFERENCES_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			RAIL_OPEN__PROP, 
			PANE_WIDTH__PROP, 
			BADGE_SEEN__PROP);
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
			case RAIL_OPEN__PROP: return isRailOpen();
			case PANE_WIDTH__PROP: return getPaneWidth();
			case BADGE_SEEN__PROP: return getBadgeSeen();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case RAIL_OPEN__PROP: internalSetRailOpen((boolean) value); break;
			case PANE_WIDTH__PROP: internalSetPaneWidth((int) value); break;
			case BADGE_SEEN__PROP: internalSetBadgeSeen((int) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.ChatPreferencesDto readChatPreferencesDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.ChatPreferencesDto result = new com.top_logic.ai.chat.api.messages.ChatPreferencesDto();
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
		out.name(RAIL_OPEN__PROP);
		out.value(isRailOpen());
		out.name(PANE_WIDTH__PROP);
		out.value(getPaneWidth());
		out.name(BADGE_SEEN__PROP);
		out.value(getBadgeSeen());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case RAIL_OPEN__PROP: setRailOpen(in.nextBoolean()); break;
			case PANE_WIDTH__PROP: setPaneWidth(in.nextInt()); break;
			case BADGE_SEEN__PROP: setBadgeSeen(in.nextInt()); break;
			default: super.readField(in, field);
		}
	}

}
