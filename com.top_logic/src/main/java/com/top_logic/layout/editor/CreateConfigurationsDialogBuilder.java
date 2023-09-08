/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.messagebox.CreateConfigurationsDialog;
import com.top_logic.layout.messagebox.CreateConfigurationsDialog.ModelPartDefinition;
import com.top_logic.layout.structure.DefaultWindowModel;
import com.top_logic.mig.html.layout.tiles.component.I18NConstants;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Utility to open a {@link CreateConfigurationsDialog}.
 * 
 * @see #open(DisplayContext)
 * @see CreateConfigurationsDialog
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class CreateConfigurationsDialogBuilder {

	private DisplayDimension _dialogWidth;

	private DisplayDimension _dialogHeight;

	private HTMLFragment _dialogTitle = null;

	private Function<List<? extends ConfigurationItem>, HandlerResult> _createConfigurationFinisher;

	/**
	 * Creates a new {@link CreateConfigurationsDialogBuilder}.
	 */
	public CreateConfigurationsDialogBuilder(
			Function<List<? extends ConfigurationItem>, HandlerResult> createConfigurationFinisher) {
		_createConfigurationFinisher = createConfigurationFinisher;
	}

	/**
	 * Opens the actual configuration dialog.
	 */
	public HandlerResult open(DisplayContext context) {
		List<ModelPartDefinition<? extends ConfigurationItem>> partDefinitions = new ArrayList<>();
		addModelPartDefinitions(partDefinitions);
		if (partDefinitions.isEmpty()) {
			throw new IllegalStateException("No part definitions added.");
		}

		CreateConfigurationsDialog dialog = createConfigurationsDialog(partDefinitions);
		dialog.setOkHandle(_createConfigurationFinisher);
		if (dialogTitle() != null) {
			((DefaultWindowModel) dialog.getDialogModel()).setWindowTitle(dialogTitle());
		}

		return dialog.openIfNecessaryOrConfirm(context);
	}

	/**
	 * Hook for subclasses to create a custom configurations dialog.
	 */
	protected CreateConfigurationsDialog createConfigurationsDialog(
			List<ModelPartDefinition<? extends ConfigurationItem>> partDefinitions) {
		return new CreateConfigurationsDialog(partDefinitions,
			I18NConstants.TILE_BUILDER_CONFIGURATION_DIALOG.key("title"),
			dialogWidth(), dialogHeight());
	}

	/**
	 * Override to add {@link ModelPartDefinition} to the given list of part definitions.
	 * 
	 * @param partDefinitions
	 *        The {@link ModelPartDefinition}s that are used in the configuration dialog.
	 * 
	 * @see CreateConfigurationsDialog
	 */
	protected abstract void addModelPartDefinitions(
			List<ModelPartDefinition<? extends ConfigurationItem>> partDefinitions);

	/**
	 * Returns the width for the dialog to create new components.
	 */
	public DisplayDimension dialogWidth() {
		return _dialogWidth;
	}

	/**
	 * Setter for {@link #dialogWidth()}
	 */
	public void setDialogWidth(DisplayDimension dialogWidth) {
		_dialogWidth = dialogWidth;
	}

	/**
	 * Returns the height for the dialog to create new components.
	 */
	public DisplayDimension dialogHeight() {
		return _dialogHeight;
	}

	/**
	 * Setter for {@link #dialogHeight()}.
	 */
	public void setDialogHeight(DisplayDimension dialogHeight) {
		_dialogHeight = dialogHeight;
		
	}

	/**
	 * Returns the title for the dialog to create new components.
	 */
	public HTMLFragment dialogTitle() {
		return _dialogTitle;
	}

	/**
	 * Setter for {@link #dialogTitle()}.
	 */
	public void setDialogTitle(HTMLFragment dialogTitle) {
		_dialogTitle = dialogTitle;
		
	}

}
