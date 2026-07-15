/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.TagName;

/**
 * Derives a display tag name for a {@link ConfigurationItem}.
 */
public class ConfigTagName {

	/**
	 * Returns the display tag name for the given config item.
	 *
	 * <p>
	 * Uses the value of {@link TagName} on the config interface with its first character
	 * upper-cased. Falls back to the simple interface name with a trailing {@code "Config"}
	 * suffix removed.
	 * </p>
	 */
	public static String of(ConfigurationItem item) {
		Class<?> iface = item.descriptor().getConfigurationInterface();
		TagName tagName = iface.getAnnotation(TagName.class);
		if (tagName != null) {
			return capitalize(tagName.value());
		}
		String name = iface.getSimpleName();
		if (name.endsWith("Config")) {
			name = name.substring(0, name.length() - "Config".length());
		}
		return name;
	}

	private static String capitalize(String value) {
		if (value.isEmpty()) {
			return value;
		}
		return Character.toUpperCase(value.charAt(0)) + value.substring(1);
	}
}
