/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.basic.config.container.ConfigPart;

/**
 * Annotation to signal that a getter method delivers the (direct) container
 * of a composite in a configuration hierarchy.
 * 
 * <p>
 * Note: To use a {@link Container} property in a configuration item, the configuration interface
 * must extends {@link ConfigPart}.
 * </p>
 * 
 * @see Root
 * @see Container
 * @see Reference
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@TagName("container")
public @interface Container {

	// Marker only.
	
}
