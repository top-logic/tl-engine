/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMemberVisitor;

/**
 * Convenience super class for {@link FormMemberVisitor} implementations. 
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractFormMemberVisitor<R, A> implements FormMemberVisitor<R, A> {

	@Override
	public R visitBooleanField(BooleanField member, A arg) {
		return visitFormField(member, arg);
	}

	@Override
	public R visitFormField(FormField member, A arg) {
		return visitFormMember(member, arg);
	}

	@Override
	public R visitComplexField(ComplexField member, A arg) {
		return visitFormField(member, arg);
	}

	@Override
	public R visitFormArray(FormArray member, A arg) {
		return visitFormContainer(member, arg);
	}

	@Override
	public R visitFormContainer(FormContainer member, A arg) {
		return visitFormMember(member, arg);
	}

	@Override
	public R visitFormContext(FormContext member, A arg) {
		return visitFormGroup(member, arg);
	}

	@Override
	public R visitFormGroup(FormGroup member, A arg) {
		return visitFormContainer(member, arg);
	}

	@Override
	public R visitFolderField(FolderField member, A arg) {
	    return visitFormGroup(member, arg);
	}

	@Override
	public R visitHiddenField(HiddenField member, A arg) {
		return visitConstantField(member, arg);
	}

	@Override
	public R visitCommandField(CommandField member, A arg) {
		return visitFormMember(member, arg);
	}
	
	@Override
	public R visitPopupMenuField(PopupMenuField member, A arg) {
		return visitFormMember(member, arg);
	}

	@Override
	public R visitIntField(IntField member, A arg) {
		return visitFormField(member, arg);
	}

	@Override
	public R visitListField(ListField member, A arg) {
		return visitConstantField(member, arg);
	}

	@Override
	public R visitSelectField(SelectField member, A arg) {
		return visitFormField(member, arg);
	}

	@Override
	public R visitSelectionTableField(SelectionTableField member, A arg) {
		return visitSelectField(member, arg);
	}

	@Override
	public R visitExpandableStringField(ExpandableStringField member, A arg) {
        return visitStringField(member, arg);
    }

	@Override
	public R visitStringField(StringField member, A arg) {
		return visitFormField(member, arg);
	}

	@Override
	public R visitTableField(TableField member, A arg) {
		return visitConstantField(member, arg);
	}

	protected R visitConstantField(ConstantField member, A arg) {
		return visitFormField(member, arg);
	}
	
	@Override
	public R visitFormTree(FormTree member, A arg) {
		return visitFormContainer(member, arg);
	}
	
	@Override
	public R visitTreeField(TreeField member, A arg) {
		return visitConstantField(member, arg);
	}
	
	@Override
	public R visitDataField(DataField member, A arg) {
		return visitFormField(member, arg);
	}

	@Override
	public R visitImageField(ImageField member, A arg) {
	    return visitFormMember(member, arg);
	}

	@Override
	public R visitGalleryField(GalleryField member, A arg) {
		return visitFormField(member, arg);
	}
}
