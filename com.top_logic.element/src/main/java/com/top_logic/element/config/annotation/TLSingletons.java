/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.config.annotation;

import java.util.Collection;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Indexed;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.element.config.SingletonConfig;
import com.top_logic.model.TLModule;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.config.TLModuleAnnotation;

/**
 * {@link TLAnnotation} holding the configuration of the singletons of a {@link TLModule}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName("singletons")
@InApp
public interface TLSingletons extends TLModuleAnnotation {

	/** Property name of {@link #getSingletons()}. */
	String SINGLETONS = "singletons";

	/**
	 * Defined Singletons (structure root elements).
	 */
	@Name(SINGLETONS)
	@Key(SingletonConfig.NAME)
	@DefaultContainer
	Collection<SingletonConfig> getSingletons();

	/**
	 * Indexed getter for the {@link #getSingletons()} property.
	 * 
	 * @param name
	 *        The name of the singleton to find.
	 * @return The {@link SingletonConfig} with the given name or <code>null</code>, if no such
	 *         singleton exists.
	 */
	@Indexed(collection = SINGLETONS)
	SingletonConfig getSingleton(String name);

	/**
	 * @see #getSingletons()
	 */
	void setSingletons(Collection<SingletonConfig> value);

}

