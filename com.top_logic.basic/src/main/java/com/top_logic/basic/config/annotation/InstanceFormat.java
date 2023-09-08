/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.basic.config.PropertyKind;

/**
 * Annotation for modifying an {@link PropertyKind#ITEM}, {@link PropertyKind#ARRAY},
 * {@link PropertyKind#LIST}, or {@link PropertyKind#MAP} valued property to contain instances
 * automatically instantiated from configurations.
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@TagName("instance-format")
public @interface InstanceFormat {

	// Pure marker annotation.

}
