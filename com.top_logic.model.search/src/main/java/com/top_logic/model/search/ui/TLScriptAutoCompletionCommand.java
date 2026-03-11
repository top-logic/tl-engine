/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.top_logic.basic.json.JSON;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Command calculating autocompletion suggestions for the given prefix.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven F�rster</a>
 */
public class TLScriptAutoCompletionCommand extends ControlCommand implements TLScriptConstants {

	private static final String COMMAND_ID = "tlScriptAutoCompletion";

	private boolean _completeCaseSensitive;

	/**
	 * Creates a {@link TLScriptAutoCompletionCommand}.
	 */
	public TLScriptAutoCompletionCommand(boolean completeCaseSensitive) {
		super(COMMAND_ID);

		_completeCaseSensitive = completeCaseSensitive;
	}

	@Override
	public ResKey getI18NKey() {
		return ResKey.text("TL-Script Autocompletion");
	}

	@Override
	protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
		TLScriptCodeEditorControl scriptCodeControl = (TLScriptCodeEditorControl) control;

		String prefix = (String) arguments.get("prefix");
		String line = (String) arguments.get("line");

		List<CodeCompletion> completions =
			TLScriptCompletionService.computeCompletions(commandContext, line, prefix, _completeCaseSensitive);

		if (!completions.isEmpty()) {
			sendCompletions(scriptCodeControl, completions);
		}

		return HandlerResult.DEFAULT_RESULT;
	}

	private void sendCompletions(TLScriptCodeEditorControl control, List<CodeCompletion> completions) {
		List<Map<String, ?>> jsonCompletions = createCompletionsPropertiesMap(completions);
		control.sendCompletions(JSON.toString(jsonCompletions));
	}

	private List<Map<String, ?>> createCompletionsPropertiesMap(Collection<CodeCompletion> completions) {
		return completions.stream().map(completion -> {
			return createCompletionPropertiesMap(completion);
		}).collect(Collectors.toList());
	}

	private Map<String, Object> createCompletionPropertiesMap(CodeCompletion completion) {
		LinkedHashMap<String, Object> completionProperties = new LinkedHashMap<>();

		completionProperties.put("name", completion.getName());
		completionProperties.put("value", completion.getValue());
		completionProperties.put("score", completion.getScore());
		completionProperties.put("snippet", completion.getSnippet());
		completionProperties.put("docHTML", completion.getDocHTML());

		return completionProperties;
	}

}
