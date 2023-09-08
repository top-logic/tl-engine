/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.gui.profile;

import com.top_logic.layout.AbstractResourceProvider;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.util.Resources;

/**
 * {@link ResourceProvider} for {@link SecurityNode}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SecurityNodeResources extends AbstractResourceProvider {

	@Override
	public String getLabel(Object object) {
		if (object == null) {
			return super.getLabel(object);
		}
		return ((SecurityNode) object).getLabel(Resources.getInstance());
	}

}

