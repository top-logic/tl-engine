/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.resources;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.ResourceView;

/**
 * Reduction of the {@link ResourceView} interface to a single method to implement.
 * 
 * @see #getResource(String)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractResourceView implements ResourceView {

	@Override
	public boolean hasStringResource(String resourceKey) {
		return getResource(resourceKey) != null;
	}

	@Override
	public ResKey getStringResource(String resourceKey, ResKey defaultValue) {
		ResKey result = getResource(resourceKey);
		if (result == null) {
			return defaultValue;
		}
		return result.fallback(defaultValue);
	}

	@Override
	public ResKey getStringResource(String resourceKey) {
		return getResource(resourceKey);
	}

	/**
	 * Looks up the resource with the given key.
	 * 
	 * @param resourceKey
	 *        The key to look up.
	 * @return The translation in the current user's locale. <code>null</code>, if the resource is
	 *         optional and no translation exists. A resource that is not optional but does not
	 *         exist returns a missing resource marker and logs the missing resource.
	 */
	protected abstract ResKey getResource(String resourceKey);

}
