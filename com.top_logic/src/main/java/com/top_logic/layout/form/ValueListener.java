/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

/**
 * Instances of this class can listen for changes of the value of
 * {@link com.top_logic.layout.form.FormField} instances. Listeners are attached to a form
 * field with the
 * {@link com.top_logic.layout.form.FormField#addValueListener(ValueListener)} method.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ValueListener {

    /**
     * This method is called to inform about a change of the value in the passed
     * {@link FormField} instance.
     *
     * <p>
     * Note: One can neither reliably test the validity of the new nor the old value at the
     * time the event occurs. If you want to check the value against the form field's
     * constraints, you can use the {@link FormField#checkConstraints(Object)} method but
     * NOT the {@link FormField#check()} method.
     * </p>
     *
     * @param field
     *            The field, whose value changed.
     * @param oldValue
     *            The value of the field before the change.
     * @param newValue
     *            The new value of the field after the change.
     */
    public void valueChanged(FormField field, Object oldValue, Object newValue);

}
