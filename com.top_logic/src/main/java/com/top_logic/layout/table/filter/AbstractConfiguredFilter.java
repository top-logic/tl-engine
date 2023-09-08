/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

/**
 * Base class of implementations of {@link ConfiguredFilter}s, that handles common aspects, like
 * calculation of visibility.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public abstract class AbstractConfiguredFilter implements ConfiguredFilter {

	@Override
	public final boolean isVisible() {
		return getFilterConfiguration().hasValueListeners();
	}

	@Override
	public void startFilterRevalidation(boolean countableRevalidation) {
		// Do nothing
	}

	@Override
	public void stopFilterRevalidation() {
		// Do nothing
	}

	@Override
	public void count(Object value) {
		// Do nothing
	}
}
