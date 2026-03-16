package com.top_logic.ai.service.tools.gitea.messages;

/**
 * JSON payload for the Gitea issue creation API.
 */
public class CreateIssueRequest extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.service.tools.gitea.messages.CreateIssueRequest} instance.
	 */
	public static com.top_logic.ai.service.tools.gitea.messages.CreateIssueRequest create() {
		return new com.top_logic.ai.service.tools.gitea.messages.CreateIssueRequest();
	}

	/** Identifier for the {@link com.top_logic.ai.service.tools.gitea.messages.CreateIssueRequest} type in JSON format. */
	public static final String CREATE_ISSUE_REQUEST__TYPE = "CreateIssueRequest";

	/** @see #getTitle() */
	public static final String TITLE__PROP = "title";

	/** @see #getBody() */
	public static final String BODY__PROP = "body";

	private String _title = "";

	private String _body = "";

	/**
	 * Creates a {@link CreateIssueRequest} instance.
	 *
	 * @see com.top_logic.ai.service.tools.gitea.messages.CreateIssueRequest#create()
	 */
	protected CreateIssueRequest() {
		super();
	}

	/**
	 * The issue title.
	 */
	public final String getTitle() {
		return _title;
	}

	/**
	 * @see #getTitle()
	 */
	public com.top_logic.ai.service.tools.gitea.messages.CreateIssueRequest setTitle(String value) {
		internalSetTitle(value);
		return this;
	}

	/** Internal setter for {@link #getTitle()} without chain call utility. */
	protected final void internalSetTitle(String value) {
		_title = value;
	}

	/**
	 * The issue body in Markdown format.
	 */
	public final String getBody() {
		return _body;
	}

	/**
	 * @see #getBody()
	 */
	public com.top_logic.ai.service.tools.gitea.messages.CreateIssueRequest setBody(String value) {
		internalSetBody(value);
		return this;
	}

	/** Internal setter for {@link #getBody()} without chain call utility. */
	protected final void internalSetBody(String value) {
		_body = value;
	}

	@Override
	public String jsonType() {
		return CREATE_ISSUE_REQUEST__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TITLE__PROP, 
			BODY__PROP);
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
			case TITLE__PROP: return getTitle();
			case BODY__PROP: return getBody();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TITLE__PROP: internalSetTitle((String) value); break;
			case BODY__PROP: internalSetBody((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.service.tools.gitea.messages.CreateIssueRequest readCreateIssueRequest(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.service.tools.gitea.messages.CreateIssueRequest result = new com.top_logic.ai.service.tools.gitea.messages.CreateIssueRequest();
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
		out.name(TITLE__PROP);
		out.value(getTitle());
		out.name(BODY__PROP);
		out.value(getBody());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TITLE__PROP: setTitle(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case BODY__PROP: setBody(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
