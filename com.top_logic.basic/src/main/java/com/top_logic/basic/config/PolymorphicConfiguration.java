/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.ReadOnly;

/**
 * Base interface for {@link ConfigurationItem}s for configuring a polymorphic
 * component hierarchy, where subclasses require specialized configuration.
 * 
 * @see TypedConfiguration Generically instantiating configuration items from
 *      {@link ConfigurationItem} interface declarations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface PolymorphicConfiguration<T> extends ConfigurationItem {

	/**
	 * Configuration option for #getImplementationClass()
	 */
	String IMPLEMENTATION_CLASS_NAME = "class";
	
	/**
	 * The corresponding implementation class for this {@link ConfigurationItem}.
	 * 
	 * <p>
	 * In a UI, the implementation class cannot be edited. Instead, the whole configuration item
	 * must be replaced with another implementation class. Otherwise, the properties defined by the
	 * item may be inconsistent with the requirements of the implementation class.
	 * </p>
	 */
	@Name(IMPLEMENTATION_CLASS_NAME)
	@Hidden
	@ReadOnly
	Class<? extends T> getImplementationClass();

	/**
	 * @see #getImplementationClass()
	 */
	void setImplementationClass(Class<? extends T> value);

}
