/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.rewriters;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.top_logic.knowledge.service.db2.migration.GuiceContext;

/**
 * Annotation for classes that serves as API for the {@link #value()}.
 * 
 * <p>
 * When an instance of a such annotated class is instantiated, it can later be used as
 * implementation for the provided API.
 * </p>
 * 
 * @see GuiceContext
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ProvidesAPI {

	/**
	 * The class of the interface for which the annotated class can be uses as instance.
	 */
	Class<?> value();

}
