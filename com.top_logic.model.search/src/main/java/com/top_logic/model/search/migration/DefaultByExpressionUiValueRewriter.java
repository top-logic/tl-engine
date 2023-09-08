/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.migration;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.layout.tools.rewrite.DescendingDocumentRewrite;
import com.top_logic.layout.tools.rewrite.DocumentRewrite;
import com.top_logic.model.search.providers.DefaultByExpression;

/**
 * {@link DocumentRewrite} migrating {@link DefaultByExpression} configurations by removing the
 * value for ui property.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class DefaultByExpressionUiValueRewriter extends DescendingDocumentRewrite {

	private static final String DEFAULT_BY_EXPRESSION_PATH = "com.top_logic.model.search.providers.DefaultByExpression";

	private static final String DEFAULT_BY_EXPRESSION_NAME = "default-by-expression";

	private static final String VALUE_FOR_UI_NAME = "value-for-ui";

	private Log _log = new LogProtocol(DefaultByExpressionUiValueRewriter.class);

	@Override
	public void init(Log log) {
		_log = log;
	}

	@Override
	protected boolean rewriteElement(Element element) {
		if (isValueForUITag(element)) {
			return removeFromDocument(element);
		} else if (isDefaultByExpression(element)) {
			return removeValueForUIAttribute(element);
		} else {
			return false;
		}
	}

	private boolean removeFromDocument(Element element) {
		Node parentNode = element.getParentNode();

		if (parentNode instanceof Element) {
			Element parent = (Element) parentNode;

			if (isDefaultByExpression(parent)) {
				parent.removeChild(element);

				return true;
			} else {
				_log.info("Not in " + DEFAULT_BY_EXPRESSION_NAME + " configured " + VALUE_FOR_UI_NAME + " property.");
			}
		}

		return false;
	}

	private boolean removeValueForUIAttribute(Element element) {
		if (element.hasAttribute(VALUE_FOR_UI_NAME)) {
			element.removeAttribute(VALUE_FOR_UI_NAME);

			return true;
		}

		return false;
	}

	private boolean isValueForUITag(Element element) {
		return VALUE_FOR_UI_NAME.equals(element.getTagName());
	}

	private boolean isDefaultByExpression(Element element) {
		return isDefaultTag(element) || hasDefaultClass(element);
	}

	private boolean hasDefaultClass(Element element) {
		return DEFAULT_BY_EXPRESSION_PATH.equals(element.getAttribute("class"));
	}

	private boolean isDefaultTag(Element element) {
		return DEFAULT_BY_EXPRESSION_NAME.equals(element.getTagName());
	}

}
