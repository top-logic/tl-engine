/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.conf;

import java.util.Map;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.service.openapi.common.document.Described;
import com.top_logic.service.openapi.server.impl.ServiceMethodBuilder;
import com.top_logic.service.openapi.server.impl.ServiceMethodBuilderByExpression;
import com.top_logic.service.openapi.server.parameter.RequestParameter;

/**
 * A single API operation bound to a certain path.
 * 
 * @see PathItem#getOperations()
 */
@com.top_logic.basic.config.annotation.TagName("Operation")
public interface Operation extends ParametersConfig, Described {

	/** @see #getResponses() */
	String RESPONSES = "responses";

	/**
	 * @see #getSummary()
	 */
	String SUMMARY = "summary";

	/**
	 * @see #getImplementation()
	 */
	String IMPLEMENTATION = "implementation";

	/**
	 * A short summary of what the operation does.
	 */
	@com.top_logic.basic.config.annotation.Name(SUMMARY)
	@Nullable
	String getSummary();

	/**
	 * Setter for {@link #getSummary()}.
	 */
	void setSummary(String value);

	@Override
	@DefaultContainer
	java.util.List<RequestParameter.Config<? extends RequestParameter<?>>> getParameters();

	/**
	 * The operation to execute upon request.
	 */
	@com.top_logic.basic.config.annotation.Name(IMPLEMENTATION)
	@Mandatory
	@ImplementationClassDefault(ServiceMethodBuilderByExpression.class)
	PolymorphicConfiguration<? extends ServiceMethodBuilder> getImplementation();

	/**
	 * Setter for {@link #getImplementation()}.
	 */
	void setImplementation(PolymorphicConfiguration<? extends ServiceMethodBuilder> value);

	/**
	 * Definition of the potential responses of this operation.
	 */
	@Key(OperationResponse.RESPONSE_CODE)
	@Name(RESPONSES)
	Map<String, OperationResponse> getResponses();

}
