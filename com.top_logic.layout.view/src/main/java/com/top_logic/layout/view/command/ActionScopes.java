/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.container.ConfigPart;

/**
 * The {@link ActionScope} at a position of a configuration tree, computed from the
 * {@link ActionScopeDefinition}s above it.
 *
 * <p>
 * This is the scope the runtime instantiates a configuration in, for a configuration that is not
 * being instantiated - e.g. one edited in the script editor.
 * </p>
 */
public class ActionScopes {

	private ActionScopes() {
		// Static utilities only.
	}

	/**
	 * The scope the given configuration is instantiated in.
	 *
	 * @param item
	 *        A configuration within a configuration tree, e.g. an action.
	 * @return The scope defined by the {@link ActionScopeDefinition}s on the way from the root of
	 *         the tree to the given configuration, {@link ActionScope#NONE} for a configuration that
	 *         is not contained in one.
	 */
	public static ActionScope scopeAt(ConfigurationItem item) {
		if (!(item instanceof ConfigPart part)) {
			return ActionScope.NONE;
		}
		ConfigurationItem container = part.container();
		if (container == null) {
			return ActionScope.NONE;
		}
		ActionScope outer = scopeAt(container);
		if (container instanceof ActionScopeDefinition definition) {
			return definition.actionScope(item, outer);
		}
		return outer;
	}

	/**
	 * Whether the given part is one of the given configurations, by identity.
	 */
	public static boolean contains(List<? extends ConfigurationItem> configs, ConfigurationItem part) {
		for (ConfigurationItem config : configs) {
			if (config == part) {
				return true;
			}
		}
		return false;
	}
}
