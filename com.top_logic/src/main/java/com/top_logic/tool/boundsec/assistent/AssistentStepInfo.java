/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.assistent;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Metainformation for assistent steps are stored here.
 * 
 * @author    <a href="mailto:skr@top-logic.com">Sylwester Kras</a>
 */
public interface AssistentStepInfo extends ConfigPart {

	@Name("labelKeySuffix")
	String getStepKey();

	/**
	 * Returns the name of the {@link com.top_logic.mig.html.layout.LayoutComponent.Config} for
	 * which this {@link AssistentStepInfo} was created.
	 * 
	 * @param assistantInfo
	 *        The {@link AssistentStepInfo} to get name for. May be <code>null</code>.
	 */
	static ComponentName getName(AssistentStepInfo assistantInfo) {
		if (assistantInfo == null) {
			return null;
		}
		return ((LayoutComponent.Config) assistantInfo.container()).getName();
	}

}
