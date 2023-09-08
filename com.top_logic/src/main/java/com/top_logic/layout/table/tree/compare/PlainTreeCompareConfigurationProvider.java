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
 * {@link TreeCompareConfigurationProvider}, that treats the given model as already provided
 * {@link TreeCompareConfiguration}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public final class PlainTreeCompareConfigurationProvider
		implements TreeCompareConfigurationProvider {

	/** Singleton {@link PlainTreeCompareConfigurationProvider} instance. */
	public static final PlainTreeCompareConfigurationProvider INSTANCE = new PlainTreeCompareConfigurationProvider();

	/**
	 * Creates a new {@link PlainTreeCompareConfigurationProvider}.
	 * 
	 */
	protected PlainTreeCompareConfigurationProvider() {
		// singleton instance
	}

	@Override
	public TreeCompareConfiguration getTreeCompareConfiguration(Object model, LayoutComponent layoutComponent) {
		return (TreeCompareConfiguration) model;
	}

	@Override
	public List<Class<?>> getSupportedModelTypes() {
		return Collections.<Class<?>> singletonList(TreeCompareConfiguration.class);
	}
}