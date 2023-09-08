/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.listener;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.layout.form.ErrorChangedListener;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.constraints.AbstractDependency;

/**
 * Abstract base class for implementing symmetric dependencies that attach value listeners
 * to multiple {@link FormField}s.
 *
 * @see AbstractDependency
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public abstract class AbstractValueListenerDependency {

    /** The {@link FormField}s between whose the dependency is established. */
    protected FormField[] relatedFields;

    /** The listeners that implement this dependency. */
    protected DependencyValueListenerImplementation[] listeners;

    /** Flag to activate or deactivate the listeners. */
    protected boolean active;


    /**
     * Creates a new AbstractValueListenerDependency between the given {@link FormField}s.
     *
     * <p>
     * This dependency only gets active after {@link #attach()} is called.
     * </p>
     */
    public AbstractValueListenerDependency(FormField[] relatedFields) {
        if (relatedFields == null) {
            throw new IllegalArgumentException("RelatedFields is null.");
        }
        this.relatedFields = relatedFields;
    }



    /**
     * The number of {@link FormField}s, which are related by this dependency.
     */
    public int size() {
        return relatedFields.length;
    }

    /**
     * The {@link FormField} at the given index.
     */
    public FormField get(int index) {
        return relatedFields[index];
    }



    /**
     * Returns whether this dependency is already attached.
     *
     * @return <code>true</code> if this dependency is attached.
     */
    public boolean isAttached() {
        return listeners != null;
    }

    /**
     * Finally establishes this dependency by adding the implementing listeners to the
     * fields.
     *
     * @see #detach() for removing all listeners that implement this dependency.
     */
    public void attach() {
        if (listeners != null) {
            throw new IllegalStateException("Already attached.");
        }
        listeners = new DependencyValueListenerImplementation[relatedFields.length];
        for (int i = 0; i < relatedFields.length; i++) {
            listeners[i] = new DependencyValueListenerImplementation(i);
            relatedFields[i].addValueListener(listeners[i]);
			relatedFields[i].addListener(FormField.ERROR_PROPERTY, listeners[i]);
        }
        active = true;
    }

    /**
     * Removes this dependency from its related fields.
     *
     * @see #attach()
     */
    public void detach() {
        if (listeners == null) {
            throw new IllegalStateException("Not yet attached.");
        }
        active = false;
        for (int i = 0; i < relatedFields.length; i++) {
            relatedFields[i].removeValueListener(listeners[i]);
			relatedFields[i].removeListener(FormField.ERROR_PROPERTY, listeners[i]);
        }
        this.listeners = null;
    }



    /**
     * This method 'touches' the values in the fields of this dependency, that is it calls
     * {@link #valueChanged(int, FormField, Object, Object)} for all FormFields. This can be
     * useful for example for initial work.<br/>
     * <br/>
     * Calls {@link #touchAllImplementation()} only if this dependency is active.
     * Deactivates the dependency while the method works.<br/>
     * This is required to avoid listener chain reactions when the values of the listened
     * fields get changed by a listener. Subclasses may override the functionality of this
     * method by overriding the {@link #touchAllImplementation()} method.
     */
    public final void touchAll() {
        if (listeners == null) {
            throw new IllegalStateException("Not yet attached.");
        }
        if (!active) return;
        active = false;
        touchAllImplementation();
        active = true;
    }



    /**
     * This method 'touches' the values in the fields of this dependency, that is it calls
     * {@link #valueChanged(int, FormField, Object, Object)} for all FormFields. This can be
     * useful for example for initial work.
     */
    protected void touchAllImplementation() {
        for (int i = 0; i < relatedFields.length; i++) {
            Object theValue = relatedFields[i].hasValue() ? relatedFields[i].getValue() : null;
            valueChanged(i, relatedFields[i], theValue, theValue);
        }
    }



    /**
     * Calls {@link #valueChanged(int, FormField, Object, Object)} only if this dependency
     * is active. Deactivates the dependency while the method works.<br/>
     * This is required to avoid listener chain reactions when the values of the
     * listened fields get changed by a listener.
     *
     * @see #valueChanged(int, FormField, Object, Object)
     */
    protected final void valueChangedIfActive(int fieldIndex, FormField field, Object oldValue, Object newValue) {
        if (!active) return;
        active = false;
        valueChanged(fieldIndex, field, oldValue, newValue);
        active = true;
    }



    /**
     * Implements the ValueListener of this dependency.
     *
     * @see ValueListener
     * @param fieldIndex
     *            Index (greater or equal to <code>0</code> and lower than {@link #size()})
     *            of the {@link FormField} that was changed.
     * @param field
     *            The field, whose value changed.
     * @param oldValue
     *            The value of the field before the change.
     * @param newValue
     *            The new value of the field after the change.
     */
    protected abstract void valueChanged(int fieldIndex, FormField field, Object oldValue, Object newValue);



    /**
     * A {@link ValueListener} implementation that enforces the dependency defined by its
     * outer class.
     *
     * @author <a href=mailto:CBR@top-logic.com>CBR</a>
     */
	protected class DependencyValueListenerImplementation implements ValueListener, ErrorChangedListener {

        private final int fieldIndex;

        public DependencyValueListenerImplementation(int fieldIndex) {
            this.fieldIndex = fieldIndex;
        }

        @Override
		public void valueChanged(FormField field, Object oldValue, Object newValue) {
            AbstractValueListenerDependency.this.valueChangedIfActive(fieldIndex, field, oldValue, newValue);
        }

		@Override
        public Bubble handleErrorChanged(FormField sender, String oldError, String newError) {
        	Object value = sender.hasValue() ? sender.getValue() : null;
        	AbstractValueListenerDependency.this.valueChangedIfActive(fieldIndex, relatedFields[fieldIndex], value, value);
			return Bubble.BUBBLE;
        }

    }

}
