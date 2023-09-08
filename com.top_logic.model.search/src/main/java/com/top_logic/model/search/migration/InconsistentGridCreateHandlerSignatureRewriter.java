/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.migration;

import org.w3c.dom.Element;

import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.StringServices;
import com.top_logic.layout.tools.rewrite.DescendingDocumentRewrite;
import com.top_logic.layout.tools.rewrite.DocumentRewrite;
import com.top_logic.model.search.providers.GridCreateHandlerByExpression;

/**
 * {@link DocumentRewrite} migrating layout template arguments making the create handler by
 * expression signature consistent.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class InconsistentGridCreateHandlerSignatureRewriter extends DescendingDocumentRewrite {

	private Log _log = new LogProtocol(InconsistentGridCreateHandlerSignatureRewriter.class);

	@Override
	public void init(Log log) {
		_log = log;
	}

	@Override
	protected boolean rewriteElement(Element element) {
		if (isGridCreateHandler(element)) {
			String initOperationName = GridCreateHandlerByExpression.Config.INIT_OPERATION;

			String initOperation = element.getAttribute(initOperationName);

			if (!StringServices.isEmpty(initOperation)) {
				element.setAttribute(initOperationName, createSwappedArgumentInitOperation(initOperation));
			} else {
				_log.info("Empty init operation in grid create handler by expression.");
			}
		}

		return false;
	}

	private String createSwappedArgumentInitOperation(String initOperation) {
		StringBuilder builder = new StringBuilder();

		builder.append("x->y->");
		builder.append("(");
		builder.append(initOperation);
		builder.append(")");
		builder.append("($y,$x)");

		return builder.toString();
	}

	private boolean isGridCreateHandler(Element element) {
		return "com.top_logic.model.search.providers.GridCreateHandlerByExpression".equals(element.getAttribute("class"));
	}

}
