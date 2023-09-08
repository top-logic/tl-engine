/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.conditional;

import com.top_logic.layout.DisplayContext;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link CommandStep} that hides a {@link PreconditionCommandHandler} due to some violated
 * precondition.
 * 
 * <p>
 * In contrast to {@link Failure} the command with the violated precondition is not even present at
 * the UI. Therefore, a {@link Hide} does not need to communicate a description what precondition is
 * violated.
 * </p>
 * 
 * @see Failure
 * @see Success
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Hide extends CommandStep {

	@Override
	protected void doExecute(DisplayContext context) {
		throw new UnsupportedOperationException("Hidden commands must not be executed.");
	}

	@Override
	public ExecutableState getExecutability() {
		return ExecutableState.NOT_EXEC_HIDDEN;
	}

}
