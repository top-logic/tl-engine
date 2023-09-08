/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.authentication.http;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.service.openapi.common.authentication.AuthenticationConfig;

/**
 * Authentication over HTTP.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DisplayOrder({
	HTTPAuthentication.DOMAIN,
})
@TagName("http-authentication")
@Label("HTTP authentication")
@Abstract
public interface HTTPAuthentication extends AuthenticationConfig {

	// No additional properties here.

}

