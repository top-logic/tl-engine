/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers.theme;

import java.util.Collection;

import com.top_logic.basic.func.Function0;
import com.top_logic.basic.func.GenericFunction;
import com.top_logic.gui.MultiThemeFactory;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.gui.config.ThemeConfig;

/**
 * {@link GenericFunction} retrieving all {@link ThemeConfig} id's.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class AllThemeIds extends Function0<Collection<String>> {

	@Override
	public Collection<String> apply() {
		return ((MultiThemeFactory) ThemeFactory.getInstance()).getThemeConfigIds();
	}

}
