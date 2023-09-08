/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.client.registry.conf;

import java.util.List;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.container.ConfigPart;

/**
 * {@link ParameterContext} that is itself defined in a {@link ParameterContext} and delegates to
 * that context.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface InParameterContext extends ConfigPart, ParameterContext {

	/**
	 * @see #getContext()
	 */
	String CONTEXT = "context";

	/**
	 * The context of this value definition to get access to currently defined parameter names.
	 */
	@Name(CONTEXT)
	@Hidden
	@Container
	ParameterContext getContext();

	@Override
	@DerivedRef({ CONTEXT, ParameterContext.PARAMETER_NAMES })
	List<String> getParameterNames();

}
