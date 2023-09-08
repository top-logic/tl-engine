/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.model;


/**
 * Direct implementation of {@link FolderContent}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultFolderContent implements FolderContent {

	private final Object folder;

	private final Object content;

	private final boolean linked;

	/**
	 * Creates a {@link DefaultFolderContent} that is not linked.
	 * 
	 * @see #DefaultFolderContent(Object, Object, boolean)
	 */
	public DefaultFolderContent(Object folder, Object content) {
		this(folder, content, false);
	}

	/**
	 * Creates a {@link DefaultFolderContent}.
	 * 
	 * @param folder
	 *        See {@link #getFolder()}.
	 * @param content
	 *        See {@link #getContent()}.
	 * @param linked
	 *        See {@link #isLink()}.
	 */
	public DefaultFolderContent(Object folder, Object content, boolean linked) {
		this.folder = folder;
		this.content = content;
		this.linked = linked;
	}

	@Override
	public Object getContent() {
		return content;
	}

	@Override
	public Object getFolder() {
		return folder;
	}

	@Override
	public boolean isLink() {
		return linked;
	}

}
