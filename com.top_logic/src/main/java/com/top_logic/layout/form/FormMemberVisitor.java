/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.DataField;
import com.top_logic.layout.form.model.ExpandableStringField;
import com.top_logic.layout.form.model.FolderField;
import com.top_logic.layout.form.model.FormArray;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.FormTree;
import com.top_logic.layout.form.model.GalleryField;
import com.top_logic.layout.form.model.HiddenField;
import com.top_logic.layout.form.model.ImageField;
import com.top_logic.layout.form.model.IntField;
import com.top_logic.layout.form.model.ListField;
import com.top_logic.layout.form.model.PasswordField;
import com.top_logic.layout.form.model.PopupMenuField;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.SelectionTableField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.form.model.TreeField;

/**
 * Visitor for the {@link FormMember} type hierarchy.
 * 
 * @see "http://de.wikipedia.org/wiki/Besucher_(Entwurfsmuster)"
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface FormMemberVisitor<R, A> {

	/**
	 * Visit choice for {@link FormArray}s.
	 */
	R visitFormArray(FormArray member, A arg);
	
	/**
	 * Visit choice for {@link FormGroup}s.
	 */
	R visitFormGroup(FormGroup member, A arg);

	/**
	 * Visit choice for {@link FormContext}s.
	 */
	R visitFormContext(FormContext member, A arg);

	/**
	 * Visit choice for {@link BooleanField}s.
	 */
	R visitBooleanField(BooleanField member, A arg);
	
	/**
	 * Visit choice for {@link ComplexField}s.
	 */
	R visitComplexField(ComplexField member, A arg);
	
	/**
	 * Visit choice for {@link IntField}s.
	 */
	R visitIntField(IntField member, A arg);
	
	/**
	 * Visit choice for {@link HiddenField}s.
	 */
	R visitHiddenField(HiddenField member, A arg);
	
	/**
	 * Visit choice for {@link ListField}s.
	 */
	R visitListField(ListField member, A arg);
	
	/**
	 * Visit choice for {@link TableField}s.
	 */
	R visitTableField(TableField member, A arg);
	
	/**
	 * Visit choice for {@link SelectField}s.
	 */
	R visitSelectField(SelectField member, A arg);
	
	/**
	 * Visit choice for {@link SelectionTableField}s.
	 */
	R visitSelectionTableField(SelectionTableField member, A arg);
	
    /**
     * Visit choice for {@link ExpandableStringField}s.
     */
	R visitExpandableStringField(ExpandableStringField member, A arg);
    
    /**
	 * Visit choice for {@link StringField}s.
	 */
	R visitStringField(StringField member, A arg);

	/**
	 * Visit choice for {@link PasswordField}s.
	 */
	default R visitPasswordField(PasswordField member, A arg) {
		return visitFormField(member, arg);
	}

	/**
	 * Visit choice for {@link CommandField}s.
	 */
	R visitCommandField(CommandField commandField, A arg);

	/**
	 * Visit choice for {@link PopupMenuField}s.
	 */
	R visitPopupMenuField(PopupMenuField popupMenuField, A arg);

	/**
	 * Visit choice for {@link FormTree}s.
	 */
	R visitFormTree(FormTree formTree, A arg);

	/**
	 * Visit choice for {@link TreeField}s.
	 */
	R visitTreeField(TreeField treeField, A arg);

	/**
	 * Visit choice for {@link FolderField}s.
	 */
	R visitFolderField(FolderField treeField, A arg);
	
    /**
     * Visit choice for {@link ImageField}s.
     */
	R visitImageField(ImageField member, A arg);

	/**
	 * Visit choice for {@link GalleryField}s.
	 */
	R visitGalleryField(GalleryField member, A arg);

    /**
     * Visit choice for {@link DataField}s.
     */
	R visitDataField(DataField member, A arg);
    
	/**
	 * Catch all choice for {@link FormContainer}s to allow extending the hierarchy in other modules.
	 */
	R visitFormContainer(FormContainer member, A arg);
	
	/**
	 * Catch all choice for {@link FormField}s to allow extending the hierarchy in other modules.
	 */
	R visitFormField(FormField member, A arg);

	/**
	 * Catch all choice for {@link FormMember}s to allow extending the hierarchy in other modules.
	 */
	R visitFormMember(FormMember member, A arg);

}
