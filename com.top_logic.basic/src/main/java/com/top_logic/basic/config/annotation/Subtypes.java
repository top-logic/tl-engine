/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;

/**
 * Annotation for a {@link PropertyKind#ARRAY}, {@link PropertyKind#LIST} or
 * {@link PropertyKind#MAP} valued configuration property getter to denote common element types.
 * 
 * @see #value()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@TagName("sub-types")
public @interface Subtypes {

	/**
	 * Specification of bindings of element tags to common sub-types of the
	 * {@link PropertyDescriptor#getElementType() property element type}.
	 */
	Subtype[] value();

	/**
	 * Whether the annotated {@link Subtype}s are added to/replace the {@link TagName}s of the types
	 * found by type index lookup.
	 */
	boolean adjust() default false;

	/**
	 * Binding of a {@link #type() configuration interface type} to a {@link #tag() tag name}.
	 */
	@TagName("sub-type")
	public @interface Subtype {
		/**
		 * The tag name to associate a certain {@link #type()} with.
		 */
		String tag();

		/**
		 * The configuration interface type to instantiate for a certain element {@link #tag()}.
		 */
		Class<? extends ConfigurationItem> type();
	}

}
