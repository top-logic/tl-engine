/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.conf;

import java.util.List;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.service.openapi.client.registry.impl.value.ValueProducerFactory;

/**
 * Context for the configuration of {@link ValueProducerFactory} implementations.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface ParameterContext extends ConfigurationItem {

	/**
	 * @see #getParameterNames()
	 */
	String PARAMETER_NAMES = "parameter-names";

	/**
	 * Access to currently defined parameter names.
	 */
	@Name(PARAMETER_NAMES)
	@Abstract
	@Hidden
	@Format(CommaSeparatedStrings.class)
	List<String> getParameterNames();

}
