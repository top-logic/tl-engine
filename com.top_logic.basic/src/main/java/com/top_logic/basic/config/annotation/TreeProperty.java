/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a structural property (LIST or ITEM of {@link com.top_logic.basic.config.PolymorphicConfiguration})
 * as a tree property in the view designer.
 *
 * <p>
 * Properties with this annotation are displayed as sub-nodes in the design tree. Structural
 * properties WITHOUT this annotation are displayed as editable fields in the configuration form
 * instead.
 * </p>
 *
 * @see Hidden
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TreeProperty {

	/**
	 * Whether this property should be displayed in the tree.
	 */
	boolean value() default true;
}
