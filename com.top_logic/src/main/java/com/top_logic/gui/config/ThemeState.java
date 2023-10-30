/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.gui.config;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * Whether a theme is {@link ThemeState#ENABLED} or {@link ThemeState#DISABLED}.
 */
public enum ThemeState implements ExternallyNamed {

	/**
	 * Cannot be chosen by the user.
	 * 
	 * <p>
	 * A disabled theme can only be used as base theme for some other theme.
	 * </p>
	 */
	DISABLED("disabled"),

	/**
	 * Can be chosen by the user.
	 */
	ENABLED("enabled"),

	;

	private final String _configName;

	private ThemeState(String configName) {
		_configName = configName;
	}

	@Override
	public String getExternalName() {
		return _configName;
	}

	private static final Map<String, ThemeState> BY_CONFIG_NAME;
	static {
		BY_CONFIG_NAME = new HashMap<>();
		for (ThemeState option : values()) {
			BY_CONFIG_NAME.put(option.getExternalName(), option);
		}
	}

	/**
	 * Lookup value by {@link #getExternalName()}.
	 */
	public static ThemeState byConfigName(String configName) {
		return BY_CONFIG_NAME.get(configName);
	}
}