/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.tree.compare;

import java.util.List;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Provider of {@link TreeCompareConfiguration}s.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public interface TreeCompareConfigurationProvider {

	/**
	 * Creates the {@link TreeCompareConfiguration} out of the given (component) model.
	 */
	TreeCompareConfiguration getTreeCompareConfiguration(Object model, LayoutComponent layoutComponent);

	/**
	 * Types, from which this {@link TreeCompareConfiguration} can create a
	 *         {@link TreeCompareConfiguration}.
	 */
	List<Class<?>> getSupportedModelTypes();

}
