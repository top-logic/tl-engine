/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security;

import com.top_logic.base.security.authorisation.symbols.Authorisation;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.tool.boundsec.LayoutSecurityConfiguration;

/**
 * Security relevant configurations for top logic.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface SecurityConfiguration extends ConfigurationItem {

	/**
	 * Configuration holding security relevant settings for the layout.
	 */
	@ItemDefault
	LayoutSecurityConfiguration getLayout();

	/**
	 * {@link Authorisation} instance of the application.
	 */
	@InstanceFormat
	Authorisation getAuthorisation();

}

