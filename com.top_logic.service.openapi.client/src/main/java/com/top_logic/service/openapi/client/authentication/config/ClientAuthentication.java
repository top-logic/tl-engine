/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.authentication.config;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.service.openapi.client.authentication.SecurityEnhancer;
import com.top_logic.service.openapi.client.registry.ServiceMethodRegistry;
import com.top_logic.service.openapi.common.authentication.AuthenticationConfig;

/**
 * {@link AuthenticationConfig} for services acting as client for an <i>OpenAPI</i> communication.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ClientAuthentication<C extends ClientAuthentication.Config<?>> extends ConfiguredInstance<C> {

	/**
	 * Configuration options for {@link ClientAuthentication}.
	 */
	@Abstract
	public interface Config<I extends ClientAuthentication<?>>
			extends PolymorphicConfiguration<I>, AuthenticationConfig {
		// Base interface.
	}

	/**
	 * Creates the {@link SecurityEnhancer} implementation that actually adds credentials to an
	 * outgoing request.
	 */
	SecurityEnhancer createSecurityEnhancer(ServiceMethodRegistry registry);

}
