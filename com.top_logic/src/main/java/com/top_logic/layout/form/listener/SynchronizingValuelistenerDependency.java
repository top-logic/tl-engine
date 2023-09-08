/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.listener;

import com.top_logic.layout.form.FormField;
import com.top_logic.util.Utils;

/**
 * Keeps the given fields values in sync, e.g. changes the value of all fields, when
 * one of the given fields value changes.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class SynchronizingValuelistenerDependency extends AbstractValueListenerDependency {

    /**
     * Creates a new instance of this class.
     *
     * @param relatedFields
     *        the fields to keep in sync
     */
    public SynchronizingValuelistenerDependency(FormField[] relatedFields) {
        super(relatedFields);
    }

    @Override
	protected void valueChanged(int aFieldIndex, FormField aField, Object aOldValue, Object aNewValue) {
        if (!Utils.equals(aOldValue, aNewValue))
        for (int i = 0, length = size(); i < length; i++) {
            if (i == aFieldIndex) continue;
            get(i).setValue(aNewValue);
        }
    }

}
