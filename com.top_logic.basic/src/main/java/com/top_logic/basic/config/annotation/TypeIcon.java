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
import com.top_logic.basic.config.TypeIcons;

/**
 * Annotation of a {@link ConfigurationItem} interface giving the configuration type an icon.
 *
 * <p>
 * The icon tells the kind of a configuration apart at a glance wherever configurations of many
 * types are displayed side by side, e.g. in an editor showing the structure of a configuration, or
 * in a list offering the types of a new element.
 * </p>
 *
 * <p>
 * The annotation is inherited along the configuration type hierarchy: a type without an
 * annotation of its own shows the icon of the most specific annotated type it extends.
 * </p>
 *
 * @see TypeIcons#iconKey(com.top_logic.basic.config.ConfigurationDescriptor)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@TagName("type-icon")
public @interface TypeIcon {

	/**
	 * @see #value()
	 */
	String VALUE = "value";

	/**
	 * The encoded theme image of the icon, e.g. {@code css:bi bi-table}.
	 *
	 * <p>
	 * The value is the key of a theme image, so that a theme can replace the icon by naming the
	 * image of a theme variable instead of a fixed icon.
	 * </p>
	 */
	@Name(VALUE)
	String value();

}
