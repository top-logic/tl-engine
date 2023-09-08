/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.resources;

import com.top_logic.layout.ResourceView;

/**
 * Reduction of the {@link ResourceView} interface to a single method to implement.
 * 
 * @see #getResource(String, boolean)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractResourceView implements ResourceView {

	@Override
	public boolean hasStringResource(String resourceKey) {
		return getResource(resourceKey, true) != null;
	}

	@Override
	public String getStringResource(String resourceKey, String defaultValue) {
		String result = getResource(resourceKey, true);
		if (result == null) {
			return defaultValue;
		}
		return result;
	}

	@Override
	public String getStringResource(String resourceKey) {
		return getResource(resourceKey, false);
	}

	/**
	 * Looks up the resource with the given key.
	 * 
	 * @param resourceKey
	 *        The key to look up.
	 * @param optional
	 *        Whether this is an optional resource.
	 * @return The translation in the current user's locale. <code>null</code>, if the resource is
	 *         optional and no translation exists. A resource that is not optional but does not
	 *         exist returns a missing resource marker and logs the missing resource.
	 */
	protected abstract String getResource(String resourceKey, boolean optional);

}
