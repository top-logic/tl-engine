/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui.commands;


import com.top_logic.common.folder.FolderDefinition;
import com.top_logic.common.webfolder.ui.commands.UploadDialog.UploadExecutor;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.component.ComponentUtil;
import com.top_logic.layout.form.model.AbstractDynamicCommand;
import com.top_logic.layout.messagebox.AbstractFormDialogBase;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Provides a dialog for uploading a new document to the server. 
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class UploadExecutable extends AbstractDynamicCommand {

    /** The web folder to upload the documents to. */
    private final SingleSelectionModel selectionModel;

	private final UploadExecutor executor;

    /**
	 * Creates a {@link UploadExecutable}.
	 * 
	 * @throws IllegalArgumentException
	 *         If given folder is <code>null</code>.
	 */
	public UploadExecutable(SingleSelectionModel aSelectionModel, UploadExecutor executor) {
		if (aSelectionModel == null) {
            throw new IllegalArgumentException("Given selection model is null.");
        }

		this.executor = executor;
        this.selectionModel = aSelectionModel;
    }

    @Override
	public HandlerResult executeCommand(DisplayContext aContext) {
    	FolderDefinition     theFolder  = (FolderDefinition) ((TLTreeNode) this.selectionModel.getSingleSelection()).getBusinessObject();
		if (!ComponentUtil.isValid(theFolder)) {
			return ComponentUtil.errorObjectDeleted(aContext);
		}

		AbstractFormDialogBase dialog = getUploadDialog(theFolder);
		return dialog.open(aContext);
		
    }

	private AbstractFormDialogBase getUploadDialog(FolderDefinition theFolder) {
		return executor.createUploadDialog(I18NConstants.UPLOAD_DIALOG, theFolder);
	}

    @Override
    protected ExecutableState calculateExecutability() {
        return ExecutableState.EXECUTABLE;
    }
}