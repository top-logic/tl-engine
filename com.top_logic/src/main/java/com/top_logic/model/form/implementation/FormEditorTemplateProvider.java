/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.form.implementation;

import java.util.function.Function;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.ImageProvider;
import com.top_logic.layout.messagebox.CreateConfigurationDialog;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.form.definition.FormElement;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link FormElementTemplateProvider} with additional configuration which defines the appearance of
 * a {@link FormElementTemplateProvider} in the form editor.
 * 
 * @see FormElementTemplateProvider
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface FormEditorTemplateProvider extends FormElementTemplateProvider {

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
	 * Creates the {@link CreateConfigurationDialog} for configuring the {@link FormElement}
	 * configuration.
	 *
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
	 * Returns whether the dialog is always opened when the {@link FormElementTemplateProvider} is
	 * placed in the editor.
	 */
	boolean openDialog();

}
