/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.ui;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Interface for scripting classes ({@link ModelNamingScheme}) which resolve
 * {@link LayoutComponent}.
 * 
 * <p>
 * Implementor must call {@link #checkVisible(ActionContext, ConfigurationItem, LayoutComponent)} to
 * ensure that the resolved component is visible.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface LayoutComponentResolver {

	/**
	 * {@link Property} that is internally used to mark an {@link ActionContext} as "resolving
	 * hidden {@link LayoutComponent}s is allowed".
	 */
	Property<Boolean> ALLOW_RESOLVING_HIDDEN =
		TypedAnnotatable.property(Boolean.class, "Allow resolving hidden components", Boolean.FALSE);

	/**
	 * Marks the given context, that upcoming component resolving must also be possible for
	 * invisible components.
	 * 
	 * <p>
	 * Usage
	 * 
	 * <pre>
	 * boolean before = LayoutComponentResolver.allowResolvingHiddenComponents(context, true);
	 * try {
	 * 	// Call layout component resolver
	 * } finally {
	 * 	LayoutComponentResolver.allowResolvingHiddenComponents(context, before);
	 * }
	 * </pre>
	 * </p>
	 * 
	 * @param context
	 *        The context in which the component should be resolved.
	 * @param value
	 *        Whether resolving hidden components must not fail.
	 * @return Former value.
	 */
	static boolean allowResolvingHiddenComponents(ActionContext context, boolean value) {
		return context.getDisplayContext().set(ALLOW_RESOLVING_HIDDEN, value);
	}

	/**
	 * Checks that the resolved component is {@link LayoutComponent#isVisible() visible}.
	 * 
	 * <p>
	 * If the component is not-null and not visible, a {@link RuntimeException} is thrown.
	 * </p>
	 * 
	 * @param context
	 *        The context in which the component was resolved.
	 * @param contextConfig
	 *        {@link ConfigurationItem} which holds the reference to the resolved component.
	 * @param component
	 *        Resolved component. May be <code>null</code>, when such a component does not exist.
	 */
	default void checkVisible(ActionContext context, ConfigurationItem contextConfig, LayoutComponent component) {
		if (component == null) {
			// Component could not be found. That may be intended, e.g. to check for non-existence.
			// If it is not intended, later code fails (hopefully).
			return;
		}
		if (context.getDisplayContext().get(ALLOW_RESOLVING_HIDDEN)) {
			// Resolving hidden components allowed.
			return;
		}
		ApplicationAssertions.assertTrue(contextConfig,
			"Referenced layout component '" + component.getName() + "' is not visible.", component.isVisible());
	}

}

