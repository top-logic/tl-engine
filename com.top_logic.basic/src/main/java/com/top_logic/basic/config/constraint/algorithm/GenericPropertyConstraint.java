/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.constraint.algorithm;


/**
 * Base class for most general {@link ConstraintAlgorithm} implementations.
 * 
 * @see ValueConstraint
 * @see GenericValueDependency
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class GenericPropertyConstraint implements ConstraintAlgorithm {

	@Override
	public Unimplementable unimplementable() {
		return null;
	}

}
