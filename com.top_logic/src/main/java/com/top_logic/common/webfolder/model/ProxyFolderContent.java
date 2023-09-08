/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.model;

/**
 * {@link FolderContent} which dispatches all methods to another one
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ProxyFolderContent implements FolderContent {

	@Override
	public Object getContent() {
		return getImplementation().getContent();
	}

	@Override
	public Object getFolder() {
		return getImplementation().getFolder();
	}

	@Override
	public boolean isLink() {
		return getImplementation().isLink();
	}

	/**
	 * Returns the actual implementation.
	 */
	protected abstract FolderContent getImplementation();

}

