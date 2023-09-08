/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.renderer;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;

/**
 * The class {@link ReadOnlyNavigationRenderer} suppresses the children menu as it supposes it
 * renders a read only view to the breadcrumb.
 * 
 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
 */
public class ReadOnlyNavigationRenderer extends NavigationRenderer {

	/** {@link ConfigurationItem} for the {@link ReadOnlyNavigationRenderer}. */
	public interface Config extends NavigationRenderer.Config {

		@Override
		@BooleanDefault(false)
		boolean shouldWriteMenu();

	}

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link ReadOnlyNavigationRenderer}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public ReadOnlyNavigationRenderer(InstantiationContext context, Config config) {
		super(context, config);
	}

}