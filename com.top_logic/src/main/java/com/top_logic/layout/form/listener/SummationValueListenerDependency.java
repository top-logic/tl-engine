/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.listener;

import com.top_logic.layout.form.FormField;


/**
 * An {@link AggregationValueListenerDependency} implementation that computes the sum over
 * the fields.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class SummationValueListenerDependency extends AggregationValueListenerDependency {

	/** See {@link #isMandatory()}. */
	private boolean mandatory;

    /**
     * Creates a {@link SummationValueListenerDependency}. The Fields must
     * contain instances of {@link Number} and the result field must be capable with
     * {@link Double} values.
     *
     * <p>
     * This dependency only gets active after {@link #attach()} is called.
     * </p>
     *
     * @param relatedFields
     *            the fields with the summands
     * @param resultField
     *            the field the result shall be stored into; may be <code>null</code> if
     *            no result shall be stored.
     */
    public SummationValueListenerDependency(FormField[] relatedFields, FormField resultField) {
        this(relatedFields, resultField, false);
    }

    /**
     * Creates a {@link SummationValueListenerDependency}. The Fields must
     * contain instances of {@link Number} and the result field must be capable with
     * {@link Double} values.
     *
     * <p>
     * This dependency only gets active after {@link #attach()} is called.
     * </p>
     *
     * @param relatedFields
     *            the fields with the summands
     * @param resultField
     *            the field the result shall be stored into; may be <code>null</code> if
     *            no result shall be stored.
	 * @param mandatory
	 *            if <code>true</code>, a result will be computed only if all related fields
	 *            are not <code>null</code>, otherwise empty values will be handled as 0.
     */
    public SummationValueListenerDependency(FormField[] relatedFields, FormField resultField, boolean mandatory) {
    	super(relatedFields, resultField);
		this.mandatory = mandatory;
    }


	/**
	 * Checks whether the result is only computed if all fields have no <code>null</code> values.
	 */
	public boolean isMandatory() {
		return mandatory;
	}

    /**
     * Computes the sum over all {@link FormField}s.
     * @see com.top_logic.layout.form.listener.AggregationValueListenerDependency#aggregateOverFields()
     */
    @Override
	protected Object aggregateOverFields() {
        double theResult = 0.0;
		boolean isMandatory = isMandatory();
        for (int i = 0; i < size(); i++) {
			if (isMandatory && getValue(i) == null) return null;
            theResult += getDoubleValue(i);
        }
        return Double.valueOf(theResult);
    }

}
