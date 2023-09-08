/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.model;

import com.top_logic.common.webfolder.ui.DocumentFieldFactory;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.layout.form.model.DocumentField;

/**
 * Description of the contents of a {@link DocumentField} for creation through the
 * {@link DocumentFieldFactory}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DocumentContext {

	/**
	 * @see #getDocument()
	 */
	private final Document _document;

	/**
	 * @see #getDocumentName()
	 */
	private final String _documentName;

	/**
	 * @see #getFolder()
	 */
	private final WebFolder _folder;

	/**
	 * @see #getTemplate()
	 */
	private final Document _template;

	/**
	 * Creates a {@link DocumentContext}.
	 * 
	 * @param document
	 *        See {@link #getDocument()}.
	 * @param documentName
	 *        See {@link #getDocumentName()}.
	 * @param folder
	 *        See {@link #getFolder()}.
	 * @param template
	 *        See {@link #getTemplate()}.
	 */
	public DocumentContext(Document document, String documentName, WebFolder folder, Document template) {
		this._document = document;
		this._documentName = documentName;
		this._folder = folder;
		this._template = template;
	}

	/**
	 * The initial document contents, or <code>null</code>, if no document does exist yet.
	 */
	public Document getDocument() {
		return _document;
	}

	/**
	 * The expected name of the document, or <code>null</code>, if all names are OK.
	 */
	public String getDocumentName() {
		return _documentName;
	}

	/**
	 * The folder into which a new document is created.
	 */
	public WebFolder getFolder() {
		return _folder;
	}

	/**
	 * The template contents, or <code>null</code>, if no template download is offered.
	 */
	public Document getTemplate() {
		return _template;
	}

}