/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.basic.config.annotation.TagName;

/**
 * The control that edits this configuration property, instead of the one its value type would get.
 *
 * <p>
 * A property is normally edited by the control registered for its value type, see
 * {@link FieldControlRegistry}. This annotation names a different {@link ReactFieldControlProvider}
 * for one property, e.g. to edit a string as a code editor rather than as a plain text input.
 * </p>
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@TagName("field-control")
public @interface FieldControl {

	/**
	 * The provider creating the control for the annotated property.
	 */
	Class<? extends ReactFieldControlProvider> value();

}
