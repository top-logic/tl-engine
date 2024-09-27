/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import java.util.List;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.util.Resources;

/**
 * An {@link AbstractDependency} implementation that enforces {@link FormField}s
 * to have values in ascending order (or <code>null</code> values).
 *
 * <p>
 * This dependency requires that the values stored in its {@link FormField}s
 * implement {@link Comparable}.
 * </p>
 *
 * @author <a href=mailto:kbu@top-logic.com>kbu</a>
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SequenceDependency extends AbstractDependency {

    /**
     * Creates a new {@link SequenceDependency} that relates the given
     * {@link FormField}s.
     */
    public SequenceDependency(FormField[] someFields) {
        super(someFields, false);
    }

    /**
     * Creates a new {@link SequenceDependency} that relates the given
     * {@link FormField}s.
     * All fields will establish additional dependenscies to the fields given by someAdditionalFileds.
     */
    public SequenceDependency(FormField[] someFields, List someAdditionalFields) {
        super(someFields, someAdditionalFields);
    }

	/**
	 * Checks, whether the {@link FormField} at the given index has a value that
	 * is in order.
	 *
	 * @see SequenceDependency for details.
	 *
	 * @see AbstractDependency#check(int, Object) for parameters and return
	 *      value.
	 */
	@Override
	public boolean check(int checkedFieldIndex, Object aCheckedValue) throws CheckException {
		if (aCheckedValue == null) {
			// Assume that missing values are allowed. To forbit missing values,
			// another constraint must be added to the field.
			return true;
		}

		// Check if the given value is in the correct (ascending) sequence.

		// Check for greater or equal
		ResKey violatesGELabel = null;
		for (int i = checkedFieldIndex - 1; i >= 0; i--) {
            Comparable theLowerValue = this.getDependencyValue(i, false);

            // Check the values of the dependency fields.
            if (theLowerValue != null) {
            	if (((Comparable) aCheckedValue).compareTo(theLowerValue) < 0) {
            		violatesGELabel = this.get(i).getLabel();
            	}

            	// Only the first failing or succeeding check is relevant. If
				// there is another value in the sequence that does not match
				// this dependency, the corresponding error will be attached to
				// another field in the sequence.
            	break;
            }
		}

		ResKey violatesLELabel = null;

		// Check lower or equal
        for (int cnt = size(), i = checkedFieldIndex + 1; i < cnt; i++) {
            Comparable theGreaterValue = this.getDependencyValue(i, true);

            /*
             * Check the values of the dependency fields.
             */
            if (theGreaterValue != null) {
            	if (((Comparable) aCheckedValue).compareTo(theGreaterValue) > 0) {
            		violatesLELabel = this.get(i).getLabel();
            	}

            	// See loop checking for lower than above.
            	break;
            }
        }

        if (violatesGELabel != null && violatesLELabel != null) {
        	// Violates both.
			throw new CheckException(
				Resources.getInstance().getString(I18NConstants.SEQUENCE_ERROR_GE_AND_LE__FIELD_FIELDSGE_FIELDSLE
					.fill(get(checkedFieldIndex).getLabel(), violatesGELabel, violatesLELabel)));
        }
        else if (violatesGELabel != null) {
			throw new CheckException(Resources.getInstance().getString(I18NConstants.SEQUENCE_ERROR_GE__FIELD_FIELDSGE.fill(get(checkedFieldIndex).getLabel(), violatesGELabel)));
        }
        else if (violatesLELabel != null) {
			throw new CheckException(Resources.getInstance().getString(I18NConstants.SEQUENCE_ERROR_LE__FIELD_FIELDSLE.fill(get(checkedFieldIndex).getLabel(), violatesLELabel)));
        }

        return true;
    }

	protected Comparable<?> getDependencyValue(int anIndex, boolean accending) {
	    FormField theField = get(anIndex);
	    if (theField.hasValue()) {
	        return (Comparable<?>) theField.getValue();
	    } else {
	        return null;
	    }
	}

}
