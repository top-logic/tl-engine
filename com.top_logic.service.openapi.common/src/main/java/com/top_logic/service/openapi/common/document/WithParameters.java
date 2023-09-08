/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.document;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.json.JsonBinding;

/**
 * {@link ConfigurationItem} for objects using {@link IParameterObject}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Abstract
public interface WithParameters extends ConfigurationItem {

	/** Configuration name for the value of {@link #getParameters()}. */
	String PARAMETERS = "parameters";

	/**
	 * A list of parameters.
	 */
	@Name(PARAMETERS)
	@JsonBinding(ParameterObjectBinding.class)
	List<IParameterObject> getParameters();

}

