/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.listener;

import com.top_logic.layout.form.FormField;

/**
 * The CopyValueListenerDependency copies the value of the source field into
 * the destination field, if the source field gets changed.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class CopyValueListenerDependency extends AggregationValueListenerDependency {

    /**
     * Creates a new instance of this class.
     *
     * @param aSourceField
     *        the field to copy changes from
     * @param aDestinationField
     *        the field to keep in sync with the source field
     */
    public CopyValueListenerDependency(FormField aSourceField, FormField aDestinationField) {
        super(new FormField[] {aSourceField}, aDestinationField);
    }

    @Override
	protected Object aggregateOverFields() {
        return getValue(0);
    }

}
