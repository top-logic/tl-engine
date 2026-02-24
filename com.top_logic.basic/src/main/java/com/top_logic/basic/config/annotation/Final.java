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

import com.top_logic.basic.config.ConfigurationItem;

/**
 * Annotates {@link ConfigurationItem} interfaces to indicate that no sub-interfaces should be
 * considered.
 *
 * <p>
 * When serializing a {@link ConfigurationItem} whose static type is annotated {@link Final}, no
 * type annotation (e.g., <code>$type</code> in JSON) is written. When reading, the expected type is
 * used directly without requiring a type annotation.
 * </p>
 *
 * <p>
 * This is the opposite of {@link Abstract}: While {@link Abstract} means "must use a subtype",
 * {@link Final} means "always use exactly this type, no polymorphism".
 * </p>
 *
 * @see Abstract
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@TagName("final")
public @interface Final {

	// Pure marker annotation.

}
