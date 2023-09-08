/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import java.util.Collection;

import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.util.Resources;

/**
 * Constraint that checks the size of the selection of a {@link SelectField}. 
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SelectionSizeConstraint extends AbstractConstraint {

	int minSize;
	int maxSize;

	/**
	 * Constraint that only checks the lower bound of the selection size.
	 * 
	 * <p>
	 * If only the upper bound of the selection size must be checked, use
	 * {@link #SelectionSizeConstraint(int, int)} with <code>-1</code> as
	 * first argument.
	 * </p>
	 * 
	 * @param minSize
	 *     The minimum size of the selection.
	 */
	public SelectionSizeConstraint(int minSize) {
		this(minSize, -1);
	}

	/**
	 * Constraint that checks (optionally) a lower and upper bound of a
	 * selection size.
	 * 
	 * @param minSize
	 *     The lower bound of the selection size. <code>-1</code>, if
	 *     there is no lower bound.
	 * @param maxSize
	 *     The upper bound of the selection size. <code>-1</code>, if
	 *     there is no upper bound.
	 */
	public SelectionSizeConstraint(int minSize, int maxSize) {
		this.minSize = minSize;
		this.maxSize = maxSize;
	}
	
	@Override
	public boolean check(Object value) throws CheckException {
		Collection selection = (Collection) value;
		int        theSize   = (selection == null) ? 0 : selection.size();

        if ((minSize >= 0) && (theSize < minSize)) {
			if (minSize == 1) {
				throw new CheckException(
					Resources.getInstance().getString(I18NConstants.NO_EMPTY_SELECTION));
			}
			throw new CheckException(
				Resources.getInstance()
					.getString(I18NConstants.SELECTION_TO_SMALL__MINSIZE.fill(Integer.valueOf(minSize))));
		} 
		if ((maxSize >= 0) && (theSize > maxSize)) {
			if (maxSize == 1) {
				throw new CheckException(
					Resources.getInstance().getString(I18NConstants.SINGLE_SELECTION_EXPECTED));
			}
			throw new CheckException(
				Resources.getInstance().getString(I18NConstants.SELECTION_TO_BIG__MAXSIZE.fill(Integer.valueOf(maxSize))));
		}
		return true;
	}

}
