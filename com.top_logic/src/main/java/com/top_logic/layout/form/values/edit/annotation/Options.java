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
import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.func.GenericFunction;
import com.top_logic.basic.func.IGenericFunction;
import com.top_logic.layout.form.model.utility.DefaultTreeOptionModel;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.form.model.utility.TreeOptionModel;
import com.top_logic.layout.form.values.edit.InvalidOptionMapping;
import com.top_logic.layout.form.values.edit.OptionMapping;
import com.top_logic.model.TLProperty;

/**
 * Annotates an {@link GenericFunction} to a {@link ConfigurationItem} property.
 * 
 * <p>
 * The annotation can be specified at the property or at its value type.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@TagName("options")
public @interface Options {

	/**
	 * The {@link GenericFunction} that produces options.
	 * 
	 * @return Either a {@link List} of options, or an {@link OptionModel} e.g. to create
	 *         tree-structured options.
	 * 
	 * @see TreeOptionModel
	 * @see DefaultTreeOptionModel
	 */
	Class<? extends IGenericFunction<? extends Iterable<?>>> fun();

	/**
	 * Specification of argument references for the options {@link #fun() function}.
	 */
	Ref[] args() default {};

	/**
	 * The {@link OptionMapping} to translate an option returned by {@link #fun()} to the actual
	 * value of the annotated {@link TLProperty}.
	 * 
	 * @return The class of the {@link OptionMapping} to transform an option into a
	 *         {@link TLProperty} value and vice-versa.
	 */
	Class<? extends OptionMapping> mapping() default InvalidOptionMapping.class;

}
