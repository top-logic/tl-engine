/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers.resources;

import java.io.IOError;
import java.io.IOException;

import com.top_logic.gui.ThemeUtil;
import com.top_logic.gui.config.ThemeConfig;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.themeedit.browser.resource.ThemeResource;
import com.top_logic.layout.tree.component.TreeComponent;
import com.top_logic.mig.html.layout.ModelEventListener;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Command to create an resource folder.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class CreateResourceFolderCommand implements Command {

	private CreateResourceFolderDialog _resourceFolderDialog;

	/**
	 * Creates a {@link CreateResourceFolderCommand} for the given {@link CreateResourceFolderDialog}.
	 */
	public CreateResourceFolderCommand(CreateResourceFolderDialog dialog) {
		_resourceFolderDialog = dialog;
	}

	@Override
	public HandlerResult executeCommand(DisplayContext context) {
		FormContext formContext = _resourceFolderDialog.getFormContext();
		HandlerResult handlerResult = new HandlerResult();

		if (formContext.checkAll()) {
			ThemeResource themeResource = createTransientThemeResource(getResourcePathForNewFolder());

			updateTreeView(context, themeResource);

			handlerResult = HandlerResult.DEFAULT_RESULT;
		} else {
			AbstractApplyCommandHandler.fillHandlerResultWithErrors(formContext, handlerResult);
		}

		return handlerResult;
	}

	private ThemeResource createTransientThemeResource(String resourcePath) {
		ThemeResource themeResource = getParentThemeResource();

		ThemeResource newThemeResource = new ThemeResource(themeResource.getTheme(), themeResource, null);
		newThemeResource.addThemeFile(themeResource.getDefiningTheme(), resourcePath);

		return newThemeResource;
	}

	private ThemeResource getParentThemeResource() throws IOError {
		TreeComponent treeComponent = getTreeComponent();
		ThemeResource themeResource = (ThemeResource) treeComponent.getSelected();

		if (themeResource == null) {
			themeResource = createTransientRootThemeResource(treeComponent);

			treeComponent.resetTreeModel();
		}

		return themeResource;
	}

	private ThemeResource createTransientRootThemeResource(TreeComponent component) throws IOError {
		try {
			ThemeConfig themeConfig = (ThemeConfig) component.getModel();

			return ThemeResource.getResources(themeConfig, false);
		} catch (IOException exception) {
			throw new IOError(exception);
		}
	}

	private String getResourcePathForNewFolder() {
		return getResourcePathOfSelection() + "/" + _resourceFolderDialog.getResourceFolderPathField().getAsString();
	}

	private String getResourcePathOfSelection() throws IOError {
		ThemeResource selectedThemeResource = getSelectedThemeResource();

		if (selectedThemeResource != null) {
			return selectedThemeResource.getThemeFiles().get(0).getResourcePath();
		} else {
			return ThemeUtil.getThemeResourcesPath(getSelectedThemeId());
		}
	}

	private String getSelectedThemeId() {
		return ((ThemeConfig) getTreeComponent().getModel()).getId();
	}

	private ThemeResource getSelectedThemeResource() {
		return (ThemeResource) getTreeComponent().getSelected();
	}

	private void updateTreeView(DisplayContext context, ThemeResource themeResource) {
		TreeComponent component = getTreeComponent();

		component.handleModelEvent(themeResource, null, ModelEventListener.MODEL_CREATED);
		component.setSelected(themeResource);

		_resourceFolderDialog.getDialogModel().getCloseAction().executeCommand(context);
	}

	private TreeComponent getTreeComponent() {
		return _resourceFolderDialog.getComponent();
	}
}