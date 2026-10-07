/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.protocol.JSSnipplet;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;

/**
 * {@link ViewAction} that takes the browser window one step back in its history, as the back
 * button of the browser does.
 *
 * <p>
 * A display whose state is part of its address - a selection bound to the URL with a route
 * parameter, a tab, a sidebar item - returns to the state it had before, since each such change is
 * a history step. A window without a toolbar of its own, e.g. a side window opened by
 * {@link OpenViewWindowCommand}, offers its user the step back with a command running this action:
 * </p>
 *
 * <pre>
 * &lt;generic-command image="css:bi bi-arrow-left" placement="TOOLBAR"&gt;
 *   &lt;label&gt;...&lt;/label&gt;
 *   &lt;history-back/&gt;
 * &lt;/generic-command&gt;
 * </pre>
 *
 * <p>
 * In a window without a step to go back to, the action does nothing.
 * </p>
 */
@InApp
public class HistoryBackAction implements ViewAction {

	/** Client code taking the window one step back in its history. */
	private static final String CLIENT_CODE = "history.back();";

	/**
	 * Configuration for {@link HistoryBackAction}.
	 */
	@TagName("history-back")
	public interface Config extends PolymorphicConfiguration<HistoryBackAction> {

		@Override
		@ClassDefault(HistoryBackAction.class)
		Class<? extends HistoryBackAction> getImplementationClass();
	}

	/**
	 * Creates a new {@link HistoryBackAction}.
	 */
	@CalledByReflection
	public HistoryBackAction(InstantiationContext context, Config config) {
		// Nothing to configure.
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		SSEUpdateQueue queue = context.getSSEQueue();
		if (queue != null) {
			queue.enqueue(JSSnipplet.create().setCode(CLIENT_CODE));
		}
		return input;
	}

}
