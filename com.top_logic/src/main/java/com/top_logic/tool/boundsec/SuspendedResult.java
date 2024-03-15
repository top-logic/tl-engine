/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.util.Map;

import com.top_logic.basic.col.MapUtil;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.mig.html.layout.CommandDispatcher;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link HandlerResult} that is {@link #isSuspended() suspended}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class SuspendedResult extends HandlerResult {

	private CommandHandler _resumeCommand;

	private LayoutComponent _resumeComponent;

	private Map<String, Object> _resumeArguments;

	private Command _resumeContinuation;

	@Override
	public boolean isSuspended() {
		return true;
	}

	@Override
	public void initContinuation(CommandHandler command, LayoutComponent component, Map<String, Object> arguments) {
		// Skip duplicate initializations. These happen, if a result is passed twice through the
		// command dispatcher.
		if (_resumeCommand == null) {
			_resumeCommand = command;
			_resumeComponent = component;
			_resumeArguments = arguments;
		}
	}

	@Override
	public void appendContinuation(Command continuation) {
		assert isSuspended() : "Not suspended.";
		Command existing = _resumeContinuation;
		_resumeContinuation = existing == null ? continuation : (context) -> {
			HandlerResult first = existing.executeCommand(context);
			if (!first.isSuccess()) {
				return first;
			}
			return continuation.executeCommand(context);
		};
	}

	@Override
	public HandlerResult resume(DisplayContext resumeContext, Map<String, Object> additionalArguments) {
		if (_resumeCommand == null) {
			throw new IllegalStateException("Proccessing has not yet finished, cannot be resumed.");
		}

		HandlerResult result = CommandDispatcher.getInstance().dispatchCommand(
			_resumeCommand, resumeContext, _resumeComponent,
			join(_resumeArguments, additionalArguments));
		if (result.isSuccess()) {
			if (_resumeContinuation != null) {
				return _resumeContinuation.executeCommand(resumeContext);
			}
		} else if (result.isSuspended()) {
			if (_resumeContinuation != null) {
				result.appendContinuation(_resumeContinuation);
			}
			return HandlerResult.DEFAULT_RESULT;
		}
		return result;
	}

	private static <K, V> Map<K, V> join(Map<K, V> map1, Map<K, V> map2) {
		if (map2.isEmpty()) {
			return map1;
		} else if (map1.isEmpty()) {
			return map2;
		} else {
			Map<K, V> result = MapUtil.newMap(map1.size() + map2.size());
			result.putAll(map1);
			result.putAll(map2);
			return result;
		}
	}

}
