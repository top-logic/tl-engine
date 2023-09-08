/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.module;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The class {@link ServiceExtensionPoint} denotes the {@link BasicRuntimeModule service} which
 * serves as extension point for the annotated service, i.e. the annotated service is an add on for
 * the service given in {@link #value()}.
 * 
 * @see ServiceDependencies
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceExtensionPoint {
	
	/**
	 * The class of the service for which the annotated class is an add on.
	 */
	Class<? extends BasicRuntimeModule<?>> value();

}

