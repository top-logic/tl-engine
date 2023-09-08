/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.processor;

import org.w3c.dom.Document;

import com.top_logic.basic.io.binary.BinaryData;

/**
 * Information about a layout definition.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConstantLayout extends Operation implements LayoutDefinition {

	private final LayoutResolver _resolver;

	private final String _layoutName;

	private BinaryData _layoutData;

	private Document _layoutDocument;

	private boolean _layoutDocumentInlined;

	/**
	 * @see LayoutResolver#getLayout(String)
	 */
	public ConstantLayout(LayoutResolver resolver, String layoutName, BinaryData layoutData, Document layoutDocument) {
		super(resolver.getProtocol(), resolver.getApplication());
		assert layoutData != null : "Constant layout for " + layoutName + " without data.";
		_resolver = resolver;
		_layoutName = layoutName;
		_layoutData = layoutData;
		_layoutDocument = layoutDocument;
	}

	@Override
	public LayoutResolver getResolver() {
		return _resolver;
	}

	@Override
	public String getLayoutName() {
		return _layoutName;
	}

	@Override
	public BinaryData getLayoutData() {
		return _layoutData;
	}

	@Override
	public Document getLayoutDocument() {
		return _layoutDocument;
	}

	@Override
	protected void error(String message) {
		error(_layoutData, message);
	}

	@Override
	protected void error(String message, Exception exception) {
		error(_layoutData, message, exception);
	}

	/**
	 * Whether {@link #getLayoutDocument()} was inlined externally.
	 */
	public boolean isLayoutDocumentInlined() {
		return _layoutDocumentInlined;
	}

	/**
	 * {@link #getLayoutDocument()} was inlined externally.
	 *
	 */
	public void markLayoutDocumentInlined() {
		_layoutDocumentInlined = true;

	}

}