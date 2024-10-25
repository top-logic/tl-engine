/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.config.annotation;

import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.element.meta.AttributeSettings;
import com.top_logic.model.annotate.AnnotationInheritance;
import com.top_logic.model.annotate.AnnotationInheritance.Policy;
import com.top_logic.model.annotate.DefaultStrategy;
import com.top_logic.model.annotate.DefaultStrategy.Strategy;
import com.top_logic.model.annotate.StringAnnotation;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * {@link TLAnnotation} that provides a configuration name for which additional unversioned
 * configuration is provided by {@link AttributeSettings}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("config-type")
@DefaultStrategy(Strategy.NONE)
@AnnotationInheritance(Policy.REDEFINE)
public interface ConfigType extends StringAnnotation, TLAttributeAnnotation, TLTypeAnnotation {

	/**
	 * Configuration name identifying additional information.
	 * 
	 * @see AttributeSettings
	 */
	@Override
	String getValue();

}
