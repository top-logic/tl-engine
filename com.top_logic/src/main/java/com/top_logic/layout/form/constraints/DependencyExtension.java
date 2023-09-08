/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormField;

/**
 * The DependencyExtension allows to establish additional dependencies for a given constraint.
 * 
 * The additional dependencies can reflect implizit dependencies that exist between fields
 * which are not expressed via constraints (e.g. implizit dependencies to fields which have value change listeners setting other field's values) 
 * 
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class DependencyExtension implements Constraint {

	/** The {@link Constraint} that implements the check. */
	private Constraint _impl;

    /** the additional fields to establish dependencies to */
	private List<FormField> _additionalDependencies;

	/**
	 * Creates a {@link DependencyExtension}.
	 *
	 * @param impl
	 *        The {@link Constraint} implementation.
	 * @param additionalDependencies
	 *        Additional dependencies to report in {@link #reportDependencies()}.
	 */
	public DependencyExtension(Constraint impl, List<FormField> additionalDependencies) {
		_impl = impl;
		_additionalDependencies = additionalDependencies;
    }
    
    /**
	 * Delegates the check to the wrapped constraint.
	 * 
	 * @see Constraint#check(java.lang.Object)
	 */
    @Override
	public boolean check(Object aValue) throws CheckException {
		return _impl.check(aValue);
    }

    /**
	 * Extends the dependencies reported by the wrapped constraind and adds the additional fields
	 * 
	 * @see Constraint#reportDependencies()
	 */
    @Override
	public Collection<FormField> reportDependencies() {
		Collection<FormField> theInner = _impl.reportDependencies();
		Collection<FormField> theResult = new ArrayList<>(theInner.size() + _additionalDependencies.size());
        theResult.addAll(theInner);
		theResult.addAll(_additionalDependencies);
        return theResult;
    }

}

