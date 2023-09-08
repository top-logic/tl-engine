/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.config;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.constraint.annotation.RegexpConstraint;
import com.top_logic.model.annotate.AnnotatedConfig;

/**
 * Base interface for defining types.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface TypeConfig extends ModelPartConfig, AnnotatedConfig<TLTypeAnnotation>, NamedPartConfig {

	@Override
	@RegexpConstraint(value = PartNameConstraints.MANDATORY_TYPE_NAME_PATTERN, errorKey = PartNameConstraints.MandatoryTypeNameKey.class)
	String getName();

}
