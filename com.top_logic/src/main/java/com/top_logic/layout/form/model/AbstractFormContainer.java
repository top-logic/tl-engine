/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.DescendantDFSIterator;
import com.top_logic.basic.col.FilterIterator;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.col.TransformIterator;
import com.top_logic.basic.config.annotation.Inspectable;
import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.PropertyListener;
import com.top_logic.basic.shared.collection.map.MappedIterator;
import com.top_logic.layout.ResourceView;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;

/**
 * Common base class for all implementations of {@link FormContainer}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractFormContainer extends AbstractFormMember implements FormContainer {

	/**
	 * Whether this form container is collapsed. 
	 * 
	 * @see FormContainer#isCollapsed()
	 */
	@Inspectable
	private boolean collapsed;
	
	/** The number of members of this {@link FormContainer} which are changed. */
	private int changedMembers = 0;

	/**
	 * Creates a new field with the given name.
	 * 
	 * @param aLabelRessource provides labels for its children
	 */
	public AbstractFormContainer(String name, ResourceView aLabelRessource) {
		super(name);
		setResources(aLabelRessource);
	}
	
	@Override
	public void setCollapsed(boolean value) {
		if (value == this.collapsed) return;
		
		this.collapsed = value;

		firePropertyChanged(COLLAPSED_PROPERTY, self(), Boolean.valueOf(!value), Boolean.valueOf(value));
	}
	
	@Override
	public boolean isCollapsed() {
		return this.collapsed;
	}

	@Override
	public void setMandatory(boolean mandatory) {
	    for (Iterator members = getMembers(); members.hasNext(); ) {
	    	((FormMember) members.next()).setMandatory(mandatory);
	    }
	}
	
	/*package protected*/ @Override
	void updateDisplayMode() {
		// Update this container's display mode.
		super.updateDisplayMode();
		
		// Notify all children about the parent's display mode change.
	    for (Iterator members = getMembers(); members.hasNext(); ) {
	    	((AbstractFormMember) members.next()).updateDisplayMode();
	    }
	}

	/**
	 * Find a group with the given name.
	 * 
	 * @param aName
	 *            Name of the group to find.
	 */
	@Override
	public FormContainer getContainer(String aName) {
	    return (FormContainer) getMember(aName);
	}

	/**
	 * Find a field with the given name.
	 * 
	 * @param aName
	 *            Name of the field to find.
	 */
	@Override
	public FormField getField(String aName) throws NoSuchElementException {
	    return (FormField) getMember(aName);
	}

	/**
	 * Check, whether adding the given form member to this group would create a
	 * cyclic membership relation. The membership relation is cyclic, if the a
	 * member is added to a group, and the member itself is already an ancestor
	 * of the group.
	 * 
	 * @param aMember
	 *     The potential new member to check.
	 * @return <code>true</code>, if adding the given member to this group
	 *     would create a cyclic membership relation.
	 */
	protected boolean createsCyclicMembership(FormMember aMember) {
		for (FormMember ancestor = this; ancestor != null; ancestor = ancestor.getParent()) {
			if (aMember == ancestor) 
				return true;
		}
		return false;
	}

	/**
	 * Adds all form {@link FormMember}s the given array to this group. 
	 * 
	 * Note: This method is declared <code>final</code>, because it is called
	 * from within a constructor.
	 */
	public final void addMembers(FormMember[] members) {
		for (int cnt = members.length, n = 0; n < cnt; n++)  {
			FormMember member = members[n];
			this.addMember(member);
		}
	}

	/**
	 * Adds all {@link FormMember}s in the given collection to this group.
	 * 
	 * Note: This method is declared <code>final</code>, because it is called
	 * from within a constructor.
	 */
	public final void addMembers(Collection<? extends FormMember> members) {
		for (Iterator<? extends FormMember> it = members.iterator(); it.hasNext(); )  {
			FormMember member = it.next();
			this.addMember(member);
		}
	}

	@Override
	public boolean hasMember(String aName) {
		return internalGetMember(aName) != null;
	}
	
	/**
	 * Subclasses of the this {@link AbstractFormContainer} do the actual adding action in
	 * {@link #internalAddMember(FormMember)}.
	 */
	@Override
	public final void addMember(FormMember member) {
		boolean memberIsChanged = member.isChanged();
		internalAddMember(member);
		if (memberIsChanged && !isTransient()) {
			increaseChangedMembers();
		}
	}

    /**
	 * This method adds the given {@link FormMember} to this {@link FormContainer}
	 * 
	 * @param member
	 *            the member to add.
	 * @see FormContainer#addMember(FormMember)
	 */
	protected abstract void internalAddMember(FormMember member);
    
    /**
	 * Subclasses of {@link AbstractFormContainer} do the actual removing action in
	 * {@link #internalRemoveMember(FormMember)}.
	 */
	@Override
	public final boolean removeMember(FormMember member) {
		boolean memberIsChanged = member.isChanged();
		boolean succeded = internalRemoveMember(member);
		if (succeded && memberIsChanged && !isTransient()) {
			decreaseChangedMembers();
		}
		return succeded;
	}

	/**
	 * This method removes the given {@link FormMember} from this {@link FormContainer}
	 * 
	 * @param member
	 *            the member to remove.
	 * @return whether the remove action was successful
	 * @see FormContainer#removeMember(FormMember)
	 */
	protected abstract boolean internalRemoveMember(FormMember member);

	/**
	 * Delegates to {@link #internalGetMember} and throws
	 * {@link NoSuchElementException}, if the requested member is not found.
	 * 
	 * @see FormContainer#getMember(String)
	 */
	@Override
	public FormMember getMember(String aName) throws NoSuchElementException {
		FormMember formMember = internalGetMember(aName);
		if (formMember == null) {
			throw new NoSuchElementException(
				"There is no member '" + aName + "' in group '" + getQualifiedName() + "'. Members: "
					+ joinMemberNames());
		}
		return formMember;
	}

	private String joinMemberNames() {
		List<String> memberNames = new ArrayList<>();
		for (FormMember member : CollectionUtil.toList(getMembers())) {
			memberNames.add(member.getName());
		}
		return "'" + StringServices.join(memberNames, "', '") + "'";
	}

	@Override
	public final Iterator<FormField> getFields() {
		return getFields(this);
	}

	@Override
	public final Iterator<? extends FormField> getDescendantFields() {
		return new FilterIterator(getDescendants(), FormField.IS_FIELD);
	}

	@Override
	public final Iterator<? extends FormMember> getDescendants() {
		return new DescendantDFSIterator(FormMemberTreeView.INSTANCE, this);
	}
	
	@Override
	public void clearConstraints() {
		for (Iterator<? extends FormMember> it = getMembers(); it.hasNext(); ) {
			it.next().clearConstraints();
		}
	}
	
	@Override
	public void reset() {
		for (Iterator<? extends FormMember> it = getMembers(); it.hasNext(); ) {
			it.next().reset();
		}
	}
	
	/**
	 * Returns this container's {@link FormMember member} with the given name,
	 * or <code>null</code>, if this container has no member with that name.
	 * 
	 * @param name
	 *     The (unqualified) name of the member to search.
	 * @return The member of this container with the given name, or
	 *     <code>null</code>, if no such member exists.
	 */
	protected abstract FormMember internalGetMember(String name);
	
	@Override
	public FormMember getFirstMemberRecursively(String aName) {
		Iterator<? extends FormMember> members = this.getMembers();
		while(members.hasNext()){
			FormMember aMember = members.next();
			if(aName.equals(aMember.getName())){
				return aMember;
			}
			if(aMember instanceof FormContainer){
				aMember = ((FormContainer) aMember).getFirstMemberRecursively(aName);
				if(aMember!=null){
					return aMember;
				}
			}
		}
		return null;

	}
	
	/**
	 * This method returns whether one of the descendant which are {@link FormField}s has an error.
	 * 
	 * @return <code>true</code> iff at least one of the members is a {@link FormField} which has
	 *         an error.
	 * @see FormField#hasError()
	 */
	public final boolean hasError() {
		return FilterUtil.hasMatch(getDescendantFields(), FormField.HAS_ERROR);
	}
	
	public final boolean hasWarnings() {
        return FilterUtil.hasMatch(getDescendantFields(), FormField.HAS_WARNINGS);
    }
	
	/**
	 * Returns whether this container has changed members.
	 * 
	 * @see FormContainer#isChanged()
	 */
	@Override
	public final boolean isChanged() {
		if (isTransient()) {
			return false;
		}
		return changedMembersCount() != 0;
	}

	/**
	 * Increases the number of changed members, and fires an &quot;is
	 * changed&quot; property changed event if necessary.
	 */
	protected final void increaseChangedMembers() {
		changedMembers++;
		if (changedMembers == 1) {
			firePropertyChanged(FormField.IS_CHANGED_PROPERTY, self(), Boolean.FALSE, Boolean.TRUE);
		}
	}
	
	/**
	 * Decreases the number of changed members, and fires an &quot;is
	 * changed&quot; property changed event if necessary.
	 */
	protected final void decreaseChangedMembers() {
		assert changedMembers > 0 : "Some field changed its state to not changed, whereas it not changed its state to changed!";
		changedMembers--;
		if (changedMembers == 0) {
			firePropertyChanged(FormField.IS_CHANGED_PROPERTY, self(), Boolean.TRUE, Boolean.FALSE);
		}
	}
	
	/** 
	 * returns the number of changed members of this form container.
	 */
	protected final int changedMembersCount() {
		return changedMembers;
	}
	
	/**
	 * If the given event is an {@link FormMember#IS_CHANGED_PROPERTY} and the sender is not
	 * <code>this</code> the number of changed descendants will be updated and a property changed
	 * event will be fired if necessary.
	 * 
	 * @see AbstractFormMember#firePropertyChangedBubleUp(EventType, Object, Object, Object)
	 */
	@Override
	protected <T extends PropertyListener, S, V> void firePropertyChangedBubleUp(
			EventType<T, ? super S, V> propertyType, S sender, V oldValue, V newValue) {
		super.firePropertyChangedBubleUp(propertyType, sender, oldValue, newValue);
		if (propertyType == FormMember.IS_CHANGED_PROPERTY) {
			if (sender.equals(internalGetMember(((FormMember) sender).getName()))) {
				if (!isTransient()) {
					if (((Boolean) newValue).booleanValue()) {
						increaseChangedMembers();
					} else {
						decreaseChangedMembers();
					}
				}
			}
		} else if (propertyType == FormMember.VISIBILITY_REQUEST) {
			if (sender != this) {
				// Only descendants (not the container itself).
				setCollapsed(false);
			}
		}
	}

	/**
	 * Returns a {@link Collection} containing all changed {@link FormMember}s.
	 * If some member is itself an {@link AbstractFormContainer}, it will not be
	 * added but all in the collection returned by {@link #getChangedMembers()}
	 * called on it.
	 */
	public Collection<FormMember> getChangedMembers() {
		Set<FormMember> members = new HashSet<>();
		Iterator<? extends FormMember> iterator = getMembers();
		while (iterator.hasNext()) {
			FormMember currentMember = iterator.next();
			if (currentMember instanceof AbstractFormContainer) {
				members.addAll(((AbstractFormContainer) currentMember).getChangedMembers());
			} else {
				if (currentMember.isChanged()) {
					members.add(currentMember);
				}
			}
		}
		return members;
		
	}

	/**
	 * Iteration through the {@link #getMembers()} with dynamic cast.
	 */
	public static <T> Iterable<T> getMembersTyped(final FormContainer formGroup, final Class<T> expectedType) {
		return new Iterable<>() {
			@Override
			public Iterator<T> iterator() {
				return new MappedIterator<Object, T>(formGroup.getMembers()) {
					@Override
					protected T map(Object input) {
						return CollectionUtil.dynamicCast(expectedType, input);
					}
				};
			}
		};
	}

	/**
	 * Find all {@link FormContainer} {@link #getMembers() members} of the given
	 * {@link AbstractFormContainer}.
	 * 
	 * @param formContainer
	 *        The container to search through.
	 * @return All {@link FormField} members of the given container.
	 */
	public static Iterator<FormContainer> getGroups(FormContainer formContainer) {
		return new TransformIterator<FormMember, FormContainer>(formContainer.getMembers()) {
			@Override
			protected boolean acceptDestination(FormContainer value) {
				return true;
			}
		
			@Override
			protected boolean test(FormMember value) {
				return value instanceof FormContainer;
			}
		
			@Override
			protected FormContainer transform(FormMember value) {
				return (FormContainer) value;
			}
		};
	}

	/**
	 * Find all {@link FormField} {@link #getMembers() members} of the given
	 * {@link AbstractFormContainer}.
	 * 
	 * @param formContainer
	 *        The container to search through.
	 * @return All {@link FormField} members of the given container.
	 */
	public static Iterator<FormField> getFields(FormContainer formContainer) {
		return new TransformIterator<FormMember, FormField>(formContainer.getMembers()) {
			@Override
			protected boolean acceptDestination(FormField value) {
				return true;
			}
		
			@Override
			protected boolean test(FormMember value) {
				return value instanceof FormField;
			}
		
			@Override
			protected FormField transform(FormMember value) {
				return (FormField) value;
			}
		};
	}

	@Override
	protected FormContainer self() {
		return this;
	}

}
