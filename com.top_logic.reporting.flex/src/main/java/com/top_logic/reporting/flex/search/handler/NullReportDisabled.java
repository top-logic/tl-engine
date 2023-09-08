/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.search.handler;

import java.util.Map;

import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.reporting.flex.search.SearchResultStoredReportSelector;
import com.top_logic.reporting.flex.search.model.FlexReport;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tool.execution.NullModelDisabled;

/**
 * {@link ExecutabilityRule} that checks for a currently selected {@link FlexReport}
 */
public class NullReportDisabled extends NullModelDisabled {

	@SuppressWarnings("hiding")
	private static final ExecutableState EXEC_STATE_DISABLED =
		ExecutableState.createDisabledState(I18NConstants.NO_REPORT);

	/**
	 * Singleton <code>INSTANCE</code>
	 */
	@SuppressWarnings("hiding")
	public static final ExecutabilityRule INSTANCE = new NullReportDisabled();

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		if (aComponent instanceof SearchResultStoredReportSelector) {
			SearchResultStoredReportSelector comp = (SearchResultStoredReportSelector) aComponent;
			FlexReport report = comp.getCurrentSelection();

			if (report == null) {
				return NullReportDisabled.EXEC_STATE_DISABLED;
			}
			return ExecutableState.EXECUTABLE;
		}
		else {
			return super.isExecutable(aComponent, model, someValues);
		}
	}
}