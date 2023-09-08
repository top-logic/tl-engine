/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.log;

import java.io.File;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.common.folder.FolderDefinition;
import com.top_logic.common.folder.impl.FileDocument;
import com.top_logic.common.folder.impl.TransientFolderDefinition;
import com.top_logic.common.folder.ui.FolderComponent;
import com.top_logic.common.webfolder.ui.WebFolderUIFactory;
import com.top_logic.common.webfolder.ui.commands.UploadDialog.UploadExecutor;
import com.top_logic.layout.Control;
import com.top_logic.layout.folder.FolderData;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.log.LogFilesBuilder;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class LogFolderComponent extends FolderComponent {

	public LogFolderComponent(InstantiationContext context, Config attr) throws ConfigurationException {
		super(context, attr);
	}

	@Override
	protected Control createControlForContext(FormContext context) {
		return WebFolderUIFactory.createControl(getBreadcrumbRenderer(), getFolderData(), context, null);
	}

	@Override
	protected boolean isSupported(Object aModel) {
		return false;
	}

	@Override
	protected String getFolderSize() {
		return "0";
	}

	@Override
	protected ExecutableState getModifyability(FolderDefinition folder) {
		return ExecutableState.NOT_EXEC_DISABLED;
	}

	@Override
	protected ExecutableState getClipability(FolderDefinition folder) {
		return ExecutableState.NOT_EXEC_DISABLED;
	}

	@Override
	protected FolderDefinition getFolderDefinition() {
		// get LogFiles as Folder

		List list = (List) LogFilesBuilder.INSTANCE.getModel(getModel(), null);

		TransientFolderDefinition fileFolder = new TransientFolderDefinition("Logfiles");
		for (Object file : list) {
			fileFolder.add(new FileDocument((File) file));
		}

		return fileFolder;
	}

	@Override
	protected UploadExecutor getUploadExecuter(FolderData folderData) {
		return null;
	}

	@Override
	protected TableConfiguration getTableConfiguration(ExecutableState canAddToClipboard, ExecutableState canUpdate,
			ExecutableState canDelete) {
		TableConfiguration columnDescriptions =
			new LogColumnDescriptionBuilder(canAddToClipboard, canUpdate, canDelete).createLogColumns();
		return columnDescriptions;
	}

	@Override
	protected void becomingInvisible() {
		super.becomingInvisible();
		this.resetFormContext();
	}

}
