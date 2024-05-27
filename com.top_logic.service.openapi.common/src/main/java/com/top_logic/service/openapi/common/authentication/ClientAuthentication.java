/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.authentication;

import com.top_logic.basic.config.annotation.Abstract;

/**
 * {@link AuthenticationConfig} for services acting as client for an <i>OpenAPI</i> communication.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Abstract
public interface ClientAuthentication extends AuthenticationConfig {

	/**
	 * Visitor method for {@link AuthenticationConfig}.
	 */
	<R, A> R visit(ClientAuthenticationVisitor<R, A> visitor, A arg);

}
