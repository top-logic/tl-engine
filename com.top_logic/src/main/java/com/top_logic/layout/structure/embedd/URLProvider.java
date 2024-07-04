/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure.embedd;

import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * 
 */
public interface URLProvider {

	/**
	 * Creates the URL of a micro-frontend of another application to embedd.
	 *
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param component
	 *        The context component.
	 * @param model
	 *        The context model. In the context of a component, the component's model. In the
	 *        context of a command, the command's result.
	 * @return The value of the <code>src</code> attribute of the <code>iframe</code> element
	 *         embedding the view.
	 */
	String getUrl(DisplayContext context, LayoutComponent component, Object model);

}
