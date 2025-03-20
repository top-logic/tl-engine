/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.export;

import com.top_logic.basic.util.ResKey;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Creates a dynamic download-name for the excel-export.
 * 
 * @author <a href="mailto:cca@top-logic.com">Christian Canterino</a>
 */
public interface DownloadNameProvider {

	/**
	 * Creates a download-name for the given component.
	 * 
	 * @return A filename without extension for the export.
	 */
	String createDownloadName(LayoutComponent component, ResKey resKey);

}