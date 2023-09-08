/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.messagebox;

import org.w3c.dom.Document;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.util.ResKey;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.template.FormGroupControl;
import com.top_logic.layout.form.template.FormTemplate;
import com.top_logic.layout.structure.DefaultDialogModel;
import com.top_logic.layout.structure.DialogModel;

/**
 * Base class for form-template-rendered {@link AbstractFormDialogBase dialogs} that do not depend
 * on components.
 * 
 * @see AbstractFormPageDialog Creating dialogs with styled header area.
 * 
 * @deprecated Use {@link AbstractTemplateDialog} with {@link TagTemplate}s.
 */
@Deprecated
public abstract class AbstractFormDialog extends AbstractFormDialogBase {

	/**
	 * Creates a {@link SimpleFormDialog}.
	 * 
	 * @param dialogTitle
	 *        The dialog title.
	 * @param width
	 *        The width of the new dialog.
	 * @param height
	 *        The height of the new dialog.
	 */
	public AbstractFormDialog(ResKey dialogTitle, DisplayDimension width, DisplayDimension height) {
		this(DefaultDialogModel.dialogModel(dialogTitle, width, height));
	}

	/**
	 * Creates a {@link SimpleFormDialog}.
	 * 
	 * @param dialogModel
	 *        See {@link #getDialogModel()}
	 */
	public AbstractFormDialog(DialogModel dialogModel) {
		super(dialogModel);
	}

	@Override
	protected HTMLFragment createView() {
		return new MessageBoxContentView(new FormGroupControl(getFormContext(), getTemplate()));
	}

	/**
	 * Return a default template, which embeds the field with the given name.
	 * 
	 * @return The requested document description, never <code>null</code>.
	 */
    protected abstract FormTemplate getTemplate();

	/**
	 * Service method to create a {@link FormTemplate} {@link #getControlProvider()}.
	 * 
	 * @param template
	 *        The actual template.
	 * @param automaticErrorIcons
	 *        Value of {@link FormTemplate#hasAutomaticErrorDisplay()}.
	 * @param resourcePrefix
	 *        The resource prefix to use for the template.
	 * @return A potential value for {@link #getTemplate()}.
	 */
	protected final FormTemplate defaultTemplate(Document template, boolean automaticErrorIcons, ResPrefix resourcePrefix) {
		return new FormTemplate(resourcePrefix, getControlProvider(), automaticErrorIcons, template);
	}

	/**
	 * The {@link ControlProvider} to use for controls created by the {@link #getTemplate()}.
	 */
	protected ControlProvider getControlProvider() {
		return DefaultFormFieldControlProvider.INSTANCE;
	}

}
