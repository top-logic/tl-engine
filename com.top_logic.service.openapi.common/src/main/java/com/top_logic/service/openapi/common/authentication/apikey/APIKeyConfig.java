/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.authentication.apikey;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;

/**
 * Definition of an API key authentication transport.
 */
@Abstract
public interface APIKeyConfig extends ConfigurationItem {

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
