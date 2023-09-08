/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.folder.ui.commands;

import com.top_logic.basic.Named;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.common.folder.FolderDefinition;
import com.top_logic.common.webfolder.model.FolderContent;
import com.top_logic.common.webfolder.ui.commands.I18NConstants;
import com.top_logic.knowledge.wrap.exceptions.WrapperRuntimeException;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.component.ComponentUtil;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Abstract superclass to provide messages to delete the content and this
 * {@link AbstractFolderAction}.
 * 
 * @author <a href="mailto:fma@top-logic.com">fma</a>
 */
public abstract class AbstractFolderDelete extends AbstractFolderAction {

	private static final ExecutableState LOCKED = ExecutableState
		.createDisabledState(I18NConstants.MSG_DOCUMENT_LOCKED);

	private static final ExecutableState NOT_EMPTY = ExecutableState
		.createDisabledState(I18NConstants.MSG_FOLDER_NOT_EMPTY);

	public AbstractFolderDelete(FolderContent node) {
		super(node);
	}

	@Override
	public HandlerResult executeCommand(DisplayContext aContext) {
		ResKey1 messageKey;
		Named content = this.getContent();
		FolderDefinition folder = getFolder();
		if (!isValid(content)) {
			return ComponentUtil.errorObjectDeleted(aContext);
		}

		if (isLink(content, folder)) {
			messageKey = I18NConstants.CONFIRM_DELETE_LINK__NAME;
		}
		else if (content instanceof FolderDefinition) {
			messageKey = I18NConstants.CONFIRM_DELETE_FOLDER__NAME;
		}
		else {
			messageKey = I18NConstants.CONFIRM_DELETE_DOCUMENT__NAME;
		}

		Command theCommand = getCommand(content, getFolder());
		String theMessage = aContext.getResources().getString(messageKey.fill(content.getName()));

		return MessageBox.confirm(aContext, MessageType.CONFIRM, theMessage,
			MessageBox.button(ButtonType.YES, theCommand), MessageBox.button(ButtonType.NO));
	}


	@Override
	protected ExecutableState calculateExecutability() {
		try{
			Named content = getContent();
			final FolderDefinition folder = getFolder();
			if (folder == null) {
				// Node is not longer part of its model. This can happen for example when the
				// structure of its former parent node is recomputed. As the node is still displayed
				// on the client, the executability is recomputed.
				return ExecutableState.NOT_EXEC_HIDDEN;
			}
			if (isLink(content, folder)) {
				return ExecutableState.EXECUTABLE;
			}

			if (content instanceof BinaryDataSource) {
				if (isLocked((BinaryDataSource) content)) {
					return LOCKED;
				}
				else {
					return ExecutableState.EXECUTABLE;
				}
			} else if (content instanceof FolderDefinition) {
				if (((FolderDefinition) content).getContents().size() == 0) {
					return ExecutableState.EXECUTABLE;
				} else {
					return NOT_EMPTY;
				}
			} else {
				return ExecutableState.EXECUTABLE;
			}
		}
		catch (WrapperRuntimeException e) {
			return ExecutableState.NOT_EXEC_HIDDEN; 
		}
	}


	protected abstract boolean isLocked(BinaryDataSource contentObject);

	protected abstract Command getCommand(final Named aContent, final FolderDefinition aFolder);

	protected abstract boolean isLink(Named content, FolderDefinition folder);

	protected abstract boolean isValid(Named content);

}
