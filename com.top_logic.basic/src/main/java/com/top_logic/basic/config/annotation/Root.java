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

/**
 * Annotation to signal that a getter method delivers an ancestor in the
 * containment hierarchy that serves as root of an <b>instance</b> (sub-)
 * hierarchy.
 * 
 * <p>
 * A type may contain more than one {@link Root} annotated getter and a return
 * type of a {@link Root} annotated getter may contain another {@link Root}
 * annotated getter.
 * </p>
 * 
 * <p>
 * Note: The root <b>type</b> of a configuration hierarchy is defined by
 * declaring a visit interface with a visitor of its hierarchy.
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
public @interface Root {

	// Marker only.
	
}
