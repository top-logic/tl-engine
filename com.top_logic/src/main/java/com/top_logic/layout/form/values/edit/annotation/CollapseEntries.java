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
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;

/**
 * Annotation for {@link List} or {@link Map} valued properties ({@link PropertyDescriptor} of type
 * {@link PropertyKind#LIST}, {@link PropertyKind#MAP}, or {@link PropertyKind#ARRAY}) to display
 * entries initially collapsed.
 * 
 * @see DisplayMinimized Collapse the whole property value.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Retention(RUNTIME)
@Target({ METHOD, TYPE })
public @interface CollapseEntries {

	/**
	 * Whether the UI for the entries of the annotated {@link PropertyDescriptor} of type
	 * {@link PropertyKind#LIST}, {@link PropertyKind#MAP}, or {@link PropertyKind#ARRAY} should be
	 * initially displayed collapsed.
	 */
	boolean value() default true;

}

