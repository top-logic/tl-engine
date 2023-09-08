/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.model;

/**
 * Contents of a folder with context information.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface FolderContent {

	/**
	 * The content object in {@link #getFolder()}.
	 */
	Object getContent();

	/**
	 * The context folder.
	 * 
	 * <p>
	 * Note: In case of {@link #isLink() linked} content, this folder needs not to be the container
	 * of the contents.
	 * </p>
	 */
	Object getFolder();

	/**
	 * Whether the {@link #getContent()} is linked to its parent {@link #getFolder()} via a
	 * non-composite association.
	 */
	boolean isLink();

}
