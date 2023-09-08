/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.basic.util.Computation;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertion;

/**
 * An {@link Action} that will execute the given {@link Computation} when {@link Action#execute()} is
 * called.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class ComputationAction implements Action {

	private final ComputationEx2<?, ?, ?> processComputation;

	/**
	 * Returns an {@link Action} that will execute the given {@link Computation} when
	 * {@link Action#execute()} is called.
	 * 
	 * @param processComputation
	 *        The {@link Computation} to execute in {@link Action#execute()}.
	 */
	public ComputationAction(ComputationEx2<?, ?, ?> processComputation) {
		super();
		this.processComputation = processComputation;
	}

	@Override
	public void execute() {
		try {
			processComputation.run();
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Error ex) {
			throw ex;
		} catch (Throwable ex) {
			throw new ApplicationAssertion(ex);
		}
	}

	@Override
	public boolean isUpdate(Action potentialUpdate) {
		return false;
	}

}
