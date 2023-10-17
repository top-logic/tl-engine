/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.TransformIterator;
import com.top_logic.basic.config.annotation.Inspectable;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.ResourceView;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.FormMemberVisitor;

/**
 * A {@link FormGroup} is a collection of
 * {@link com.top_logic.layout.form.FormMember}s (either
 * {@link com.top_logic.layout.form.FormField}s or other {@link FormContainer}
 * objects) identified by their respective
 * {@link com.top_logic.layout.form.FormMember#getName() names}.
 * 
 * {@link com.top_logic.layout.form.model.FormContext}s are special
 * {@link FormGroup}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FormGroup extends AbstractFormContainer {
	
	/**
	 * {@link Map} of group members identified by their respective {@link FormMember#getName()
	 * names}.
	 */
	@Inspectable
	protected Map<String, FormMember> formMembersByName = new LinkedHashMap<>();
	
	/**
	 * Creates an empty group.
	 * @see FormMember#getName()
	 */
	public FormGroup(String name, ResourceView aLabelRessource) {
		super(name, aLabelRessource);
	}

	/**
	 * Creates an empty group.
	 * 
	 * @see FormMember#getName()
	 */
	public FormGroup(String name, ResPrefix i18nPrefix) {
		super(name, i18nPrefix);
	}

	/**
	 * Creates a {@link FormGroup} filled with the given array of group members.
	 * 
	 * @see FormMember#getName()
	 */
	public FormGroup(String name, ResPrefix resPrefix, FormMember[] members) {
		this(name, resPrefix);

		if (members != null)
			addMembers(members);
	}

	/**
	 * Creates a {@link FormGroup} filled with the given {@link Collection} of group members.
	 * 
	 * @see FormMember#getName()
	 * 
	 * @param members
	 *        A {@link Collection} of {@link FormMember group members}.
	 */
	public FormGroup(String name, ResPrefix i18nPrefix, Collection<? extends FormMember> members) {
		this(name, i18nPrefix);
		if (members != null) {
			addMembers(members);
		}
	}

	/**
	 * an iterator of {@link FormField} names in this group.
	 */
    public final Iterator<String> getFieldNames() {
    	return new TransformIterator<>(formMembersByName.entrySet().iterator()) {
			@Override
			protected boolean acceptDestination(String value) {
				return true;
			}

			@Override
			protected boolean test(Entry<String, FormMember> value) {
				return value.getValue() instanceof FormField;
			}

			@Override
			protected String transform(Entry<String, FormMember> value) {
				return value.getKey();
			}
    	};
    }

	/**
	 * an iterator of {@link FormContainer} names in this group.
	 */
    public final Iterator<String> getGroupNames() {
    	return new TransformIterator<>(formMembersByName.entrySet().iterator()) {
			@Override
			protected boolean acceptDestination(String value) {
				return true;
			}

			@Override
			protected boolean test(Entry<String, FormMember> value) {
				return value.getValue() instanceof FormContainer;
			}

			@Override
			protected String transform(Entry<String, FormMember> value) {
				return value.getKey();
			}
    	};
    }

	/**
	 * an iterator of {@link FormContainer}s in this group.
	 */
    public final Iterator<FormContainer> getGroups() {
    	return getGroups(this);
    }

	@Override
	public final Iterator<? extends FormMember> getMembers() {
		return formMembersByName.values().iterator();
	}

	/**
	 * an iterator of all {@link FormMember} names in this group.
	 */
	public final Iterator<String> getMemberNames() {
		return formMembersByName.keySet().iterator();
	}

	@Override
	protected FormMember internalGetMember(String aName) {
		return formMembersByName.get(aName);
	}

	/**
	 * Finds a {@link #getDescendants() descendant} {@link FormMember} with the
	 * given qualified name (see {@link FormMember#getQualifiedName()}).
	 * 
	 * @param ancestor
	 *     The top-level ancestor of the searched member.
	 * @param qname
	 *     The {@link FormMember#getQualifiedName() qualified name} of
	 *     the searched member
	 * @return A descendant member with the given qualified name.
	 * 
	 * @throws NoSuchElementException
	 *     If no such member is found among the descendants of the given
	 *     ancestor
	 */
    public static FormMember getMemberByQualifiedName(FormContainer ancestor, String qname) 
    	throws NoSuchElementException 
    {
        int contextSeparatorIndex = qname.indexOf('.');
        
        int localNameStart;
        String contextName;
        if (contextSeparatorIndex < 0) {
        	contextName = qname;
        	localNameStart = qname.length();
        } else {
        	contextName = qname.substring(0, contextSeparatorIndex);
        	localNameStart = contextSeparatorIndex + 1;
        }
        assert contextName.equals(ancestor.getName()) :
            "Received update for wrong form context: expected=" + ancestor.getName() +
            ", contextName=" + contextName;
        
        if (contextSeparatorIndex < 0) {
        	return ancestor;
        } else {
        	return lookupMember(ancestor, qname, localNameStart);
        }
    }

    /**
	 * Finds a descendant {@link FormMember} with a given relative name.
	 * 
	 * <p>
	 * A relative name is a suffix of a {@link FormMember#getQualifiedName() qualified name}. This
	 * method returns the descendant member of the given {@link FormMember} that has a qualified
	 * name that is composed from the qualified name of the context as prefix and the given relative
	 * name as suffix.
	 * </p>
	 */
	public static FormMember getMemberByRelativeName(FormMember context, String aName)
		throws NoSuchElementException 
	{
    	int index = 0;
    	
		FormMember current = context;
		if (StringServices.startsWithChar(aName, '/')) {
    		index = 1;
    		current = current.getFormContext();
		}

		while (true) {
			int nextSepIndex = aName.indexOf('/', index);
			String step;
			if (nextSepIndex < 0) {
				step = aName.substring(index);
			} else {
				step = aName.substring(index, nextSepIndex);
			}
			if ("..".equals(step)) {
				current = current.getParent();
			} else {
				current = lookupMemberOrSelf(current, step, 0);
			}
			if (nextSepIndex < 0) {
				return current;
			} else {
				index = nextSepIndex + 1;
			}
		}
    }

	private static FormMember lookupMemberOrSelf(FormMember ancestor, String qname, int localNameStart)
		throws NoSuchElementException 
	{
		if (qname.isEmpty() || ".".equals(qname)) {
			// The single dot or empty name means the current container.
			return ancestor;
		}
		
		return lookupMember(ancestor, qname, localNameStart);
	}

	private static FormMember lookupMember(FormMember root, String qname, int localNameStart)
			throws NoSuchElementException
	{
		int separatorIndex = 0;
		FormMember ancestor = root;
        while (true) {
            separatorIndex = qname.indexOf('.', localNameStart);
			String step;
            if (separatorIndex < 0) {
            	step = qname.substring(localNameStart);
            } else {
            	step = qname.substring(localNameStart, separatorIndex);
            }
            
			if (!(ancestor instanceof FormContainer)) {
				throw new IllegalArgumentException("Cannot resolve path '" + step + "' relative to primitive member '"
					+ ancestor.getQualifiedName() + "', resolving '" + qname + "' relative to '"
					+ root.getQualifiedName() + "'.");
			}
			ancestor = ((FormContainer) ancestor).getMember(step);
            
            if (separatorIndex < 0) {
            	return ancestor;
            } else {
            	localNameStart = separatorIndex + 1;
            }
        }
	}
	
	/**
	 * Adds the given {@link FormMember} to this group.
	 * 
	 * If a member with the same name already exists in this group, the old
	 * member is replaced.
	 * 
	 * Note: This method is declared <code>final</code>, because it is called
	 * from within a constructor.
	 * 
	 * @param aMember
	 *     The new member to add.
	 */
	@Override
	protected final void internalAddMember(FormMember aMember) {
		assert ! createsCyclicMembership(aMember) : "no cyclic membership";
		assert ! hasMember(aMember.getName()) : "members have unique names "+ aMember.getName();
		
		// Establish the reverse association.
		((AbstractFormMember) aMember).setParent(this);

		this.formMembersByName.put(aMember.getName(), aMember);
		
		firePropertyChanged(FormContainer.MEMBER_ADDED_PROPERTY, self(), null, aMember);
	}
	
	/**
	 * Replace a aMember with the same name keeping the order of Members.
	 * 
	 * @return The Object replace or null if not found.
	 */
	public FormMember replaceMember(FormMember aMember) {
        assert ! createsCyclicMembership(aMember) : "no cyclic membership";
		FormMember oldMember = formMembersByName.get(aMember.getName());
		if (oldMember == null) {
			return null;
		}

		firePropertyChanged(MEMBER_REMOVED_PROPERTY, self(), oldMember, null);
		((AbstractFormMember) oldMember).setParent(null);

		// Establish the reverse association.
		((AbstractFormMember) aMember).setParent(this);
		formMembersByName.put(aMember.getName(), aMember);
		firePropertyChanged(FormContainer.MEMBER_ADDED_PROPERTY, self(), null, aMember);

		return oldMember;
	}

	@Override
	protected final boolean internalRemoveMember(FormMember aMember) {
		FormMember removedMember = this.formMembersByName.remove(aMember.getName());
		if (removedMember == null) {
			return false;
		}

		firePropertyChanged(MEMBER_REMOVED_PROPERTY, self(), removedMember, null);
		// Note: Listeners to the remove event must be able to construct the
		// fields qualified name. This is only possible, if it is not yet
		// removed from its parent group.
		((AbstractFormMember) removedMember).setParent(null);

		return true;
	}

	/**
	 * Remove the form member with the given name from this context. If no such
	 * member exists, this method does nothing
	 * 
	 * @param aName
	 *        the name of the member to remove,
	 * @return The removed member, or null, if this container did not contain a
	 *         member with the given name.
	 */
	public FormMember removeMember(String aName) {
		FormMember result = this.formMembersByName.get(aName);
		if (result == null) {
			// Member not found.
			return null;
		}

		this.removeMember(result);
		return result;
	}

	@Override
	public boolean focus() {
		Iterator<? extends FormMember> members = getMembers();
		while (members.hasNext()) {
			if (members.next().focus()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Object visit(FormMemberVisitor v, Object arg) {
		return v.visitFormGroup(this, arg);
	}

	@Override
	public int size() {
		return formMembersByName.size();
	}
}
