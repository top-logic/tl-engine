/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui.mail;

import java.util.Collections;
import java.util.List;

import com.top_logic.knowledge.wrap.Document;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.basic.Command;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Opens a dialog which allows a document to be send via mail to some users.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class MailExecutable implements Command {

    /** The document to be send. */
    private final Document document;

	/**
	 * Creates a {@link MailExecutable}.
	 * 
	 * @param document
	 *        The document to be held, must not be <code>null</code>.
	 * @throws IllegalArgumentException
	 *         If given document is <code>null</code>.
	 */
    public MailExecutable(Document document) {
		if (document == null) {
            throw new IllegalArgumentException("Given document is null.");
        }

		this.document = document;
    }

	@Override
	public HandlerResult executeCommand(DisplayContext context) {
		List<Document> attachments = Collections.singletonList(document);
		DisplayDimension width = DisplayDimension.dim(700, DisplayUnit.PIXEL);
		DisplayDimension height = DisplayDimension.dim(550, DisplayUnit.PIXEL);
		MailDialog dialog = new MailDialog(attachments, width, height);
		return dialog.open(context);
    }
}