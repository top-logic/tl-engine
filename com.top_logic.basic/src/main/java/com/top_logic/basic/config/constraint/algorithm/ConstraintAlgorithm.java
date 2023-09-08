/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.constraint.algorithm;

import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.util.ResKey;

/**
 * Algorithm for implementing a constraint on a configuration item property.
 * 
 * <p>
 * A simple form checks a property without any context, see {@link ValueConstraint}. A dependency
 * between two properties is expressed through {@link GenericValueDependency}. Any other form must
 * be expressed through {@link GenericPropertyConstraint}.
 * </p>
 * 
 * <p>
 * A {@link ConstraintAlgorithm} is annotated to a configuration property through the
 * {@link Constraint} annotation.
 * </p>
 * 
 * @see Constraint
 * @see ValueConstraint
 * @see ValueDependency
 * @see GenericPropertyConstraint
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ConstraintAlgorithm extends Unimplementable {

	/**
	 * Checks the implemented constraint on the given {@link PropertyModel}s.
	 * 
	 * <p>
	 * A constraint violation must be annotated to each given {@link PropertyModel} that is part of
	 * the problem, see {@link PropertyModel#setProblemDescription(ResKey)}.
	 * </p>
	 * 
	 * @param models
	 *        The related {@link PropertyModel}s that take part in the constraint check.
	 * 
	 * @see PropertyModel#setProblemDescription(ResKey)
	 */
	void check(PropertyModel<?>... models);

	/**
	 * Whether {@link #check(PropertyModel...)} will ever report an error for the
	 * {@link PropertyModel} with the given index.
	 * 
	 * @return <code>false</code>, if the value of the {@link PropertyModel} with the given index is
	 *         used for constraint checking, but it never receives a failure message. If
	 *         <code>false</code> is reported for any index, this constraint is not symmetric.
	 */
	boolean isChecked(int index);

	/**
	 * The signature of the generic {@link #check(PropertyModel...)} method (the number and types of
	 * {@link PropertyModel} arguments it supports).
	 * 
	 * @return A concrete argument type list of the {@link #check(PropertyModel...)} method, or
	 *         <code>null</code>, if {@link #check(PropertyModel...)} method can operate on any list
	 *         of {@link PropertyModel}s.
	 */
	Class<?>[] signature();

}
