/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.ResourceView;

/**
 * {@link ResourceView} that directly uses keys as labels.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class PlainKeyResources implements ResourceView {

	/**
	 * Singleton {@link PlainKeyResources} instance.
	 */
	public static final PlainKeyResources INSTANCE = new PlainKeyResources();

	private PlainKeyResources() {
		// Singleton constructor.
	}

	@Override
	public boolean hasStringResource(String resourceKey) {
		return true;
	}

	@Override
	public ResKey getStringResource(String resourceKey, ResKey defaultValue) {
		return getStringResource(resourceKey);
	}

	@Override
	public ResKey getStringResource(String resourceKey) {
		return ResKey.text(resourceKey);
	}
}