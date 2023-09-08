/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.authentication.oauth;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.service.openapi.common.authentication.AuthenticationConfig;

/**
 * Authentication using the <i>OAuth2</i> authentication.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Abstract
@Label("OAuth authentication")
public interface OAuthAuthentication extends AuthenticationConfig {

	// no additional config here

}

