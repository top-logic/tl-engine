/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools.ticket26872;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.layout.table.model.SortColumnsConfig;
import com.top_logic.layout.table.model.SortColumnsTableConfigurationProvider;
import com.top_logic.layout.tools.rewrite.LayoutRewrite;

/**
 * {@link LayoutRewrite} migrating the <code>idColumn</code> template configuration property, which
 * is only used for sorting the rows of a table, by removing it and adding a corresponding
 * {@link SortColumnsTableConfigurationProvider}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class TemplateIDColumnRewriter implements LayoutRewrite {

	private static final String ID_COLUMN_TEMPLATE_ARGUMENT = "idColumn";

	private static final String CONFIGURATION_PROVIDERS_TEMPLATE_ARGUMENT = "configurationProviders";
	
	private Log _log = new LogProtocol(TemplateIDColumnRewriter.class);

	@Override
	public void init(Log log) {
		_log = log;
	}

	@Override
	public boolean rewriteLayout(String layoutKey, String template, Document document) {
		Element root = document.getDocumentElement();
		
		if (root.hasAttribute(ID_COLUMN_TEMPLATE_ARGUMENT)) {
			return moveIDColumnToSortOrderProvider(document, root);
		}

		return false;
	}

	private boolean moveIDColumnToSortOrderProvider(Document document, Element root) {
		String idColumn = root.getAttribute(ID_COLUMN_TEMPLATE_ARGUMENT);

		root.removeAttribute(ID_COLUMN_TEMPLATE_ARGUMENT);

		NodeList configurationProviderTags = root.getElementsByTagName(CONFIGURATION_PROVIDERS_TEMPLATE_ARGUMENT);

		switch (configurationProviderTags.getLength()) {
			case 0: {
				Node configurationProvidersNode = document.createElement(CONFIGURATION_PROVIDERS_TEMPLATE_ARGUMENT);
				root.appendChild(configurationProvidersNode);

				configurationProvidersNode.appendChild(createSortOrderConfigurationProvider(document, idColumn));

				return true;
			}
			case 1: {
				Node configurationProvidersNode = configurationProviderTags.item(0);

				configurationProvidersNode.appendChild(createSortOrderConfigurationProvider(document, idColumn));

				return true;
			}
			default: {
				_log.error("Multiple tags with " + CONFIGURATION_PROVIDERS_TEMPLATE_ARGUMENT + " unsupported.");

				return false;
			}
		}
	}

	private Element createSortOrderConfigurationProvider(Document document, String idColumn) {
		Element sortOrderConfigurationProvider = document.createElement(SortColumnsConfig.SORT_COLUMNS);

		sortOrderConfigurationProvider.setAttribute(SortColumnsConfig.ORDER, idColumn);

		return sortOrderConfigurationProvider;
	}

}
