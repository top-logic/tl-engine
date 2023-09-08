/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.reflect;

import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.TagName;

/**
 * Descriptor for an indexed type.
 * 
 * @see TypeIndex#getDescriptor(String)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TypeDescriptor {

	/**
	 * The required configuration interface to instantiate this class.
	 * 
	 * <p>
	 * A configuration interface C is required to instantiation classs I, if I declares C as second
	 * parameter in it's configuration constructor (the one with {@link InstantiationContext} as
	 * first argument).
	 * </p>
	 * 
	 * @return The {@link TypeDescriptor} describing the configuration interface for this class, or
	 *         <code>null</code>, if this is not a configured class.
	 */
	TypeDescriptor getConfiguration();

	/**
	 * The value of the {@link TagName} annotation of this configuration type.
	 */
	String getTagName();

	/**
	 * Name of the described type.
	 */
	String getClassName();

	/**
	 * Whether the type is "abstract".
	 * 
	 * <p>
	 * A class is considered <code>abstract</code>, if it has the Java <code>abstract</code>
	 * modifier. An interface is considered abstract, if it carries the {@link Abstract} annotation.
	 * </p>
	 */
	boolean isAbstract();

	/**
	 * Whether the type is a Java interface type.
	 */
	boolean isInterface();

	/**
	 * Whether the type has the <code>public</code> modifier.
	 */
	boolean isPublic();

	/**
	 * The direct specializations of this type.
	 */
	List<? extends TypeDescriptor> getSpecializations();

	/**
	 * All configuration interfaces that directly resolve to this implementation class.
	 * 
	 * <p>
	 * A configuration interface C directly resolves to an implementation I, if C extends
	 * {@link PolymorphicConfiguration} binding its type parameter to I.
	 * </p>
	 * 
	 * <p>
	 * Note: The resulting list does not include configurations that bind the implementation type
	 * variable of {@link PolymorphicConfiguration} to a subtype of I.
	 * </p>
	 */
	List<? extends TypeDescriptor> getConfigurationOptions();

}
