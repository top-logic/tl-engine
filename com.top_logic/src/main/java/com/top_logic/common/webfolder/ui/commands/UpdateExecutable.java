/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui.commands;


import com.top_logic.common.webfolder.model.FolderContent;
import com.top_logic.common.webfolder.ui.WebFolderUIFactory;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.component.ComponentUtil;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;

/**
 * Provides a dialog for updating an existing document on the server. 
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class UpdateExecutable extends AbstractWebfolderAction {

    /** Update is not allowed, because document isn't locked. */
	private static final ExecutableState NO_ACTION = ExecutableState.createDisabledState(I18NConstants.MSG_NOT_LOCKED);

    private final boolean _manualLocking;

	/**
	 * Creates a {@link UpdateExecutable}.
	 * 
	 * @param node
	 *        See {@link AbstractWebfolderAction#AbstractWebfolderAction(FolderContent)}.
	 * @throws IllegalArgumentException
	 *         If given document is <code>null</code>.
	 */
	public UpdateExecutable(FolderContent node, boolean manualLocking) {
    	super(node);
		_manualLocking = manualLocking;
    }

    @Override
	public HandlerResult executeCommand(DisplayContext aContext) {
        Document document = this.getDocument();
		if (!ComponentUtil.isValid(document)) {
			return ComponentUtil.errorObjectDeleted(aContext);
		}
		if (!getManualLocking()) {
			boolean success = document.getDAP().lock();
			if (!success) {
				return HandlerResult.error(I18NConstants.UPDATE_NOT_POSSIBLE_BECAUSE_LOCKING_FAILED);
			}
			registerUnlockDocumentOnLogoutListener(document);
		}
		return new UpdateDialog(document, getManualLocking()).open(aContext);
    }

	private void registerUnlockDocumentOnLogoutListener(Document document) {
		TLContextManager.getSubSession().addUnboundListener(new UnlockDocumentOnLogoutListener(document));
	}

    @Override
    protected ExecutableState calculateExecutability() {
    	if (isLink()) {
    		return ExecutableState.NOT_EXEC_HIDDEN;
    	}
		DataAccessProxy theDAP = this.getDocument().getDAP();
		if (LockExecutable.isLocked(theDAP)) {
			return calculateExecutabilityWhenLocked(theDAP);
		}
		if (getManualLocking()) {
			return UpdateExecutable.NO_ACTION;
		}
		return ExecutableState.EXECUTABLE;
	}

	private ExecutableState calculateExecutabilityWhenLocked(DataAccessProxy theDAP) {
		if (TLContext.isAdmin()) {
			return ExecutableState.EXECUTABLE;
		}
		String theName = LockExecutable.getLocker(theDAP);
		String theUser = TLContext.getContext().getCurrentUserName();

		if (theUser.equals(theName)) {
			return ExecutableState.EXECUTABLE;
		}
		if (getManualLocking()) {
			return ExecutableState.NOT_EXEC_HIDDEN;
		}
		return ExecutableState.createDisabledState(I18NConstants.UPDATE_NOT_POSSIBLE_BECAUSE_LOCKED);
	}

	/**
	 * @see WebFolderUIFactory#getManualLocking()
	 */
	protected boolean getManualLocking() {
		return _manualLocking;
	}

}