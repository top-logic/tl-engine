/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.react;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.model.search.expr.parser.ParseException;
import com.top_logic.model.search.expr.parser.SearchExpressionParser;
import com.top_logic.model.search.expr.parser.TokenMgrError;
import com.top_logic.model.search.ui.CodeCompletion;
import com.top_logic.model.search.ui.TLScriptCompletionService;
import com.top_logic.model.search.ui.TLScriptDocumentation;

/**
 * React control for editing TL-Script expressions using <code>CodeMirror</code> 6.
 *
 * <p>
 * Communicates with the {@code TLScriptEditor} React component via the commands
 * </p>
 * <ul>
 * <li>{@link #CMD_COMPLETE} - computes code completions with their documentation, answered in the
 * state {@link #COMPLETION_RESPONSE},</li>
 * <li>{@link #CMD_HOVER} - looks up the documentation of the token under the mouse, answered in the
 * state {@link #HOVER_RESPONSE},</li>
 * <li>{@link #CMD_VALIDATE} - parses the script and reports problems in the state
 * {@link #DIAGNOSTICS},</li>
 * <li>{@link #CMD_VALUE_CHANGED} - publishes the edited text to the value callback.</li>
 * </ul>
 *
 * <p>
 * The client shows the text the user types before it reaches the server. A value the server sets
 * while it publishes such an edit (e.g. the edited text in normalized form, reported back by the
 * field or channel the callback writes to) is therefore recorded in the control state without being
 * sent to the client: replacing the document the user is typing in would move the cursor and could
 * drop keystrokes typed in the meantime.
 * </p>
 */
public class TLScriptEditorReactControl extends ReactControl {

	/** Command publishing the edited text; argument {@link #VALUE}. */
	private static final String CMD_VALUE_CHANGED = "valueChanged";

	/** Command parsing a script; argument {@link #ARG_TEXT}, answer in {@link #DIAGNOSTICS}. */
	private static final String CMD_VALIDATE = "validate";

	/**
	 * Command computing completions; arguments {@link #ARG_LINE}, {@link #ARG_PREFIX},
	 * {@link #ARG_TEXT_TO_CURSOR} and {@link #ARG_REQUEST_ID}, answer in
	 * {@link #COMPLETION_RESPONSE}.
	 */
	private static final String CMD_COMPLETE = "complete";

	/**
	 * Command looking up the documentation of a token; arguments {@link #ARG_TOKEN} and
	 * {@link #ARG_REQUEST_ID}, answer in {@link #HOVER_RESPONSE}.
	 */
	private static final String CMD_HOVER = "hover";

	/** Argument of {@link #CMD_VALIDATE}: the script source to parse. */
	private static final String ARG_TEXT = "text";

	/** Argument of {@link #CMD_COMPLETE}: the text of the line up to the cursor. */
	private static final String ARG_LINE = "line";

	/** Argument of {@link #CMD_COMPLETE}: the part of the word before the cursor to complete. */
	private static final String ARG_PREFIX = "prefix";

	/** Argument of {@link #CMD_COMPLETE}: the whole script source up to the cursor. */
	private static final String ARG_TEXT_TO_CURSOR = "textToCursor";

	/**
	 * Argument of {@link #CMD_COMPLETE} and {@link #CMD_HOVER}: the client's identifier of the
	 * request, sent back in the answer so the client can drop outdated answers.
	 */
	private static final String ARG_REQUEST_ID = "requestId";

	/** Argument of {@link #CMD_HOVER}: the token to describe. */
	private static final String ARG_TOKEN = "token";

	/**
	 * State key of the editor text; also the argument of {@link #CMD_VALUE_CHANGED} carrying the
	 * edited text.
	 */
	private static final String VALUE = "value";

	/** State key of the flag whether the editor is read-only. */
	private static final String READ_ONLY = "readOnly";

	/** State key of the problems found by {@link #CMD_VALIDATE}. */
	private static final String DIAGNOSTICS = "diagnostics";

	/**
	 * State key of the answer to {@link #CMD_COMPLETE}: a map with {@link #ARG_REQUEST_ID} and
	 * {@link #COMPLETIONS}.
	 */
	private static final String COMPLETION_RESPONSE = "completionResponse";

	/** Entry of {@link #COMPLETION_RESPONSE}: the list of completion entries. */
	private static final String COMPLETIONS = "completions";

	/**
	 * State key of the answer to {@link #CMD_HOVER}: a map with {@link #ARG_REQUEST_ID} and, when
	 * the token has documentation, {@link #DOC_HTML}.
	 */
	private static final String HOVER_RESPONSE = "hoverResponse";

