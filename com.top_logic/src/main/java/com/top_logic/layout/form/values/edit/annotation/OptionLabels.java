/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.layout.LabelProvider;

/**
 * Annotates a {@link LabelProvider} to a {@link ConfigurationItem} property used for displaying its
 * options.
 * 
 * <p>
 * The annotation can be specified at the property or at its value type.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@TagName("option-labels")
public @interface OptionLabels {

	/**
	 * The {@link LabelProvider} class to use.
	 */
	Class<? extends LabelProvider> value();

}
