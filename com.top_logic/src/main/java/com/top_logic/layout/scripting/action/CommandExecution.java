/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;

/**
 * {@link ModelAction} that executes a {@link CommandModel}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface CommandExecution extends ModelAction {

	/**
	 * Whether execution of the {@link Command} is expected to fail.
	 */
	boolean getFailureExpected();

	/** @see #getFailureExpected() */
	void setFailureExpected(boolean value);

}
