/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import com.top_logic.basic.Settings;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.DocumentVersion;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResourceView;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FolderField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.tag.TextInputTag;
import com.top_logic.model.TLObject;
import com.top_logic.util.Zipper;

/**
 * Contains utility methods for displaying {@link WebFolder}s
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class WebFolderUtils {

	/**
	 * {@link ResourceView} which provides default values for the elements in
	 * the web folder view, e.g. the buttons to delete or upload documents, or
	 * to attach to clipboard.
	 */
	public static final ResourceView DEFAULT_WEBFOLDER_RESOURCES = com.top_logic.common.webfolder.ui.I18NConstants.TABLE;
	
	
	/**
	 * {@link ResourceView} which provides default values for elements in the table which contains
	 * the content of {@link WebFolder}s.
	 */
	public static final ResourceView DEFAULT_WEBFOLDER_TABLE_RESOURCES = DEFAULT_WEBFOLDER_RESOURCES;
	
	/**
	 * Utility method to update the {@link FolderField}s in {@link FormHandler}s
	 * when the model of it had changed.
	 * 
	 * @param form
	 *        the handler which contains the {@link FolderField}s to for update.
	 *        Must not be <code>null</code>.
	 * @param aModel
	 *        the changed model. In case it is neither a {@link WebFolder} nor a
	 *        {@link Document} nothing happens.
	 */
	public static void updateWebfolder(FormHandler form, Object aModel) {
		if (aModel instanceof DocumentVersion) {
			aModel = WrapperHistoryUtils.getCurrent(((DocumentVersion) aModel).getDocument());
		}
		if ((aModel instanceof WebFolder) || (aModel instanceof Document)) {
			if (form.hasFormContext()) {
				FormContext theCtx = form.getFormContext();
				Iterator<? extends FormMember> members = theCtx.getDescendants();
	
				while (members.hasNext()) {
					FormMember member = members.next();
					if (member instanceof FolderField) {
						((FolderField) member).updateViewState(aModel);
					}
				}
			}
		}
	}

	/**
	 * Utility method to update the description of a {@link DocumentVersion} when a new one is
	 * uploaded.
	 * 
	 * @param document
	 *        The {@link Document} whose current {@link DocumentVersion} description shall be
	 *        updated.
	 * @param formContext
	 *        The {@link FormContext}.
	 */
	public static void updateDescription(Document document, FormContext formContext) {
		if (formContext.hasMember(DocumentVersion.DESCRIPTION)) {
			FormField descriptionField = formContext.getField(DocumentVersion.DESCRIPTION);
			String description = descriptionField.getValue().toString();
			document.getDocumentVersion().setDescription(description);
		}
	}

	/**
	 * Multiline {@link StringField} for description of a {@link DocumentVersion}.
	 * 
	 * @param name
	 *        Name of the description field.
	 * 
	 * @param rows
	 *        Number of displayed rows in the field.
	 * 
	 * @return multiline {@link StringField}.
	 */
	public static StringField createDescriptionField(String name, int rows) {
		StringField stringField = FormFactory.newStringField(name);
		TextInputTag multiLine = new TextInputTag();
		multiLine.setMultiLine(true);
		multiLine.setRows(rows);
		stringField.setControlProvider(multiLine);
		return stringField;
	}

	/**
	 * Copy all current document versions of {@link WebFolder} into destination directory.
	 * 
	 * @param folder
	 *        Folder which should be copied.
	 * @param destinationDir
	 *        Existing directory where the contents are copied to.
	 * @param recursive
	 *        if <code>true</code>, subfolders of folder are copied, too.
	 * @param createEmptyDirs
	 *        if <code>true</code>, subfolders will be created, even if they and all of their
	 *        subfolders are empty
	 */
	public static void copyContents(WebFolder folder, File destinationDir, boolean recursive, boolean createEmptyDirs)
			throws IOException {
		if (!destinationDir.isDirectory()) {
			throw new IllegalArgumentException(destinationDir.getAbsolutePath()
				+ " does not exist or is not a directory");
		}

		Collection<WebFolder> subfolders = new HashSet<>();
		Collection<? extends TLObject> content = folder.getOwnedContent();
		for (TLObject wrapper : content) {
			if (wrapper instanceof Document) {
				Document document = (Document) wrapper;
				copyDocument(document, destinationDir);
			} else if (wrapper instanceof WebFolder) {
				subfolders.add((WebFolder) wrapper);
			}
			// ignore other things in folders
		}

		if (recursive) {
			for (WebFolder subfolder : subfolders) {
				boolean isEmpty = !hasDocuments(subfolder, recursive);

				if (isEmpty && !createEmptyDirs) {
					continue;
				}

				String name = subfolder.getName();
				File directory = new File(destinationDir, name);
				if (!directory.isDirectory() && !directory.mkdir()) {
					throw new IOException("Failed to create directory " + directory.getAbsolutePath());
				}

				copyContents(subfolder, directory, recursive, createEmptyDirs);
			}
		}
	}

	/**
	 * Search the folder and all subfolders for {@link Document}s
	 */
	private static boolean hasDocuments(WebFolder folder, boolean recursive) {
		Collection<WebFolder> subfolders = new HashSet<>();
		Collection<? extends TLObject> contents = folder.getOwnedContent();
		for (TLObject content : contents) {
			if (content instanceof Document) {
				return true;
			} else if (content instanceof WebFolder) {
				subfolders.add((WebFolder) content);
			}
		}
		
		if (recursive) {
			// lookup contents of all subfolders
			for (WebFolder subfolder : subfolders) {
				if (hasDocuments(subfolder, recursive)) {
					return true;
				}
			}
		}
		return false;
	}

	private static void copyDocument(Document document, File destinationDir) throws IOException {
		String name = document.getName();
		File destination = new File(destinationDir, name);
		InputStream input = document.getStream();
		try {
			FileUtilities.copyToFile(input, destination);
		} finally {
			input.close();
		}
	}

	/**
	 * Add all contents of the folders and their subfolders to a zipped file.
	 * 
	 * @param folders
	 *        folders that should be zipped
	 * @param folderNames
	 *        names of the folders, as they should appear in the zip file
	 */
	public static File zipWebfolders(LabelProvider folderNames, WebFolder... folders) throws IOException {
		File tmpDir = Settings.getInstance().getTempDir();
		File zipFile = File.createTempFile("folderContent", ".zip", tmpDir);
		try (Zipper zipper = new Zipper(zipFile)) {
			for (int i=0, cnt=folders.length; i<cnt; i++) {
				addRecursive(folders[i], folderNames.getLabel(folders[i]), folderNames, zipper,
					new HashSet<>());
			}
		}
		return zipFile;
	}

	private static void addRecursive(WebFolder source, String currentFolderName, LabelProvider folderNames,
			Zipper destination,
			Collection<WebFolder> visited) throws IOException {
		if (source == null || visited.contains(source)) {
			// seems to be a cyclic reference. just do nothing
			return;
		}

		if (StringServices.isEmpty(currentFolderName)) {
			throw new IllegalArgumentException("Display name of the folder must not be null. Folder was " + source);
		} else if (!currentFolderName.endsWith(File.separator)) {
			currentFolderName = currentFolderName + File.separator;
		}
		visited.add(source);

		for (TLObject wrapper : source.getContent()) {
			if (wrapper instanceof Document) {
				Document doc = (Document) wrapper;
				InputStream content = doc.getStream();
				// use the display names to simulate the directory structure
				try {
					destination.addFile(content, currentFolderName + doc.getName());
				} finally {
					content.close();
				}
			} else if (wrapper instanceof WebFolder) {
				WebFolder subFolder = (WebFolder) wrapper;
				addRecursive(subFolder, currentFolderName + folderNames.getLabel(subFolder), folderNames, destination,
					visited);
			}
		}
	}
}

