/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

/**
 * Return {@link ExecutableState#NO_EXEC_CURRENT} for current data and
 * {@link ExecutableState#EXECUTABLE} for historic data.
 * 
 * @since 5.7.6
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class HistoricDataDisableCurrentRule extends HistoricDataExecRule {

	/** Singleton {@link HistoricDataDisableCurrentRule} instance. */
	public static final HistoricDataDisableCurrentRule INSTANCE = new HistoricDataDisableCurrentRule();

	@Override
	protected ExecutableState getCurrentState(Object model) {
		return ExecutableState.NO_EXEC_CURRENT;
	}

	@Override
	protected ExecutableState getHistoricState(Object model) {
		return ExecutableState.EXECUTABLE;
	}

}

