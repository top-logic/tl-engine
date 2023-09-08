/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.folder.ui.commands;

import com.top_logic.basic.Named;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.common.folder.FolderDefinition;
import com.top_logic.common.webfolder.model.FolderContent;
import com.top_logic.layout.form.model.AbstractDynamicCommand;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public abstract class AbstractFolderAction extends AbstractDynamicCommand {

	private final FolderContent node;

	/**
	 * Creates a {@link AbstractFolderAction}.
	 * 
	 * @param node
	 *        The {@link FolderContent} on which this action operates.
	 */
	public AbstractFolderAction(FolderContent node) {
		assert node != null : "Node must not be null.";
		this.node = node;
	}

	/**
	 * The {@link #getContent()} casted to {@link BinaryDataSource}.
	 */
	protected final BinaryDataSource getDocument() {
		return (BinaryDataSource) node.getContent();
	}

	/**
	 * The {@link #getContent()} casted to {@link Named}.
	 */
	protected final Named getContent() {
		return (Named) node.getContent();
	}

	/**
	 * The context folder.
	 */
	protected final FolderDefinition getFolder() {
		return (FolderDefinition) node.getFolder();
	}

}
