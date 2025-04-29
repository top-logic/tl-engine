/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.export;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * {@link DownloadNameProvider} that creates a download name derived from the component and
 * application title.
 */
@InApp
public class ComponentDownloadName implements DownloadNameProvider {

	/**
	 * Singleton {@link ComponentDownloadName} instance.
	 */
	public static final ComponentDownloadName INSTANCE = new ComponentDownloadName();

	private ComponentDownloadName() {
		// Singleton constructor.
	}

	@Override
	public ResKey createDownloadName(LayoutComponent component, Object model) {
		ResKey titleKey = componentTitle(component);
		ResKey app = com.top_logic.layout.I18NConstants.APPLICATION_TITLE;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", TLContext.getLocale());
		format.setTimeZone(TLContext.getTimeZone());
		String today = format.format(new Date());
		if (model != null) {
			String modelLabel = MetaLabelProvider.INSTANCE.getLabel(model);

			return I18NConstants.COMPONENT_DOWNLOAD_NAME__DATE_TITLE_APP_MODEL.fill(today, titleKey, app, modelLabel);
		} else {
			return I18NConstants.COMPONENT_DOWNLOAD_NAME__DATE_TITLE_APP.fill(today, titleKey, app);
		}
	}

	private ResKey componentTitle(LayoutComponent component) {
		LayoutComponent titleComponent = component;
		while (true) {
			if (titleComponent == null) {
				return com.top_logic.layout.table.export.I18NConstants.DEFAULT_EXPORT_NAME;
			}

			ResKey titleKey = titleComponent.getTitleKey();
			if (Resources.getInstance().getString(titleKey, null) != null) {
				return titleKey;
			}

			titleComponent = titleComponent.getParent();
		}
	}

}
