/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.resource;

import java.io.File;

import com.top_logic.layout.ResourceProvider;
import com.top_logic.mig.html.DefaultResourceProvider;

/**
 * {@link ResourceProvider} for {@link File}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ThemeDirResourceProvider extends DefaultResourceProvider {

	/** Singleton {@link ThemeDirResourceProvider} instance. */
	@SuppressWarnings("hiding")
	public static final ThemeDirResourceProvider INSTANCE = new ThemeDirResourceProvider();

	/**
	 * Creates a new {@link ThemeDirResourceProvider}.
	 */
	protected ThemeDirResourceProvider() {
		// singleton instance
	}

	@Override
	public String getLabel(Object object) {
		if (object instanceof ThemeResource) {
			return ((ThemeResource) object).getName();
		}
		return super.getLabel(object);
	}

}
