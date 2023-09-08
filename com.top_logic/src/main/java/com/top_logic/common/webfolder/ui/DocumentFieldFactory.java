/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui;

import java.util.Arrays;

import com.top_logic.common.webfolder.model.DocumentContext;
import com.top_logic.common.webfolder.model.FolderDocument;
import com.top_logic.common.webfolder.ui.commands.LockExecutable;
import com.top_logic.common.webfolder.ui.commands.UnlockExecutable;
import com.top_logic.knowledge.gui.layout.upload.FileNameConstraint;
import com.top_logic.knowledge.gui.layout.upload.SimpleFileNameStrategy;
import com.top_logic.layout.form.model.DocumentField;

/**
 * Factory creating {@link DocumentField}s from {@link DocumentContext} descriptions.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DocumentFieldFactory {

	/**
	 * Creates a DocumentField with given name and given initial contents.
	 * 
	 * @param name
	 *        the name of the new field.
	 * @param context
	 *        The contents description of the field.
	 * @return The new {@link DocumentField}.
	 */
	public static DocumentField createDocumentField(String name, DocumentContext context) {

		FolderDocument documentHandle = new FolderDocument(context.getFolder(), context.getDocument());

		DocumentField result =
			new DocumentField(name, new LockExecutable(documentHandle), new UnlockExecutable(documentHandle));

		if (context.getDocument() != null) {
			result.initializeField(context.getDocument());
		}

		if (context.getTemplate() != null) {
			result.setTemplate(context.getTemplate());
		} else {
			result.setTemplate(null);
		}
		
		if (context.getDocumentName() != null) {
			result.getDocumentField().setFileNameConstraint(
				new FileNameConstraint(new SimpleFileNameStrategy(null, Arrays.asList(context.getDocumentName()))));
		} else {
			result.getDocumentField().setFileNameConstraint(new FileNameConstraint(SimpleFileNameStrategy.INSTANCE));
		}

		return result;
	}

}
