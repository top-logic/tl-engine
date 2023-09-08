/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.TagName;

/**
 * Annotation specifying how create the UI for an {@link PropertyKind#ITEM} property.
 * 
 * <p>
 * By default, an item property is displayed as a group containing the configuration of that item.
 * But especially for {@link InstanceFormat} properties, it may be useful to only allow selecting
 * between pre-configured options.
 * </p>
 * 
 * <p>
 * The annotation can be specified at the property or at its value type.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@TagName("item-display")
public @interface ItemDisplay {

	/**
	 * How to display {@link PropertyKind#ITEM} properties.
	 */
	ItemDisplayType value();

	/**
	 * Options for the {@link ItemDisplay} annotation.
	 * 
	 * @see ItemDisplay#value()
	 */
	enum ItemDisplayType implements ExternallyNamed {

		/**
		 * Display a configuration type selector with dynamically changing group contents based on
		 * the selected configuration type.
		 */
		POLYMORPHIC("polymorphic"),

		/**
		 * Display a group with inner configuration options of the item property (default).
		 */
		MONOMORPHIC("monomorphic"),

		/**
		 * Display the item as atomic value (e.g. as selection of pre-configured options, or a
		 * formatted text field).
		 */
		VALUE("value");

		private String _name;

		private ItemDisplayType(String name) {
			_name = name;
		}

		@Override
		public String getExternalName() {
			return _name;
		}
	}
}
