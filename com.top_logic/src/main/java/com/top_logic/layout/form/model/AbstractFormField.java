/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import java.text.Format;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.InlineList;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.KeyEvent;
import com.top_logic.layout.KeyEventDispatcher;
import com.top_logic.layout.KeyEventListener;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.FocusHandling;
import com.top_logic.layout.basic.Focusable;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.constraints.GenericMandatoryConstraint;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Utils;

/**
 * Server-side representation of a form (input) field.
 * 
 * TODO BHU please explain the (complex) state handling and the lifecycle
 *     of this class.
 *     
 * TODO BHU/KHA this class is to complex for subclassing,
 *      we could extract the Listener and Constraint part.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractFormField extends AbstractFormMember implements FormField, KeyEventDispatcher {

    /** 
	 * Constant that represents "no input" from the client-side.
	 * 
	 * @see #_rawValue
	 */
	public static final Object NO_RAW_VALUE = null;

	/** Convenience constant to specify that a field has no constraint. */
	public static final Constraint NO_CONSTRAINT = null;

    /** Convenience constant to specify that a field is "immutable". */
    public static final boolean IMMUTABLE = true;

    /** Convenience constant to specify that a field is "mandatory". */
    public static final boolean MANDATORY = true;

	/**
	 * Object to indicate that no value changed event must be fired.
	 * 
	 * @see #fireValueChanged(Object)
	 */
	private static final Object VALUE_UNCHANGED = new Object();

    /**
	 * Convenience constant to specify that a field should normalize 
	 * its input. 
	 * 
	 * @see #getNormalizeInput()
	 */
    public static final boolean NORMALIZE = true;

	/**
	 * Initial state after a programmatic initialization of this field. No constraints have been
	 * checked. It is not yet decided, whether the field's value is a valid one or not.
	 */
	private static final int INITIAL_STATE = 0;

	/**
	 * The user input could not be parsed into an application value. {@link #getValue()} must not be
	 * called.
	 */
    private static final int ILLEGAL_INPUT_STATE = 2;

	/**
	 * The validity of this field could not yet be decided, because a dependent field is invalid 
	 * ({@link #ILLEGAL_INPUT_STATE}, or {@link #ILLEGAL_VALUE_STATE}), or the validity of the
	 * dependency cannot be decided for the same reasons.
	 */
	private static final int WAIT_STATE = 3;
	
	/**
	 * Some constraints of this field are violated, but this field has an application value. 
	 */
    private static final int ILLEGAL_VALUE_STATE = 4;
    
    /**
     * This field is valid, all constraints (and dependencies) are valid.
     */
	private static final int VALID_STATE = 5;

    /** 
	 * Decides, whether this field is "mandatory". 
	 * 
	 * <p>Being mandatory is only a hint for the view of this field to 
	 * render its field with special  decorations (e.g. with yellow background, 
	 * or with a bold-faced label). Being mandatory has no semantic meaning. 
	 * All semantic aspects of a field is checked by {@link Constraint}s. A field 
	 * that is marked mandatory should have a {@link Constraint} associated with it. 
	 * </p>
	 *
	 * @see #setMandatory(boolean)
	 */
    private boolean mandatory;

    /**
     * @see FormField#isBlocked()
     */
    private boolean blocked;
    
    /**
     * @see FormField#isFrozen()
     */
    private boolean frozen;
    
	/** 
	 * Decides, whether values are normalized after input. 
	 *
	 * @see #getNormalizeInput()
	 */
    private boolean normalizeInput;
    
    /** The original value transmitted from the client-side view of this {@link FormField}. */
    private Object _rawValue = NO_RAW_VALUE;
	
    /** 
     * The default value of this field. 
	 * 
	 * <p>
	 * A field with a default value can be reset to this value with a call 
	 * to {@link #reset()}. The default value is also the value to which the 
	 * current value (see {@link #_value}) is compared by {@link #isChanged()}. 
	 * </p>
     */
    private Object defaultValue;

    /** 
	 * The application value of this field. 
	 * 
	 * As opposed to the {@link #_rawValue raw input value}, the application 
	 * value is an application object that is parsed by this field from the 
	 * user's input (see {@link #parseRawValue(Object)} ).
	 */
    private Object _value;

    /** 
	 * An optional application value (of the same type as {@link #_value}) 
	 * that can be used to construct those error messages that help the user to 
	 * format its input in a way that can be parsed by this field. 
	 */
    private Object exampleValue;

    /**
	 * The (translated) error concerning this field, <code>null</code> if
	 * field is OK
	 */
    private String error;
    
    /**
	 * {@link InlineList} of {@link Constraint}s associated with this field.
	 * 
	 * @see #checkValue(Object) where the constraints are applied.
	 */
	private Object constraints = InlineList.newInlineList();
    
    /**
     * The (translated) {@link InlineList} of warnings concerning this field, 
     * <code>null</code> if field is OK
     */
	private List<String> warnings;
    
    /**
     * {@link InlineList} of {@link Constraint}s associated with this field.
     * 
     * @see #checkWarnings(Object) where the constraints are applied.
     */
	private Object warningConstraints = InlineList.newInlineList();
    
    /**
	 * Set of {@link AbstractFormField}s with fields that must be
	 * {@link #check() re-checked}, if the {@link #_value} of this field
	 * changes.
	 * 
	 * <p>
	 * Note: this field is initialized on demand (see
	 * {@link #addDependant(FormField)})
	 * </p>
	 * 
	 * @see #checkWithAllDependencies()
	 */
	private Set<FormField> dependants;

    /** One of <code>.._STATE</code> */
    private int state = INITIAL_STATE;
    
    /**
	 * Internal variable to detect whether an {@link FormMember#IS_CHANGED_PROPERTY is changed}
	 * event shall be fired. Can be set to <code>false</code> to temporarily disable firing this
	 * event, e.g. in non atomic methods.
	 */
    private boolean fireChangedEvent = true;

    /**
	 * Creates a new field with given parameters.
	 * 
	 * @param name
	 *     See {@link #getName()}.
	 * @param mandatory
	 *     See {@link #isMandatory()}.
	 * @param immutable
	 *     See {@link #isImmutable()}.
	 * @param normalizeInput
	 *     See {@link #getNormalizeInput()}.
	 * @param constraint optional See {@link #addConstraint(Constraint)}.
	 */
    public AbstractFormField(String name, boolean mandatory, boolean immutable, boolean normalizeInput, Constraint constraint) {
    	super(name, immutable);
    	
        this.internalSetMandatory(mandatory);
        this.frozen = false;
        this.blocked = false;
        this.normalizeInput = normalizeInput;
        
    	if (constraint != null) {
    		addConstraint(constraint);
    	} 
    }
    
    @Override
	public final FormField addWarningConstraint(Constraint aConstraint) {
        this.warningConstraints = this.internalAddConstraint(this.warningConstraints, aConstraint);
        return this;
    }
    
    @Override
	public final FormField addConstraint(Constraint aConstraint) {
    	this.constraints = this.internalAddConstraint(this.constraints, aConstraint);
    	return this;
    }

	/**
	 * Adds the given {@link Constraint} to the given {@link InlineList} of
	 * Constraints.
	 * 
	 * @param inlineConstraintList
	 *        The {@link InlineList} of {@link Constraint}s to add to.
	 * @param constraint
	 *        The {@link Constraint} to add.
	 * @return The given {@link InlineList} with the given constraint added.
	 */
    private Object internalAddConstraint(Object inlineConstraintList, Constraint constraint) {
        if (! InlineList.contains(inlineConstraintList, constraint)) {
            inlineConstraintList = InlineList.add(Constraint.class, inlineConstraintList, constraint);
            handleConstraintChange();
            
            // The new constraint may depend on the values of a set of other fields.
            // Therefore, this field must be re-checked, if any of the values of
            // those other fields changes. To accomplish this, this field is added
            // as dependent field to all those fields that the new constraint
            // depends on.
            for (Iterator<FormField> it = constraint.reportDependencies().iterator(); it.hasNext(); ) {
                FormField dependency = it.next();
                
                // NOTE: The following cast is necessary due to the following reason:
                // The mechanism that is used to find all fields that must be
                // checked again, if some other field has changed, is considered an
                // implementation detail. Therefore, the addDependant() and
                // removeDependant() methods are not part of the public FormField
                // API. This decision causes the following limitation: A field
                // may only depend on those fields that are implemented in the
                // same inheritance subtree.
                //
                // See also: removeConstraint().
				dependency.addDependant(this);
            }
        }
        
        return inlineConstraintList;
    }
    
    @Override
	public final boolean removeWarningConstraint(Constraint constraint) {
        boolean result = InlineList.contains(this.warningConstraints, constraint);
		if (result) {
			this.warningConstraints = this.internalRemoveConstraint(this.warningConstraints, constraint);
		}
		return result;
    }
    
	@Override
	public final boolean removeConstraint(Constraint constraint) {
	    boolean result = InlineList.contains(this.constraints, constraint);
		if (result) {
		    this.constraints = this.internalRemoveConstraint(this.constraints, constraint);
		}
		return result;
    }

	/**
	 * Removes the given {@link Constraint} from the given {@link InlineList} of
	 * Constraints.
	 * 
	 * @param inlineConstraintList
	 *        The {@link InlineList} of {@link Constraint}s to remove from.
	 * @param constraint
	 *        The {@link Constraint} to remove.
	 * @return The given {@link InlineList} with the given constraint removed.
	 */
    private Object internalRemoveConstraint(Object inlineConstraintList, Constraint constraint) {
		inlineConstraintList = InlineList.remove(inlineConstraintList, constraint);
		
		handleConstraintChange();
		
		// Build a set of remaining dependencies (the union of all
		// dependencies of all remaining constraints).
		HashSet<FormField> remainingDependencies = new HashSet<>();
		for (Iterator<Constraint> it = InlineList.iterator(Constraint.class, inlineConstraintList); it.hasNext(); ) {
		    Constraint remainingConstraint = it.next();
		    remainingDependencies.addAll( 
		            remainingConstraint.reportDependencies());
		}
		
		// Remove this field from all dependent sets of the dependencies of the
		// removed constraint, except those which are in the set of remaining
		// dependencies.
		for (Iterator<FormField> it = constraint.reportDependencies().iterator(); it.hasNext(); ) {
		    FormField dependency = it.next();
		    if (remainingDependencies.contains(dependency)) continue;
		    
		    // NOTE: For a justification of the following cast, see the
		    // addConstraint() method.
			dependency.removeDependant(this);
		}
		
		return inlineConstraintList;
	}

	@Override
	public List<Constraint> getConstraints() {
		return InlineList.toList(Constraint.class, this.constraints);
	}

	@Override
	public List<Constraint> getWarningConstraints() {
		return InlineList.toList(Constraint.class, this.warningConstraints);
	}

	@Override
	public void clearConstraints() {
	    
	    if (! InlineList.isEmpty(this.warningConstraints)) {
			this.warningConstraints = InlineList.clear(this.warningConstraints);
	    }
	    
	    if (! InlineList.isEmpty(this.constraints)) {
			this.constraints = InlineList.clear(this.constraints);
	        handleConstraintChange();
	    }
	}
	
	@Override
	public final boolean addDependant(FormField dependant) {
		if (dependants == null) {
			// On demand initialized field.
			dependants = new HashSet<>();
		}
    	return this.dependants.add(dependant);
	}

	@Override
	public boolean removeDependant(FormField dependant) {
    	if (dependants == null) {
			// On demand initialized field.
    		return false;
    	}
    	return this.dependants.remove(dependant);
	}
    
    /**
     * Accessor for on demand initialized field {@link #dependants}.
     */
    private final Set<FormField> internalGetDependants() {
    	if (dependants == null) {
			// On demand initialized field.
    		return Collections.emptySet();
    	}
    	return dependants;
    }
    
    /**
     * Another (dependent) field is only checked, if it is not in
     * {@link #INITIAL_STATE}, because no error must be reported for fields that
     * have never been touched by the user.
     */
    @Override
	public void checkWithAllDependencies() {
    	check();
    	checkDependents();
    }

	/**
	 * checks all dependents of the given {@link AbstractFormField}.
	 */
	private void checkDependents() {
		for (Iterator<FormField> it = internalGetDependants().iterator(); it.hasNext();) {
			FormField dependant = it.next();
			
			dependant.checkDependency();
		}
	}

    @Override
	public final boolean isValid() {
        /*
         * We're valid if we have
         * 1. just been created OR
         * 2. valid input
         * 
         * Why are we valid upon creation? Upon field creation initial field
         * values are set programmatically and are hence considered being valid. 
         */
    	return (state == INITIAL_STATE) || (state == VALID_STATE);
    }

    /**
     * @see com.top_logic.layout.form.FormField#hasValue()
     */
    @Override
	public final boolean hasValue() {
    	return 
    		(state == INITIAL_STATE) || 
    		(state == WAIT_STATE) || 
    		(state == ILLEGAL_VALUE_STATE) || 
    		(state == VALID_STATE);
    }
    
    @Override
	public final Object getValue() {
		if (!hasValue()) {
			return narrowValue(null);
		}
    	return this._value;
    }

	/**
	 * This field's value, regardless of its {@link #hasValue() validity state}.
	 * 
	 * @see #getValue()
	 */
	protected final Object getStoredValue() {
		return this._value;
	}

    @Override
	public final void setValue(final Object newValue) {
    	try {
			internalSetValue(newValue, true);
		} catch (VetoException ex) {
			// Ignore, do not produce user feedback.
		}
    }

	/**
	 * Calling this method indicates that the new value is not a programmatic
	 * update, but comes from the GUI.
	 * 
	 * @throws VetoException
	 *         if a {@link ValueVetoListener} does not allow setting the new
	 *         value.
	 */
    final void deliverValue(final Object newValue) throws VetoException {
    	internalSetValue(newValue, false);
    	checkWithAllDependencies();
    }
    
	private void internalSetValue(final Object newValue, boolean programmaticUpdate) throws VetoException {
		// Invoke the checks and normalizations defined by this field. If
		// normalization fails, an attempt to set an illegal value is detected
		// and nothing must happen to this field.
    	Object newNormalizedValue = narrowValue(newValue);
    	
    	// Unparse the new value. If unparsing fails, an attempt
		// to set an illegal value is detected and nothing must happen to this
		// field.
    	Object newRawValue = unparseValue(newNormalizedValue);

    	// Update the state of this field. 
    	//
    	// Note: This must happen before actually setting the raw and application 
    	// values, because the event listeners triggered from those setters would 
    	// otherwise see the field in a state, where it potentially has no value.
		int oldState = state;
		boolean oldHasError = hasError();
		String oldError = null;
		if (oldHasError) {
			oldError = getError();
		}
    	clearError();
		Object oldValue;
		try {
			// Internally store the new value of this field and start server-side
			// event processing.
			oldValue = storeValue(newNormalizedValue, programmaticUpdate);
		} catch (VetoException ex) {
			// Undo state modification
			setState(oldState);
			if (oldHasError) {
				internalSetError(oldError);
			}

			throw ex;
		}

    	// Update the raw value and provide feedback to the user agent. 
    	// 
    	// Note: This must happen *after* the call to storeValue(), because
		// otherwise a property listener would observe the field's old value, if
		// accessing the field in response to a property event.
		storeAndFireRawValue(newRawValue, programmaticUpdate);

		/* Fire the value changed event *after* the call to storeAndFireRawValue(), to ensure that
		 * both, application and client value, represent the same value. Otherwise ValueListener are
		 * triggered and have no access to the new raw value. */
		fireValueChanged(oldValue);
    }
    
	@Override
	protected void notifyDisplayModeChanged(int oldDisplayMode, int newDisplayMode) {
		// Clear/restore error when making field inactive/reactivating field. Since restoring the
		// error is only possible if the field does not contain user input that cannot be parsed,
		// this only happens for fields that have no illegal input.
		if (state != ILLEGAL_INPUT_STATE) {
			if (newDisplayMode == ACTIVE_MODE) {
				// Restore a potential error.
				check();
			} else {
				// Clear error because the user can no longer change the value to fix the error.
				clearError();
			}
		}

		super.notifyDisplayModeChanged(oldDisplayMode, newDisplayMode);
	}

    @Override
	public Object getDefaultValue() {
    	return this.defaultValue;
    }

    @Override
	public void setDefaultValue(Object defaultValue) {
    	boolean changed = isChanged();
    	this.defaultValue = narrowValue(defaultValue);
		checkChanged(changed);
    }

	@Override
	public Object getExampleValue() {
		return this.exampleValue;
	}

	@Override
	public void setExampleValue(Object exampleValue) {
		this.exampleValue = exampleValue;
	}

    @Override
	public final Object getRawValue() {
        return _rawValue;
    }

    @Override
	public final boolean hasError() {
		return error != null;
    }

    @Override
	public final String getError() {
    	if (! hasError()) {
            throw new IllegalStateException("!hasError()");
        }
    	return this.error;
    }
    
    @Override
	public final boolean hasWarnings() {
        return warnings != null;
    }
    
    @Override
	public final List<String> getWarnings() {
        if (! hasWarnings()) {
            throw new IllegalStateException("!hasWarnings()");
        }
        return new ArrayList<>(this.warnings);
    }

	@Override
	public void clearError() {
		setState(INITIAL_STATE);
		internalSetError(null);
	}
	
	@Override
	public void setError(String message) {
		// Make sure, first, no previous (more informative) error is
		// overwritten, and second enforce the rules for calling this method as
		// specified by the interfaces documentation.
		assert isValid();
		
		setState(ILLEGAL_VALUE_STATE);
		internalSetError(message);
	}

	@Override
	public void setWarnings(List<String> messages) {
	    internalSetWarnings(messages);
	}
    
	/**
	 * Add the given warning message for this field.
	 * 
	 * @param messages
	 *        The new list of warning messages, or <code>null</code>, if the
	 *        current warning messages message should be cleared.
	 */
	protected void internalSetWarnings(List<String> messages) {
		boolean oldHasWarning = hasWarnings();
		List<String> oldWarnings = oldHasWarning ? getWarnings() : null;

		this.warnings = messages == null || messages.isEmpty() ? null : messages;

		boolean newHasWaring = hasWarnings();
		List<String> newWarnings = newHasWaring ? getWarnings() : null;

		boolean warningsTextChanged = !Utils.equals(newWarnings, oldWarnings);
		if (newHasWaring != oldHasWarning || warningsTextChanged)
			firePropertyChanged(HAS_WARNINGS_PROPERTY, selfAsField(), Boolean.valueOf(oldHasWarning),
				Boolean.valueOf(newHasWaring));

		if (warningsTextChanged)
			firePropertyChanged(WARNINGS_PROPERTY, selfAsField(), CollectionUtil.toList(oldWarnings),
				CollectionUtil.toList(newWarnings));
	}
	
	/**
	 * Sets the given error message for this field.
	 * 
	 * @param anError
	 *     The new error message, or <code>null</code>, if the current
	 *     error message should be cleared.
	 */
	protected void internalSetError(String anError) {
    	boolean oldHasError = hasError();
    	String oldError = oldHasError ? getError() : null;
    	
    	this.error = anError;

    	boolean newHasError = hasError();
    	String newError = newHasError ? getError() : null;
    	
    	// This field may only have an error associated, if it is in an error
		// state.
    	assert 
    		(! newHasError) || 
    		(state == ILLEGAL_INPUT_STATE) || 
    		(state == ILLEGAL_VALUE_STATE);
    	
    	boolean errorTextChanged = !StringServices.equals(newError, oldError);
    	if (newHasError != oldHasError || errorTextChanged)
			firePropertyChanged(HAS_ERROR_PROPERTY, selfAsField(), Boolean.valueOf(oldHasError),
				Boolean.valueOf(newHasError));
    	
    	if (errorTextChanged)
			firePropertyChanged(ERROR_PROPERTY, selfAsField(),
				StringServices.nonNull(oldError),
				StringServices.nonNull(newError));
	}
    
    @Override
	public boolean isMandatory() {
		return this.mandatory;		
	}
	
	@Override
	public void setMandatory(boolean newMandatory) {
		boolean oldMandatory = this.mandatory;
		if (newMandatory == oldMandatory) return;

		this.internalSetMandatory(newMandatory);
		handleConstraintChange();
		
		firePropertyChanged(MANDATORY_PROPERTY, selfAsField(), Boolean.valueOf(oldMandatory),
			Boolean.valueOf(this.mandatory));
	}
	
	private void internalSetMandatory(boolean newMandatory) {
		this.mandatory = newMandatory;
		
		if (newMandatory) {
			addConstraint(mandatoryConstraint());
		} else {
			removeConstraint(mandatoryConstraint());
		}
	}

	/**
	 * The constraint that is internally set when the field becomes mandatory.
	 * 
	 * @implSpec The constraint is removed, when the field becomes non-mandatory, therefore this
	 *           method must always return the same constraint to avoid inconsistencies.
	 */
	protected Constraint mandatoryConstraint() {
		return GenericMandatoryConstraint.SINGLETON;
	}
	
	@Override
	public FieldMode getMode() {
		FieldMode memberMode = super.getMode();
		if (memberMode.isValueVisible() && isBlocked()) {
			return FieldMode.BLOCKED;
		}
		return memberMode;
	}

	@Override
	public final boolean isLocallyImmutable() {
		// A frozen and blocked field is implicitly immutable. 
		return this.frozen || this.blocked || super.isLocallyImmutable();
	}
	
    @Override
	public boolean isFrozen() {
    	return frozen || blocked;
    }
    
    @Override
	public void setFrozen(boolean frozen) {
		boolean oldValue = this.frozen;
		if (frozen == oldValue) return;

		this.frozen = frozen;

		updateDisplayMode();
	}
	
	@Override
	public boolean isBlocked() {
		return this.blocked;
	}
	
	@Override
	public void setBlocked(boolean blocked) {
		boolean oldValue = this.blocked;
		if (blocked == oldValue) return;
		
		this.blocked = blocked;

		firePropertyChanged(FormField.BLOCKED_PROPERTY, selfAsField(), Boolean.valueOf(oldValue),
			Boolean.valueOf(blocked));
		updateDisplayMode();
	}
	
	/* package protected */void updateRawValue() {
		if (!hasValue()) {
			// Don't try to change client side value to preserve as much of the
			// user input.
			return;
		}
		storeAndFireRawValue(unparseValue(getValue()), true);
	}

	private void handleConstraintChange() {
		if (this.state == ILLEGAL_VALUE_STATE || this.state == VALID_STATE) {
			clearError();
		}
	}


	/**
	 * <p>
	 * A normalizing field overwrites the user's input with a (re-)formatted 
	 * version, after the input was accepted by the field. The user input is accepted
	 * by this field, if the input could be parsed to an application value (see 
	 * {@link #_value}). The normalization occurs during the process of checking the 
	 * input value (see {@link #check()}. Invalid input (which could not be parsed) 
	 * is never overwritten.
	 * </p>
	 * 
	 * @return whether this field is normalizing its input.
	 */
	public boolean getNormalizeInput() {
		return normalizeInput;
	}
	
	/**
	 * @see #getNormalizeInput()
	 */
	public void setNormalizeInput(boolean normalizeInput) {
		this.normalizeInput = normalizeInput;
	}
	
    @Override
	public final void update(Object newRawValue) throws VetoException {
    	if (! isActive()) {
    		Logger.warn("Tried to update inactive field, spoofing attempt?", this);
    		return;
    	}
    	
    	// This check is more rigorous and should not be necessary in
		// production mode (because it is inefficient). Updates should not
		// have been forwarded to this field, if some of its ancestors
		// is inactive.
    	assert listensForUpdate() : "!listensForUpdate()";
    	
    	if (! Utils.equals(_rawValue, newRawValue)) {
    		processInput(newRawValue, false);
    	}
    }

	/**
	 * Parse the current user input after dynamically changing the algorithm in
	 * {@link #parseRawValue(Object)}.
	 */
    protected final void reparse() {
    	try {
    		// Again parse the current raw value, but pretending that this is a
			// programmatic update.
			processInput(this._rawValue, true);
		} catch (VetoException ex) {
			// New value was rejected even if the update was flagged as 
			// programmatic update.

			// Do not produce user feedback.

			throw new IllegalStateException("Reparsing field value not possible", ex);
		}
		internalCheck();
    	checkDependents();
    }

    @Override
	public final boolean isCheckRequired(boolean isFinalSubmit) {
    	if (isFinalSubmit) {
        	return (state == INITIAL_STATE);
    	} else {
        	return false;
    	}
    }
    
    @Override
	public final void check() {
    	internalCheck();
    }

	@Override
	public final void checkDependency() {
		// If the dependent field is still in initial state, do not check,
		// because this would produce spurious error messages at the user
		// interface. A field must only be checked, if the user has touched
		// its contents, or if the user tries to finally submit the form.
		if (state == INITIAL_STATE) {
			return;
		}

		internalCheck();
	}

	private final void internalCheck() {
		switch (state) {
    	case INITIAL_STATE: {
    		locallyCheck();
    		break;
    	}
    	
    	case ILLEGAL_INPUT_STATE: {
    		break;
    	}
    	
    	default: { 
    		locallyCheck();
    		break;
    	}
		}
	}

	/**
	 * Entry point of the parse chain for this field.
	 * 
	 * <p>
	 * The parsing algorithm (which transforms the {@link #_rawValue raw value}
	 * feed from the user interface into this field to an
	 * {@link #_value application value}) is formulated in terms of the abstract
	 * methods {@link #parseRawValue(Object)} and
	 * {@link #unparseValue(Object)}.
	 * </p>
	 * 
	 * <p>
	 * Input processing occurs in the following steps:
	 * </p>
	 * 
	 * <ol>
	 * <li>The new input is passed to {@link #parseRawValue(Object)} and
	 * transformed into an application value. </li>
	 * 
	 * <li>If {@link #getNormalizeInput() input normalization} is enabled, the
	 * new application value is passed to {@link #unparseValue(Object)} and
	 * transformed back into a (normalized) raw value (and transported back to
	 * the client-side in a {@link FormField#VALUE_PROPERTY} event. </li>
	 * 
	 * <li>If no error occurred during processing, the check chain in
	 * {@link #locallyCheck()} is invoked to check the {@link Constraint}s of
	 * this field for the new application value. If an error occurred, this
	 * fields switches to {@link #ILLEGAL_INPUT_STATE}.</li>
	 * </li>
	 * </ol>
	 * 
	 * @param programmaticUpdate 
	 *        Whether the new value was not directly entered by the user.
	 */
    private final void processInput(Object newRawValue, boolean programmaticUpdate) throws VetoException {
    	try {
			// Directly update raw value to bring this server-side model in sync
			// with the client-side view. storeAndFireRawValue() must not be called,
			// because it would send an event and transport the new value back
			// to the client.
			Object oldRawValue = storeRawValue(newRawValue);

			recordFieldRawInput(newRawValue, programmaticUpdate);

			Object newValue = parseRawValue(newRawValue);

			// Normalization must happen after setting the application value,
			// because the update production for the client-side display may
			// access the application value in addition to the raw value.
			if (normalizeInput) {
				newRawValue = unparseValue(newValue);

				// If input normalization is enabled, the new raw value is
				// updated again (after directly setting it in update()). This
				// second update is forwarded to the client-side, if the
				// normalization changed the raw value in any way.
				oldRawValue = storeRawValue(newRawValue);
			} else {
				// Prevent sending an update of the raw value back to the client.
				oldRawValue = newRawValue;
			}

    		// Temporarily switch from any state (especially INVALID_INPUT_STATE) to wait state. This is
			// required to allow access to the newly parsed value from value
			// from listeners that are notified from the storeAndFireRawValue() method. To
			// be 100% correct, this state change should happen between the
			// assignment to 'this.value' and the notification of the listeners in
			// storeAndFireRawValue(). But the state change cannot be moved to
			// storeAndFireRawValue(), because this method is also called from other
			// contexts.
			final int currentState = state;
    		setState(WAIT_STATE);
			try {
				Object oldValue = storeValue(newValue, programmaticUpdate);

				fireValueChanged(oldValue);
			} catch (VetoException ex) {
				// Make sure to revert the UI.
				storeAndFireRawValue(oldRawValue, programmaticUpdate);
				// roll back state of the field since the field is no longer in
				// wait state
				setState(currentState);
				throw ex;
			}
			
			if (!Utils.equals(_rawValue, newRawValue)) {
				// A value listener has updated the value in a recursive call. Do not revert its
				// changes.
				return;
			}

			fireRawValue(oldRawValue, newRawValue, programmaticUpdate);
    	} catch (CheckException ex) {
    		setState(ILLEGAL_INPUT_STATE);
    		internalSetError(ex.getMessage());
    		return;
    	}
    	
    	locallyCheck();
    	checkDependents();
    }

	private void recordFieldRawInput(Object newRawValue, boolean programmaticUpdate) {
		if (ScriptingRecorder.isRecordingActive() && !programmaticUpdate && canRecordRawValue()) {
			ScriptingRecorder.recordFieldRawInput(this, newRawValue);
		}
	}

	private void recordFieldInput(Object newValue, boolean programmaticUpdate) {
		if (ScriptingRecorder.isRecordingActive() && !programmaticUpdate && !canRecordRawValue()) {
			ScriptingRecorder.recordFieldInput(this, newValue);
		}
	}

    /**
     * Script recording: Whether field inputs should be recorded as raw values. 
     */
	protected boolean canRecordRawValue() {
		return false;
	}

    /**
	 * Entry point for the check chain of this field.
	 * 
	 * <p>
	 * A new application value is checked for compatibility with all
	 * {@link Constraint}s associated with this field. Depending on the result
	 * of the check, this field changes to {@link #VALID_STATE} (if all checks
	 * succeeded), {@link #WAIT_STATE} (if some checks could not be run due to
	 * missing values in other fields), or {@link #ILLEGAL_VALUE_STATE} (if some
	 * {@link Constraint} failed with an error).
	 * </p>
	 */
    private final void locallyCheck() {
		try {
			if (checkValue(_value)) {
				setState(VALID_STATE);
				internalSetWarnings(checkWarnings(_value));
			} else {
				setState(WAIT_STATE);
			}
			internalSetError(null);
		} catch (CheckException ex) {
			setState(ILLEGAL_VALUE_STATE);
			internalSetError(ex.getMessage());
		}
    }

    /**
	 * Check the given value for compliance with all {@link Constraint}s of
	 * this field.
	 * 
	 * @param aValue
	 *     The value to check.
	 * @return whether the complete chain of {@link Constraint}s succeeded. If
	 *     the result is <code>false</code>, but no
	 *     {@link CheckException} is thrown, some constraints in the check
	 *     chain aborted due to missing input.
	 * @throws CheckException
	 *     if some constraint in the check chain failed.
	 */
    private final boolean checkValue(Object aValue) throws CheckException {
    	boolean success = true;
		if (isActive()) {
			for (Iterator<Constraint> it = InlineList.iterator(Constraint.class, this.constraints); it.hasNext();) {
				success &= it.next().check(aValue);
			}
    	}
    	
    	// Note: Must not explicitly check for the mandatory property, because there are
    	// fields that are marked mandatory but must not be checked. A default constraint
    	// is added for mandatory fields on changing the mandatory property. But this
    	// constraint might have been cleared. 
    	
    	return success;
    }

    
    /**
     * Check the given value for compliance with all warning {@link Constraint}s of
     * this field.
     * 
     * @param aValue
     *     The value to check.
     * @return a list of all warning messages of the complete chain of {@link Constraint}s.
     */
    private final List<String> checkWarnings(Object aValue) {
        
        List<String> theList = null;
        for (Iterator<Constraint> it = InlineList.iterator(Constraint.class, this.warningConstraints); it.hasNext(); ) {
            try { 
                it.next().check(aValue);
            } catch (CheckException e) {
                if (theList == null) {
                    theList = new ArrayList<>();
                }
                theList.add(e.getMessage());
            }
        }
        return theList;
    }
    
    @Override
	public final boolean checkConstraints() {
		if (!hasValue()) {
			return false;
		}
        return checkConstraints(getValue());
    }

	@Override
	public final boolean checkConstraints(Object aValue) {
		try {
			return checkValue(aValue);
		} catch (CheckException e) {
			return false;
		}
	}

    /**
	 * Fires a {@link ValueListener value event} with the given object as <code>oldValue</code>.
	 * 
	 * @param oldValue
	 *        Either the old value to fire or {@link #VALUE_UNCHANGED} to indicate that no event
	 *        must be fired.
	 * 
	 * @see #storeValue(Object, boolean)
	 */
	private void fireValueChanged(Object oldValue) {
		if (oldValue != VALUE_UNCHANGED) {
			fireValueChanged(oldValue, this._value);
		}
	}

	/**
	 * Stores the given application value into this field.
	 * 
	 * <p>
	 * To inform {@link ValueListener} call {@link #fireValueChanged(Object)} with the return value.
	 * </p>
	 * 
	 * @param newValue
	 *        The new application value to store.
	 * @param programmaticUpdate
	 *        whether the value setting was triggered by a program or by the user.
	 * 
	 * @return Either {@link #VALUE_UNCHANGED} to indicate that no events must be fired, or the old
	 *         value before the value is fired.
	 * 
	 * @see #fireValueChanged(Object)
	 */
	private final Object storeValue(Object newValue, boolean programmaticUpdate) throws VetoException {
		Object oldValue = this._value;

		if (Utils.equals(oldValue, newValue)) {
			return VALUE_UNCHANGED;
		}

		if (!programmaticUpdate) {
			checkVeto(newValue);
		}

		recordFieldInput(newValue, programmaticUpdate);

		boolean changed = isChanged();
		this._value = newValue;
		checkChanged(changed);
		
		return oldValue;
	}

	private final void storeAndFireRawValue(Object newRawValue, boolean programmaticUpdate) {
		recordFieldRawInput(newRawValue, programmaticUpdate);
		Object oldRawValue = storeRawValue(newRawValue);
		if (programmaticUpdate) {
			fireRawValue(oldRawValue, newRawValue, programmaticUpdate);
		}
	}

	/**
	 * Stores the given value as new "raw value" and returns the old "raw value".
	 */
	protected final Object storeRawValue(Object newRawValue) {
		Object oldRawValue = this._rawValue;
    	this._rawValue = newRawValue;
		return oldRawValue;
	}

	protected void fireRawValue(Object oldRawValue, Object newRawValue, boolean programmaticUpdate) {
		if (!Utils.equals(oldRawValue, newRawValue)) {
			firePropertyChanged(FormField.VALUE_PROPERTY, selfAsField(), oldRawValue, newRawValue);
		}
	}

	/**
	 * Parses the given {@link #_rawValue raw input value} into an
	 * {@link #_value application object} that is type compatible (see
	 * {@link #narrowValue(Object)} with this field. E.g. an {@link IntField}
	 * would return an {@link Integer}, while a {@link ComplexField} defines
	 * its type by its associated {@link Format}.
	 * 
	 * @param rawValue
	 *     The new raw input value.
	 * @return The new application object.
	 * 
	 * @throws CheckException
	 *     If parsing fails due to illegal input.
	 */
    protected abstract Object parseRawValue(Object rawValue) throws CheckException;

    /**
	 * Transforms the given application object back into a raw value that can
	 * be passed to the user interface (the client-side representation of this
	 * form field).
	 * 
	 * @param value
	 *     The application value to unparse.
	 * @return The (normalized) raw value that would be parsed to the given
	 *     application object, when passed to
	 *     {@link #parseRawValue(Object)}.
	 *     
	 * @see #parseRawValue(Object) for the inverse operation.
	 */
    protected abstract Object unparseValue(Object value);

    /**
	 * Checks the given value for type compatibility with this field.
	 * 
	 * <p>
	 * If the given object is not type compatible with this field (e.g. a
	 * {@link Double} object in an {@link IntField}), this method throws an
	 * {@link IllegalArgumentException} or {@link ClassCastException}.
	 * </p>
	 * 
	 * <p>
	 * If the given value is type compatible with this field, this method
	 * returns the given value, or a normalized version of it (e.g. an empty
	 * list instead of <code>null</code>). Since all application value that
	 * are passed to this field are guided through this method, this makes a
	 * concrete field robust against not perfectly matching values that are
	 * programmatically set on this field.
	 * </p>
	 * 
	 * @param value
	 *     An application value
	 * @return
	 *     The given value or a normalized version of it.
	 */
    protected abstract Object narrowValue(Object value) throws IllegalArgumentException, ClassCastException;

	/**
	 * Sets the {@link #setDefaultValue(Object) default value} of this field and {@link #reset()
	 * reset} it. After a call of this function the field is not changed.
	 * 
	 * @param aValue
	 *            the new default value of this field
	 * @see #isChanged()
	 */
	@Override
	public final void initializeField(Object aValue) {
		fireChangedEvent = false;
		boolean changed = isChanged();
		setDefaultValue(aValue);
		reset();
		fireChangedEvent = true;
		assert !isChanged() : "Field must not be changed after initializing.";
		if (changed) {
			firePropertyChanged(FormMember.IS_CHANGED_PROPERTY, self(), Boolean.TRUE, Boolean.FALSE);
		}
	}

    @Override
	public final void reset() {
    	setValue(this.defaultValue);
    }

    @Override
	public boolean isChanged() {
    	if (isTransient()) {
    		return false;
    	}
		if (!hasValue()) {
			/* The field has no value when the raw value can not be parsed. In such a situation the
			 * field is changed. */
			return true;
		}
		/*
		 * Can not access '#getValue()' as the field may be in an invalid state. As events are fired
		 * at each time the pointer 'value' is assigned, it makes sense to check that value.
		 */
		Object currentValue = this._value;
		Object currentDefault = getDefaultValue();

		return !CollectionUtil.equals(currentDefault, currentValue);
    }
    
    @Override
	public boolean listensForUpdate() {
    	for (FormMember ancestor = this; ancestor != null; ancestor = ancestor.getParent()) {
    		if (! ancestor.isActive()) {
    		    return false;
    		}

    		if (! ancestor.getInheritDeactivation()) {
    			// Stop traversing ancestors.
    			return true;
    		}
    	}
    	
    	return true;
    }
   
    @Override
	public boolean addValueListener(ValueListener listener) {
		return addListener(ValueListener.class, listener);
	}

	@Override
	public boolean hasValueListeners() {
		return hasListeners(ValueListener.class);
	}

	@Override
	public boolean removeValueListener(ValueListener listener) {
		return removeListener(ValueListener.class, listener);
	}

	@Override
	public List<ValueListener> getValueListeners() {
		return getListeners(ValueListener.class);
	}

	@Override
	public boolean addKeyListener(KeyEventListener listener) {
		return addListener(KeyEventListener.class, listener);
	}

	@Override
	public boolean removeKeyListener(KeyEventListener listener) {
		return removeListener(KeyEventListener.class, listener);
	}

	/**
	 * Whether this field has {@link KeyEventListener}s attated to it.
	 */
	public boolean hasKeyListeners() {
		return hasListeners(KeyEventListener.class);
	}

	/**
	 * Forwards the given {@link KeyEvent} to all {@link KeyEventListener}s attached to this field.
	 * 
	 * @param commandContext
	 *        See {@link KeyEventListener#handleKeyEvent(DisplayContext, KeyEvent)}.
	 * @param event
	 *        The event to dispatch.
	 */
	@Override
	public HandlerResult dispatchKeyEvent(DisplayContext commandContext, KeyEvent event) {
		List<KeyEventListener> listeners = getListeners(KeyEventListener.class);
		Object oldSource = event.setSource(this);
		try {
			for (KeyEventListener listener : listeners) {
				HandlerResult result = listener.handleKeyEvent(commandContext, event);
				if (!result.isSuccess()) {
					return result;
				}
			}
		} finally {
			event.setSource(oldSource);
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * All registered {@link KeyEventListener}s.
	 * 
	 * <p>
	 * Note: The result must only be used for inspecting current listener, it must not be used for
	 * dispatching events to the listeners, because this might result in concurrent modification
	 * exceptions.
	 * </p>
	 * 
	 * @return Direct reference to the internal listeners collection, or <code>null</code>, if no
	 *         {@link KeyEventListener}s have been registered.
	 */
	public Collection<KeyEventListener> inspectKeyListeners() {
		return getListenersDirect(KeyEventListener.class);
	}

	/**
	 * Notifies all
	 * {@link #addValueListener(ValueListener) registered value listeners} about
	 * a change in this field's application value.
	 * 
	 * @param oldValue
	 *     The value before the change.
	 * @param newValue
	 *     the value after the change.
	 */
	protected void fireValueChanged(Object oldValue, Object newValue) {
		List<ValueListener> currentListeners = getValueListeners();
		for (int cnt = currentListeners.size(), n = 0; n < cnt; n++) {
			currentListeners.get(n).valueChanged(selfAsField(), oldValue, newValue);
		}
	}

	@Override
	public boolean addValueVetoListener(ValueVetoListener listener) {
		return addListener(ValueVetoListener.class, listener);
	}
	
	@Override
	public boolean removeValueVetoListener(ValueVetoListener listener) {
		return removeListener(ValueVetoListener.class, listener);
	}
	
	/**
	 * This method checks whether some added {@link ValueVetoListener} has a veto.
	 * 
	 * @param newValue
	 *        the value which this field is about to set.
	 * @throws VetoException
	 *         if some {@link ValueVetoListener} throws some
	 */
	private void checkVeto(Object newValue) throws VetoException {
		List<ValueVetoListener> currentListeners = getListeners(ValueVetoListener.class);
		for (int cnt = currentListeners.size(), n = 0; n < cnt; n++) {
			currentListeners.get(n).checkVeto(this, newValue);
		}
	}

	/**
	 * Sets {@link #state} to the given state.
	 * 
	 * @param state
	 *        new value of {@link #state}
	 */
	private void setState(int state) {
		boolean changed = isChanged();
		this.state = state;
		checkChanged(changed);
	}

	/**
	 * Checks whether the changed state was changed and fires an
	 * {@link FormMember#IS_CHANGED_PROPERTY} event if necessary
	 * 
	 * @param wasChangedBefore
	 *        the changed state to check against.
	 */
	private void checkChanged(boolean wasChangedBefore) {
		if (fireChangedEvent && wasChangedBefore != isChanged()) {
			firePropertyChanged(FormMember.IS_CHANGED_PROPERTY, self(), Boolean.valueOf(wasChangedBefore),
				Boolean.valueOf(!wasChangedBefore));
    	}
	}

	@Override
	public boolean focus() {
		if (FocusHandling.focus(DefaultDisplayContext.getDisplayContext(), this)) {
			firePropertyChanged(Focusable.FOCUS_PROPERTY, selfAsField(), Boolean.FALSE, Boolean.TRUE);
		}
		return true;
	}

	/**
	 * The sender to use in events that originate from {@link FormField} aspects.
	 * 
	 * @see #self()
	 */
	protected FormField selfAsField() {
		return this;
	}

}
