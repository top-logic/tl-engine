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

import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.ConfigurationSchemaConstants;

/**
 * Annotation of a property whose values are unique in the whole configuration: the value
 * identifies the item that carries it, wherever the item stands.
 * 
 * <p>
 * The value of a {@link GlobalKey} property identifies the item among all items of the
 * configuration tree the {@link ConfigurationReader} reads from its sources, whether the item is
 * an entry of a list (keyed by this property, by another one, or not at all), the value of an item
 * property or an entry of a map. The reader rejects two items with the same value. An item can
 * therefore be addressed without naming the path to it: An element with the attribute
 * {@link ConfigurationSchemaConstants#CONFIG_REF} in a later source updates the item with that
 * value below the element it is written in, wherever that item sits.
 * </p>
 * 
 * <p>
 * The property typically also serves as {@link Key} of the lists holding such items, so that an
 * entry written in a later source matches the entry of the same key. An item without value is
 * anonymous as in any keyed list: it takes part in no uniqueness check and cannot be referenced.
 * </p>
 * 
 * @implNote Whether a value counts as "no value" is decided by
 *           {@link ConfigUtil#isAnonymousKey(Object)}.
 * 
 * @see Key
 * @see ConfigurationSchemaConstants#CONFIG_REF
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@TagName("global-key")
public @interface GlobalKey {

	// Pure marker annotation.

}
