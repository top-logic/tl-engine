/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure.embedd;

import java.util.Map;

import com.top_logic.layout.DisplayContext;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Callback interface that can react on an action in the browser passing some arguments to the
 * server.
 */
public interface UICallback {

	/**
	 * Server-side functionality invoked from the browser.
	 *
	 * @param commandContext
	 *        The current {@link DisplayContext}.
	 * @param arguments
	 *        Some JSON-like arguments passed from the UI.
	 */
	HandlerResult executeCommand(DisplayContext commandContext, Map<String, Object> arguments);

}
