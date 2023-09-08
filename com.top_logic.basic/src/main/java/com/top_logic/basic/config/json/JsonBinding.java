/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.annotation.Binding;
import com.top_logic.basic.config.annotation.TagName;

/**
 * Annotation for configuration {@link PropertyDescriptor properties} to define a Json serialization
 * of values of the annotated property.
 * 
 * @see Binding
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@TagName("json-binding")
public @interface JsonBinding {

	/**
	 * Json serialization class for values of the annotated property.
	 */
	Class<? extends JsonValueBinding<?>> value();

}

