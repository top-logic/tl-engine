/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.configuration;

import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link InstantiationContext} that provides a constant {@link LayoutComponent} to the instantiated
 * implementations for the {@link InstantiationContext#OUTER} value.
 */
public final class ComponentContextInstantiationContext extends DefaultInstantiationContext {
	/**
	 * Creates a {@link ComponentContextInstantiationContext}.
	 *
	 * @param logSource
	 *        See {@link DefaultInstantiationContext#DefaultInstantiationContext(Class)}.
	 * @param component
	 *        The {@link LayoutComponent} to resolve as {@link InstantiationContext#OUTER} value.
	 */
	public ComponentContextInstantiationContext(Class<?> logSource, LayoutComponent component) {
		super(logSource);

		fillReferenceValue(this, new IdRef(LayoutComponent.class, InstantiationContext.OUTER), component);
	}
}