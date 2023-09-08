/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.listener;

import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.util.Utils;

/**
 * An {@link AbstractValueListenerDependency} implementation that allows only one of a group of
 * {@link BooleanField}s to be selected at once.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class RadioButtonValueListenerDependency extends AbstractValueListenerDependency {

    /**
     * Indicates whether it is allowed to deselect the selected field (<code>true</code>)
     * or if at least one field has to be selected (<code>false</code>).
     */
    private boolean allowDeselection;


    /**
     * Creates a {@link RadioButtonValueListenerDependency} between the given
     * fields, where exactly one field has to be checked.
     *
     * <p>
     * This dependency only gets active after {@link #attach()} is called.
     * </p>
     *
     * @param relatedFields
     *            the fields between which the dependency consists
     */
    public RadioButtonValueListenerDependency(BooleanField[] relatedFields) {
        this(relatedFields, false);
    }

    /**
     * Creates a {@link RadioButtonValueListenerDependency}.
     *
     * <p>
     * This dependency only gets active after {@link #attach()} is called.
     * </p>
     *
     * @param relatedFields
     *            the fields between which the dependency consists
     * @param allowDeselection
     *            Indicates whether it is allowed to deselect the selected field (<code>true</code>)
     *            or if at least one field has to be selected (<code>false</code>).
     */
    public RadioButtonValueListenerDependency(BooleanField[] relatedFields, boolean allowDeselection) {
        super(relatedFields);
        this.allowDeselection = allowDeselection;
    }


    /**
     * The {@link BooleanField} at the given index.
     */
    public BooleanField getBooleanField(int index) {
        return (BooleanField)get(index);
    }



    /**
     * Checks all Fields initial after attaching.
     *
     * @see AbstractValueListenerDependency#attach()
     */
    @Override
	public void attach() {
        super.attach();
        touchAll();
    }


    @Override
	protected void valueChanged(int fieldIndex, FormField field, Object oldValue, Object newValue) {
        valueChanged(fieldIndex, (BooleanField)field, (oldValue instanceof Boolean ? ((Boolean)oldValue).booleanValue() : false), (newValue instanceof Boolean ? ((Boolean)newValue).booleanValue() : false));
    }



    /**
     * Checks all fields and deselects all but the first one, if more than one fields are
     * selected. It is not needed to call this method from other classes.
     */
    @Override
	protected void touchAllImplementation() {
        deselectAllButOne();
    }



    /**
     * This method handles the changeEvent of the field with the given index.
     *
     * @param fieldIndex
     *            index of the changed field
     */
    private void valueChanged(int fieldIndex, BooleanField field, boolean oldValue, boolean newValue) {
        if (newValue && !oldValue) {
            deselectAllBut(fieldIndex);
        }
        else if (!newValue && oldValue) {
            if (!allowDeselection) {
                field.setAsBoolean(true);
            }
        }
    }

    /**
     * Deselects all fields but the given one.
     *
     * @param excludeIndex
     *            the field which shall no get deselected; may be -1 to indicate all fields
     *            shall be deselected
     */
    public void deselectAllBut(int excludeIndex) {
        for (int i = 0; i < size(); i++) {
            if (i == excludeIndex) continue;
			if (Utils.getbooleanValue(getBooleanField(i).getValue())) {
                getBooleanField(i).setAsBoolean(false);
            }
        }
    }

    /**
     * Deselects all fields but the first selected one.
     * If deselection is not allowed, the first one is selected, if no other is selected.
     */
    private void deselectAllButOne() {
        boolean selected = false;
        for (int i = 0; i < size(); i++) {
			if (Utils.getbooleanValue(getBooleanField(i).getValue())) {
                if (selected) getBooleanField(i).setAsBoolean(false);
                else selected = true;
            }
        }
        if (!selected && !allowDeselection && size() > 0) {
            getBooleanField(0).setAsBoolean(true);
        }
    }

}
