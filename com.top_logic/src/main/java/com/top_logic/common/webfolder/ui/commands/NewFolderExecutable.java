/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui.commands;


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
public class NewFolderExecutable extends AbstractDynamicCommand {

    private SingleSelectionModel selectionModel;

    /** 
     * Creates a {@link NewFolderExecutable}.
     */
    public NewFolderExecutable(SingleSelectionModel aSelectionModel) {
        if (aSelectionModel == null) {
            throw new IllegalArgumentException("Given selection model is null.");
        }

        this.selectionModel = aSelectionModel;
    }

    @Override
	public HandlerResult executeCommand(DisplayContext aContext) {
        WebFolder webFolder = (WebFolder) ((TLTreeNode) this.selectionModel.getSingleSelection()).getBusinessObject();
		if (!ComponentUtil.isValid(webFolder)) {
			return ComponentUtil.errorObjectDeleted(aContext);
		}

		new NewFolderDialog(webFolder).open(aContext);

        return HandlerResult.DEFAULT_RESULT;
    }

    @Override
    protected ExecutableState calculateExecutability() {
        return ExecutableState.EXECUTABLE;
    }
}