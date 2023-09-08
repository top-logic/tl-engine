/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.authentication;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.service.openapi.client.registry.ServiceMethodRegistry;
import com.top_logic.service.openapi.common.authentication.SecretConfiguration;

/**
 * {@link SecretConfiguration} for the client in an Open API communication.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Abstract
public interface ClientSecret extends SecretConfiguration, ServiceMethodRegistry.ServiceRegistryPart {

	// No properties here

}

