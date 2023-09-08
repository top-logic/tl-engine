/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.annotation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.annotation.TagName;

/**
 * Annotation for {@link ConfigurationItem} which of its properties is shown in the title if the
 * item is collapsed.
 * 
 * <p>
 * This annotation can also be used on {@link PropertyKind#LIST list }, {@link PropertyKind#MAP
 * map}, or {@link PropertyKind#ARRAY array} like properties to define the title property for the
 * entries of its property value. This annotation overrides a potential annotation on the entry
 * type.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Retention(RUNTIME)
@Target({ TYPE, METHOD })
@TagName("title-property")
public @interface TitleProperty {

	/**
	 * Name of the {@link PropertyDescriptor} that is used in the title of a collapsed
	 * {@link ConfigurationItem}.
	 */
	String name();

}

