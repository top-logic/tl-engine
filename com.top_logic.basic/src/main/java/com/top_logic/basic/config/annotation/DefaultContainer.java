/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
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
 * Annotation on a property getter of kind {@link PropertyKind#ITEM}, {@link PropertyKind#ARRAY},
 * {@link PropertyKind#LIST}, {@link PropertyKind#MAP} signaling that the container tag can be
 * omitted.
 * 
 * <p>
 * When a property is annotated {@link DefaultContainer}, the configuration contents of the property
 * can be directly placed inside the tag representing the owner of the property. Only a single
 * property can be the default container. When inheriting from multiple sources, only one of them
 * may have a default container property.
 * </p>
 * 
 * <p>
 * An item declared as
 * </p>
 * 
 * <code style="white-space:pre">
 * interface A extends ConfigurationItem {
 *   &#64;DefaultContainer
 *   List&lt;B&gt; getBs();
 * }
 * </code>
 * 
 * <p>
 * can be configured with
 * </p>
 * 
 * <xmp> <a> <b>...</b> <b>...</b> <b>...</b> </a> </xmp>
 * 
 * <p>
 * instead of
 * </p>
 * 
 * <xmp> <a> <bs> <b>...</b> <b>...</b> <b>...</b> </bs> </a> </xmp>
 * 
 * <p>
 * With a singleton instance property of kind {@link PropertyKind#ITEM}, {@link DefaultContainer} is
 * only useful, if the property also has a {@link Subtypes} annotation, or its content types have
 * custom {@link TagName}s. Such custom tag names of content types are only considered in the
 * context of {@link PropertyKind#ITEM} properties, if the property is the {@link DefaultContainer}.
 * </p>
 * 
 * <p>
 * An item declared as
 * </p>
 * 
 * <code style="white-space:pre">
 * interface A extends ConfigurationItem {
 *   &#64;DefaultContainer
 *   &#64;Subtypes({
 *   	&#64;Subtype(tag="c", type=C.class),
 *   	&#64;Subtype(tag="d", type=D.class),
 *   })
 *   B getB();
 * }
 * </code>
 * 
 * <p>
 * can be configured with
 * </p>
 * 
 * <xmp> <a> <c>...</c> </a> </xmp>
 * 
 * <p>
 * instead of
 * </p>
 * 
 * <xmp> <a> <b config:interface="my.package.C">...</b> </a> </xmp>
 * 
 * <p>
 * Note: The new tags introduced by the combination of {@link Subtypes} and {@link DefaultContainer}
 * must not conflict with other properties of the context type that declares the
 * {@link DefaultContainer} property. This is also true for {@link DefaultContainer} properties of
 * collection kind.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@TagName("default-container")
public @interface DefaultContainer {

	// Pure marker annotation.

}
