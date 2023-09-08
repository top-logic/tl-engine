/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.config;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLModule;
import com.top_logic.model.annotate.TLAnnotation;

/**
 * {@link TLAnnotation} of the default implementation class and interface package of the annotated
 * {@link TLModule}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName("package-binding")
public interface JavaPackage extends TLModuleAnnotation {

	/** @see #getImplementationPackage() */
	String IMPLEMENTATION_PACKAGE = "implementation-package";

	/** @see #getImplementationPackage() */
	String INTERFACE_PACKAGE = "interface-package";

	/**
	 * The name of the default implementation class package for the annotated {@link TLModule}.
	 */
	@Name(IMPLEMENTATION_PACKAGE)
	@Nullable
	String getImplementationPackage();

	/**
	 * @see #getImplementationPackage()
	 */
	void setImplementationPackage(String value);

	/**
	 * The name of the default interface package for the annotated {@link TLModule}.
	 */
	@Name(INTERFACE_PACKAGE)
	@Nullable
	String getInterfacePackage();

	/**
	 * @see #getInterfacePackage()
	 */
	void setInterfacePackage(String value);

}

