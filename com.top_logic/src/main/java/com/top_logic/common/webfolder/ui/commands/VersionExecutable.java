/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui.commands;


import com.top_logic.knowledge.wrap.Document;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Opens a dialog which displays the different versions of the held document.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class VersionExecutable implements Command {

    /** The document to show the versions for. */
	private final Document document;

	/**
	 * Creates a {@link VersionExecutable}.
	 * 
	 * @param document
	 *        The document to be held, must not be <code>null</code>.
	 * @throws IllegalArgumentException
	 *         If given document is <code>null</code>.
	 */
    public VersionExecutable(Document document) {
		if (document == null) {
			throw new IllegalArgumentException("Given document is null.");
		}

		this.document = document;
    }

    @Override
	public HandlerResult executeCommand(DisplayContext aContext) {
		return new VersionDialog(I18NConstants.VERSION_DIALOG, this.document).open(aContext);
    }

}