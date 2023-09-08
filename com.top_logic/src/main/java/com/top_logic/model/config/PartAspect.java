/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.config;

import com.top_logic.basic.config.constraint.annotation.RegexpConstraint;

/**
 * Definition aspect of a type part.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface PartAspect extends ModelPartConfig, NamedPartConfig {

	@RegexpConstraint(value = PartNameConstraints.MANDATORY_TYPE_PART_NAME_PATTERN, errorKey = PartNameConstraints.MandatoryTypePartNameKey.class)
	@Override
	String getName();

}
