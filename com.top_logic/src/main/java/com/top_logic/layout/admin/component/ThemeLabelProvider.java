/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.admin.component;

import com.top_logic.gui.Theme;
import com.top_logic.layout.LabelProvider;

/**
 * {@link LabelProvider} for {@link Theme}s.
 * 
 * @see Theme#getName()
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class ThemeLabelProvider implements LabelProvider {

	public static final ThemeLabelProvider INSTANCE = new ThemeLabelProvider();

	@Override
	public String getLabel(Object object) {
		return ((Theme) object).getName();
	}

}
