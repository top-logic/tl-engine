/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.authentication;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.service.openapi.common.authentication.SecretConfiguration;
import com.top_logic.service.openapi.server.OpenApiServer;
import com.top_logic.service.openapi.server.OpenApiServer.OpenAPIServerPart;

/**
 * {@link SecretConfiguration} for the server part in an Open API communication.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Abstract
public interface ServerSecret extends SecretConfiguration, ConfigPart, OpenAPIServerPart {

	/**
	 * @see #getOwner()
	 */
	String OWNER = "owner";

	/**
	 * The <i>OpenAPI</i> server configuration to which this {@link ServerSecret} belongs.
	 */
	@Container
	@Name(OWNER)
	@Hidden
	OpenApiServer.Config<?> getOwner();

	@Override
	@DerivedRef(ServerSecret.OWNER)
	OpenApiServer.Config<? extends OpenApiServer> getServerConfiguration();

}

