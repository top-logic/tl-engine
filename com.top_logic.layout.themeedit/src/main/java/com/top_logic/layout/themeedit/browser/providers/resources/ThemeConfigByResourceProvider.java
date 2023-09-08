/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers.resources;

import com.top_logic.gui.config.ThemeConfig;
import com.top_logic.layout.component.model.ModelProvider;
import com.top_logic.layout.themeedit.browser.resource.ThemeResource;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Provides the defining {@link ThemeConfig} from a given {@link ThemeResource}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class ThemeConfigByResourceProvider implements ModelProvider {

	@Override
	public Object getBusinessModel(LayoutComponent businessComponent) {
		ThemeResource themeResource = (ThemeResource) businessComponent.getModel();
		
		return themeResource == null ? null : themeResource.getDefiningTheme();
	}

}
