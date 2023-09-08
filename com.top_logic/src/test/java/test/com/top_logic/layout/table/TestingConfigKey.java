/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table;

import com.top_logic.layout.table.ConfigKey;

/**
 * {@link ConfigKey} build from a known {@link String}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestingConfigKey extends ConfigKey {

	private String key;

	/**
	 * Creates a new {@link TestingConfigKey}.
	 * 
	 * @param key
	 *        is later returned by {@link #get()};
	 */
	public TestingConfigKey(String key) {
		super();
		this.key = key;
	}

	@Override
	public String get() {
		return key;
	}
}
