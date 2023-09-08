/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import java.util.List;

import com.top_logic.base.services.simpleajax.XMLValueConstants;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.listener.EventType;
import com.top_logic.layout.KeyEventListener;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.form.model.ValueVetoListener;


/**
 * A {@link FormField} is the server-side representation of an input field on
 * the user interface.
 * 
 * A form field stores the value provided by the user, parses the input to a
 * value of the expected type and supervises checking this value against
 * additional {@link com.top_logic.layout.form.Constraint constraints}
 * associated with this form field.
 * 
 * @see #check() for the control flow of processing user input
 * @see #addConstraint(Constraint) for adding additional constraints to this
 *      form field.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface FormField extends FormMember {
	/**
	 * The {@link #getValue() value} that represents the empty input.
	 * 
	 * This constant only applies to field implementations for which the empty
	 * input cannot be represented by a member of the field's type. E.g. for an
	 * {@link com.top_logic.layout.form.model.IntField}, the empty input is not
	 * a number (the underlying type of the field). In contrast, for a
	 * {@link com.top_logic.layout.form.model.StringField}, the empty input
	 * (the empty string) is a legal value of the field's type. Therefore
	 * {@link #EMPTY_INPUT} does not apply to
	 * {@link com.top_logic.layout.form.model.StringField}, but to
	 * {@link com.top_logic.layout.form.model.IntField}.
	 * 
	 * @see com.top_logic.layout.form.model.StringField#EMPTY_STRING_VALUE
	 */
	public static final Object EMPTY_INPUT = null;

	/**
	 * Type of the <code>mandatory</code> property.
	 * 
	 * The value transmitted to the client in response to a change of this property is a
	 * {@link com.top_logic.base.services.simpleajax.XMLValueConstants#BOOLEAN_ELEMENT}.
	 * 
	 * @see #isMandatory()
	 * @see MandatoryChangedListener
	 */
	EventType<MandatoryChangedListener, FormField, Boolean> MANDATORY_PROPERTY =
		new EventType<>("mandatory") {

			@Override
			public Bubble dispatch(MandatoryChangedListener listener, FormField sender, Boolean oldValue,
					Boolean newValue) {
				return listener.handleMandatoryChanged(sender, oldValue, newValue);
			}

		};
	
	/**
	 * Type of the <code>hasError</code> property.
	 * 
	 * The value transmitted to the client in response to a change of this property is a
	 * {@link com.top_logic.base.services.simpleajax.XMLValueConstants#BOOLEAN_ELEMENT}.
	 * 
	 * @see #hasError()
	 * @see HasErrorChanged
	 */
	EventType<HasErrorChanged, FormField, Boolean> HAS_ERROR_PROPERTY =
		new EventType<>("hasError") {

			@Override
			public Bubble dispatch(HasErrorChanged listener, FormField sender, Boolean oldValue, Boolean newValue) {
				return listener.hasErrorChanged(sender, oldValue, newValue);
			}

		};
	
	/**
	 * Type of the <code>error</code> property.
	 * 
	 * The value transmitted to the client in response to a change of the this property is a
	 * {@link com.top_logic.base.services.simpleajax.XMLValueConstants#STRING_ELEMENT}.
	 * 
	 * @see #getError()
	 * @see ErrorChangedListener
	 */
	EventType<ErrorChangedListener, FormField, String> ERROR_PROPERTY =
		new EventType<>("error") {

			@Override
			public Bubble dispatch(ErrorChangedListener listener, FormField sender, String oldValue, String newValue) {
				return listener.handleErrorChanged(sender, oldValue, newValue);
			}

		};

	/**
	 * Type of the <code>hasWarnings</code> property.
	 * 
	 * The value transmitted to the client in response to a change of this property is a
	 * {@link com.top_logic.base.services.simpleajax.XMLValueConstants#BOOLEAN_ELEMENT}.
	 * 
	 * @see #hasError()
	 * @see HasWarningsChangedListener
	 */
	EventType<HasWarningsChangedListener, FormField, Boolean> HAS_WARNINGS_PROPERTY =
		new EventType<>("hasWarnings") {

			@Override
			public Bubble dispatch(HasWarningsChangedListener listener, FormField sender, Boolean oldValue,
					Boolean newValue) {
				return listener.hasWarningsChanged(sender, oldValue, newValue);
			}

		};
    
    /**
	 * Type of the <code>warnings</code> property.
	 * 
	 * The value transmitted to the client in response to a change of the this property is a
	 * {@link com.top_logic.base.services.simpleajax.XMLValueConstants#STRING_ELEMENT}.
	 * 
	 * @see #getWarnings()
	 * @see WarningsChangedListener
	 */
	EventType<WarningsChangedListener, FormField, List<String>> WARNINGS_PROPERTY =
		new EventType<>("warnings") {

			@Override
			public Bubble dispatch(WarningsChangedListener listener, FormField sender, List<String> oldValue,
					List<String> newValue) {
				return listener.warningsChanged(sender, oldValue, newValue);
			}

		};
	
	/**
	 * Type of the <code>value</code> property.
	 * 
	 * The value transmitted to the client in response to a change of this property is an
	 * {@link com.top_logic.base.services.simpleajax.XMLValueConstants#ARRAY_ELEMENT} of
	 * {@link com.top_logic.base.services.simpleajax.XMLValueConstants#STRING_ELEMENT} objects.
	 * 
	 * A property event of this type does not carry an old value. Instead, the old value is
	 * {@link com.top_logic.base.services.simpleajax.XMLValueConstants#NULL_ELEMENT}.
	 * 
	 * <span style="text-decoration: line-through;"> The value transmitted to the client in response
	 * to a change of this property is implementation dependent. A
	 * {@link com.top_logic.layout.form.model.StringField} would e.g. send a
	 * {@link com.top_logic.base.services.simpleajax.XMLValueConstants#STRING_ELEMENT}, while a
	 * {@link com.top_logic.layout.form.model.BooleanField} would send a
	 * {@link com.top_logic.base.services.simpleajax.XMLValueConstants#BOOLEAN_ELEMENT}. </span>
	 * 
	 * @see #getValue()
	 * @see FormField#getRawValue()
	 * @see RawValueListener
	 */
	EventType<RawValueListener, FormField, Object> VALUE_PROPERTY =
		new EventType<>("value") {

			@Override
			public Bubble dispatch(RawValueListener listener, FormField sender, Object oldValue, Object newValue) {
				return listener.handleRawValueChanged(sender, oldValue, newValue);
			}

		};
	
	/**
	 * Type of the <code>blocked</code> property.
	 * 
	 * @see #isBlocked()
	 * @see BlockedStateChangedListener
	 */
	EventType<BlockedStateChangedListener, FormField, Boolean> BLOCKED_PROPERTY =
		new EventType<>("blocked") {

			@Override
			public Bubble dispatch(BlockedStateChangedListener listener, FormField sender, Boolean oldValue,
					Boolean newValue) {
				return listener.handleIsBlockedChanged(sender, oldValue, newValue);
			}

		};

	/**
	 * {@link Filter} that accepts {@link FormField}s with {@link #hasError() errors}.
	 */
	public static final Filter<FormField> HAS_ERROR = new Filter<>() {
		@Override
		public boolean accept(FormField anObject) {
			return anObject.hasError();
		}
	};

	/**
	 * {@link Filter} that accepts {@link FormField}s with {@link #hasWarnings() warnings}.
	 */
	public static final Filter<FormField> HAS_WARNINGS = new Filter<>() {
	    @Override
		public boolean accept(FormField anObject) {
	        return anObject.hasWarnings();
	    }
	};

	/**
	 * {@link Filter} that accepts {@link FormField}s.
	 */
	public static final Filter<Object> IS_FIELD = new Filter<>() {
		@Override
		public boolean accept(Object anObject) {
			return anObject instanceof FormField;
		}
	};

    /**
	 * If a field is mandatory, the user must input a value for that field to
	 * make a valid input.
	 * 
	 * A view that renders a mandatory field should provide a hint to the user
	 * that he must enter a value for that field (e.g. display the field in a
	 * certain color).
	 * 
	 * The decision, whether a value for a mandatory field is accepted as input
	 * for that field, is up to the {@link Constraint constraints} associated
	 * with that field (e.g. if a single whitespace character is a valid input
	 * for a mandatory field). This means that the <code>mandatory</code>
	 * property does not have any implications on the semantics of a form field.
	 * All checks are up to the {@link Constraint constraints} associated with
	 * the field.
	 * 
	 * A mandatory field must have at least one associated constraint.
	 * 
	 * @see #addConstraint(Constraint)
	 */
	public boolean isMandatory();
	
	
    /**
	 * Decides, whether this field is "frozen".
	 * 
	 * <p>
	 * A view of a frozen field prevents editing the field's value. By freezing
	 * a field, one establishes an access restriction to the field's value (in
	 * absence of the right to edit a field).
	 * </p>
	 * 
	 * <p>
	 * Note: A frozen field is also {@link #isImmutable() immutable}.
	 * </p>
	 */
    public boolean isFrozen();
    
    /**
     * Freezes this field.
     * 
     * @see #isFrozen()
     */
    public void setFrozen(boolean frozen);
    
	
    /**
	 * Decides, whether this field is "blocked".
	 * 
	 * <p>
	 * A view of a blocked field does not display the field's value, but a
	 * view-dependent replacement that provides the user with feedback that he
	 * is not allowed to see the value. By blocking a field, one establishes an
	 * access restriction for a field's value (in absence of the right to view a
	 * field).
	 * </p>
	 * 
	 * <p>
	 * Note: A blocked field is also {@link #isFrozen() frozen} and
	 * {@link #isImmutable() immutable}.
	 * </p>
	 */
    public boolean isBlocked();
    
    /**
	 * Blocks this field.
	 * 
	 * @see #isBlocked()
	 * @see FormField#BLOCKED_PROPERTY
	 */
    public void setBlocked(boolean blocked);
	
    /**
	 * Update this form field with a new input value from the user interface
	 * view.
	 * 
	 * @param newRawValue
	 *     The client-side value of this {@link FormField}. The value
	 *     can be any JavaScript object that can be marshaled by the
	 *     protocol defined in {@link XMLValueConstants}.
	 * @throws VetoException
	 *         if some {@link ValueVetoListener} has a veto for update this
	 *         field.
	 */
	public void update(Object newRawValue) throws VetoException;

    /**
	 * Checks, whether this {@link FormField} reacts on updates sent from the
	 * client formula.
	 * 
	 * A {@link FormField} listens for updates, if it is
	 * {@link #isActive() active} and all of its ancestors are also
	 * {@link #isActive() active}.
	 */
    public boolean listensForUpdate();

    /**
	 * Checks, whether this form field has user input (provided by a call to
	 * {@link #update(Object)}) that is not yet processed. During the final
	 * submission of a form, a form field requires a check, even if it is
	 * untouched by the user and has still its default value. This is the case,
	 * because even the default value might be in conflict with some constraints
	 * on the field. Errors generated by untouched fields must only be reported
	 * during the final submission of the form.
	 * 
	 * The processing of the input is triggered by a call to {@link #check()}.
	 * 
	 * @param isFinalSubmit
	 *     Whether this test is performed during the final submission of the
	 *     form.
	 */
    public boolean isCheckRequired(boolean isFinalSubmit);

    /**
     * Checks, whether the current {@link #getRawValue() raw value} is a valid input for
     * this field.
     * 
     * During the check, the input value provided in the last call to
     * {@link #update(Object)} is parsed into a value suitable for the concrete form field
     * type (e.g. integer for an integer fields) and stored as new {@link #getValue() value}
     * of this field. Afterwards, all {@link Constraint constraints} associated with this
     * field (see {@link #addConstraint(Constraint)} and
     * {@link #addWarningConstraint(Constraint)} are checked against the new value.
     * 
     * If parsing succeeds, this field {@link #hasValue() has a value} that can be retrieved
     * with {@link #getValue()}. If both, parsing and constraint checking, succeeds for
     * error constaints, this field {@link #isValid() is valid}. If either parsing or error
     * constraint checking failed, the field {@link #hasError() has errors}. If the check
     * fails for warning constraints, the field {@link #hasWarnings()} has warnings.
     * If no constraint failed, but some did abort processing (see
     * {@link Constraint#check(Object)}) due to missing prerequisites, this field is
     * neither valid, nor does it have errors.
     * 
     * <p>
     * Note: This method must NOT be called within a {@link ValueListener} of this field
     * because of the side effects of this method. Use {@link #checkConstraints()} or
     * {@link #checkConstraints(Object)} instead.
     * </p>
     */
    public void check();

	/**
	 * Checks this field in response to a change in a dependent field.
	 */
	@FrameworkInternal
	public void checkDependency();

    /**
     * Checks the fields current value against the FormFields constraints. In opposition to
     * the {@link #check()} method, this method just applies the constraints of the field
     * without any side effects. In particular, this will not set the field in an error state
     * if a constraint fails.
     * 
     * @return TODO BHU true / false ?
     */
    public boolean checkConstraints();

    /**
     * Checks the given value against the FormFields constraints and checks, whether the
     * given value would be accepted by this field, regardless witch value is set at this
     * time. In opposition to the {@link #check()} method, this method just applies the
     * constraints of the field without any side effects.
     */
    public boolean checkConstraints(Object value);

    /**
	 * <code>true</code>, if this form field detected errors during
	 *         parsing or constraint checking.
	 *         
	 * @see #check()
	 */
    public boolean hasError();
    
    /**
	 * The internationalized error message that describes the problem
	 *         that occurred during parsing or constraint checking.
     *         
     * @throws IllegalStateException if {@link #hasError()} is false.
	 *         
	 * @see #check()
	 */
    public String getError();

    /**
     * Externally set an error message, which should be propagated to the user agent like
     * errors detected during internal validation.
     * 
     * <b>Note:</b> This method <b>must not</b> be called from within the
     * {@link Constraint#check(Object)} method of {@link Constraint constraints}. The way
     * to report an error from a constraint is to throw a {@link CheckException}. This
     * method is reserved for <i>external</i> validation purposes (at the time the form has
     * been submitted and some additional semantic consistency checks must be performed,
     * which cannot be expressed with regular {@link Constraint constraints}). See the
     * update process of an attributed object in the element module for an example.
     * 
     * This method <b>must only</b> be called in a state, where this field contains a valid
     * value (where {@link #isValid()} returns <code>true</code>. A call to this method
     * invalidates this value as an "act of nature beyond control".
     * 
     * After a call to this method, {@link #hasError()} returns <code>true</code> and
     * {@link #getError()} reports the error message passed to this method.
     * 
     * @param message
     *            The internalized error message that should be propagated to the user
     *            agent.
     */
    public void setError(String message);
    
    /**
	 * Removes a currently set error on this field.
	 *
	 * @see #getError()
	 * @see #setError(String)
	 */
	public void clearError();

	/**
	 * <code>true</code>, if this form field detected warnings during parsing or constraint
	 *         checking.
	 * 
	 * @see #check()
	 * @see FormField#HAS_WARNINGS_PROPERTY
	 */
    public boolean hasWarnings();
    
    /**
     * The list of internationalized warning message that describes the problem
     *         that occurred during parsing or constraint checking.
     *         
     * @throws IllegalStateException if {@link #hasWarnings()} is false.
     *         
     * @see #check()
     */
    public List<String>/*<String>*/ getWarnings();

    /**
     * Externally add aall warning messages, which should be propagated to the user
     * agent like warning detected during internal validation.
     * 
     * <b>Note:</b> This method <b>must not</b> be called from within the
     * {@link Constraint#check(Object)} method of {@link Constraint constraints}.
     * The way to report a warning from a constraint is to throw a
     * {@link CheckException} from a constraint that was added through 
     * {@link #addWarningConstraint(Constraint)}.
     * This method is reserved for <i>external</i>
     * validation purposes (at the time the form has been submitted and some
     * additional semantic consistency checks must be performed, which cannot be
     * expressed with regular {@link Constraint constraints}). See the update
     * process of an attributed object in the element module for an example.
     * 
     * This method <b>must only</b> be called in a state, where this field
     * contains a valid value (where {@link #isValid()} returns
     * <code>true</code>. A call to this method invalidates this value as an
     * "act of nature beyond control".
     * 
     * After a call to this method, {@link #hasWarnings()} returns
     * <code>true</code> and {@link #getWarnings()} reports the error message
     * passed to this method.
     * 
     * @param messages
     *            The internalized warning messages that should be propagated to
     *            the user agent.
     */
    public void setWarnings(List<String> messages);
    
    /**
     * <code>true</code>, if parsing and constraint checking succeeded.
     * 
     * @see #check()
     */
    public boolean isValid();
    
    /**
	 * Check, whether this field has a value that can be retrieved with
	 * {@link #getValue()}.
	 * 
	 * A field is considered to have a value, if
	 * <ul>
	 * <li>the user has entered something and parsing was successful (no
	 * matter whether the {@link #addConstraint(Constraint) constraints}
	 * associated with this field have been successfully checked).</li>
	 * 
	 * <li>or if this field has still its
	 * {@link #getDefaultValue() initial value}. Depending on the concrete
	 * implementation, this might even be the <code>null</code> value, which
	 * represents the "empty input".</li>
	 * </ul>
	 * 
	 * @see #check()
	 * 
	 * @return <code>true</code>, if this field has a value that can be
	 *         retrieved with {@link #getValue()}.
	 */
    public boolean hasValue();
    
	/**
	 * <p>
	 * The value (in contrast to the {@link #getRawValue()}) of a form field is
	 * a typed value that is computed by parsing the string input sent from the
	 * user interface (in {@link #update(Object)}).
	 * </p>
	 * 
	 * <p>
	 * The concrete type of the value is up to the concrete implementation of
	 * this interface. An {@link com.top_logic.layout.form.model.IntField} would
	 * e.g. parse the input to objects of type {@link Integer}.
	 * </p>
	 * 
	 * <p><b>Note:</b>
	 * The value returned by this method must not be modified. Otherwise
	 * unexpected behavior occur
	 * </p>
	 * 
	 * @return The parsed value of this field.
	 */
    public Object getValue();

    /**
	 * Report, whether the current {@link #getValue() value} of this field
	 * differs from its {@link #getDefaultValue() default value}.
	 * 
	 * <p>
	 * Note: A change may compensate a former change leaving the field in sum
	 * unchanged. A field with initial value of <code>1</code> and a change of
	 * this value to <code>2</code> and a further change back to
	 * <code>1</code> will leave the field unchanged.
	 * </p>
	 * 
	 * @return <code>true</code>, if this field's value has been changed from
	 *         its initial value (either by the user or by a call to
	 *         {@link #setValue(Object)}.
	 */
	@Override
	public boolean isChanged();

    /**
	 * Programatically set a new value into this form field.
	 * 
	 * <p>
	 * Changes are propagated to the GUI (if the {@link #VALUE_PROPERTY property event} is
	 * subscribed by some views of this field and a {@link RawValueListener} is currently
	 * {@link com.top_logic.layout.form.model.FormContext#addListener(EventType, com.top_logic.basic.listener.PropertyListener)
	 * registered} at the top-level {@link com.top_logic.layout.form.model.FormContext}.
	 * </p>
	 * 
	 * <p>
	 * The given value must be changed after delivering to the {@link FormField}. Otherwise no
	 * events will be fired and unexpected behavior occur.
	 * </p>
	 * 
	 * <p>
	 * <b>Note:</b> This method <b>must not</b> be called from the constructors of any implementing
	 * class. The reason is that the given value is checked for compatibility with this field. This
	 * checking in general is only possible after the initialization of a field is completed.
	 * </p>
	 * 
	 * @see #getValue()
	 */
    public void setValue(Object value);

    /**
	 * Sets a new default value, which is the {@link #getValue() value} of this
	 * field after a call to {@link #reset()}.
	 * 
	 * @see #getDefaultValue()
	 * @see #isChanged()
	 */
    public void setDefaultValue(Object defaultValue);

    /**
	 * Returns the default value of this field.
	 * 
	 * <p>
	 * The default value is either the value, with which this field was created,
	 * and which was initially displayed to the user, or the last value passed
	 * to {@link #setDefaultValue(Object)}. After a call to {@link #reset()},
	 * the {@link #getValue() value} of a field equals its default value.
	 * </p>
	 * 
	 * <p>
	 * The {@link #getDefaultValue()} is the basis of computing the
	 * {@link #isChanged()} property.
	 * </p>
	 * 
	 * @see #isChanged()
	 */
    public Object getDefaultValue();

    /**
	 * Ensures that this field gets the given <code>defaultValue</code> as
	 * {@link #getDefaultValue() default value} and as {@link #getValue() value}.
	 * 
	 * @param defaultValue
	 *            The new default value which shall be also the value.
	 */
	public void initializeField(Object defaultValue);

    /**
	 * The raw value is the (array of) string(s) that was entered on the user
	 * interface view of this form field.
	 * 
	 * @return The current raw value stored in this form field.
	 */
    public Object getRawValue();

    /**
     * Add a new {@link Constraint constraint} to this form field. 
     * 
     * @param constraint
     *     The constraint to add.
     * @return
     *     This form field. Useful for call chaining.
     */
    public FormField addConstraint(Constraint constraint);

    /**
	 * Remove the given constraint from this form field.
	 * 
	 * @param constraint
	 *     The constraint to remove
	 * @return Whether the given constraint was successfully removed (because it
	 *     was assigned to this field before).
	 */
    public boolean removeConstraint(Constraint constraint);

    /**
	 * Report all {@link Constraint}s currently associated with this field.
	 * 
	 * <p>
	 * The result cannot/may not be modified. 
	 * </p>
	 */
	public List<Constraint> getConstraints();

	/**
	 * Report all warning {@link Constraint}s currently associated with this field.
	 * 
	 * <p>
	 * The result cannot/may not be modified.
	 * </p>
	 */
	public List<Constraint> getWarningConstraints();

    /**
     * Add a new {@link Constraint constraint} to this form field. 
     * 
     * @param constraint
     *     The constraint to add.
     * @return
     *     This form field. Useful for call chaining.
     */
    public FormField addWarningConstraint(Constraint constraint);

    /**
     * Remove the given constraint from this form field.
     * 
     * @param constraint
     *     The constraint to remove
     * @return Whether the given constraint was successfully removed (because it
     *     was assigned to this field before).
     */
    public boolean removeWarningConstraint(Constraint constraint);

    /**
	 * Annotates a valid value as an example input for this field.
	 * 
	 * This value is exclusively used in error messages that are issued during
	 * the parse phase of this field. During the parse phase, this field is
	 * deriving a concrete Java object from the entered raw string value. If
	 * this parsing fails, this field uses the string representation of the
	 * {@link #setExampleValue(Object) example value} to give the user an idea
	 * of an input that would be accepted by this field.
	 * 
	 * This value is not used in error messages of additional {@link Constraint}s
	 * that are {@link #addConstraint(Constraint) added} to this field. It is
	 * the responsibility of those {@link Constraint}s to generate a useful
	 * error message, if the already parsed value does not satisfy the
	 * {@link Constraint}.
	 * 
	 * TODO BHU: Decide about moving this property to
	 * {@link com.top_logic.layout.form.model.ComplexField}, since all other
	 * fields should be able to generate a useful error message on their own.
	 * 
	 * @param exampleValue
	 *     An object would be a legal {@link #getValue() value} of this field. 
	 */
	public void setExampleValue(Object exampleValue);
    
	/**
	 * @see #setExampleValue(Object)
	 */
	public Object getExampleValue();
	
    /**
	 * Add a listener to this form field that is notified, if the value of this
	 * field changes.
	 * 
	 * @param listener
	 *     The listener to add.
	 * @return
	 *     <code>true</code>, if the listener was added, <code>false</code>, if 
	 *     the listener was already attached to this field. 
	 */
    public boolean addValueListener(ValueListener listener);

    /**
	 * Remove a listener previously attached to this field with
	 * {@link #addValueListener(ValueListener)}.
	 * 
	 * @param listener
	 *     The listener to remove.
	 * @return
	 *     <code>true</code>, if the listener was removed successfully (if it was 
	 *     previously attached to this field), <code>false</code> otherwise.
	 */
    public boolean removeValueListener(ValueListener listener);

	/**
	 * Determines whether this {@link FormField} has currently {@link ValueListener} attached
	 */
	public boolean hasValueListeners();

	/**
	 * Returns the {@link ValueListener} attached to this {@link FormField}.
	 * 
	 * <p>
	 * The returned list must not be modified.
	 * </p>
	 */
	@FrameworkInternal
	public List<ValueListener> getValueListeners();

	/**
	 * Add a {@link KeyEventListener} to this form field.
	 * 
	 * @param listener
	 *        The listener to add.
	 * @return <code>true</code>, if the listener was added, <code>false</code>, if the listener was
	 *         already attached to this field.
	 */
	public boolean addKeyListener(KeyEventListener listener);

	/**
	 * Remove a {@link KeyEventListener} from this form field.
	 * 
	 * @param listener
	 *        The listener to remove.
	 * @return <code>true</code>, if the listener was removed successfully (if it was previously
	 *         attached to this field), <code>false</code> otherwise.
	 */
	public boolean removeKeyListener(KeyEventListener listener);

	/**
	 * Add a listener to this form field that is notified, if the value of this field is about to
	 * change. The listener has the possibility to give a veto for changing the value.
	 * 
	 * @param listener
	 *            The listener to add.
	 * @return <code>true</code>, if the listener was added, <code>false</code>, if the listener was
	 *         already attached to this field.
	 */
    public boolean addValueVetoListener(ValueVetoListener listener);

    /**
     * Remove a listener previously attached to this field with
     * {@link #addValueVetoListener(ValueVetoListener)}.
     * 
     * @param listener
     *     The listener to remove.
     * @return
     *     <code>true</code>, if the listener was removed successfully (if it was 
     *     previously attached to this field), <code>false</code> otherwise.
     */
    public boolean removeValueVetoListener(ValueVetoListener listener);


	/**
	 * Checks the given field and all other fields that depend on the value of
	 * the given field (because the value of the given field has changed).
	 * 
	 * <p>
	 * Another field than the given field is also {@link #check() checked}, if
	 * such field has a {@link Constraint} that reports the given field as one
	 * of its dependencies. If the decision of a {@link Constraint} of another
	 * field depends on the value of the given field, this other field must be
	 * re-checked, if the value of the given field changes. This is the case,
	 * because the decision, whether the other field is valid or not may change
	 * with the value of the given field.
	 * </p>
	 */
	public void checkWithAllDependencies();

	/**
	 * Internally adds a dependency.
	 * 
	 * @return Whether this dependency was newly added.
	 */
	@FrameworkInternal
	boolean addDependant(FormField dependant);

	/**
	 * Internally removes a dependency.
	 * 
	 * @return Whether this dependency existed before the call.
	 */
	@FrameworkInternal
	boolean removeDependant(FormField dependant);
}
