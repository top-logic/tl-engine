/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.error;

import com.top_logic.basic.util.ResKey;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Provider for internationalized failure dialog context descriptions.
 * 
 * @see HandlerResult#init(ContextDescription)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ContextDescription {

	/**
	 * The dialog title.
	 */
	ResKey getErrorTitle();

	/**
	 * The message to display.
	 */
	ResKey getErrorMessage();

	/**
	 * Writes the recorded problem to the application log.
	 */
	void logError();

}
