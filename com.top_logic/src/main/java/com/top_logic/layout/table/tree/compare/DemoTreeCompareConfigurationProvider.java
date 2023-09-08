/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.tree.compare;

import java.util.Collections;
import java.util.List;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link TreeCompareConfigurationProvider} for TL-Demo.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class DemoTreeCompareConfigurationProvider implements TreeCompareConfigurationProvider {

	@Override
	public TreeCompareConfiguration getTreeCompareConfiguration(Object model, LayoutComponent layoutComponent) {
		if (model != null) {
			return new TreeCompareConfiguration();
		}
		return null;
	}

	@Override
	public List<Class<?>> getSupportedModelTypes() {
		return Collections.<Class<?>> singletonList(Object.class);
	}

}
