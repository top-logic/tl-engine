/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form.implementation;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.ImageProvider;
import com.top_logic.layout.messagebox.CreateConfigurationDialog;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.definition.FormElement;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Implementation of a {@link FormElement} creating a corresponding template for rendering.
 * 
 * @see #createTemplate(FormEditorContext)
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
@Label("Form part")
public interface FormElementTemplateProvider {

	/**
	 * Creates the actual template.
	 * 
	 * @param context
	 *        Context information for template creation.
	 * 
	 * @return The template for rendering this form element.
	 */
	HTMLTemplateFragment createTemplate(FormEditorContext context);

	/**
	 * Whether the element is rendered over the entire line.
	 * 
	 * @param modelType
	 *        Context type in which this {@link FormElementTemplateProvider} is evaluated.
	 */
	boolean getWholeLine(TLStructuredType modelType);

	/**
	 * Whether the element is a tool for the form editor.
	 */
	boolean getIsTool();

	/**
	 * The {@link ImageProvider} to render an icon for this element in the toolbar of a form editor.
	 */
	ImageProvider getImageProvider();

	/**
	 * The label text for the element in the toolbar of a form editor.
	 * 
	 * @param context
	 *        Context information for label creation.
	 */
	default ResKey getLabel(FormEditorContext context) {
		return ResKey.forClass(this.getClass());
	}

	/**
	 * The name for the label.
	 */
	String getName();

	/**
	 * The class of the {@link FormElement} configuration created by this provider.
	 */
	Class<? extends FormElement<?>> getFormElementType();

	/**
	 * Returns the width of the dialog to edit the attributes.
	 */
	DisplayDimension getDialogWidth();

	/**
	 * Returns the height of the dialog to edit the attributes.
	 */
	DisplayDimension getDialogHeight();

	/**
	 * Creates the {@link CreateConfigurationDialog} for configuring the {@link FormElement}
	 * configuration.
	 * 
	 * @param contextComponent
	 *        The component for which a from is currently edited, <code>null</code> if the edited
	 *        form does not belong to a unique component.
	 * @param dialogTitle
	 *        The dialog title to display.
	 * @param okHandle
	 *        Function that is called with the created configuration.
	 */
	CreateConfigurationDialog<? extends FormElement<?>> createConfigDialog(LayoutComponent contextComponent,
			ResKey dialogTitle, Function<? super FormElement<?>, HandlerResult> okHandle);

	/**
	 * The identifier between server and client elements.
	 */
	String getID();

	/**
	 * Returns whether the {@link FormElementTemplateProvider} is visible. By default it is <code>true</code>.
	 */
	boolean isVisible(TLStructuredType type, FormMode formMode);

	/**
	 * Returns whether the dialog is always opened when the {@link FormElementTemplateProvider} is placed in the
	 * editor.
	 */
	boolean openDialog();

}
