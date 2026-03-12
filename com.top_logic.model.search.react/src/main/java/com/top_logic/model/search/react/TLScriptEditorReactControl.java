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
import java.util.function.Consumer;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.model.search.expr.parser.ParseException;
import com.top_logic.model.search.expr.parser.SearchExpressionParser;
import com.top_logic.model.search.expr.parser.TokenMgrError;
import com.top_logic.model.search.ui.CodeCompletion;
import com.top_logic.model.search.ui.TLScriptCompletionService;

/**
 * React control for editing TL-Script expressions using CodeMirror 6.
 *
 * <p>
 * Communicates with the {@code TLScriptEditor} React component via:
 * </p>
 * <ul>
 * <li>{@code complete} - computes code completions (response via SSE state patch)</li>
 * <li>{@code hover} - returns type/doc info for a token</li>
 * <li>{@code validate} - parses the script and returns diagnostics</li>
 * <li>{@code valueChanged} - syncs the edited value back to the form field</li>
 * </ul>
 */
public class TLScriptEditorReactControl extends ReactControl {

	private static final String VALUE = "value";

	private static final String READ_ONLY = "readOnly";

	private static final String DIAGNOSTICS = "diagnostics";

	private Consumer<String> _valueCallback;

	/**
	 * Creates a new {@link TLScriptEditorReactControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param value
	 *        The initial TL-Script text, may be {@code null}.
	 * @param readOnly
	 *        Whether the editor is read-only.
	 */
	public TLScriptEditorReactControl(ReactContext context, String value, boolean readOnly) {
		super(context, null, "TLScriptEditor");
		putState(VALUE, value != null ? value : "");
		putState(READ_ONLY, Boolean.valueOf(readOnly));
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

	/** Updates the editor value from the server side. */
	public void setValue(String value) {
		putState(VALUE, value);
	}

	/** Updates the read-only state. */
	public void setReadOnly(boolean readOnly) {
		putState(READ_ONLY, Boolean.valueOf(readOnly));
	}

	@ReactCommand("valueChanged")
	void handleValueChanged(Map<String, Object> arguments) {
		Object rawValue = arguments.get(VALUE);
		String newValue = rawValue != null ? rawValue.toString() : null;
		if (_valueCallback != null) {
			_valueCallback.accept(newValue);
		}
	}

	@ReactCommand("validate")
	void handleValidate(Map<String, Object> arguments) {
		String text = (String) arguments.get("text");
		List<Map<String, Object>> diagnostics = computeDiagnostics(text);
		putState(DIAGNOSTICS, diagnostics);
	}

	@ReactCommand("complete")
	void handleComplete(Map<String, Object> arguments) {
		String line = (String) arguments.get("line");
		String prefix = (String) arguments.get("prefix");
		String requestId = (String) arguments.get("requestId");

		// Pass null for DisplayContext — documentation will be omitted for now.
		List<CodeCompletion> completions =
			TLScriptCompletionService.computeCompletions(null, line, prefix, false);

		List<Map<String, Object>> items = new ArrayList<>();
		for (CodeCompletion c : completions) {
			Map<String, Object> entry = new HashMap<>();
			entry.put("name", c.getName());
			entry.put("value", c.getValue());
			entry.put("score", Integer.valueOf(c.getScore()));
			String docHTML = c.getDocHTML();
			if (docHTML != null && !docHTML.isEmpty()) {
				entry.put("docHTML", docHTML);
			}
			items.add(entry);
		}

		Map<String, Object> response = new HashMap<>();
		response.put("requestId", requestId);
		response.put("completions", items);
		putState("completionResponse", response);
	}

	@ReactCommand("hover")
	void handleHover(Map<String, Object> arguments) {
		String token = (String) arguments.get("token");
		Map<String, Object> hoverInfo = new HashMap<>();
		hoverInfo.put("token", token);
		// TODO: Look up documentation via SearchBuilder or model introspection.
		putState("hoverInfo", hoverInfo);
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
			Map<String, Object> diag = new HashMap<>();
			if (ex.currentToken != null && ex.currentToken.next != null) {
				diag.put("line", Integer.valueOf(ex.currentToken.next.beginLine));
				diag.put("col", Integer.valueOf(ex.currentToken.next.beginColumn));
			} else {
				diag.put("line", Integer.valueOf(1));
				diag.put("col", Integer.valueOf(1));
			}
			diag.put("severity", "error");
			diag.put("message", ex.getMessage());
			return Collections.singletonList(diag);
		} catch (TokenMgrError ex) {
			Map<String, Object> diag = new HashMap<>();
			diag.put("line", Integer.valueOf(1));
			diag.put("col", Integer.valueOf(1));
			diag.put("severity", "error");
			diag.put("message", ex.getMessage());
			return Collections.singletonList(diag);
		}
	}
}
