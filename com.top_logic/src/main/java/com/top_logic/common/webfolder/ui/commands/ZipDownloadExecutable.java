/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui.commands;


import com.top_logic.common.webfolder.ui.WebFolderUIFactory;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.component.ComponentUtil;
import com.top_logic.layout.form.model.AbstractDynamicCommand;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Provides a dialog for creating a new folder in an existing {@link WebFolder}. 
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ZipDownloadExecutable extends AbstractDynamicCommand {

    private SingleSelectionModel selectionModel;

	/**
	 * Creates a {@link ZipDownloadExecutable}.
	 */
    public ZipDownloadExecutable(SingleSelectionModel aSelectionModel) {
        if (aSelectionModel == null) {
            throw new IllegalArgumentException("Given selection model is null.");
        }

        this.selectionModel = aSelectionModel;
    }

    @Override
	public HandlerResult executeCommand(DisplayContext aContext) {
		WebFolder webFolder = getWebFolder();
		if (!ComponentUtil.isValid(webFolder)) {
			return ComponentUtil.errorObjectDeleted(aContext);
		}
		return new ZipDownloadDialog(webFolder).open(aContext);
    }

	private WebFolder getWebFolder() {
		Object theSelection = this.selectionModel.getSingleSelection();
		WebFolder webFolder =
			(theSelection instanceof WebFolder) ? (WebFolder) theSelection : (WebFolder) ((TLTreeNode) theSelection)
				.getBusinessObject();
		return webFolder;
	}

    @Override
    protected ExecutableState calculateExecutability() {
		WebFolder webFolder = getWebFolder();
		if (!ComponentUtil.isValid(webFolder)) {
			return ExecutableState.NOT_EXEC_HIDDEN;
		}
		FolderInfo folderInfo = new FolderInfo(webFolder);
		if (folderInfo.isTooLarge()) {
			return ExecutableState.createDisabledState(
				I18NConstants.ZIP_DOWNLOAD_FOLDER_DIALOG_DISABLED__LIMIT.fill(
				WebFolderUIFactory.getInstance().getZipDownloadSizeLimit()));
		}
		if (folderInfo.isEmpty()) {
			return ExecutableState.createDisabledState(I18NConstants.ZIP_DOWNLOAD_FOLDER_DIALOG_DISABLED_EMPTY);
		}
        return ExecutableState.EXECUTABLE;
    }
}