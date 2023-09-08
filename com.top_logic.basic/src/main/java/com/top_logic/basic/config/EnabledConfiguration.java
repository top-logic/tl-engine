/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;

/**
 * Configuration interface which can be used to mark some entity as enabled.
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface EnabledConfiguration extends NamedConfigMandatory {

	/** The attribute to configure the value of the entity to enable */
	String ENABLED_VALUE = "enabled";

	/**
	 * Returns the name of the entity which is enabled or not
	 */
	@Override
	String getName();

	/**
	 * Determines whether the represented identity is enabled or not.
	 */
	@Name(ENABLED_VALUE)
	@BooleanDefault(true)
	boolean isEnabled();

}
