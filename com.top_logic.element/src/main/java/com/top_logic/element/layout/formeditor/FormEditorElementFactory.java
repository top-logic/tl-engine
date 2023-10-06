/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor;

import java.util.function.Function;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.copy.ConfigCopier;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.template.model.Templates;
import com.top_logic.layout.messagebox.CreateConfigurationDialog;
import com.top_logic.layout.structure.DialogClosedListener;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.definition.FormElement;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.form.implementation.FormEditorElementTemplateProvider;
import com.top_logic.model.form.implementation.FormElementTemplateProvider;
import com.top_logic.model.form.implementation.FormMode;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A factory to create and edit {@link Templates} for the elements of the form editor.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class FormEditorElementFactory {

	/**
	 * The empty default form editor element.
	 * 
	 * @param element
	 *        The element which to surround by an editor element.
	 */
	public static FormEditorElementTemplateProvider createFormEditorTemplate(FormElementTemplateProvider element) {
		return new FormEditorElementTemplateProvider();
	}

	/**
	 * Creates and opens a dialog to change the values of the {@link ConfigurationItem}. If the
	 * dialog is closed, this control will be repainted.
	 * 
	 * @see CreateConfigurationDialog
	 */
	public static HandlerResult openDialog(DisplayContext commandContext, FormElement<?> def,
			Function<? super FormElement<?>, HandlerResult> okHandle, DialogClosedListener dialogCloseListener,
			TLStructuredType editedType) {

		FormElementTemplateProvider formElement = TypedConfigUtil.createInstance(def);

		ResKey dialogTitle = dialogTitle(formElement, editedType);

		CreateConfigurationDialog<? extends FormElement<?>> dialog =
			formElement.createConfigDialog(dialogTitle, okHandle);

		if (def != null) {
			// edit ContentDefinition -> create preview
			FormElement<?> previewConfType = dialog.getModel();
			ConfigCopier.copyContent(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, def, previewConfType);
		}

		dialog.getDialogModel().addListener(DialogModel.CLOSED_PROPERTY, dialogCloseListener);

		return dialog.open(commandContext);
	}

	private static ResKey dialogTitle(FormElementTemplateProvider formElement, TLStructuredType editedType) {
		FormEditorContext formEditorContext = new FormEditorContext.Builder()
			.formMode(FormMode.CREATE)
			.formType(editedType)
			.build();

		ResKey label = formElement.getLabel(formEditorContext);
		return I18NConstants.DIALOG_TITLE_EDIT_FORM_ELEMENT__ELEMENT.fill(label);
	}

}