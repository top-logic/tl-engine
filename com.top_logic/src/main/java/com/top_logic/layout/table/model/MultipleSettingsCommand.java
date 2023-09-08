/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import java.util.List;
import java.util.Map;

import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.util.TLContext;

/**
 * Superclass for commands handling storage of multiple settings in the personal configuration.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class MultipleSettingsCommand implements Command {

	/** The key in {@link #storedConfigKeys()} holding the actual keys. */
	protected static final String KEYS_KEY = "keys";

	/** The key in {@link #storedConfigKeys()} holding the key names. */
	protected static final String NAMES_KEY = "names";

	/** {@link PersonalConfiguration} of the current user. */
	protected final PersonalConfiguration _pc = TLContext.getContext().getPersonalConfiguration();

	private ConfigKey _configKey;

	/**
	 * Creates a new {@link MultipleSettingsCommand}.
	 */
	public MultipleSettingsCommand(ConfigKey configKey) {
		_configKey = configKey;
	}

	/**
	 * The keys for the personal configuration holding the table settings.
	 * 
	 * <p>
	 * The map contains the actual keys under the key {@link #KEYS_KEY} and the names for the keys
	 * in the same order under the key {@link #NAMES_KEY}.
	 * </p>
	 * 
	 * @return May be <code>null</code>, when currently nothing is stored.
	 * 
	 * @see #storeConfigKeys(Map)
	 */
	protected Map<String, List<String>> storedConfigKeys() {
		return cast(_pc.getJSONValue(configKey()));
	}

	/**
	 * Stores the configuration keys.
	 * 
	 * @param configKeys
	 *        May be <code>null</code>. If not <code>null</code>, it must contain the actual keys
	 *        under the key {@link #KEYS_KEY} and the names under the key {@link #NAMES_KEY}.
	 * 
	 * @see #storedConfigKeys()
	 */
	protected void storeConfigKeys(Map<String, List<String>> configKeys) {
		_pc.setJSONValue(configKey(), configKeys);
	}

	@SuppressWarnings("unchecked")
	private static Map<String, List<String>> cast(Object storedConfigNames) {
		if (storedConfigNames == null) {
			return null;
		}
		Map<?, ?> tmp = (Map<?, ?>) storedConfigNames;
		tmp.keySet().forEach(String.class::cast);
		tmp.values().stream().map(List.class::cast).flatMap(List::stream).forEach(String.class::cast);
		return (Map<String, List<String>>) tmp;
	}

	/**
	 * The key under which the list of keys is found that deliver the settings of tables.
	 * 
	 * @return May be <code>null</code>.
	 */
	protected String configKey() {
		return _configKey.get();
	}

}

