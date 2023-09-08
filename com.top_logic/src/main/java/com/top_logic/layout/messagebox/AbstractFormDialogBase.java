/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.messagebox;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.generate.CodeUtil;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Base class for control-rendered dialogs that do not depend on components.
 * 
 * @see #createView() Constructing the dialog contents.
 * @see MessageBox Confirm dialogs without further user interaction.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractFormDialogBase extends AbstractDialog implements FormHandler {

	/**
	 * Creates a {@link AbstractFormDialogBase}.
	 * 
	 * @param dialogModel
	 *        See {@link #getDialogModel()}.
	 */
	public AbstractFormDialogBase(DialogModel dialogModel) {
		super(dialogModel);
		Object oldValue = dialogModel.set(FORM_DIALOG_PROPERTY_KEY, this);
		assert (oldValue == null) : "Must not reuse DialogModels!";
	}

	/**
	 * {@link DialogModel#get(Property)} key for finding the {@link AbstractFormDialogBase} for a
	 * given {@link DialogModel}.
	 */
	private static final Property<AbstractFormDialogBase> FORM_DIALOG_PROPERTY_KEY =
		TypedAnnotatable.property(AbstractFormDialogBase.class, "formDialogPropertyKey");

	private FormContext formContext;

	/**
	 * The {@link ResPrefix} used for the {@link FormContext}.
	 * 
	 * <p>
	 * When adding fields in {@link #fillFormContext(FormContext)} they must have explicit labels
	 * set (preferred) or this method must be overridden to set a specialized {@link ResPrefix} for
	 * the whole context.
	 * </p>
	 */
	@Deprecated
	protected ResPrefix getResourcePrefix() {
		return ResPrefix.NONE;
	}

	/**
	 * Constructs the {@link FormContext} of this dialog.
	 * 
	 * @return The new {@link FormContext}.
	 */
	protected FormContext createFormContext() {
		return new FormContext(getFormId(), getResourcePrefix());
	}

	/**
	 * The stable ID to allow persisting user configurations of input elements to be stored. The ID
	 * must be unique among all concurrently displayed elements
	 */
	protected String getFormId() {
		return CodeUtil.toCamelCase(getClass().getName());
	}

	/**
	 * Fills the given {@link FormContext} for this {@link SimpleFormDialog}.
	 * 
	 * @param context
	 *        The {@link FormContext} being created.
	 */
	protected abstract void fillFormContext(FormContext context);

	@Override
	public FormContext getFormContext() {
		if (this.formContext == null) {
			this.formContext = createFormContext();

			this.formContext.setOwningModel(this);

			// Make sure that fillFormContext() may legally call getFormContext().
			fillFormContext(this.formContext);
		}
		return this.formContext;
	}

	@Override
	public boolean hasFormContext() {
		return this.formContext != null;
	}

	@Override
	public Command getApplyClosure() {
		return null;
	}

	@Override
	public ModelName getModelName() {
		return ModelResolver.buildModelName(this);
	}

	/**
	 * Command checking {@link #getFormContext() form context} for errors.
	 * 
	 * @see FormContext#checkAll()
	 */
	protected Command checkContextCommand() {
		return context -> {
			if (!getFormContext().checkAll()) {
				return AbstractApplyCommandHandler.createErrorResult(getFormContext());
			}
			return HandlerResult.DEFAULT_RESULT;
		};
	}

	/**
	 * Resolve the {@link AbstractFormDialogBase} for a displayed {@link DialogModel}.
	 */
	public static AbstractFormDialogBase getFormDialog(DialogModel dialogModel) {
		return dialogModel.get(FORM_DIALOG_PROPERTY_KEY);
	}

}
