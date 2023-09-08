/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui;

import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.AbstractFormMemberVisitor;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.DataField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.GalleryField;
import com.top_logic.layout.form.model.HiddenField;
import com.top_logic.layout.form.model.IntField;
import com.top_logic.layout.form.model.PasswordField;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.template.SelectionControlProvider;
import com.top_logic.layout.form.template.TextInputControlProvider;

/**
 * Is used to create copies of {@link FormField}s with its {@link #copyField(FormField)} method.
 * See {@link #copyField(FormField) there} for the exact semantic.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class FieldCopier extends AbstractFormMemberVisitor<FormMember, String> {

	/**
	 * The instance of the {@link FieldCopier}. This is not a singleton, as (potential)
	 * subclasses can create further instances.
	 */
	public static final FieldCopier INSTANCE = new FieldCopier();

	/**
	 * Only subclasses may need to instantiate it. Everyone else should use the
	 * {@link #copyField(FormField)} method directly.
	 */
	protected FieldCopier() {
		// See JavaDoc above.
	}

	/**
	 * Create a new {@link FormField} and copies the value of the original field to this new field. <br/>
	 * <strong>Warning:</strong> <br/>
	 * Until the implementation is complete and not every {@link FormField} type is supported,
	 * <code>null</code> is returned for unsupported {@link FormField} types.
	 * 
	 * @return <code>null</code> if the field could not be copied.
	 */
	public static FormField copyField(FormField field) {
		return copyField(field, null);
	}

	/**
	 * Create a new {@link FormField} and copies the value of the original field to this new field. <br/>
	 * <strong>Warning:</strong> <br/>
	 * Until the implementation is complete and not every {@link FormField} type is supported,
	 * <code>null</code> is returned for unsupported {@link FormField} types.
	 * 
	 * @return <code>null</code> if the field could not be copied.
	 */
	public static FormField copyField(FormField field, String name) {
		FormField copiedField = (FormField) field.visit(INSTANCE, name);
		if (copiedField != null) {
			copiedField.setValue(field.getValue());
			assert copiedField.getName().equals(name);
		}
		return copiedField;
	}

	@Override
	public FormMember visitBooleanField(BooleanField field, String name) {
		String fieldName = name == null ? field.getName() : (String) name;
		return FormFactory.newBooleanField(fieldName);
	}

	@Override
	public FormMember visitIntField(IntField field, String name) {
		String fieldName = name == null ? field.getName() : (String) name;
		return FormFactory.newIntField(fieldName);
	}

	@Override
	public FormMember visitStringField(StringField field, String name) {
		String fieldName = name == null ? field.getName() : (String) name;
		StringField result = FormFactory.newStringField(fieldName);
		TextInputControlProvider controlProvider = new TextInputControlProvider();
		controlProvider.setMultiLine(true);
		controlProvider.setRows(3);
		controlProvider.setColumns(70);
		result.setControlProvider(controlProvider);
		return result;
	}

	@Override
	public FormMember visitPasswordField(PasswordField field, String name) {
		String fieldName = name == null ? field.getName() : (String) name;
		return FormFactory.newPasswordField(fieldName, false, false, null);
	}

	@Override
	public FormMember visitSelectField(SelectField field, String name) {
		String fieldName = name == null ? field.getName() : (String) name;
		SelectField copiedField =
			FormFactory.newSelectField(fieldName, field.getOptionModel(), field.isMultiple(), false, false, null);
		copiedField.setCustomOrder(field.hasCustomOrder());
		copiedField.setOptionLabelProvider(field.getOptionLabelProvider());
		copiedField.setControlProvider(SelectionControlProvider.SELECTION_INSTANCE);
		return copiedField;
	}

	@Override
	public FormMember visitComplexField(ComplexField field, String name) {
		String fieldName = name == null ? field.getName() : (String) name;
		return FormFactory.newComplexField(fieldName, field.getFormat());
	}

	@Override
	public FormMember visitGalleryField(GalleryField field, String name) {
		String fieldName = name == null ? field.getName() : (String) name;
		GalleryField copy = FormFactory.newGalleryField(fieldName);
		copy.setValue(field.getValue());
		return copy;
	}

	@Override
	public FormMember visitHiddenField(HiddenField field, String name) {
		String fieldName = name == null ? field.getName() : (String) name;
		HiddenField copy = FormFactory.newHiddenField(fieldName);
		copy.setControlProvider(field.getControlProvider());
		copy.setValue(field.getValue());
		return copy;
	}

	@Override
	public FormMember visitDataField(DataField field, String name) {
		String fieldName = name == null ? field.getName() : (String) name;
		DataField copy = FormFactory.newDataField(fieldName);
		copy.setValue(field.getValue());
		return copy;
	}

	@Override
	public FormMember visitFormMember(FormMember unsupportedFormMember, String name) {
		if (!(unsupportedFormMember instanceof FormField)) {
			throw new IllegalArgumentException("Can only copy FormFields, not: "
				+ unsupportedFormMember.getClass().getCanonicalName());
		}
		return null;
	}
}