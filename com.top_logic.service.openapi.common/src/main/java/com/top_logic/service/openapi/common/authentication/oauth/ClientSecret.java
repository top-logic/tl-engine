/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.authentication.oauth;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Encrypted;
import com.top_logic.basic.config.annotation.Mandatory;

/**
 * Identification of a client in a token or authentication server, e.g. when tokens are requested or
 * checked.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ClientSecret extends ConfigurationItem {

	/**
	 * Id of this client known by the token or authentication server.
	 */
	@Mandatory
	String getClientId();

	/**
	 * Password to authenticate this client at the token or authentication server.
	 */
	@Mandatory
	@Encrypted
	String getPassword();
}

