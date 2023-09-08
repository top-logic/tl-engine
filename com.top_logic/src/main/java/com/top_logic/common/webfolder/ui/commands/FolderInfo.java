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
 * The FolderInfo computes and stores information about size and structure of a given web folder
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
	public WebFolder getWebFolder() {
		return webFolder;
	}
	public long getContentSize() {
		return contentSize;
	}
	public boolean isTooLarge() {
		return isTooLarge;
	}

	public boolean isEmpty() {
		return documentCount == 0;
	}

	public int getDocumentCount() {
		return documentCount;
	}
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
