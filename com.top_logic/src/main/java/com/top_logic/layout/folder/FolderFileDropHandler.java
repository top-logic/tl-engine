/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.folder;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.util.ResKey;
import com.top_logic.common.folder.model.FolderNode;
import com.top_logic.common.webfolder.ui.I18NConstants;
import com.top_logic.common.webfolder.ui.commands.LockExecutable;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.dnd.DnDFileUtilities;

/**
 * Implementation of {@link FileDropHandler} to handle files dropped on a {@link FolderControl}.
 * 
 * <p>
 * Is used to handle dropped files and folders. Receives a folder as all included files with their
 * path. That's why empty folders will get lost in the uploading process.
 * </p>
 * 
 * @author <a href="mailto:Diana.Pankratz@top-logic.com">Diana Pankratz</a>
 */
public class FolderFileDropHandler implements FileDropHandler {

	/** If manual locking by the user is necessary before uploading a file */
	private boolean _manualLocking;

	/** If the upload in the {@link FolderControl} is allowed/possible */
	private boolean _uploadPossible;

	/** Maximum size of all dropped files that is allowed to prevent overhead. */
	private long _maxSize;

	/**
	 * Creates a new {@link FolderFileDropHandler}.
	 * 
	 * @param manualLocking
	 *        If manual locking is necessary.
	 * @param uploadPossible
	 *        If upload with drag and drop is allowed.
	 * @param maxSize
	 *        Maximum size of documents that can be uploaded at once.
	 */
	public FolderFileDropHandler(boolean manualLocking, boolean uploadPossible, long maxSize) {
		_manualLocking = manualLocking;
		_uploadPossible = uploadPossible;
		_maxSize = maxSize;
	}

