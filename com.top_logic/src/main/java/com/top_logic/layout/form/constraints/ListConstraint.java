/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import java.util.Collection;

import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.util.Resources;

/**
 * Constraint that checks all elements of a {@link Collection} value against another
 * {@link Constraint}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ListConstraint implements Constraint {

	/**
	 * The {@link Constraint} that is used to check the {@link Collection} elements.
	 */
	private Constraint elementConstraint;
	
	/**
	 * Create a new {@link Constraint} that applies the given {@link Constraint} to all elements of
	 * a {@link Collection}.
	 * 
	 * @param elementConstraint
	 *        See {@link #elementConstraint}.
	 */
	public ListConstraint(Constraint elementConstraint) {
		this.elementConstraint = elementConstraint;
	}

	@Override
	public boolean check(Object value) throws CheckException {
		if (value == null) {
			// Same as empty list. All constraints trivially match all entry
			// values.
			return true;
		}
		
		Collection<?> collection = (Collection<?>) value;

		int checkedIndex = 0;
		try {
			for (Object element : collection) {
				boolean result = elementConstraint.check(element);
				if (!result) {
					// Abort the check early.
					return false;
				}

				checkedIndex++;
			}
		} catch (CheckException ex) {
			throw new CheckException(
				Resources.getInstance().getString(I18NConstants.LIST_ELEMENT_INVALID__MESSAGE_INDEX.fill(ex.getMessage(), Integer.valueOf(checkedIndex))));
		}

		return true;
	}

	@Override
	public Collection<FormField> reportDependencies() {
		return elementConstraint.reportDependencies();
	}

}
