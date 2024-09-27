/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.resources;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.ResourceView;
import com.top_logic.util.Resources;

/**
 * {@link ResourceView} that delegates to {@link Resources} without prefix.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GlobalResources implements ResourceView {

	/**
	 * Singleton {@link GlobalResources} instance.
	 */
	public static final GlobalResources INSTANCE = new GlobalResources();

	private GlobalResources() {
		// Singleton constructor.
	}

	@Override
	public ResKey getStringResource(String resourceKey) {
		return key(resourceKey);
	}

	@Override
	public ResKey getStringResource(String resourceKey, ResKey defaultValue) {
		return key(resourceKey).fallback(defaultValue);
	}

	@Override
	public boolean hasStringResource(String resourceKey) {
		return Resources.getInstance().getString(key(resourceKey), null) != null;
	}

	private ResKey key(String resourceKey) {
		return ResPrefix.GLOBAL.key(resourceKey);
	}

}
