/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import java.util.Iterator;

import org.apache.commons.collections4.iterators.EmptyIterator;

import com.top_logic.basic.col.TreeView;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;

/**
 * {@link TreeView} for a hierarchy of {@link FormMember}s in
 * {@link FormContainer}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class FormMemberTreeView implements TreeView<FormMember> {
	
	/**
	 * Singleton instance of this class.
	 */
	public static final FormMemberTreeView INSTANCE = new FormMemberTreeView();

	private FormMemberTreeView() {
		// Singleton constructor.
	}
	
	@Override
	public Iterator<? extends FormMember> getChildIterator(FormMember node) {
		// Make sure, the passed object is of an expected type.
		FormMember member = node;
		if (member instanceof FormContainer) {
			return ((FormContainer) member).getMembers();
		} else {
			return EmptyIterator.INSTANCE;
		}
	}

	@Override
	public boolean isLeaf(FormMember aNode) {
		return !getChildIterator(aNode).hasNext();
	}

	@Override
	public boolean isFinite() {
		return false;
	}

}