/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.configuration;

import com.top_logic.layout.basic.CommandModel;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Instances of {@link CommandModelConfiguration} are configured factories for
 * {@link CommandModel}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface CommandModelConfiguration {

	/**
	 * This method creates the {@link CommandModel} build from this
	 * {@link CommandModelConfiguration}.
	 * 
	 * @param aLayoutComponent
	 *        A {@link LayoutComponent} which is used to register the command.
	 * @return The created {@link CommandModel}, or <code>null</code> if the command cannot be
	 *         resolved.
	 */
	CommandModel createCommandModel(LayoutComponent aLayoutComponent);

}
