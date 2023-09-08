/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui.commands;

import com.top_logic.basic.Logger;
import com.top_logic.basic.Named;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.common.folder.FolderDefinition;
import com.top_logic.common.folder.ui.commands.AbstractFolderDelete;
import com.top_logic.common.webfolder.model.FolderContent;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.component.ComponentUtil;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Delete an object from a web folder.
 * 
 * Depending on the type of connection between the folder the object,
 * this class will only remove an association or delete a document with
 * it's content from the server.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class WebFolderDeleteExecutable extends AbstractFolderDelete {

	/**
	 * Creates a new {@link WebFolderDeleteExecutable}.
	 * 
	 * @param node
	 *        See {@link AbstractWebfolderAction#AbstractWebfolderAction(FolderContent)}.
	 */
	public WebFolderDeleteExecutable(FolderContent node) {
    	super(node);
    }
    
	@Override
	protected Command getCommand(final Named aContent, final FolderDefinition aFolder) {
		Command command = new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext commandContext) {
				Wrapper deletedObject = (Wrapper) aContent;
				if (!ComponentUtil.isValid(deletedObject)) {
					return ComponentUtil.errorObjectDeleted(commandContext);
				}

				HandlerResult theResult = new HandlerResult();
				KnowledgeBase theKB = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase();

				Transaction theTX = theKB.beginTransaction();
				try {
					final WebFolder folder = (WebFolder) aFolder;
					assert folder != null : "The node this " + AbstractFolderDelete.class.getName() + " [" + this
						+ "] belongs to, was removed from its parent node.";
					boolean hasChanged = folder.remove(deletedObject);

					if (hasChanged) {
						theTX.commit();
					}
				} catch (KnowledgeBaseException ex) {
					theResult.addErrorText("Failed to commit deletion.");

					Logger.error("Failed to commit deletion '" + deletedObject + "'.", ex, this);
				}
				finally {
					theTX.rollback();
				}

				return theResult;
			}
		};
		return command;
	}

	@Override
	protected boolean isLink(Named aContent, FolderDefinition aFolder) {
		return aFolder.isLinkedContent(aContent);
	}

	@Override
	protected boolean isValid(Named aContent) {
		return ComponentUtil.isValid(aContent);
	}

	@Override
	protected boolean isLocked(BinaryDataSource contentObject) {
		DataAccessProxy theDAP = ((Wrapper) contentObject).getDAP();
		return LockExecutable.isLocked(theDAP);
	}

}