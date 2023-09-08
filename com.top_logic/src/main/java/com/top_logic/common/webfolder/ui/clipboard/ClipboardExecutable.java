/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui.clipboard;



import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.form.model.AbstractDynamicCommand;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Provides a dialog for adding objects from the clipboard to the current {@link WebFolder}. 
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ClipboardExecutable extends AbstractDynamicCommand {
    
    private SingleSelectionModel selectionModel;

    /**
	 * Creates a {@link ClipboardExecutable}.
	 */
    public ClipboardExecutable(SingleSelectionModel aSelectionModel) {
        if (aSelectionModel == null) {
            throw new IllegalArgumentException("Given selection model is null.");
        }

        this.selectionModel = aSelectionModel;
    }

    @Override
	public HandlerResult executeCommand(DisplayContext aContext) {
        WebFolder webFolder = (WebFolder) ((TLTreeNode) this.selectionModel.getSingleSelection()).getBusinessObject();

		ClipboardDialog.createDialog(webFolder).open(aContext);

        return HandlerResult.DEFAULT_RESULT;
    }

    @Override
    protected ExecutableState calculateExecutability() {
        return ExecutableState.EXECUTABLE;
    }
}