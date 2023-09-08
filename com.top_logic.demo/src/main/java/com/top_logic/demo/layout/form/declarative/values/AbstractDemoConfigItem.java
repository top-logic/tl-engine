/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.declarative.values;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;

/**
 * Common properties for {@link DemoConfigItem} and {@link DemoPolymorphicConfig}
 * 
 * <p>
 * Note: {@link DemoPolymorphicConfig} does not directly extend {@link DemoConfigItem} to avoid
 * mixing plain configuration items and {@link PolymorphicConfiguration}s in options of the form.
 * Such mixture leads to problematic option lists (options for polymorphic configurations are
 * implementation classes, options for plain configurations are configuration item interfaces.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface AbstractDemoConfigItem extends ConfigurationItem {

	/**
	 * @see #getFirst()
	 */
	String FIRST = "first";

	/**
	 * @see #getSecond()
	 */
	String SECOND = "second";

	/**
	 * Some property.
	 */
	@Name(FIRST)
	String getFirst();

	/**
	 * @see #getFirst()
	 */
	void setFirst(String first);

	/**
	 * Some other property.
	 */
	@Name(SECOND)
	String getSecond();

	/**
	 * @see #getSecond()
	 */
	void setSecond(String second);

}
