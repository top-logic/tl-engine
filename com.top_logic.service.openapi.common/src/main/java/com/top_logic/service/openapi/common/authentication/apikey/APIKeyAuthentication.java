/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.authentication.apikey;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.service.openapi.common.authentication.AuthenticationConfig;

/**
 * {@link AuthenticationConfig} to authenticate using an API key.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DisplayOrder({
	APIKeyAuthentication.DOMAIN,
	APIKeyAuthentication.PARAMETER_NAME,
	APIKeyAuthentication.POSITION
})
@TagName("api-key-authentication")
@Label("API key")
public interface APIKeyAuthentication extends AuthenticationConfig {

	/** Configuration name for {@link #getPosition()}. */
	String POSITION = "position";

	/** Configuration name for {@link #getParameterName()}. */
	String PARAMETER_NAME = "parameter-name";

	/**
	 * The position where the API key is found in the request.
	 */
	@Mandatory
	@Name(POSITION)
	APIKeyPosition getPosition();

	/**
	 * Setter for {@link #getPosition()}.
	 */
	void setPosition(APIKeyPosition value);

	/**
	 * Name of the parameter holding the API key.
	 */
	@Mandatory
	@Name(PARAMETER_NAME)
	String getParameterName();

	/**
	 * Setter for {@link #getParameterName()}.
	 */
	void setParameterName(String value);

}


