/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.config;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.model.config.ScopeConfig;

/**
 * Representation of configuration of a single element type (e.g. Project).
 * 
 * @author <a href="mailto:skr@top-logic.com">Sylwester Kras</a>
 */
@TagName(ClassConfig.TAG_NAME)
@DisplayOrder({
	ClassConfig.NAME,
	ClassConfig.ABSTRACT,
	ClassConfig.FINAL,
	ClassConfig.FINAL,
	ClassConfig.GENERALIZATIONS,
	ClassConfig.ANNOTATIONS,
	ClassConfig.ATTRIBUTES,
	ClassConfig.TYPES,
})
public interface ClassConfig extends ObjectTypeConfig, ScopeConfig {

	/** Default tag for defining classes. */
	String TAG_NAME = "class";

	/** Property name of {@link ClassConfig#isAbstract()}. */
	String ABSTRACT = "abstract";

	/** Property name of {@link ClassConfig#isFinal()}. */
	String FINAL = "final";

	/**
	 * Whether the type cannot be instantiated directly.
	 */
	@Name(ABSTRACT)
	boolean isAbstract();

	/** @see #isAbstract() */
	void setAbstract(boolean value);

	/**
	 * Whether this type cannot be specialized.
	 */
	@Name(FINAL)
	boolean isFinal();

	/** @see #isFinal() */
	void setFinal(boolean value);

}