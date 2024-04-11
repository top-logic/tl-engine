/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.configuration;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.layout.View;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Instances of {@link ViewConfiguration} are used to create {@link View}s for
 * {@link LayoutComponent}s which are configured in the layout XML files.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ViewConfiguration {

	/**
	 * Creates the {@link View} for the given {@link LayoutComponent}.
	 * 
	 * @param component
	 *        The context {@link LayoutComponent} that owns the created {@link View}.
	 */
	public HTMLFragment createView(LayoutComponent component);

}
