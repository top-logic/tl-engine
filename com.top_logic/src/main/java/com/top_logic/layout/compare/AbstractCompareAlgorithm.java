/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.compare;

import com.top_logic.basic.col.TypedAnnotatable.Property;

/**
 * Abstract super class for {@link CompareAlgorithm}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractCompareAlgorithm implements CompareAlgorithm {

	@Override
	public <T> T getPlugin(Property<T> pluginKey) {
		return pluginKey.getDefaultValue(null);
	}

}
