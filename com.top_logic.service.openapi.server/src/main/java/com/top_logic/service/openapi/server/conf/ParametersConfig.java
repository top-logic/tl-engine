/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.conf;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.service.openapi.server.parameter.RequestParameter;

/**
 * Configuration of parameters for an operation.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ParametersConfig extends ConfigurationItem {

	/**
	 * @see #getParameters()
	 */
	String PARAMETERS = "parameters";

	/**
	 * A list of parameters that are applicable for this operation.
	 * 
	 * <p>
	 * The list must not include multiple parameters with the same name.
	 * </p>
	 */
	@com.top_logic.basic.config.annotation.Name(PARAMETERS)
	@Key(RequestParameter.Config.NAME_ATTRIBUTE)
	java.util.List<RequestParameter.Config<? extends RequestParameter<?>>> getParameters();

}

