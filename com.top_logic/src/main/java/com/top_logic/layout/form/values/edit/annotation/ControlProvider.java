/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.form.FormMember;

/**
 * Annotation for assigning a {@link com.top_logic.layout.form.template.ControlProvider} to a
 * declarative form field.
 * 
 * <p>
 * The annotation can be specified at the property or at its value type.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@TagName("control-provider")
public @interface ControlProvider {

	/**
	 * {@link com.top_logic.layout.form.template.ControlProvider} class to instantiate.
	 * 
	 * @see FormMember#setControlProvider(com.top_logic.layout.form.template.ControlProvider)
	 */
	Class<? extends com.top_logic.layout.form.template.ControlProvider> value();
	
}
