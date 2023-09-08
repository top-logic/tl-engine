/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.persistency.attribute.tempate;

import com.top_logic.model.search.expr.parser.SearchExpressionParser;

/**
 * {@link AbstractTemplateStorageMapping} for general XML content.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class XMLTemplateStorageMapping extends AbstractTemplateStorageMapping {

	/**
	 * Singleton {@link XMLTemplateStorageMapping} instance.
	 */
	public static final XMLTemplateStorageMapping INSTANCE = new XMLTemplateStorageMapping();

	private XMLTemplateStorageMapping() {
		// Singleton constructor.
	}

	@Override
	protected void configureParser(SearchExpressionParser parser) {
		parser.setCheckHtml(false);
	}
}
