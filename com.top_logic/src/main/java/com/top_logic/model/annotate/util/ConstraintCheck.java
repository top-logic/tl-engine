/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.util;

import com.top_logic.basic.col.Sink;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLConstraints;
import com.top_logic.model.util.Pointer;

/**
 * Constraint checking algorithm that can be {@link TLConstraints annotated} to an attribute of a
 * model element.
 * 
 * @see TLConstraints
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ConstraintCheck {

	/**
	 * Performs a constraint check on the given {@link TLObject}.
	 * 
	 * <p>
	 * A potential failure is reported in the context of the attribute that was {@link TLConstraints
	 * annotated} with this check. A check is supposed to have a "main" value that is checked. Check
	 * failures are reported at the location, where this value is displayed, if the check is
	 * performed in a user interface.
	 * </p>
	 *
	 * @param object
	 *        The object to check for a certain consistency constraint.
	 * @param attribute
	 *        The main attribute whose value is checked. Failures are reported in the context of
	 *        this attribute. The check may involve other attributes of the same object or
	 *        associated objects, see {@link #traceDependencies(TLObject, TLStructuredTypePart, Sink)}.
	 * 
	 * @return <code>null</code> for a successful check, or the reason why the check failed as
	 *         internationalizable message.
	 */
	ResKey check(TLObject object, TLStructuredTypePart attribute);

	/**
	 * Analyzes which model values are accessed for evaluating this check.
	 * 
	 * <p>
	 * It is assumed that the check has to be re-evaluated, whenever one of those reported model
	 * values change. If this check is performed in a user interface, the check is re-evaluated,
	 * whenever the user changes form fields representing values this check depends on.
	 * </p>
	 * 
	 * @param object
	 *        The object that is checked.
	 * @param attribute
	 *        The main attribute whose value is checked. The main value
	 *        <code>object.attribute</code> is not required to be reported as dependency.
	 * 
	 * @param trace
	 *        A {@link Sink} of {@link Pointer}s to the model whose values determine the check
	 *        result. The tracing reports all those {@link Pointer}s to the given {@link Sink}.
	 */
	void traceDependencies(TLObject object, TLStructuredTypePart attribute, Sink<Pointer> trace);
}
