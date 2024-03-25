/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.messagebox;

import java.util.function.Function;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.declarative.DeclarativeFormBuilder;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.initializer.InitializerIndex;
import com.top_logic.layout.form.values.edit.initializer.InitializerProvider;
import com.top_logic.layout.structure.DefaultDialogModel;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link AbstractCreateConfigurationDialog} for editing one concrete {@link ConfigurationItem}.
 * 
 * @see CreateConfigurationsDialog Creating more than one item.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CreateConfigurationDialog<C extends ConfigurationItem> extends AbstractCreateConfigurationDialog<C> {

	private final C _model;

	/**
	 * Creates a new {@link CreateConfigurationDialog}.
	 * 
	 * @param type
	 *        The {@link ConfigurationItem} to instantiate.
	 * @param okHandle
	 *        Function that is called with the result item.
	 * @param title
	 *        The dialog title.
	 * @param width
	 *        The width of the dialog.
	 * @param height
	 *        The height of the dialog.
	 */
	public CreateConfigurationDialog(Class<? extends C> type, Function<? super C, HandlerResult> okHandle,
			ResKey title, DisplayDimension width, DisplayDimension height) {
		this(type, DefaultDialogModel.dialogModel(title, width, height), okHandle);
	}

	/**
	 * Creates a {@link CreateConfigurationDialog}.
	 * 
	 * @param type
	 *        The {@link ConfigurationItem} to instantiate.
	 * @param dialogModel
	 *        The dimensions of the created dialog.
	 * @param okHandle
	 *        Function that is called with the result item.
	 */
	public CreateConfigurationDialog(Class<? extends C> type, DefaultDialogModel dialogModel,
			Function<? super C, HandlerResult> okHandle) {
		this(type, dialogModel, okHandle, null);
	}

	/**
	 * Creates a {@link CreateConfigurationDialog}.
	 * 
	 * @param type
	 *        The {@link ConfigurationItem} to instantiate.
	 * @param dialogModel
	 *        The dimensions of the created dialog.
	 * @param okHandle
	 *        Function that is called with the result item.
	 * @param base
	 *        Configuration to copy its values from.
	 */
	public CreateConfigurationDialog(Class<? extends C> type, DefaultDialogModel dialogModel,
			Function<? super C, HandlerResult> okHandle, C base) {
		super(dialogModel);
		setOkHandle(okHandle);
		if(base != null) {
			_model = TypedConfiguration.copy(base);
		} else {
			_model = TypedConfiguration.newConfigItem(type);
		}
	}

	@Override
	protected void fillFormContext(FormContext context) {
		initContainer(context, getModel());
	}

	/**
	 * Initializes the given {@link FormContainer} by creating form fields derived from the the
	 * given model.
	 * 
	 * @param container
	 *        The {@link FormContainer} to fill member into.
	 * @param model
	 *        The {@link ConfigurationItem} to create FormFields for.
	 */
	protected void initContainer(FormContainer container, C model) {
		InitializerProvider initializers = createInitializers();
		initializers.set(DeclarativeFormBuilder.FORM_MODEL, model);

		EditorFactory.initEditorGroup(container, model, initializers);
	}

	/**
	 * Creates the {@link InitializerProvider} for the declarative form.
	 */
	protected InitializerProvider createInitializers() {
		return new InitializerIndex();
	}

	@Override
	protected HandlerResult beforeSave(DisplayContext context) {
		C model = getModel();
		ConfigurationTranslator.INSTANCE.translateIfAutoTranslateEnabled(model);
		return super.beforeSave(context);
	}

	/**
	 * Returns the configuration model.
	 */
	@Override
	public final C getModel() {
		return _model;
	}

}

