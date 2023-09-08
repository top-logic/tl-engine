/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.defaults.NullDefault;

/**
 * Annotation to mark a configuration property as mandatory
 * <p>
 * "Mandatory" means: When the {@link ConfigurationItem} is created, there has to be an explicit
 * value for this property. This value is allowed to be null. To forbid null, use
 * {@link NonNullable}.
 * </p>
 * 
 * @see Nullable
 * @see NullDefault
 * @see NonNullable
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@TagName("mandatory")
public @interface Mandatory {

	// Pure marker annotation.

}
