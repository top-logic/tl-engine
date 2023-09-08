/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.renderer;

import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.model.AbstractExecutabilityModel;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link AbstractExecutabilityModel} hiding the {@link CommandModel} when the key returns
 * <code>null</code>.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MultipleTableSettingsExecutability extends AbstractExecutabilityModel {

	private ConfigKey _storageKey;

	/**
	 * Creates a new {@link MultipleTableSettingsExecutability}.
	 */
	public MultipleTableSettingsExecutability(ConfigKey key) {
		_storageKey = key;
	}

	@Override
	protected ExecutableState calculateExecutability() {
		String configKey = _storageKey.get();
		if (configKey == null) {
			return ExecutableState.NOT_EXEC_HIDDEN;
		}
		return calculateExecutability(configKey);
	}

	/**
	 * Actual value of {@link #calculateExecutability()}.
	 * 
	 * @param configKey
	 *        The non null configuration key storing the list of setting keys.
	 */
	protected ExecutableState calculateExecutability(String configKey) {
		return ExecutableState.EXECUTABLE;
	}

}

