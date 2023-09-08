/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.client.diagramjs.command;

import com.google.gwt.core.client.JavaScriptObject;

import jsinterop.annotations.JsMethod;

/**
 * Handler for a diagramJS command.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface CommandHandler {

	/**
	 * Main handling of the command for the given {@code context}.
	 */
	@JsMethod
	public void execute(JavaScriptObject context);

	/**
	 * Handling if the execution of the command failed for the given {@code context}.
	 */
	@JsMethod
	public void revert(JavaScriptObject context);

	/**
	 * Decides if the command can be handled for the given {@code context}.
	 */
	@JsMethod
	public boolean canExecute(JavaScriptObject context);

	/**
	 * Processing before the actual handling starts for the given {@code context}.
	 */
	@JsMethod
	public void preExecute(JavaScriptObject context);

	/**
	 * Processing after the actual handling ends for the given {@code context}.
	 */
	@JsMethod
	public void postExecute(JavaScriptObject context);

}