	/** Completion entry key: the label shown in the completion list. */
	private static final String COMPLETION_NAME = "name";

	/** Completion entry key: the text matched against the typed prefix. */
	private static final String COMPLETION_VALUE = "value";

	/** Completion entry key: the rank of the completion, higher is better. */
	private static final String COMPLETION_SCORE = "score";

	/** Completion entry key: the cursor position within {@link #COMPLETION_REPLACEMENT}. */
	private static final String COMPLETION_CURSOR_OFFSET = "cursorOffset";

	/** Completion entry key: the text to insert. */
	private static final String COMPLETION_REPLACEMENT = "replacement";

	/**
	 * Key of the HTML documentation, in a completion entry of {@link #COMPLETION_RESPONSE} and in
	 * {@link #HOVER_RESPONSE}.
	 */
	private static final String DOC_HTML = "docHTML";

	/** Diagnostic key: the one-based line of the problem. */
	private static final String DIAGNOSTIC_LINE = "line";

	/** Diagnostic key: the one-based column of the problem. */
	private static final String DIAGNOSTIC_COL = "col";

	/** Diagnostic key: the severity of the problem. */
	private static final String DIAGNOSTIC_SEVERITY = "severity";

	/** Diagnostic key: the problem description. */
	private static final String DIAGNOSTIC_MESSAGE = "message";

	/** Value of {@link #DIAGNOSTIC_SEVERITY} for a parse error. */
	private static final String SEVERITY_ERROR = "error";

	private static final Pattern TOKEN_MGR_ERROR_PATTERN =
		Pattern.compile("line (\\d+), column (\\d+)");

	private static final Pattern ACE_TAB_STOP_PATTERN = Pattern.compile("\\$\\d+");

	private Consumer<String> _valueCallback;

	private final List<String> _contextVariables;

	/**
	 * Whether the value callback is currently publishing a value the client sent.
	 *
	 * @see #setValue(String)
	 */
	private boolean _publishingClientValue;

	/**
	 * Creates a new {@link TLScriptEditorReactControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param value
	 *        The initial TL-Script text, may be {@code null}.
	 * @param readOnly
	 *        Whether the editor is read-only.
	 * @param contextVariables
	 *        Names of variables always in scope for completion; must not be {@code null} (an empty
	 *        list if there are none).
	 */
	public TLScriptEditorReactControl(ReactContext context, String value, boolean readOnly,
			List<String> contextVariables) {
		super(context, null, "TLScriptEditor");
		putState(VALUE, value != null ? value : "");
		putState(READ_ONLY, Boolean.valueOf(readOnly));
		_contextVariables = Objects.requireNonNull(contextVariables);
	}

	/**
	 * Sets the callback that is invoked when the value changes from the client.
	 *
	 * @param callback
	 *        The callback, or {@code null} to remove.
	 */
	public void setValueCallback(Consumer<String> callback) {
		_valueCallback = callback;
	}

	/**
	 * Updates the editor value from the server side.
	 *
	 * <p>
	 * A value set while the value callback publishes an edit of the client is only recorded, not
	 * sent: the client already shows the text the user typed.
	 * </p>
	 */
	public void setValue(String value) {
		if (_publishingClientValue) {
			updateStateSilently(() -> putState(VALUE, value));
			return;
		}
		putState(VALUE, value);
	}

	/** Updates the read-only state. */
	public void setReadOnly(boolean readOnly) {
		putState(READ_ONLY, Boolean.valueOf(readOnly));
	}

	@ReactCommandHandler(CMD_VALUE_CHANGED)
	void handleValueChanged(Map<String, Object> arguments) {
		if (Boolean.TRUE.equals(getState(READ_ONLY))) {
			// The editor displays its text without offering an edit; a value arriving nevertheless
			// comes from a client whose display lags behind the server, or from one sending an edit
			// the user interface does not offer. Drop it instead of publishing it to the callback.
			return;
		}
		Object rawValue = arguments.get(VALUE);
		String newValue = rawValue != null ? rawValue.toString() : null;
		updateStateSilently(() -> putState(VALUE, newValue != null ? newValue : ""));
		if (_valueCallback != null) {
			_publishingClientValue = true;
			try {
				_valueCallback.accept(newValue);
			} finally {
				_publishingClientValue = false;
			}
		}
	}

