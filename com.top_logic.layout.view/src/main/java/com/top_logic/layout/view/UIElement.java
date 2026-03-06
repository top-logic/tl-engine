/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.layout.react.ViewControl;

/**
 * Base interface for all view elements.
 *
 * <p>
 * A UIElement is a stateless factory for {@link ViewControl}. The UIElement tree is shared across
 * all sessions (parsed once from configuration). Each session gets its own control tree by calling
 * {@link #createControl(ViewContext)}.
 * </p>
 */
public interface UIElement {

	/**
	 * Configuration interface for {@link UIElement}.
	 *
	 * <p>
	 * Every concrete UIElement has a corresponding Config sub-interface. Because this extends
	 * {@link PolymorphicConfiguration}, new element types can be registered in any module.
	 * </p>
	 */
	interface Config extends PolymorphicConfiguration<UIElement> {

		/** Configuration name for {@link #getPersonalizationKey()}. */
		String PERSONALIZATION_KEY = "personalization-key";

		/**
		 * Optional override for the auto-derived personalization key.
		 *
		 * <p>
		 * If set, stateful elements use this key instead of the auto-derived path from
		 * {@link ViewContext#getPersonalizationKey()}.
		 * </p>
		 */
		@com.top_logic.basic.config.annotation.Name(PERSONALIZATION_KEY)
		String getPersonalizationKey();
	}

	/**
	 * Creates a {@link ViewControl} for this element in the given session context.
	 *
	 * @param context
	 *        The view context providing session-scoped infrastructure.
	 * @return A control for the current session. Typically a
	 *         {@link com.top_logic.layout.react.control.ReactControl}.
	 */
	ViewControl createControl(ViewContext context);
}
