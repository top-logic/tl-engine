/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

import java.util.Map;

import com.top_logic.layout.component.ComponentUtil;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Say hidden if the model of a component is a wrapper that is not in the revision.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class HistoricDataExecRule implements ExecutabilityRule {

	protected HistoricDataExecRule() {
	}

    @Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		boolean historicModel = ComponentUtil.isHistoric(model);

		// FIXME KBU/BHU add a check if wrapper exists in current revision!
		return historicModel ? getHistoricState(model) : getCurrentState(model);
    }

	/**
	 * Returns the {@link ExecutableState} for a current model.
	 * 
	 * @param model
	 *        The model of the component. It is a current model.
	 * 
	 * @return Actual return value of
	 *         {@link HistoricDataExecRule#isExecutable(LayoutComponent, Object, Map)}.
	 */
	protected abstract ExecutableState getCurrentState(Object model);

	/**
	 * Returns the {@link ExecutableState} for a historic model.
	 * 
	 * @param model
	 *        The model of the component. It is a historic model.
	 * 
	 * @return Actual return value of
	 *         {@link HistoricDataExecRule#isExecutable(LayoutComponent, Object, Map)}.
	 */
	protected abstract ExecutableState getHistoricState(Object model);

	/**
	 * Get the model from the component
	 * 
	 * @param aComponent
	 *        the component
	 * @return the model
	 */
	protected Object getModel(LayoutComponent aComponent) {
		return aComponent.getModel();
	}
}
