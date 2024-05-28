/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.authentication.oauth;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.service.openapi.common.authentication.ClientAuthentication;

/**
 * {@link TokenBasedAuthentication} to authenticate using client credentials.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName("client-credentials-authentication")
@Label("OpenID authentication")
public interface ClientCredentials extends TokenBasedAuthentication, ClientAuthentication {

	// Sum interface

}

