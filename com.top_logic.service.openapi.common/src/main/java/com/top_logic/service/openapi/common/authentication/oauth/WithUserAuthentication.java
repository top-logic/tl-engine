/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.authentication.oauth;

import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.func.misc.AlwaysFalse;
import com.top_logic.service.openapi.common.authentication.ClientAuthentication;

/**
 * Uses the personal access token of the user currently logged in via OIDC for authorization.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface WithUserAuthentication extends ClientAuthentication {

	@Override
	@Derived(fun = AlwaysFalse.class, args = {})
	boolean isSeparateSecretNeeded();

}
