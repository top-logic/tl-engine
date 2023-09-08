/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.annotation.defaults;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.TagName;

/**
 * Annotation to specify a class that should be instantiated as default value for a configuration
 * property with {@link InstanceFormat}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@TagName("instance-default")
public @interface InstanceDefault {

	/**
	 * The default (singleton) class.
	 */
	Class<?> value();

}
