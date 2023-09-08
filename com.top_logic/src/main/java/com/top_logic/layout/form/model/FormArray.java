/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import java.util.ArrayList;
import java.util.Iterator;

import com.top_logic.basic.config.annotation.Inspectable;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.ResourceView;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.FormMemberVisitor;

/**
 * TODO BHU: This class is currently not used, decide about it.
 * 
 * {@link com.top_logic.layout.form.FormContainer} that stores a list of
 * {@link com.top_logic.layout.form.FormMember}s indexed with an integer.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FormArray extends AbstractFormContainer {

	@Inspectable
	ArrayList<FormMember> members;
	
	/**
	 * @see AbstractFormContainer#AbstractFormContainer(String, ResourceView)
	 */
	public FormArray(String name, ResPrefix i18nPrefix) {
		super(name, i18nPrefix);
		members = new ArrayList<>();
	}

	/**
	 * Creates a new {@link FormArray} with a given set of
	 * {@link FormMember members}.
	 */
	public FormArray(String name, ResPrefix i18nPrefix, FormMember[] members) {
		this(name,i18nPrefix);
		addMembers(members);
	}

	@Override
	protected FormMember internalGetMember(String name) {
		if (Character.isDigit(name.charAt(0))) {
			return members.get(Integer.parseInt(name));
		} else {
			for (int cnt = members.size(), n = 0; n < cnt; n++) {
				FormMember member = members.get(n);
				if (name.equals(member.getName())) {
					return member;
				}
			}
		}
		return null;
	}

	@Override
	protected void internalAddMember(FormMember member) {
		addMember(members.size(), member);
	}
	
	/**
	 * Adds the given member at the specified index.
	 * 
	 * @param index
	 *     The index at which the new member is added.
	 * @param member
	 *     The new member to add.
	 */
	public void addMember(int index, FormMember member) {
		assert ! createsCyclicMembership(member) : "no cyclic membership";

		internalAddMember(index, member);
	}

	private void internalAddMember(int index, FormMember member) {
		((AbstractFormMember) member).setParent(this);
		
		members.add(index, member);
		firePropertyChanged(FormContainer.MEMBER_ADDED_PROPERTY, self(), null, member);
	}

	@Override
	protected boolean internalRemoveMember(FormMember member) {
		boolean removed = members.remove(member);
		if (removed) {
			((AbstractFormMember) member).setParent(null);
			firePropertyChanged(MEMBER_REMOVED_PROPERTY, self(), member, null);
		}
		return removed;
	}

	@Override
	public Iterator<? extends FormMember> getMembers() {
		return members.iterator();
	}

	@Override
	public boolean focus() {
		for (FormMember member : members) {
			if (member.focus()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Object visit(FormMemberVisitor v, Object arg) {
		return v.visitFormArray(this, arg);
	}

	@Override
	public int size() {
		return members.size();
	}
}
