/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form;

import static java.util.Objects.*;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormDialogNamingScheme.FormDialogName;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.messagebox.AbstractFormDialogBase;
import com.top_logic.layout.scripting.ScriptingUtil;
import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.layout.structure.DialogWindowControl;
import com.top_logic.mig.html.layout.MainLayout;

/**
 * This {@link ModelNamingScheme} is used to identify the currently active dialog.
 * 
 * @author <a href="mailto:Jan.Stolzenburg@top-logic.com">Jan.Stolzenburg</a>
 */
public class FormDialogNamingScheme extends AbstractModelNamingScheme<AbstractFormDialogBase, FormDialogName> {

	/** Identifies as {@link FormContext} the currently active dialog. */
	public interface FormDialogName extends ModelName {

		// Nothing needed.

	}

	/**
	 * Singleton {@link FormDialogNamingScheme} instance.
	 */
	public static final FormDialogNamingScheme INSTANCE = new FormDialogNamingScheme();

	private FormDialogNamingScheme() {
		/* Private singleton constructor */
	}

	@Override
	public Class<FormDialogName> getNameClass() {
		return FormDialogName.class;
	}

	@Override
	public Class<AbstractFormDialogBase> getModelClass() {
		return AbstractFormDialogBase.class;
	}

	@Override
	public AbstractFormDialogBase locateModel(ActionContext context, FormDialogName name) {
		return findActiveFormDialog(context.getDisplayContext());
	}

	private AbstractFormDialogBase findActiveFormDialog(DisplayContext context) {
		MainLayout mainLayout = findMainLayout(context);
		DialogWindowControl activeDialog = ScriptingUtil.getActiveDialog(mainLayout);
		DialogModel dialogModel = activeDialog.getDialogModel();
		return AbstractFormDialogBase.getFormDialog(dialogModel);
	}

	private MainLayout findMainLayout(DisplayContext context) {
		MainLayout mainLayout = context.getLayoutContext().getMainLayout();
		return requireNonNull(mainLayout, "No main layout found!");
	}

	@Override
	protected void initName(FormDialogName name, AbstractFormDialogBase formDialog) {
		// Nothing needed, as there is exactly one top-level dialog.
	}

}
