/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.authentication.http.basic;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.service.openapi.common.authentication.http.HTTPAuthentication;

/**
 * Authentication using HTTP <i>BasicAuth</i> mechanism.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName("basic-authentication")
@Label("HTTP authentication (BasicAuth)")
public interface BasicAuthentication extends HTTPAuthentication {

	// No additional properties here.

}

