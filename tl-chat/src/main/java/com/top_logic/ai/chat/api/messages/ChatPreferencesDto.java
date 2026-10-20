package com.top_logic.ai.chat.api.messages;

/**
 * Per-user layout of the right rail and artefact pane, for one chat.
 *
 * <p>
 * This is what the user decided, not what the page currently shows. A fact the user never touched
 * is absent, and the element's configured default applies to it - so a rail configured
 * <code>open="true"</code> opens for a new user and stays closed for one who closed it (#423).
 * </p>
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

	/** @see #getRailOpen() */
	public static final String RAIL_OPEN__PROP = "railOpen";

	/** @see #getPaneWidth() */
	public static final String PANE_WIDTH__PROP = "paneWidth";

	/** @see #getBadgeSeen() */
	public static final String BADGE_SEEN__PROP = "badgeSeen";

	/** @see #getSections() */
	public static final String SECTIONS__PROP = "sections";

	private Boolean _railOpen = null;

	private int _paneWidth = 0;

	private int _badgeSeen = 0;

	private final java.util.Map<String, Boolean> _sections = new java.util.LinkedHashMap<>();

	/**
	 * Creates a {@link ChatPreferencesDto} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.ChatPreferencesDto#create()
	 */
	protected ChatPreferencesDto() {
		super();
	}

	/**
	 * Whether the right rail is open, or null while the user has not decided.
	 */
	public final Boolean getRailOpen() {
		return _railOpen;
	}

	/**
	 * @see #getRailOpen()
	 */
	public com.top_logic.ai.chat.api.messages.ChatPreferencesDto setRailOpen(Boolean value) {
		internalSetRailOpen(value);
		return this;
	}

	/** Internal setter for {@link #getRailOpen()} without chain call utility. */
	protected final void internalSetRailOpen(Boolean value) {
		_railOpen = value;
	}

	/**
	 * Checks, whether {@link #getRailOpen()} has a value.
	 */
	public final boolean hasRailOpen() {
		return _railOpen != null;
	}

	/**
	 * Width of the artefact pane in pixels; 0 means the configured or built-in default.
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

	/**
	 * Per section name, whether the user left that section open.
	 *
	 * <p>
	 * A section the user never toggled is absent and follows the <code>open</code> flag its
	 * configuration gives it.
	 * </p>
	 */
	public final java.util.Map<String, Boolean> getSections() {
		return _sections;
	}

	/**
	 * @see #getSections()
	 */
	public com.top_logic.ai.chat.api.messages.ChatPreferencesDto setSections(java.util.Map<String, Boolean> value) {
		internalSetSections(value);
		return this;
	}

	/** Internal setter for {@link #getSections()} without chain call utility. */
	protected final void internalSetSections(java.util.Map<String, Boolean> value) {
		if (value == null) throw new IllegalArgumentException("Property 'sections' cannot be null.");
		_sections.clear();
		_sections.putAll(value);
	}

	/**
	 * Adds a key value pair to the {@link #getSections()} map.
	 */
	public com.top_logic.ai.chat.api.messages.ChatPreferencesDto putSection(String key, boolean value) {
		internalPutSection(key, value);
		return this;
	}

	/** Implementation of {@link #putSection(String, boolean)} without chain call utility. */
	protected final void  internalPutSection(String key, boolean value) {
		if (_sections.containsKey(key)) {
			throw new IllegalArgumentException("Property 'sections' already contains a value for key '" + key + "'.");
		}
		_sections.put(key, value);
	}

	/**
	 * Removes a key from the {@link #getSections()} map.
	 */
	public final void removeSection(String key) {
		_sections.remove(key);
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
			BADGE_SEEN__PROP, 
			SECTIONS__PROP);
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
			case RAIL_OPEN__PROP: return getRailOpen();
			case PANE_WIDTH__PROP: return getPaneWidth();
			case BADGE_SEEN__PROP: return getBadgeSeen();
			case SECTIONS__PROP: return getSections();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case RAIL_OPEN__PROP: internalSetRailOpen((Boolean) value); break;
			case PANE_WIDTH__PROP: internalSetPaneWidth((int) value); break;
			case BADGE_SEEN__PROP: internalSetBadgeSeen((int) value); break;
			case SECTIONS__PROP: internalSetSections((java.util.Map<String, Boolean>) value); break;
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
		if (hasRailOpen()) {
			out.name(RAIL_OPEN__PROP);
			out.value(getRailOpen());
		}
		out.name(PANE_WIDTH__PROP);
		out.value(getPaneWidth());
		out.name(BADGE_SEEN__PROP);
		out.value(getBadgeSeen());
		out.name(SECTIONS__PROP);
		out.beginObject();
		for (java.util.Map.Entry<String,Boolean> entry : getSections().entrySet()) {
			out.name(entry.getKey());
			out.value(entry.getValue());
		}
		out.endObject();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case RAIL_OPEN__PROP: setRailOpen(in.nextBoolean()); break;
			case PANE_WIDTH__PROP: setPaneWidth(in.nextInt()); break;
			case BADGE_SEEN__PROP: setBadgeSeen(in.nextInt()); break;
			case SECTIONS__PROP: {
				java.util.Map<String, Boolean> newValue = new java.util.LinkedHashMap<>();
				in.beginObject();
				while (in.hasNext()) {
					newValue.put(in.nextName(), in.nextBoolean());
				}
				in.endObject();
				setSections(newValue);
				break;
			}
			default: super.readField(in, field);
		}
	}

}
