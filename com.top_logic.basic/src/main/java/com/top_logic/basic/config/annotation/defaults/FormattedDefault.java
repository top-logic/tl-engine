/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.annotation.defaults;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.annotation.TagName;

/**
 * Annotation to annotate a {@link String} which is interpreted by a
 * {@link ConfigurationValueProvider} in a property of kind
 * {@link PropertyKind#PLAIN}.
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@TagName("formatted-default")
public @interface FormattedDefault {

	String value();

}
