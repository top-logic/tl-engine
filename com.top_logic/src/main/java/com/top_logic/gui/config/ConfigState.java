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
 * Whether a theme is {@link ConfigState#ENABLED} or {@link ConfigState#DISABLED}.
 */
public enum ConfigState implements ExternallyNamed {
	/**
	 * Can be chosen by the user.
	 */
	ENABLED("enabled"),

	/**
	 * Cannot be chosen by the user.
	 */
	DISABLED("disabled");

	private final String _configName;

	private ConfigState(String configName) {
		_configName = configName;
	}

	@Override
	public String getExternalName() {
		return _configName;
	}

	private static final Map<String, ConfigState> BY_CONFIG_NAME;
	static {
		BY_CONFIG_NAME = new HashMap<>();
		for (ConfigState option : values()) {
			BY_CONFIG_NAME.put(option.getExternalName(), option);
		}
	}

	/**
	 * Lookup value by {@link #getExternalName()}.
	 */
	public static ConfigState byConfigName(String configName) {
		return BY_CONFIG_NAME.get(configName);
	}
}