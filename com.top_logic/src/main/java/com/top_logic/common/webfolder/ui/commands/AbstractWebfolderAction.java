/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui.commands;

import com.top_logic.common.webfolder.model.FolderContent;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.form.model.AbstractDynamicCommand;

/**
 * Base class for actions on a {@link FolderContent}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractWebfolderAction extends AbstractDynamicCommand {

	private final FolderContent node;

	/**
	 * Creates a {@link AbstractWebfolderAction}.
	 * 
	 * @param node
	 *        The {@link FolderContent} on which this action operates.
	 */
	public AbstractWebfolderAction(FolderContent node) {
    	assert node != null : "Node must not be null.";
        this.node = node;
    }

	/**
	 * The {@link #getContentObject()} casted to {@link Document}.
	 */
    protected final Document getDocument() {
		return (Document) node.getContent();
    }

	/**
	 * Whether {@link #getContentObject()} is linked to its {@link #getFolder()}.
	 */
    protected final boolean isLink() {
    	return node.isLink();
    }

	/**
	 * The base content object.
	 */
	protected final Wrapper getContentObject() {
		return (Wrapper) node.getContent();
	}

	/**
	 * The context folder.
	 */
	protected final WebFolder getFolder() {
		return (WebFolder) node.getFolder();
	}

}
