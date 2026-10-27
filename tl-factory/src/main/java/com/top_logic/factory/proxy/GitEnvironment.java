/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.proxy;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration of git passed to a git process through its environment instead of a file.
 *
 * <p>
 * Git (since 2.31) reads {@link #ENV_CONFIG_COUNT} entries from the variables
 * {@link #ENV_CONFIG_KEY_PREFIX}{@code <n>} and {@link #ENV_CONFIG_VALUE_PREFIX}{@code <n>},
 * {@code n} counting from {@code 0}, after all configuration files, as if given with {@code -c}
 * on the command line. The values are taken as they are, without the quoting of a configuration
 * file.
 * </p>
 */
public final class GitEnvironment {

	/**
	 * Environment variable with the number of configuration entries in the environment.
	 */
	public static final String ENV_CONFIG_COUNT = "GIT_CONFIG_COUNT";

	/**
	 * Prefix of the environment variable with the key of an entry, followed by its index.
	 */
	public static final String ENV_CONFIG_KEY_PREFIX = "GIT_CONFIG_KEY_";

	/**
	 * Prefix of the environment variable with the value of an entry, followed by its index.
	 */
	public static final String ENV_CONFIG_VALUE_PREFIX = "GIT_CONFIG_VALUE_";

	private GitEnvironment() {
		// Static utility.
	}

	/**
	 * The environment variables passing the given configuration entries to git.
	 *
	 * <p>
	 * A process gets one such set only; the entries of all contributors are collected into one
	 * list before.
	 * </p>
	 *
	 * @param entries
	 *        Key and value of each entry, in the order git is to read them; a key may repeat.
	 * @return The variables, empty if there are no entries.
	 */
	public static Map<String, String> config(List<Map.Entry<String, String>> entries) {
		if (entries.isEmpty()) {
			return Collections.emptyMap();
		}
		Map<String, String> result = new LinkedHashMap<>();
		result.put(ENV_CONFIG_COUNT, Integer.toString(entries.size()));
		int index = 0;
		for (Map.Entry<String, String> entry : entries) {
			result.put(ENV_CONFIG_KEY_PREFIX + index, entry.getKey());
			result.put(ENV_CONFIG_VALUE_PREFIX + index, entry.getValue());
			index++;
		}
		return result;
	}

}
