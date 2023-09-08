/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.annotation;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.annotation.TagName;

/**
 * Annotation for {@link PropertyDescriptor} whether the configuration UI for the value must be
 * displayed as minimal as possible.
 * 
 * @see CollapseEntries Minimize the each entry of the property value.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Retention(RUNTIME)
@Target({ METHOD, TYPE })
@TagName("display-minimized")
public @interface DisplayMinimized {

	/**
	 * Whether the GUI element that is created for the annotated {@link PropertyDescriptor} should
	 * be as minimal as possible, e.g. if a group is displayed, whether it should be displayed
	 * collapsed.
	 */
	boolean value() default true;
	
}

