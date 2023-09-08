/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.layout.form.model.FormContext;

/**
 * A {@link Constraint} implementation checks values entered in a
 * {@link FormField}.
 * 
 * <p>
 * A {@link Constraint} is added to a {@link FormField} in the
 * {@link FormField#addConstraint(Constraint)} method. It checks the
 * {@link FormField#getValue() value} if its field and may optionally depend on
 * the values of other {@link FormField}s in the same {@link FormContext}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Constraint {
	/**
	 * Check the given value for compatibility with this constraint.
	 * 
	 * If the check fails, a {@link CheckException} is thrown with an
	 * internationalized error message. The check can be defered by returning
	 * <code>false</code>, if and only if some of the
	 * {@link #reportDependencies() dependencies} of this constraint do not yet
	 * {@link FormField#hasValue() have a value}.
	 * 
	 * <b>Note:</b> It is illegal to return <code>false</code> from this
	 * method, if the current value, or the values of the dependencies are in
	 * any way not suitable for being checked. The only two options in such
	 * situation is to accept these values (this dependency does not apply), or
	 * to throw a {@link CheckException}, which explains the reason for the
	 * reject. <b>This statement especially applies to the
	 * {@link FormField#EMPTY_INPUT empty input value}, which must not be a
	 * reason for delaying the check by returning <code>false</code>.</b>
	 * 
	 * The reason for the restriction mentioned above is the following: During 
	 * the final check of a form, all fields are checked and the input is only 
	 * accepted, if all fields are {@link FormField#isValid() valid}. It must
	 * be made sure that no constraint check during this final test can defer
	 * the check without some error being reported that can be displayed to the 
	 * client.  
	 * 
	 * @param value
	 *     The value to check.
	 * @return <code>true</code>, if the check succeeds, <code>false</code>
	 *     if the check could not be run, because some of the dependencies
	 *     (see {@link #reportDependencies()}) do not yet have a value (see
	 *     {@link FormField#hasValue()}).
	 * 
	 * @throws CheckException
	 *     If the check fails. The error message provided is
	 *     internationalized.
	 */
	public boolean check(Object value) throws CheckException;
	
	/**
	 * Return a collection of other {@link FormField}s on which this constraint depends.
	 * 
	 * A constraint depends on another {@link FormField}, if that field must contain a value, before
	 * this constraint can be {@link #check(Object) checked}. The field, to which this constraint is
	 * {@link FormField#addConstraint(Constraint) added}, is not a member of the dependencies of
	 * this constraint. If this constraint can be checked with solely knowing the value that was
	 * entered in the field, to which this constraint is added, then this constraint has no
	 * dependencies and must return an empty collection.
	 * 
	 * @return An (immutable) collection of other {@link FormField}s that must contain values,
	 *         before this constraint can be checked. The result must be an empty collection, if
	 *         this constraint has no dependencies.
	 */
	default Collection<FormField> reportDependencies() {
		return Collections.emptyList();
	}
}
