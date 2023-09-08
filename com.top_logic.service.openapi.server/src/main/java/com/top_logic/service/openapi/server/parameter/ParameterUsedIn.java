/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.parameter;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.top_logic.service.openapi.common.document.ParameterLocation;

/**
 * Annotation for {@link RequestParameter} that defines where the parameter is expected.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface ParameterUsedIn {

	/**
	 * The {@link ParameterLocation} where the annotated {@link RequestParameter} is expected.
	 */
	ParameterLocation value();

}

