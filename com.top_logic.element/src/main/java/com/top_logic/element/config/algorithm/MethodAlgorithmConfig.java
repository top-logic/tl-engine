/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.config.algorithm;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.element.meta.algorithm.MethodInvocationAlgorithm;

/**
 * Configuration of {@link MethodInvocationAlgorithm}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("method-call")
public interface MethodAlgorithmConfig extends GenericAlgorithmConfig {

	/** @see #getMethodName() */
	String METHOD_NAME_PROPERTY = "methodName";

	/**
	 * The name of the Java method to invoke at the context object.
	 */
	// @Mandatory
	@Name(METHOD_NAME_PROPERTY)
	String getMethodName();
	

}
