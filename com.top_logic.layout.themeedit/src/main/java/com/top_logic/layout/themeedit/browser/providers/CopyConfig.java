/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers;

import java.util.function.Function;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.func.Function1;

/**
 * {@link Function} copying an input configuration.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CopyConfig extends Function1<ConfigurationItem, Object> {

	@Override
	public ConfigurationItem apply(Object arg) {
		return TypedConfiguration.copy((ConfigurationItem) arg);
	}

}
