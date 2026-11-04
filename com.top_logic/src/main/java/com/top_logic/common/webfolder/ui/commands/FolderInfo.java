/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui.commands;

import java.util.Collection;
import java.util.HashSet;

import com.top_logic.common.webfolder.ui.WebFolderUIFactory;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.model.TLObject;

/**
 * The {@link FolderInfo} computes and stores information about size and structure of a given web
 * folder.
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class FolderInfo {
	private WebFolder webFolder;
	private long contentSize;
	private boolean isTooLarge;
	private int documentCount;
	private int folderCount;

	public FolderInfo(WebFolder webFolder) {
		this.webFolder = webFolder;
		this.contentSize = 0;
		this.documentCount = 0;
		this.folderCount = 0;
		init(this.webFolder, new HashSet<>());
		this.isTooLarge = this.contentSize > WebFolderUIFactory.getInstance().getZipDownloadSizeLimit() * (1024 * 1024);
	}
	/** The analyzed folder. */
	public WebFolder getWebFolder() {
		return webFolder;
	}
	/** The total size of all documents in the folder and its sub-folders in bytes. */
	public long getContentSize() {
		return contentSize;
	}
	/**
	 * Whether the folder is larger than the configured zip download size limit.
	 *
	 * @see WebFolderUIFactory#getZipDownloadSizeLimit()
	 */
	public boolean isTooLarge() {
		return isTooLarge;
	}

	/** Whether the folder and its sub-folders contain no documents. */
	public boolean isEmpty() {
		return documentCount == 0;
	}

	/** The number of documents in the folder and its sub-folders. */
	public int getDocumentCount() {
		return documentCount;
	}
	/** The number of sub-folders of the folder. */
	public int getFolderCount() {
		return folderCount;
	}
	private void init(WebFolder aFolder, Collection<WebFolder> visited) {
		if (aFolder == null)
			return;
		Collection<? extends TLObject> content = aFolder.getContent();
		for (TLObject wrapper : content) {
			if (wrapper instanceof Document) {
				contentSize += ((Document) wrapper).getSize();
				documentCount++;
			} else if (wrapper instanceof WebFolder) {
				if (visited.contains(wrapper)) {
					// do nothing in order to prevent cycles
				} else {
					visited.add((WebFolder) wrapper);
					init((WebFolder) wrapper, visited);
					folderCount++;
				}
			}
		}
	}

}
