/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.model;

import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.WebFolder;

/**
 * {@link FolderContent} based on a {@link Document}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FolderDocument implements FolderContent {

	private final Document _document;
	private final WebFolder _folder;

	/**
	 * Creates a {@link FolderDocument}.
	 * 
	 * @param folder
	 *        The folder, in which the given document is displayed.
	 * @param document
	 *        The base document.
	 */
	public FolderDocument(WebFolder folder, Document document) {
		super();
		this._folder = folder;
		this._document = document;
	}

	@Override
	public Object getContent() {
		return _document;
	}

	@Override
	public Object getFolder() {
		return _folder;
	}

	@Override
	public boolean isLink() {
		return false;
	}

}