	/**
	 * Receives a list of files as {@link BinaryData}.
	 * 
	 * <p>
	 * The file names contain their full path to recreate a folder tree if a folder was dropped.
	 * </p>
	 * 
	 * @param context
	 *        The {@link DisplayContext} of the {@link FolderControl}
	 * @param selectedFolder
	 *        The currently selected {@link FolderNode} in the {@link FolderControl}.
	 * @param files
	 *        The uploaded files as {@link BinaryData}
	 * 
	 * @see FileDropHandler#uploadFiles(DisplayContext, FolderNode, List)
	 */
	@Override
	public void uploadFiles(DisplayContext context, FolderNode selectedFolder, List<BinaryData> files) {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			WebFolder webFolder = (WebFolder) selectedFolder.getBusinessObject();
			// Map with folder names as keys and List of files per folder as value.
			Map<String, List<BinaryData>> droppedElements = groupFiles(files);
			List<ResKey> infoMessages = new ArrayList<>();
			List<String> fileDuplicates = new ArrayList<>();
			List<String> folderDuplicates = new ArrayList<>();

			if (DnDFileUtilities.exceededUploadSize(droppedElements, getMaxUploadSize())) {
				tx.rollback();
				DnDFileUtilities.showUploadSizeExceededMessage(getMaxUploadSize());
				return;
			}

			// Iterate through folders.
			for (String element : droppedElements.keySet()) {
				List<BinaryData> folderFiles = droppedElements.get(element);
				// List of subFolders in the WebFolder
				List<String> subFolders = getSubFolders(webFolder);
				if (!subFolders.contains(element)) {
					fileDuplicates.addAll(addFileToWebFolder(webFolder, folderFiles));
				} else {
					folderDuplicates.add(element);
				}
			}
			tx.commit();

			addDuplicateMessage(infoMessages, fileDuplicates, folderDuplicates);

			if (!infoMessages.isEmpty()) {
				DnDFileUtilities.showInfoMessage(infoMessages);
			}

		}
	}

	/**
	 * Adds upload failed messages for folder and file duplicates to the info messages to show them
	 * to the user.
	 * 
	 * @param infoMessages
	 *        List of all infoMessages. The new message will be added to this list.
	 * @param fileDuplicates
	 *        Failed uploads of duplicate files.
	 * @param folderDuplicates
	 *        Failed uploads of duplicate folder.
	 */
	private void addDuplicateMessage(List<ResKey> infoMessages, List<String> fileDuplicates,
			List<String> folderDuplicates) {
		if (!folderDuplicates.isEmpty()) {
			ResKey message = I18NConstants.FOLDER_NAME_ALREADY_EXISTS__NAME.fill(String.join(", ", folderDuplicates));
			infoMessages.add(message);
		}

		if (!fileDuplicates.isEmpty()) {
			ResKey message = I18NConstants.FILE_NAME_ALREADY_EXISTS__NAME.fill(String.join(", ", fileDuplicates));
			infoMessages.add(message);
		}
	}

	/**
	 * Receive whether upload with drag and drop is allowed.
	 * 
	 * <p>
	 * If uploads are not allowed the {@link WebFolder} will be marked red if a user hovers a file/folder
	 * over it. Dropped files will be ignored.
	 * </p>
	 */
	@Override
	public boolean getuploadPossible() {
		return _uploadPossible;
	}

	@Override
	public long getMaxUploadSize() {
		return _maxSize;
	}

	/** @see #getuploadPossible() */
	public void setuploadPossible(boolean uploadPossible) {
		_uploadPossible = uploadPossible;
	}

	/** @see #getMaxUploadSize() */
	public void setMaxUploadSize(long maxUploadSize) {
		_maxSize = maxUploadSize;
	}

	/**
	 * Adds all uploaded files with the same root folder the selected {@link FolderNode} of the
	 * {@link FolderControl}.
	 * 
	 * @param webFolder
	 *        The {@link FolderNode} as a {@link WebFolder} where the files are dropped.
	 * @param droppedElementFiles
	 *        All dropped files of one root folder.
	 */
	@SuppressWarnings("deprecation")
	private List<String> addFileToWebFolder(WebFolder webFolder, List<BinaryData> droppedElementFiles) {

		List<String> existingFiles = new ArrayList<>();
		for (BinaryData file : droppedElementFiles) {
			int splitIndex = file.getName().lastIndexOf(':');
			String filePath = "";
			String fileName = file.getName();
			if (splitIndex != -1) {
				filePath = file.getName().substring(0, splitIndex);
				fileName = file.getName().substring(splitIndex + 1);
			}

			List<String> filePathElements = list(filePath.split(":"));
			filePathElements.removeAll(Arrays.asList(null, ""));
			WebFolder currentFolder = createFilePath(webFolder, filePathElements);
			Map<String, Document> webFolderFiles = getWebFolderFiles(currentFolder);
			Document webFolderFile = webFolderFiles.get(fileName);
			if (webFolderFile == null || (!_manualLocking || LockExecutable.isLocked(webFolderFile.getDAP()))) {
				currentFolder.createOrUpdateDocument(fileName, file);
			} else {
				existingFiles.add(fileName);
			}

		}

		return existingFiles;
	}

	/**
	 * Creates a {@link Map} of all existing files of a {@link WebFolder} with the file names as the
	 * keys.
	 * 
	 * <p>
	 * Only objects of the type {@link Document} are added, to exclude folders.
	 * <p>
	 * Is used to compare uploaded files with existing files to avoid duplicates.
	 * 
	 * @param currentWebFolder
	 *        The {@link WebFolder} whose files are to be added to the {@link Map}.
	 * @return A {@link Map} of all files with their names as the keys.
	 * @see #addFileToWebFolder(WebFolder, List)
	 */
	private Map<String, Document> getWebFolderFiles(WebFolder currentWebFolder) {
		Map<String, Document> webFolderFiles = new HashMap<>();
		Object[] objects = currentWebFolder.getContents().toArray();
		for (Object object : objects) {
			if (object.getClass() == Document.class) {
				Document document = (Document) object;
				webFolderFiles.put(document.getName(), document);
			}
		}
		return webFolderFiles;
	}

	/**
	 * Creates a {@link List} of all subfolders of a {@link WebFolder}.
	 * 
	 * <p>
	 * Is used to check if a folder already exists.
	 * </p>
	 * 
	 * @param webFolder
	 *        The current {@link WebFolder}.
	 * @return {@link List} A List of the names of all subfolders.
	 * @see #addFileToWebFolder(WebFolder, List)
	 */
	private List<String> getSubFolders(WebFolder webFolder) {
		List<String> subFolders = new ArrayList<>();
		Object[] objects = webFolder.getContents().toArray();
		for (Object object : objects) {
			if (object.getClass() == WebFolder.class) {
				subFolders.add(((WebFolder) object).getName());
			}
		}
		return subFolders;
	}

	/**
	 * Searches for the path of the dropped file and creates nonexistent subfolders.
	 * 
	 * @param webFolder
	 *        The {@link WebFolder} where the files are dropped.
	 * @param filePathElements
	 *        The folders of the file path.
	 * @return The {@link WebFolder} in which the file should be stored.
	 */
	@SuppressWarnings("deprecation")
	private WebFolder createFilePath(WebFolder webFolder, List<String> filePathElements) {
		for (String filePathElement : filePathElements) {
			WebFolder subFolder =
				(WebFolder) webFolder.getChildByName(filePathElement);
			if (subFolder == null) {
				webFolder.createSubFolder(filePathElement);
				webFolder =
					(WebFolder) webFolder.getChildByName(filePathElement);
			} else {
				webFolder = subFolder;
			}
		}
		return webFolder;
	}

	/**
	 * Divides all dropped files into groups of their root folders.
	 * 
	 * <p>
	 * Files of the same group are stored in a {@link List} and added to a {@link HashMap} with the
	 * folder name as the key. Files without a root folder (when the user drops a file not a folder)
	 * will be stored in a group with an empty key name ("").
	 * </p>
	 * 
	 * @param files
	 *        All dropped files. Their path is included in the name to rebuild a directory tree if a
	 *        directory was dropped.
	 * @return {@link Map} of all files grouped by their root directories.
	 */
	private Map<String, List<BinaryData>> groupFiles(List<BinaryData> files) {
		Map<String, List<BinaryData>> droppedElements = new HashMap<>();
		for (BinaryData file : files) {
			String fileName = file.getName();
			if (fileName.charAt(0) == ':') {
				fileName = fileName.substring(1);
			}
			int splitIndex = fileName.indexOf(':');
			String rootFolder = "";
			if (splitIndex != -1) {
				rootFolder = fileName.substring(0, splitIndex);
			}
			List<BinaryData> mapElement = droppedElements.get(rootFolder);
			if (mapElement == null) {
				List<BinaryData> folderFiles = new ArrayList<>();
				folderFiles.add(file);
				droppedElements.put(rootFolder, folderFiles);
			} else {
				mapElement.add(file);
			}
		}
		return droppedElements;
	}
}
