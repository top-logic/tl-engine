/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.conditional;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link CommandStep} that is not even tried to be {@link #doExecute(DisplayContext) executed}
 * since its preparation detected a violated precondition.
 * 
 * <p>
 * In contrast to {@link Hide}, a {@link Failure} step does not prevent the command from being
 * displayed to the user. Instead, a {@link Failure} is represented as inactive button describing
 * the violated precondition.
 * </p>
 * 
 * @see Success
 * @see Hide
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class Failure extends CommandStep {

	private final ResKey _error;

	/**
	 * Creates a {@link Failure}.
	 *
	 * @param error
	 *        See {@link #getError()}
	 */
	public Failure(ResKey error) {
		_error = error;
	}

	/**
	 * The reason, why this {@link CommandStep} cannot be executed.
	 */
	public ResKey getError() {
		return _error;
	}

	@Override
	protected void doExecute(DisplayContext context) {
		throw new TopLogicException(_error);
	}

	@Override
	public ExecutableState getExecutability() {
		return ExecutableState.createDisabledState(_error);
	}
}