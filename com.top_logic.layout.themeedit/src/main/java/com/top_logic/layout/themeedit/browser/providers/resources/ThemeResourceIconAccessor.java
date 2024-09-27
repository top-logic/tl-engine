/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers.resources;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.themeedit.browser.resource.ResourceType;
import com.top_logic.layout.themeedit.browser.resource.ThemeResource;
import com.top_logic.layout.themeedit.browser.resource.ThemeResourceImage;

/**
 * {@link Accessor} building a {@link ThemeImage} for a {@link ThemeResource}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ThemeResourceIconAccessor extends ReadOnlyAccessor<ThemeResource> {

	@Override
	public Object getValue(ThemeResource resource, String property) {
		String themeKey = resource.getThemeKey();
		ThemeImage icon;
		String tooltip;
		
		if (ResourceType.FONT.equals(resource.getType())) {
			icon = Icons.FONT;
			tooltip = themeKey;
		} else {			
			icon = ThemeImage.resourceIcon(themeKey, resource.getDefiningTheme().getPathEffective() + themeKey);
			tooltip = icon.toEncodedForm();
		}

		return new ThemeResourceImage(icon, ResKey.text(tooltip));
	}
}
