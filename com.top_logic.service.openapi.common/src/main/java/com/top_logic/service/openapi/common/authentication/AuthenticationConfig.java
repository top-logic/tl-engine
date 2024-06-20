/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.authentication;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.func.misc.AlwaysTrue;

/**
 * Configuration of the authentication in Open API communication.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Abstract
public interface AuthenticationConfig extends ConfigurationItem {

	/** Configuration name of the property {@link #getDomain()}. */
	String DOMAIN = "domain";

	/**
	 * Definition of the domain for which this {@link AuthenticationConfig} can be used.
	 */
	@Name(DOMAIN)
	@Mandatory
	String getDomain();

	/**
	 * Setter for {@link #getDomain()}.
	 */
	void setDomain(String value);

	/**
	 * Whether for this kind of authorization a separate secret is necessary.
	 */
	@Hidden
	@Derived(fun = AlwaysTrue.class, args = {})
	boolean isSeparateSecretNeeded();

}

