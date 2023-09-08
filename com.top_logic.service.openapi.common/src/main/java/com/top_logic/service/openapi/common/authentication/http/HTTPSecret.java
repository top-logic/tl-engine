/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.authentication.http;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.service.openapi.common.authentication.SecretConfiguration;

/**
 * {@link SecretConfiguration} for HTTP authentication.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface HTTPSecret extends SecretConfiguration {

	/** Configuration name for the value of {@link #getLogin()}. */
	String LOGIN = "login";

	/**
	 * User and password that to authenticate.
	 */
	@Mandatory
	@Name(LOGIN)
	LoginCredentials getLogin();

	/**
	 * Setter for {@link #getLogin()}.
	 */
	void setLogin(LoginCredentials value);

}

