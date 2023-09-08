/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.folder;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.common.folder.model.FolderNode;
import com.top_logic.common.folder.ui.commands.ContentDownload;
import com.top_logic.common.folder.ui.commands.Icons;
import com.top_logic.common.webfolder.WebFolderUtils;
import com.top_logic.common.webfolder.model.WebFolderAccessor;
import com.top_logic.common.webfolder.ui.I18NConstants;
import com.top_logic.common.webfolder.ui.NotExecutableListener;
import com.top_logic.common.webfolder.ui.WebFolderFieldProvider;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModelUtilities;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.HiddenField;
import com.top_logic.layout.table.model.FieldProvider;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Field provider for FolderNodes.
 *
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class FolderFieldProvider implements FieldProvider {


	private final ExecutableState allowClipboard;
	private final ExecutableState allowWrite;
	private final ExecutableState allowDelete;

	/**
	 * new instance
	 * 
	 * @param allowClipboard
	 *        is the usage of clipboard allowed
	 * @param allowWrite
	 *        may new content be created
	 * @param allowDelete
	 *        may content be deleted
	 */
	public FolderFieldProvider(ExecutableState allowClipboard, ExecutableState allowWrite,
			ExecutableState allowDelete) {
		this.allowClipboard = allowClipboard;
		this.allowWrite = allowWrite;
		this.allowDelete = allowDelete;
	}

	@Override
	public FormMember createField(Object aModel, Accessor anAccessor, String aProperty) {
		String fieldName = getFieldName(aModel, anAccessor, aProperty);

		if (WebFolderAccessor.DELETE.equals(aProperty)) {
			return this.createDeleteField(fieldName, aModel);
		}
		else if (WebFolderAccessor.DOWNLOAD.equals(aProperty)) {
			return this.createDownloadField(fieldName, aModel);
        }
        else { 
			throw new RuntimeException("Can not provide field for " + aProperty);
        }
    }


	protected FormMember createDownloadField(String name, Object userObject) {

		if (userObject instanceof FolderNode) {
			userObject = ((FolderNode) userObject).getBusinessObject();
		}

		if (userObject instanceof FolderDefinition) {
			return folderDownload(name, userObject);
		} else if (userObject instanceof BinaryDataSource) {
			return contentDownload(name, userObject);
		}
		else {
			HiddenField hiddenField = FormFactory.newHiddenField(name);
			/* Set here empty label: For a HiddenField no control is rendered. Then a
			 * "fallback renderer" is used which is de facto the ResourceRenderer which renders the
			 * label for an form field. */
			hiddenField.setLabel(StringServices.EMPTY_STRING);
			return hiddenField;
		}
	}

	/**
	 * download of folder, hidden per default, is activated for {@link WebFolder}
	 * 
	 * @return field for folder download
	 */
	protected FormMember folderDownload(String name, Object userObject) {
		return FormFactory.newHiddenField(name);
	}

	protected FormMember contentDownload(String name, Object userObject) {
		Command theExecutable = getDownloadExecutable(userObject);

		CommandField downloadCommand =
			WebFolderFieldProvider.createField(name, theExecutable, com.top_logic.tool.export.Icons.DOWNLOAD,
				com.top_logic.tool.export.Icons.DOWNLOAD_DISABLED);

		// When the corresponding form context is immutable it would be not
		// possible to execute the command. As download is essentially the
		// view of the document it must be also possible in that case.
		downloadCommand.setInheritDeactivation(false);

		return downloadCommand;
	}

	protected Command getDownloadExecutable(Object userObject) {
		if (userObject instanceof BinaryDataSource) {
			return new ContentDownload((BinaryDataSource) userObject);
		}
		throw new RuntimeException("can not create download executable for " + userObject);
	}


	/**
	 * the field to delete aModel
	 */
	protected FormMember createDeleteField(String name, Object aModel) {

		Command theExecutable = getDeleteExecutable(name, aModel);

		FormGroup wrapedField = new FormGroup(name, WebFolderUtils.DEFAULT_WEBFOLDER_RESOURCES);
		CommandField deleteField =
			WebFolderFieldProvider.createField(name, theExecutable, Icons.REMOVE_NODE,
				Icons.REMOVE_NODE_DISABLED);
		wrapedField.addMember(deleteField);
		if (this.allowDelete.isExecutable()) {
			NotExecutableListener.createNotExecutableListener(I18NConstants.FIELD_DISABLED, deleteField).addAsListener(
				wrapedField);
		} else {
			CommandModelUtilities.applyExecutability(this.allowDelete, deleteField);
		}
		return wrapedField;
	}

	/**
	 * an executable to delete the given model, null means that delete is not possible
	 */
	protected Command getDeleteExecutable(String name, Object aModel) {
		return null;
	}

	protected ExecutableState getAllowClipboard() {
		return (allowClipboard);
	}

	protected ExecutableState getAllowWrite() {
		return (allowWrite);
	}

	protected ExecutableState getAllowDelete() {
		return (allowDelete);
	}

}

