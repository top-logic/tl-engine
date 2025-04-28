/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.export;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.top_logic.basic.util.ResKey;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link DownloadNameProvider} that creates a download name derived from the component title.
 */
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
		ResKey titleKey = component.getTitleKey();
		ResKey app = com.top_logic.layout.I18NConstants.APPLICATION_TITLE;
		String today = new SimpleDateFormat("yyyy-dd-MM").format(new Date());
		return I18NConstants.COMPONENT_DOWNLOAD_NAME__DATE_TITLE_APP.fill(today, titleKey, app);
	}

}
