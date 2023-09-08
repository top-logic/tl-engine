/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import java.util.Collection;
import java.util.List;

import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.util.Resources;

/**
 * {@link Constraint} that checks the length of a {@link List}
 * {@link FormField#getValue() value} in a {@link ComplexField}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CollectionSizeConstraint extends AbstractConstraint {

    /** Minimum length, -1 means don't care */
    int minLength;

    /** Maximum length, -1 means don't care */
	int maxLength;

	/**
	 * Constraint that only checks for a minimum length.
	 * 
	 * @see #CollectionSizeConstraint(int)
	 */
	public CollectionSizeConstraint(int minLength) {
		this(minLength, -1);
	}
	
	/**
	 * Constraint that (optionally) checks a lower and upper bound on the length
	 * of the {@link FormField#getValue() value} of a {@link ComplexField}.
	 * 
	 * @param minLength
	 *     A lower bound of the length of the value, or
	 *     <code>-1</code>, if a lower bound should not be enforced.
	 * @param maxLength
	 *     An upper bound of the length of the value, or
	 *     <code>-1</code>, if an upper bound should not be enforced.
	 */
	public CollectionSizeConstraint(int minLength, int maxLength) {
		this.minLength = minLength;
		this.maxLength = maxLength;
	}

    /**
	 * Check value against {@link #minLength}/{@link #maxLength}, treat
	 * <code>null</code> as empty list.
	 */
	@Override
	public boolean check(Object value) throws CheckException {
		Collection collectionValue = (Collection) value;
		int length = collectionValue == null ? 0 : collectionValue.size();
		
		if ((minLength > 0) && (length == 0)) 
			throw new CheckException(Resources.getInstance().getString(I18NConstants.NOT_EMPTY));
		if ((minLength >= 0) && (length < minLength)) {
			throw new CheckException(Resources.getInstance().getString(I18NConstants.COLLECTION_TO_SMALL__MINIMUM_LENGTH.fill(Integer.valueOf(minLength), Integer.valueOf(length))));
		}
		if ((maxLength >= 0) && (length > maxLength)) {
			throw new CheckException(Resources.getInstance().getString(I18NConstants.COLLECTION_TO_LARGE__MAXIMUM_LENGTH.fill(Integer.valueOf(maxLength), Integer.valueOf(length))));
		}

		return true;
	}
	
	/**
	 * Whether this constraint limits the maximum length of the input list.
	 */
	public boolean hasMaxLength() {
		return maxLength >= 0;
	}
	
	/**
	 * The maximum length of the input list.
	 * 
	 * @see #hasMaxLength()
	 */
	public int getMaxLength() {
		return maxLength;
	}
	
	/**
	 * Whether this constraint limits the minimum length of the input list.
	 */
	public boolean hasMinLength() {
		return minLength >= 0;
	}
	
	/**
	 * The minimum length of the input list.
	 * 
	 * @see #getMinLength()
	 */
	public int getMinLength() {
		return minLength;
	}

}
