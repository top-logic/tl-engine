/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.top_logic.basic.listener.EventType;

/**
 * A {@link FormContainer} is a collection of
 * {@link com.top_logic.layout.form.FormMember}s (either
 * {@link com.top_logic.layout.form.FormField}s or other
 * {@link FormContainer}s).
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface FormContainer extends CollapsibleFormMember {
	
	/**
	 * See {@link #addMember(FormMember)}
	 * 
	 * @see MemberChangedListener
	 */
	EventType<MemberChangedListener, FormContainer, FormMember> MEMBER_ADDED_PROPERTY =
		new EventType<>("memberAdded") {

			@Override
			public Bubble dispatch(MemberChangedListener listener, FormContainer sender, FormMember oldValue,
					FormMember newValue) {
				return listener.memberAdded(sender, newValue);
			}

		};
	
	/**
	 * See {@link #removeMember(FormMember)}
	 * 
	 * @see MemberChangedListener
	 */
	EventType<MemberChangedListener, FormContainer, FormMember> MEMBER_REMOVED_PROPERTY =
		new EventType<>("memberRemoved") {

			@Override
			public Bubble dispatch(MemberChangedListener listener, FormContainer sender, FormMember oldValue,
					FormMember newValue) {
				return listener.memberRemoved(sender, oldValue);
			}
		};

	/**
	 * Check, whether this container contains a member with the given name.
	 * 
	 * @param name
	 *     The name of the member in question. 
	 * @return
	 *     Whether this container contains a member with the given name.
	 */
	public boolean hasMember(String name);

	/**
	 * Return the member with the given name.
	 * 
	 * @throws NoSuchElementException,
	 *     if this container does not contain a member with the given
	 *     name.
	 */
	public FormMember getMember(String name) throws NoSuchElementException;

	/**
	 * Return the field with the given name.
	 * 
	 * @see #getContainer(String)
	 * @see #getMember(String)
	 * @throws ClassCastException
	 *         If the member with the given name is not a {@link FormField}.
	 * @throws NoSuchElementException,
	 *         if this container does not contain a member with the given name.
	 */
	public FormField getField(String aName) throws NoSuchElementException;
	
	/**
	 * Returns the child container with the given name.
	 * 
	 * This method works like {@link #getMember(String)}, but it makes sure
	 * that the object returned is a {@link FormContainer}. If this assertion
	 * fails, a {@link ClassCastException} is thrown.
	 * 
	 * @throws ClassCastException
	 *     If the member with the given name is not a
	 *     {@link FormContainer}.
	 * 
	 * @throws NoSuchElementException
	 *     If this container does not contain a member with the given
	 *     name.
	 */
	public FormContainer getContainer(String name) throws NoSuchElementException;
	
	/**
	 * Adds the given member to this container.
	 */
	public void addMember(FormMember member);

	/**
	 * Removes the given member from this container.
	 * 
	 * @return <code>true</code>, if the member was found and removed,
	 *     <code>false</code>, otherwise.
	 */
	public boolean removeMember(FormMember member);
	
	/**
	 * Return an {@link Iterator}<{@link FormMember}> of all members
	 * in this container.
	 */
	public Iterator<? extends FormMember> getMembers();
	
	/** 
	 * Returns the size of this FormContainer.
	 */
	public int size();
	
	/**
	 * {@link Iterator} of {@link FormField}s locally in this {@link FormContainer}.
	 */
	public Iterator<FormField> getFields();

	/**
	 * Returns an {@link Iterator} over all {@link FormField}s that are
	 * descendants of this group.
	 * 
	 * @see #getDescendants() for an explanation of "descendant".
	 */
	public Iterator<? extends FormField> getDescendantFields();
	
	/**
	 * Returns an {@link Iterator} over all {@link FormMember} that are
	 * descendants of this group.
	 * 
	 * <p>
	 * A {@link FormMember} is a descendant of a group, if it is a
	 * {@link #addMember(FormMember) member} of the group, or recursively a
	 * member of any descendant group.
	 * </p>
	 */
	public Iterator<? extends FormMember> getDescendants();
	
	/** 
	 * Get the first member with the given name.
	 * Note that the result will not be unique if member names
	 * aren't across different FormContainers!
	 * 
	 * @param aName the name
	 * @return the FormMember or <code>null</code> if none can be found.
	 */
	public FormMember getFirstMemberRecursively(String aName);
	
	/**
	 * Determine whether one or more descendant field is changed.
	 */
	@Override
	public boolean isChanged();

	/**
	 * Checks all FormFields of this form context against their constraints. In opposition to the
	 * {@link #checkAll()} method, this method has no side effects like changing error state of
	 * field. It just answers the question whether the fields passes their constraints at this time.
	 * 
	 * @return whether all fields in the context passes their constraints.
	 */
	default boolean checkAllConstraints() {
		for (Iterator<? extends FormField> it = getDescendantFields(); it.hasNext();) {
			FormField field = it.next();
			if (!field.checkConstraints())
				return false;
		}
		return true;
	}

	/**
	 * Check the whole form context during in response to the final submission of the form.
	 * 
	 * <p>
	 * Note: Don't use this method within a value listener of a form field because of side effects
	 * in the fields states. Use {@link #checkAllConstraints()} instead.
	 * </p>
	 * 
	 * @return Whether all fields in the context are valid.
	 */
	default boolean checkAll() {
		// Note: Two passes are required, because checking fields with all
		// dependencies may report errors even on fields, on which the check is
		// not invoked.

		// First pass: Check all fields that require a check.
		for (Iterator<? extends FormField> it = getDescendantFields(); it.hasNext();) {
			FormField field = it.next();

			if (field.isCheckRequired(true))
				field.checkWithAllDependencies();
		}

		// Second pass: Test, whether all fields are valid.
		for (Iterator<? extends FormField> it = getDescendantFields(); it.hasNext();) {
			FormField field = it.next();
			if (!field.isValid()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Sets the values of the descendant fields as default value, i.e. this method ensures that the
	 * descendant fields does not have changes.
	 */
	default void setFieldsToDefaultValues() {
		for (Iterator<? extends FormField> it = getDescendantFields(); it.hasNext();) {
			FormField field = it.next();
			if (field.isChanged()) {
				field.setDefaultValue(field.getValue());
			}
		}
	}

}
