/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;

/**
 * Utilities for the {@link ViewAction} lists that a command or a composite action is configured
 * with.
 */
public class ViewActions {

	/**
	 * Instantiates a configured list of {@link ViewAction}s.
	 *
	 * @param configs
	 *        The configurations to instantiate, in the order they run in.
	 * @return The instantiated actions, without the ones that could not be created (the failure is
	 *         reported to the given context).
	 */
	public static List<ViewAction> instantiate(InstantiationContext context,
			List<PolymorphicConfiguration<? extends ViewAction>> configs) {
		return configs.stream()
			.<ViewAction> map(config -> context.getInstance(config))
			.filter(action -> action != null)
			.toList();
	}

	/**
	 * Whether one of the given actions applies the values entered into the enclosing form.
	 *
	 * @see ViewAction#appliesFormState()
	 */
	public static boolean appliesFormState(List<ViewAction> actions) {
		return actions.stream().anyMatch(ViewAction::appliesFormState);
	}
}
