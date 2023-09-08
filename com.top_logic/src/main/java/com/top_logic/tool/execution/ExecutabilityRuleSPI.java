/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution;

import java.util.Map;

import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * API of {@link ExecutabilityRule}.
 * 
 * <p>
 * Technical interface to have the same method signature in {@link CommandHandler} and
 * {@link ExecutabilityRule} without letting {@link CommandHandler} directly implement
 * {@link ExecutabilityRule} (it must not be possible to configure {@link CommandHandler}s, where
 * {@link ExecutabilityRule}s are required).
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ExecutabilityRuleSPI {

	/**
	 * Check, if the owning command is executable within the given component and with the values
	 * defined in the argument map.
	 * 
	 * <p>
	 * This method has only to check the conditions given by the business rules not the security or
	 * display state of the layout component.
	 * </p>
	 * 
	 * <p>
	 * The resulting {@link ExecutableState} is used for displaying buttons in the GUI.
	 * </p>
	 * 
	 * @param aComponent
	 *        The component asking for the state, must not be <code>null</code>.
	 * @param model
	 *        The model that should be considered the target model of the operation. This is not
	 *        required to be the given component's model, but is determined by
	 *        {@link CommandHandler#getTargetModel(LayoutComponent, Map)} of the context command.
	 * @param someValues
	 *        The map of values defined by {@link CommandHandler#getAttributeNames()}, must not be
	 *        <code>null</code>.
	 * @return The requested state, never <code>null</code>.
	 */
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues);

}
