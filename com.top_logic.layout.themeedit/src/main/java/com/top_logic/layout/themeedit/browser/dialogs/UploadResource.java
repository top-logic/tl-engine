/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.dialogs;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.layout.table.model.EditableRowTableModel;
import com.top_logic.layout.themeedit.browser.resource.ThemeResource;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Stores the newly uploaded resource into the selected theme folder.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class UploadResource extends AbstractCommandHandler {

	/**
	 * Creates a {@link UploadResource}.
	 */
	public UploadResource(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent component, Object model,
			Map<String, Object> someArguments) {
		try {
			ThemeResource themeResource = (ThemeResource) model;

			String resourcePath = getResourcePathOfThemeResource(themeResource);
			themeResource.addThemeFile(themeResource.getTheme(), resourcePath + "/");

			updateView(component, resourcePath);

			component.closeDialog();
		} catch (IOException exception) {
			throw new IOError(exception);
		}

		return HandlerResult.DEFAULT_RESULT;
	}

	private void updateView(LayoutComponent aComponent, String selectedResourcePath) throws IOException {
		UploadForm uploadForm = getResourceUploadForm(aComponent);
		String newThemeResourcePath = createNewThemeResourceFile(uploadForm, selectedResourcePath);

		updateTableView(aComponent, newThemeResourcePath);
	}

	private String getResourcePathOfThemeResource(ThemeResource themeResource) {
		return themeResource.getTheme().getPathEffective() + themeResource.getThemeKey();
	}

	private String createNewThemeResourceFile(UploadForm uploadForm, String selectedDirectoryFile) throws IOException {
		String resourcePath = selectedDirectoryFile + "/" + uploadForm.getData().getName();

		FileManager fileManager = FileManager.getInstance();

		File resourceFile = fileManager.getIDEFile(resourcePath);
		resourceFile.getParentFile().mkdirs();
		resourceFile.createNewFile();
		FileUtilities.copyToFile(uploadForm.getData(), resourceFile);

		return resourcePath;
	}

	private UploadForm getResourceUploadForm(LayoutComponent aComponent) {
		FormComponent form = (FormComponent) aComponent;

		return (UploadForm) EditorFactory.getModel(form.getFormContext());
	}

	private void updateTableView(LayoutComponent aComponent, String themeResourcePath) {
		TableComponent table = (TableComponent) aComponent.getDialogParent();

		table.invalidate();

		selectThemeResource(themeResourcePath, table);
	}

	private void selectThemeResource(String themeResourcePath, TableComponent table) {
		findTableThemeResourceByName(themeResourcePath, table).ifPresent(themeResource -> {
			table.setSelected(themeResource);
		});
	}

	private Optional<ThemeResource> findTableThemeResourceByName(String themeResourcePath, TableComponent table) {
		return getAllTableThemeResources(table).stream().filter(themeResource -> {
			return themeResource.getThemeFiles().get(0).getResourcePath().equals(themeResourcePath);
		}).findFirst();
	}

	@SuppressWarnings("unchecked")
	private Collection<ThemeResource> getAllTableThemeResources(TableComponent table) {
		EditableRowTableModel tableModel = table.getTableModel();

		return tableModel.getAllRows();
	}

}
