/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

/**
 * View into the application resources.
 * 
 * <p>
 * Implementations provide the lookup of application resources with keys
 * relative to some internal context.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ResourceView {
	
	/**
	 * Lookup a string resource with the given key relative to the current
	 * context.
	 * 
	 * @param resourceKey
	 *     The key that identifies the resource.
	 * @return
	 *     The resource of type {@link String}. If there is no resource 
	 *     associated with the given key (see {@link #hasStringResource(String)}), 
	 *     a text is returned that is good for finding missing resources during 
	 *     debugging. 
	 */
	public String getStringResource(String resourceKey);

	public String getStringResource(String resourceKey, String defaultValue);

	/**
	 * Check, whether a resource is associated with the given key.
	 * 
	 * @param resourceKey
	 *     The key that identifies the resource.
	 * @return
	 *     Whether a call to {@link #getStringResource(String)} would return a 
	 *     valid value.
	 */
	public boolean hasStringResource(String resourceKey);

}
