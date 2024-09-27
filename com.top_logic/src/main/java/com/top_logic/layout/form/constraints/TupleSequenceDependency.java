/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.util.Resources;

/**
 * This dependency checks tuple of fields for sequences. Each tuple of start and
 * end value will be checked for ascending order. Only if the first tuple has
 * equal values, the second tuple will be checked and so on.
 * <p/>
 * A <code>null</code> value in a start or end field will result in a valid
 * check result for that tuple. If one of the checks fails a
 * {@link CheckException} will be thrown with an internationalized error message
 * 
 * @author <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class TupleSequenceDependency extends AbstractDependency {

	
	/** 
	 * The FormFields must be ordered by start-end tuples, i.e.
	 * {start1, end1, start2, end2, ...}
	 * 
	 * @param relatedFields  an ordered array of {@link FormField}s. 
	 */
	public TupleSequenceDependency(FormField[] relatedFields) {
		super(relatedFields);
		
	}

	/** 
	 * <code>true</code> if all fields values are in a valid order 
	 * @throws CheckException if one field breaks the defined dependency.
	 */
	@Override
	protected boolean check(int checkedFieldIndex, Object aCheckedValue) throws CheckException {
		int startIdx  = 0;
		int endIdx    = 1;
		int brokenIdx = 0;
		ResKey violatesLabel = null;
		
		// only the fields up to the checkedFieldIndex need to be checked at all
		while(endIdx < size() && (startIdx <= checkedFieldIndex)) { 
			Comparable<Object> startVal = getComparableValue(startIdx);
			Comparable<Object> endVal   = getComparableValue(endIdx);
			
			// only if a start value is given, the following fields need to be checked at all
			if (startVal != null) {
				// if a start value with no end value is considered a valid state
				if (endVal == null) {
					return true;
				}
				int compareResult = startVal.compareTo(endVal);
				// if the start value is greater than the end value a potential error is present
				if(compareResult > 0) {
					// only if the error occurs in the checked fields, this dependency is broken.
					// if earlier fields are correct, the later fields do not have to be in any order
					if (startIdx == checkedFieldIndex || endIdx == checkedFieldIndex) {
						violatesLabel = get(startIdx).getLabel();
						brokenIdx = endIdx;
						break;
					}
					else {
						return true;
					}
				}
				// only if start and end value are the same, the following fields have to be checked at all
				else if (compareResult == 0) {
					startIdx+=2;
					endIdx+=2;
				}
				// start value is less than end value which means the fields are in a valid state
				else {
					return true;
				}
			}
			// if the start value is null, the value of the end field is always valid 
			else {
				return true;
			}
		}
		// a violation of this dependency happened, so a CheckExection must be thrown.
		if (violatesLabel != null) {
			throw new CheckException(Resources.getInstance().getString(I18NConstants.SEQUENCE_ERROR_GE__FIELD_FIELDSGE.fill(get(brokenIdx).getLabel(), violatesLabel)));
		}
		return true;
	}

	/** 
	 * Returns the {@link Comparable} value of the {@link FormField} defined by the given index
	 * 
	 * @param   anIndex the index of the {@link FormField} to get the value from.
	 * @return  the value of the requested field as a {@link Comparable}.
	 */
	private Comparable<Object> getComparableValue(int anIndex) {
		FormField theField = get(anIndex);
		if (theField instanceof SelectField) {
			return (Comparable<Object>) ((SelectField)theField).getSingleSelection();
		}
		return (Comparable<Object>) theField.getValue();
	}
}
