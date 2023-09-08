/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

/**
 * Return {@link ExecutableState#NOT_EXEC_HIDDEN} for historical data and
 * {@link ExecutableState#EXECUTABLE} for current data.
 * 
 * @since 5.7.6
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class HistoricDataHideHistoricRule extends HistoricDataExecRule {

	/** Singleton {@link HistoricDataHideHistoricRule} instance. */
	public static final HistoricDataHideHistoricRule INSTANCE = new HistoricDataHideHistoricRule();

	@Override
	protected ExecutableState getCurrentState(Object model) {
		return ExecutableState.EXECUTABLE;
	}

	@Override
	protected ExecutableState getHistoricState(Object model) {
		return ExecutableState.NOT_EXEC_HIDDEN;
	}

}

