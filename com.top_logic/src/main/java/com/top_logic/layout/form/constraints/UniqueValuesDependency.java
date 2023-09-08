/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import java.util.Collection;

import com.top_logic.basic.StringServices;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.util.Resources;

/**
 * An {@link AbstractDependency} implementation that enforces all {@link FormField}s to
 * have unique values (or <code>null</code> values).
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class UniqueValuesDependency extends AbstractDependency {

    /** Flag to indicate that empty fields should not be included by the check. */
    boolean ignoreEmpty;


    /**
     * Creates a {@link UniqueValuesDependency}. Empty fields are excluded by
     * the check.
     * <p>
     * This dependency only gets active after {@link #attach()} is called.
     * </p>
     */
    public UniqueValuesDependency(FormField[] aRelatedFields) {
        this(aRelatedFields, true);
    }

    /**
     * Creates a {@link UniqueValuesDependency}.
     * <p>
     * This dependency only gets active after {@link #attach()} is called.
     * </p>
     *
     * @param ignoreEmpty
     *            if <code>true</code>, empty fields are excluded by the check.
     */
    public UniqueValuesDependency(FormField[] aRelatedFields, boolean ignoreEmpty) {
        super(aRelatedFields, false);
        this.ignoreEmpty = ignoreEmpty;
    }

    @Override
	protected boolean check(int aCheckedFieldIndex, Object aValue) throws CheckException {
        Object theValue = StringServices.isEmpty(aValue) ? null : aValue;

        // take care of empty collection values
        if (ignoreEmpty && ((theValue == null) || ((theValue instanceof Collection) && ((Collection) theValue).isEmpty()))) {
            return true;
        }

        for (int i = 0; i < size(); i++) {
            if (i == aCheckedFieldIndex) continue;

            FormField theField = get(i);
            Object theOtherValue = theField.hasValue() ? theField.getValue() : null;

            if (StringServices.isEmpty(theOtherValue)) theOtherValue = null;

            if ((theValue == null && theOtherValue == null) || (theValue != null && theOtherValue != null && theValue.equals(theOtherValue))) {
                throw new CheckException(getErrorMessage());
            }
        }
        return true;
    }

    protected String getErrorMessage() {
		return Resources.getInstance().getString(I18NConstants.VALUE_NOT_UNIQUE);
    }

}
