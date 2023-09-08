/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.listener;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;


/**
 * Abstract base class for {@link AbstractValueListenerDependency} implementations that
 * aggregate over an amount of {@link FormField}s if a value has changed.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public abstract class AggregationValueListenerDependency extends AbstractValueListenerDependency {

    /** Holds a single listener instance */
    protected final SingletonDependencyValueListenerImplementation listener = new SingletonDependencyValueListenerImplementation();

    /** Saves the field the result shall be stored into. */
    protected FormField resultField;

    /** Flag whether to touch the fields initially at attaching of the dependency. */
    protected boolean touchAtAttach;


    /**
     * Creates a {@link AggregationValueListenerDependency}.
     *
     * <p>
     * This dependency only gets active after {@link #attach()} is called.
     * </p>
     *
     * @param relatedFields
     *            the fields to aggregate over
     * @param resultField
     *            the field the result shall be stored into; may be <code>null</code> if
     *            no result shall be stored.
     */
    public AggregationValueListenerDependency(FormField[] relatedFields, FormField resultField) {
        super(relatedFields);
        this.resultField = resultField;
        this.touchAtAttach = true;
    }


    /**
     * Gets the current value of the field with the given index.
     */
    public Object getValue(int index) {
        FormField theField = get(index);
        return theField.hasValue() ? theField.getValue() : null;
    }

    /**
     * Gets the current value of the field with the given index as double or 0.0, if the
     * value is not a number.
     */
    public double getDoubleValue(int index) {
        Object theValue = getValue(index);
        return (theValue instanceof Number ? ((Number)theValue).doubleValue() : 0.0);
    }

    /**
     * Gets the result field.
     */
    public FormField getResultField() {
        return resultField;
    }

    /**
     * This method returns the touchAtAttach flag.
     */
    public boolean isTouchAtAttach() {
        return this.touchAtAttach;
    }

    /**
     * This method sets the touchAtAttach flag.
     */
    public void setTouchAtAttach(boolean touchAtAttach) {
        this.touchAtAttach = touchAtAttach;
    }


    /**
     * Overridden because only a singleton instance of the listener is needed.
     * Aggregates initial over all fields after attaching.
     *
     * @see AbstractValueListenerDependency#attach()
     */
    @Override
	public void attach() {
        if (listeners != null) {
            throw new IllegalStateException("Already attached.");
        }
        listeners = new DependencyValueListenerImplementation[0];
        for (int i = 0; i < relatedFields.length; i++) {
            relatedFields[i].addValueListener(listener);
			relatedFields[i].addListener(FormField.ERROR_PROPERTY, listener);
        }
        active = true;
        if (touchAtAttach) touchAll();
    }

    /**
     * Overridden because only a singleton instance of the listener is needed.
     *
     * @see AbstractValueListenerDependency#detach()
     */
    @Override
	public void detach() {
        if (listeners == null) {
            throw new IllegalStateException("Not yet attached.");
        }
        active = false;
        for (int i = 0; i < relatedFields.length; i++) {
            relatedFields[i].removeValueListener(listener);
			relatedFields[i].removeListener(FormField.ERROR_PROPERTY, listener);
        }
        this.listeners = null;
    }



    /**
     * Aggregates initial over all fields of this dependency. The result gets stored in the
     * result field. Subclasses may extend this method for initial setup.
     */
    @Override
	protected void touchAllImplementation() {
        Object theResult = aggregateOverFields();
        if (resultField != null) {
            resultField.setValue(theResult);
        }
    }



    /**
     * Aggregates over all fields of this dependency. The result gets stored in the result
     * field.
     *
     * @see com.top_logic.layout.form.listener.AbstractValueListenerDependency#valueChanged(int, com.top_logic.layout.form.FormField, java.lang.Object, java.lang.Object)
     */
    @Override
	protected final void valueChanged(int aFieldIndex, FormField aField, Object aOldValue, Object aNewValue) {
        touchAllImplementation();
    }



    /**
     * Implements the Aggregation of this dependency. To get the values of the proper
     * fields, the {@link #getValue(int)} or {@link #getDoubleValue(int)} methods can be
     * used. The number of the fields can be got by the {@link #size()} method, the fields
     * itself can be got with the {@link #get(int)} and {@link #getResultField()} methods.
     *
     * @return the result of the aggregation, which will be stored in the result field.
     */
    protected abstract Object aggregateOverFields();



    /**
     * A {@link ValueListener} implementation that enforces the dependency defined by its
     * outer class.
     *
     * @author <a href=mailto:CBR@top-logic.com>CBR</a>
     */
    protected class SingletonDependencyValueListenerImplementation extends DependencyValueListenerImplementation {

        public SingletonDependencyValueListenerImplementation() {
            super(-1);
        }

        @Override
		public void valueChanged(FormField field, Object oldValue, Object newValue) {
            touchAll();
        }

        @Override
		public Bubble handleErrorChanged(FormField sender, String oldError, String newError) {
			touchAll();
			return Bubble.BUBBLE;
        }

    }

}
