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
 * Annotation of a property that serves as {@link Key} of lists and whose values are unique in the
 * whole configuration, not only within one list.
 * 
 * <p>
 * The key value of an entry of a list keyed by a {@link GlobalKey} property identifies the entry
 * among all entries of all such lists of the configuration tree the {@link ConfigurationReader}
 * reads from its sources. The reader rejects two entries with the same key value, wherever they
 * stand. An entry can therefore be addressed without naming the path to it: An element with the
 * attribute {@link ConfigurationSchemaConstants#CONFIG_REF} in a later source updates the entry
 * with that key below the element it is written in, wherever that entry sits.
 * </p>
 * 
 * <p>
 * An entry without key value (see {@link ConfigUtil#isAnonymousKey(Object)}) is anonymous as in
 * any keyed list: it takes part in no uniqueness check and cannot be referenced.
 * </p>
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
