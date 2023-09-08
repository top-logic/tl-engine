/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.authentication;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;

/**
 * Configuration of a secret for Open API communication.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Abstract
public interface SecretConfiguration extends ConfigurationItem {

	/** Configuration name of {@link #getDomain()}. */
	String DOMAIN = "domain";

	/**
	 * The domain of the secret.
	 * 
	 * @see AuthenticationConfig#getDomain()
	 */
	@Name(DOMAIN)
	@Mandatory
	String getDomain();

}

