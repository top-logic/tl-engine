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

import com.top_logic.basic.config.CommaSeparatedStringArray;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyDescriptorConfig;

/**
 * Fixed list of the values a configuration property accepts.
 *
 * <p>
 * The values are given in the text format of the property, i.e. they are parsed with the
 * {@link ConfigurationValueProvider} of the property. This works for every property whose values
 * have a text format: texts, numbers, truth values, dates, enumerations (restricting the property to
 * a subset of the enumeration constants). For a list or array property, the values restrict each
 * element of the list; they are parsed with the format of the elements (for a {@link ListBinding},
 * its {@link ListBinding#format()}). A property without a text format (a configuration item, a
 * polymorphic configuration) cannot be annotated, this is reported as configuration error.
 * </p>
 *
 * <p>
 * A value outside the list is a configuration error, both when the value is read from a
 * configuration and when it is set to a configuration item. The error names the allowed values.
 * </p>
 *
 * <p>
 * A single-valued property without explicit default that is not nullable gets the first allowed
 * value as default (like a non-nullable enumeration property its first constant). A nullable
 * property keeps <code>null</code> as default. An explicit default outside the list is an error.
 * </p>
 *
 * <p>
 * Example in Java:
 * </p>
 *
 * <pre>
 * &#64;AllowedValues({ "open", "closed" })
 * String getState();
 * </pre>
 *
 * <p>
 * Example in a property declared in XML (see {@link PropertyDescriptorConfig#getAnnotations()}):
 * </p>
 *
 * <pre>
 * &lt;property name="level" type="int"&gt;
 *     &lt;allowed-values value="1, 2, 3"/&gt;
 * &lt;/property&gt;
 * </pre>
 *
 * @see PropertyDescriptor#getAllowedValues()
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@TagName(AllowedValues.TAG_NAME)
public @interface AllowedValues {

	/**
	 * Tag name of the annotation in a property declaration in XML.
	 */
	String TAG_NAME = "allowed-values";

	/**
	 * The allowed values, each in the text format of the annotated property (of its elements for a
	 * list or array property).
	 *
	 * <p>
	 * In XML, the values are given as comma-separated list.
	 * </p>
	 */
	@Format(CommaSeparatedStringArray.class)
	String[] value();

}
