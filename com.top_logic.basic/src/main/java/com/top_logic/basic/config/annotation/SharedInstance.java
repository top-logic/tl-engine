/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.basic.config.ConfiguredInstance;

/**
 * Annotation to a {@link ConfiguredInstance} that marks it as being safe for sharing between
 * different usages with identical configurations.
 * 
 * <p>
 * Only those classes must be marked as {@link SharedInstance}s that have no state that can be
 * modified after an instance is constructed.
 * </p>
 * 
 * <p>
 * This annotation is not inherited. It must be repeated on each subclass that keeps the stateless
 * property of its super class.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SharedInstance {

	// Marker annotation

}
