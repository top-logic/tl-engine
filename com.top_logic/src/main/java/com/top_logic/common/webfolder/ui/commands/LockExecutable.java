/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui.commands;

import com.top_logic.common.webfolder.model.FolderContent;
import com.top_logic.dsa.DAPropertyNames;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.dsa.DatabaseAccessException;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.component.ComponentUtil;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Utils;

/**
 * Lock an unlocked document (for performing an update later on). 
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class LockExecutable extends AbstractWebfolderAction {

    /**
	 * Creates a {@link LockExecutable}.
	 * 
	 * @param node
	 *        Node representing a document, must not be <code>null</code>.
	 */
	public LockExecutable(FolderContent node) {
    	super(node);
    }

    @Override
	public HandlerResult executeCommand(DisplayContext aContext) {
		Document document = getDocument();
		if (!ComponentUtil.isValid(document)) {
			return ComponentUtil.errorObjectDeleted(aContext);
		}

    	HandlerResult theResult = new HandlerResult();
		{
            DataAccessProxy theDAP = document.getDAP();

            if (!theDAP.lock()) {
				theResult.addError(I18NConstants.LOCK_NOT_POSSIBLE_BECAUSE_ALREADY_LOCKED);
            }
		}

        return theResult;
    }

    @Override
    protected ExecutableState calculateExecutability() {
    	if (isLink()) {
    		return ExecutableState.NOT_EXEC_HIDDEN;
    	}

		Document document = getDocument();
		if (document == null) {
			return ExecutableState.NOT_EXEC_HIDDEN;
		}

		{
			DataAccessProxy theDAP = document.getDAP();

            if (LockExecutable.isLocked(theDAP)) {
                return ExecutableState.NOT_EXEC_HIDDEN;
            }
            else {
                return ExecutableState.EXECUTABLE;
            }
        }
    }

    /** 
     * Check, if the given document is locked.
     * 
     * @param    aDAP    The accessing proxy to the repository, must not be <code>null</code>.
     * @return   <code>true</code> if document is locked, <code>false</code> otherwise.
     * @throws   DatabaseAccessException     If getting properties from given {@link DataAccessProxy}.
     */
    public static boolean isLocked(DataAccessProxy aDAP) throws DatabaseAccessException {
    	final Boolean locked = DataAccessProxy.getProperty(aDAP, DAPropertyNames.LOCKED, Boolean.class);
    	return Utils.isTrue(locked);
    }
    
    /** 
     * Return the ID of the {@link Person} locking the given document.
     * 
     * @param    aDAP    The accessing proxy to the repository, must not be <code>null</code>.
     * @return   The ID of the {@link Person}, may be <code>null</code> if document isn't locked.
     */
    public static String getLocker(DataAccessProxy aDAP) {
        return DataAccessProxy.getProperty(aDAP, DAPropertyNames.LOCKER, String.class);
    }
}