	@ReactCommandHandler(CMD_VALIDATE)
	void handleValidate(Map<String, Object> arguments) {
		String text = (String) arguments.get(ARG_TEXT);
		List<Map<String, Object>> diagnostics = computeDiagnostics(text);
		putState(DIAGNOSTICS, diagnostics);
	}

	@ReactCommandHandler(CMD_COMPLETE)
	void handleComplete(Map<String, Object> arguments) {
		String line = (String) arguments.get(ARG_LINE);
		String prefix = (String) arguments.get(ARG_PREFIX);
		String textToCursor = (String) arguments.get(ARG_TEXT_TO_CURSOR);
		String requestId = (String) arguments.get(ARG_REQUEST_ID);

		List<CodeCompletion> completions = TLScriptCompletionService.computeCompletions(displayContext(), line,
			prefix, textToCursor, _contextVariables, false);

		List<Map<String, Object>> items = new ArrayList<>();
		for (CodeCompletion c : completions) {
			Map<String, Object> entry = new HashMap<>();
			entry.put(COMPLETION_NAME, c.getName());
			entry.put(COMPLETION_VALUE, c.getValue());
			entry.put(COMPLETION_SCORE, Integer.valueOf(c.getScore()));
			String snippet = c.getSnippet();
			if (snippet != null) {
				Matcher tabStop = ACE_TAB_STOP_PATTERN.matcher(snippet);
				if (tabStop.find()) {
					// Where the first ACE tab stop ($1) sits in the inserted text, so the client can
					// place the cursor there (e.g. inside the parentheses of "all()").
					entry.put(COMPLETION_CURSOR_OFFSET, Integer.valueOf(tabStop.start()));
				}
				entry.put(COMPLETION_REPLACEMENT, ACE_TAB_STOP_PATTERN.matcher(snippet).replaceAll(""));
			}
			String docHTML = c.getDocHTML();
			if (docHTML != null && !docHTML.isEmpty()) {
				entry.put(DOC_HTML, docHTML);
			}
			items.add(entry);
		}

		Map<String, Object> response = new HashMap<>();
		response.put(ARG_REQUEST_ID, requestId);
		response.put(COMPLETIONS, items);
		putState(COMPLETION_RESPONSE, response);
	}

	@ReactCommandHandler(CMD_HOVER)
	void handleHover(Map<String, Object> arguments) {
		String token = (String) arguments.get(ARG_TOKEN);
		String requestId = (String) arguments.get(ARG_REQUEST_ID);

		Map<String, Object> response = new HashMap<>();
		response.put(ARG_REQUEST_ID, requestId);
		TLScriptDocumentation.documentation(displayContext(), token)
			.ifPresent(docHTML -> response.put(DOC_HTML, docHTML));
		putState(HOVER_RESPONSE, response);
	}

	/**
	 * The context of the current request, providing the locale of function documentation, or
	 * {@code null} when there is none.
	 */
	private static DisplayContext displayContext() {
		return DefaultDisplayContext.hasDisplayContext() ? DefaultDisplayContext.getDisplayContext() : null;
	}

	private List<Map<String, Object>> computeDiagnostics(String text) {
		if (text == null || text.isBlank()) {
			return Collections.emptyList();
		}
		try {
			SearchExpressionParser parser =
				new SearchExpressionParser(new StringReader(text));
			parser.expr();
			return Collections.emptyList();
		} catch (ParseException ex) {
			if (ex.currentToken != null && ex.currentToken.next != null) {
				return diagnostic(ex.currentToken.next.beginLine, ex.currentToken.next.beginColumn, ex.getMessage());
			}
			return diagnostic(1, 1, ex.getMessage());
		} catch (TokenMgrError ex) {
			int errorLine = 1;
			int errorCol = 1;
			Matcher m = TOKEN_MGR_ERROR_PATTERN.matcher(ex.getMessage());
			if (m.find()) {
				errorLine = Integer.parseInt(m.group(1));
				errorCol = Integer.parseInt(m.group(2));
			}
			return diagnostic(errorLine, errorCol, ex.getMessage());
		}
	}

	private static List<Map<String, Object>> diagnostic(int line, int col, String message) {
		Map<String, Object> diag = new HashMap<>();
		diag.put(DIAGNOSTIC_LINE, Integer.valueOf(line));
		diag.put(DIAGNOSTIC_COL, Integer.valueOf(col));
		diag.put(DIAGNOSTIC_SEVERITY, SEVERITY_ERROR);
		diag.put(DIAGNOSTIC_MESSAGE, message);
		return Collections.singletonList(diag);
	}
}
