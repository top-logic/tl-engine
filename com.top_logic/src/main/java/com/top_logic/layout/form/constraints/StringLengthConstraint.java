/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.util.Resources;

/**
 * {@link Constraint} that checks the length of a {@link String}
 * {@link FormField#getValue() value} in a {@link StringField}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StringLengthConstraint extends AbstractStringConstraint {

	/**
	 * Value for {@link #minLength} and {@link #maxLength} declaring that the bound is not enforced.
	 */
	public static final int NO_LIMIT = -1;

	/** Minimum length, {@link #NO_LIMIT} means don't care */
    int minLength;

	/** Maximum length, {@link #NO_LIMIT} means don't care */
	int maxLength;

	private final boolean allowEmpty;
	
	/**
	 * Constraint that only checks for a minimum string length.
	 * 
	 * @see #StringLengthConstraint(int, int)
	 */
	public StringLengthConstraint(int minLength) {
		this(minLength, NO_LIMIT, false);
	}
	
	/**
	 * Constraint that (optionally) checks a lower and upper bound on the length
	 * of the {@link FormField#getValue() value} of a {@link StringField}.
	 * 
	 * @param minLength
	 *     A lower bound of the length of the entered value, or
	 *     {@link #NO_LIMIT}, if a lower bound should not be enforced.
	 * @param maxLength
	 *     An upper bound of the length of the entered value, or
	 *     {@link #NO_LIMIT}, if an upper bound should not be enforced.
	 */
	public StringLengthConstraint(int minLength, int maxLength) {
		this(minLength, maxLength, false);
	}

	/**
	 * Constraint that (optionally) checks a lower and upper bound on the length of the
	 * {@link FormField#getValue() value} of a {@link StringField}.
	 * 
	 * @param minLength
	 *        A lower bound of the length of the entered value, or {@link #NO_LIMIT}, if a lower
	 *        bound should not be enforced.
	 * @param maxLength
	 *        An upper bound of the length of the entered value, or {@link #NO_LIMIT}, if an upper
	 *        bound should not be enforced. {@link Integer#MAX_VALUE} is normalized to
	 *        {@link #NO_LIMIT}, see {@link #hasMaxLength()}.
	 * @param allowEmpty
	 *        Ignore empty strings in the check.
	 */
	public StringLengthConstraint(int minLength, int maxLength, boolean allowEmpty) {
		this.minLength = minLength;
		// An upper bound of Integer.MAX_VALUE cannot be violated, since String.length() is an int.
		// Callers deriving the bound from an unbounded source (such as
		// com.top_logic.model.annotate.TLSize without an upper bound) therefore mean "no limit".
		// Keeping the value would make hasMaxLength() report a limit that can never be reached.
		this.maxLength = maxLength == Integer.MAX_VALUE ? NO_LIMIT : maxLength;
		this.allowEmpty = allowEmpty;
	}
	
    /**
	 * Check value against {@link #minLength}/{@link #maxLength}, treat
	 * <code>null</code> as empty string.
	 */
	@Override
	protected boolean checkString(String value) throws CheckException {
		if (allowEmpty && (value == null || value.trim().length() == 0)) {
			return true;
		}
		
	    int length = value == null ? 0 : value.length();
		
		if ((minLength > 0) && (length == 0)) 
			throw new CheckException(Resources.getInstance().getString(I18NConstants.NOT_EMPTY));
		if ((minLength >= 0) && (length < minLength)) {
			throw new CheckException(Resources.getInstance().getString(
				I18NConstants.STRING_TO_SHORT__MINIMUM_LENGTH_UNDERFLOW.fill(Integer.valueOf(minLength),
					Integer.valueOf(length), Integer.valueOf(minLength - length))));
		}
		if ((maxLength >= 0) && (length > maxLength)) {
			throw new CheckException(Resources.getInstance().getString(
				I18NConstants.STRING_TO_LONG__MAXIMUM_LENGTH_OVERFLOW.fill(Integer.valueOf(maxLength),
					Integer.valueOf(length), Integer.valueOf(length - maxLength))));
		}

		return true;
	}
	
	/**
	 * Whether this constraint limits the maximum length of the input.
	 */
	public boolean hasMaxLength() {
		return maxLength >= 0;
	}
	
	/**
	 * The maximum length of the input.
	 * 
	 * @see #hasMaxLength()
	 */
	public int getMaxLength() {
		return maxLength;
	}
	
	/**
	 * Whether this constraint limits the minimum length of the input.
	 */
	public boolean hasMinLength() {
		return minLength >= 0;
	}
	
	/**
	 * The minimum length of the input.
	 * 
	 * @see #getMinLength()
	 */
	public int getMinLength() {
		return minLength;
	}

}
