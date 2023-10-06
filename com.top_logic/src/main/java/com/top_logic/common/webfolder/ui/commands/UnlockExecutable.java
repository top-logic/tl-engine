/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui.commands;

import com.top_logic.basic.Logger;
import com.top_logic.common.webfolder.model.FolderContent;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.component.ComponentUtil;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.TLContext;

/**
 * Unlock a locked document (when no update is performed). 
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class UnlockExecutable extends AbstractWebfolderAction {

    /**
	 * Creates a {@link UnlockExecutable}.
	 * 
	 * @param node
	 *        See {@link AbstractWebfolderAction#AbstractWebfolderAction(FolderContent)}.
	 */
	public UnlockExecutable(FolderContent node) {
    	super(node);
    }

    @Override
	public HandlerResult executeCommand(DisplayContext aContext) {
		Document document = this.getDocument();
		if (!ComponentUtil.isValid(document)) {
			return ComponentUtil.errorObjectDeleted(aContext);
		}

    	HandlerResult theResult = new HandlerResult();
		try {
            DataAccessProxy theDAP = document.getDAP();

            if (!theDAP.unlock()) {
				theResult.addErrorText("Unlocking document failed in repository");
            }
        }
        catch (Exception ex) {
			theResult.addErrorText("Failed to unlock document");

            Logger.error("Failed to unlock document '" + document + "'!", ex, this);
        }

        return theResult;
    }

    @Override
    protected ExecutableState calculateExecutability() {
    	if (isLink()) {
    		return ExecutableState.NOT_EXEC_HIDDEN;
    	}

		{
            Document document = getDocument();
			if (document == null) {
				return ExecutableState.NOT_EXEC_HIDDEN;
			}

			DataAccessProxy theDAP = document.getDAP();
            if (LockExecutable.isLocked(theDAP)) {
                if (!TLContext.isAdmin()) {
                    String theName = LockExecutable.getLocker(theDAP);
                    String theUser = TLContext.getContext().getCurrentUserName();

                    if (!theUser.equals(theName)) {
                        Person thePerson = Person.byName(theName);

                        if (thePerson != null) {
                            theName = thePerson.getFullName();
                        }

						return ExecutableState
							.createDisabledState(I18NConstants.MSG_ALREADY_LOCKED__USER.fill(theName));
                    }
                }

                return ExecutableState.EXECUTABLE;
            }
            else {
                return ExecutableState.NOT_EXEC_HIDDEN;
            }
		}
    }

}