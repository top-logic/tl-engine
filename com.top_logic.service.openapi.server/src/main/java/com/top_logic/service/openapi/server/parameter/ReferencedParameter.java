/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.parameter;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.order.DisplayOrder;

/**
 * Reference to a {@link ConcreteRequestParameter}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DisplayOrder({
	ReferencedParameter.REFERENCE_NAME,
	ReferencedParameter.PARAMETER_DEFINITION,
})
@Label("Parameter reference")
public interface ReferencedParameter extends ConfigurationItem {

	/** @see #getParameterDefinition() */
	String PARAMETER_DEFINITION = "parameter-definition";

	/** @see #getReferenceName() */
	String REFERENCE_NAME = "reference-name";

	/**
	 * Name of the parameter reference.
	 */
	@Mandatory
	@Name(REFERENCE_NAME)
	String getReferenceName();

	/**
	 * Setter for {@link #getReferenceName()}.
	 */
	void setReferenceName(String value);

	/**
	 * Definition of the referenced parameter.
	 */
	@Mandatory
	@Name(PARAMETER_DEFINITION)
	ConcreteRequestParameter.Config<? extends ConcreteRequestParameter<?>> getParameterDefinition();

	/**
	 * Setter for {@link #getParameterDefinition()}.
	 */
	void setParameterDefinition(ConcreteRequestParameter.Config<? extends ConcreteRequestParameter<?>> value);

}

