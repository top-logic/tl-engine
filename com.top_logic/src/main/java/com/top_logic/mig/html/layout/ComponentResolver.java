/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.basic.config.InstantiationContext;


/**
 * Plugin for {@link LayoutComponent} that resolves the component.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ComponentResolver {

	/**
	 * Resolves the given component.
	 * 
	 * <p>
	 * Potential messages are written to the context.
	 * </p>
	 * 
	 * @param context
	 *        {@link InstantiationContext} that is used to resolve component.
	 * @param component
	 *        The component to resolve.
	 * 
	 * @see LayoutComponent#componentsResolved(com.top_logic.basic.config.InstantiationContext)
	 */
	public abstract void resolveComponent(InstantiationContext context, LayoutComponent component);

}

