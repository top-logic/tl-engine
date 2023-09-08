/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.config;

import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLModule;
import com.top_logic.model.annotate.StringAnnotation;
import com.top_logic.model.annotate.TLAnnotation;

/**
 * {@link TLAnnotation} to set the name of the factory class for a {@link TLModule}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName("factory")
public interface FactoryClass extends StringAnnotation, TLModuleAnnotation {

	/**
	 * The name of the factory for the annotated {@link TLModule}.
	 */
	@Override
	String getValue();

}

