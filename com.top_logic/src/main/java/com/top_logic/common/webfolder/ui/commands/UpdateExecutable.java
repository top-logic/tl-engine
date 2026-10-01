/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui.commands;


import com.top_logic.common.webfolder.model.FolderContent;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.component.ComponentUtil;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Provides a dialog for updating an existing document on the server. 
 * 
 * <p>
 * An update creates a new revision of the document.
 * </p>
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class UpdateExecutable extends AbstractWebfolderAction {

	/**
	 * Creates a {@link UpdateExecutable}.
	 * 
	 * @param node
	 *        See {@link AbstractWebfolderAction#AbstractWebfolderAction(FolderContent)}.
	 * @throws IllegalArgumentException
	 *         If given document is <code>null</code>.
	 */
	public UpdateExecutable(FolderContent node) {
    	super(node);
    }

    @Override
	public HandlerResult executeCommand(DisplayContext aContext) {
        Document document = this.getDocument();
		return new UpdateDialog(document).open(aContext);
    }

    @Override
    protected ExecutableState calculateExecutability() {
    	if (isLink()) {
    		return ExecutableState.NOT_EXEC_HIDDEN;
    	}
		if (!ComponentUtil.isValid(getContentObject())) {
			return ExecutableState.NO_EXEC_INVALID;
		}
		return ExecutableState.EXECUTABLE;
	}

}
