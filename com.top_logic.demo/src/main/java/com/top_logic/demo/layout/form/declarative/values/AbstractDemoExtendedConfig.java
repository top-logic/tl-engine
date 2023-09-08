/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.declarative.values;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;

/**
 * Extended properties for {@link DemoExtendedConfigItem} and
 * {@link DemoConfiguredExtendedInstance.Config}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface AbstractDemoExtendedConfig extends ConfigurationItem {

	/**
	 * @see #getExtended()
	 */
	String EXTENDED = "extended";

	/**
	 * An option only available, if {@link DemoConfiguredExtendedInstance} (instead of
	 * {@link DemoConfiguredInstance} is choosen.
	 */
	@Name(EXTENDED)
	String getExtended();

	/**
	 * @see #getExtended()
	 */
	void setExtended(String value);

}
