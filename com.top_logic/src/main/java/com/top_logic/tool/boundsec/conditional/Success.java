/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.conditional;

import java.util.function.Consumer;

import com.top_logic.layout.DisplayContext;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link CommandStep} that can be executed expecting success.
 * 
 * <p>
 * When {@link #doExecute(com.top_logic.layout.DisplayContext) execution} of a {@link Success} step
 * still fails, this is reported explicitly to the user after he invoked the command.
 * </p>
 * 
 * @see Failure
 * @see Hide
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class Success extends CommandStep {

	@Override
	public ExecutableState getExecutability() {
		return ExecutableState.EXECUTABLE;
	}

	/**
	 * Creates a {@link Success} from the given {@link Consumer}.
	 * <p>
	 * This method is useful when the {@link Success} should make a simple call which can be passed
	 * as a lambda expression or method reference.
	 * </p>
	 * 
	 * @param consumer
	 *        Is not allowed to be null.
	 * @return Never null.
	 */
	public static Success toSuccess(Consumer<? super DisplayContext> consumer) {
		return new FunctionalSuccess(consumer);
	}

}
