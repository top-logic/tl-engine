/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import com.top_logic.basic.config.ConfigurationChange;
import com.top_logic.basic.config.ConfigurationListener;

/**
 * {@link ConfigurationListener} counting number of {@link ConfigurationChange}s.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class Counter implements ConfigurationListener {
	private int _cnt;

	@Override
	public void onChange(ConfigurationChange change) {
		_cnt++;
	}

	public int getCount() {
		return _cnt;
	}
}