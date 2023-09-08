/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.title;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.util.ResKey;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutComponent.Config;

/**
 * Creates a title for the given {@link LayoutComponent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TitleProvider {

	/**
	 * Creates a title for the given component.
	 * 
	 * @return A non null {@link HTMLFragment} writing the title for the given component.
	 */
	HTMLFragment createTitle(LayoutComponent component);

	/**
	 * Delivers a simple title key for the given component (resp. for the component with the given
	 * configuration).
	 * 
	 * @return May be <code>null</code> when it is not possible to get a simple key for the given
	 *         component.
	 */
	ResKey getSimpleTitle(Config component);

}

