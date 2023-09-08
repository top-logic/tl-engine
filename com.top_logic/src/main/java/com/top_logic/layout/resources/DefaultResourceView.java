/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.resources;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.ResourceView;
import com.top_logic.util.Resources;

/**
 * Implementation of {@link ResourceView} that identifies its contex with a
 * prefix to all of its keys.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultResourceView implements ResourceView {
	/**
	 * Character used by this implementation to concatenate resource section
	 * keys.
	 */
	public static final char KEY_SEPARATOR = '.';

	/**
	 * {@link String} version of {@link #KEY_SEPARATOR}.
	 */
	public static final String KEY_SEPARATOR_STRING = 
		Character.toString(KEY_SEPARATOR);
	
	private ResPrefix keyPrefix;

	/**
	 * Create a new view with the context given as key prefix.
	 * 
	 * @param keyPrefix
	 *        The prefix to prepend to all keys in {@link #getStringResource(String) lookups}.
	 * 
	 * @deprecated The given {@link ResPrefix} should be used directly.
	 */
	@Deprecated
	public DefaultResourceView(ResPrefix keyPrefix) {
		assert keyPrefix != null;
		
		this.keyPrefix = keyPrefix;
	}

	@Override
	public String getStringResource(String resourceKey) {
		// Use the key as local suffix to the context-provided prefix.
		return Resources.getInstance().getString(getQualifiedResourceName(resourceKey));
	}

	@Override
	public String getStringResource(String resourceKey, String defaultValue) {
		// Use the key as local suffix to the context-provided prefix.
		return Resources.getInstance().getString(getQualifiedResourceName(resourceKey), defaultValue);
	}

	@Override
	public boolean hasStringResource(String resourceKey) {
		return Resources.getInstance().getString(getQualifiedResourceName(resourceKey), null) != null;
	}
	
	private ResKey getQualifiedResourceName(String resourceKey) {
		return keyPrefix.key(resourceKey);
	}

}
