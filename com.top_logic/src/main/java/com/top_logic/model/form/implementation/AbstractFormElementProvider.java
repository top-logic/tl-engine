/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form.implementation;

import static com.top_logic.layout.DisplayDimension.*;
import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.function.Function;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.values.edit.initializer.InitializerIndex;
import com.top_logic.layout.form.values.edit.initializer.InitializerProvider;
import com.top_logic.layout.messagebox.CreateConfigurationDialog;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.ReactiveFormCSS;
import com.top_logic.model.form.definition.FormElement;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Base class for {@link FormElementTemplateProvider}s.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public abstract class AbstractFormElementProvider<T extends FormElement<?>> extends AbstractConfiguredInstance<T>
		implements FormElementTemplateProvider {

	/**
	 * Default dialog width of the configuration dialog of a {@link FormElementTemplateProvider}.
	 */
	public static final DisplayDimension DIALOG_WIDTH = dim(300, DisplayUnit.PIXEL);

	/**
	 * Default dialog height of the configuration dialog of a {@link FormElementTemplateProvider}.
	 */
	public static final DisplayDimension DIALOG_HEIGHT = dim(230, DisplayUnit.PIXEL);
	
	/**
	 * CSS class rendered for an input cell.
	 */
	protected static String inputCellCSS(FormEditorContext context) {
		String labelCSS = context.getLabelPlacement().cssClass();
		String rfInputCell = ReactiveFormCSS.RF_INPUT_CELL;
		if (labelCSS != null) {
			rfInputCell = rfInputCell + " " + labelCSS;
		}
		return rfInputCell;
	}

	/** The context of the form. */
	private InstantiationContext _context;

	private boolean _isInEditMode;

	private String _id;

	/** Prefix for UUID for ResKeys. */
	public static final String PREFIX = "form-";

	/**
	 * Create a new {@link AbstractFormElementProvider} for a given {@link FormElement} in a
	 * given {@link InstantiationContext}. Holds the {@link InstantiationContext}.
	 */
	public AbstractFormElementProvider(InstantiationContext context, T config) {
		super(context, config);

		_context = context;
	}
	
	@Override
	public DisplayDimension getDialogWidth() {
		return DIALOG_WIDTH;
	}

	@Override
	public DisplayDimension getDialogHeight() {
		return DIALOG_HEIGHT;
	}

	@Override
	public CreateConfigurationDialog<? extends FormElement<?>> createConfigDialog(ResKey dialogTitle,
			Function<? super FormElement<?>, HandlerResult> okHandle) {
		CreateConfigurationDialog<T> result =
			new CreateConfigurationDialog<>(getFormElementType(), okHandle, dialogTitle,
			getDialogWidth(),
			getDialogHeight()) {

			@Override
			protected void initContainer(FormContainer container, T model) {
				initConfiguration(model);
				super.initContainer(container, model);
			}

			@Override
			protected InitializerProvider createInitializers() {
				return createConfigInitializers();
			}
		};
		return result;
	}

	/**
	 * Creates the {@link InitializerProvider} for the configuration dialog form.
	 */
	protected InitializerIndex createConfigInitializers() {
		return new InitializerIndex();
	}

	/**
	 * Hook called when the dialog opens.
	 * 
	 * @param model
	 *        The displayed configuration.
	 */
	protected void initConfiguration(T model) {
		// Hook for subclasses.
	}

	@Override
	public Class<T> getFormElementType() {
		@SuppressWarnings("unchecked")
		Class<T> result = (Class<T>) getConfig().getConfigurationInterface();
		return result;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public String getID() {
		return _id;
	}

	/**
	 * Sets the identifier between server and client elements.
	 */
	protected void setID(String id) {
		_id = id;
	}

	@Override
	public HTMLTemplateFragment createDesignTemplate(FormEditorContext context) {
		_isInEditMode = context.isInEditMode();
		setID(context.getFrameScope().createNewID());
		context.getFormEditorMapping().putMapping(getID(), getConfig());
		HTMLTemplateFragment formTemplate = createDisplayTemplate(context);

		return FormEditorElementTemplateProvider.wrapFormEditorElement(formTemplate, this, context);
	}

	/**
	 * Creates a {@link HTMLTemplateFragment} to add the ID of the {@link FormElement} to element of
	 * the DOM.
	 * 
	 * @see #getID()
	 * 
	 * @return The data-id attribute with the ID as value.
	 */
	protected HTMLTemplateFragment getIdAttribute() {
		return getID() != null ? attr(HTMLConstants.DATA_ATTRIBUTE_PREFIX + "id", getID()) : empty();
	}

	/**
	 * Returns the {@link InstantiationContext}.
	 * 
	 * @return The {@link InstantiationContext}.
	 */
	public InstantiationContext getContext() {
		return _context;
	}

	/**
	 * Returns whether the editor is in edit mode.
	 * 
	 * @return Whether the editor is in edit mode.
	 */
	protected boolean isInEditMode() {
		return _isInEditMode;
	}

	@Override
	public boolean openDialog() {
		return false;
	}

	@Override
	public boolean isVisible(TLStructuredType type, FormMode formMode) {
		return true;
	}

}
