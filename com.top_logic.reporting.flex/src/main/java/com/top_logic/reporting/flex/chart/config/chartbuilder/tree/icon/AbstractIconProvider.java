/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.chartbuilder.tree.icon;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;

/**
 * Configurable base implementation of {@link IconProvider}
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public abstract class AbstractIconProvider implements IconProvider {

	/**
	 * Base config-interface for {@link AbstractIconProvider}.
	 */
	public interface Config extends PolymorphicConfiguration<AbstractIconProvider> {

		/**
		 * Property name <code>KEY</code>
		 */
		public static final String KEY = "key";

		/**
		 * the key to identify an icon
		 */
		@Name(KEY)
		public Integer getKey();

	}

}