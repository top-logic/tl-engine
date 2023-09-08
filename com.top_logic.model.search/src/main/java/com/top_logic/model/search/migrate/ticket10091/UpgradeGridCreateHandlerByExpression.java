/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.migrate.ticket10091;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;

import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.StringServices;
import com.top_logic.layout.tools.rewrite.DescendingDocumentRewrite;
import com.top_logic.layout.tools.rewrite.DocumentRewrite;
import com.top_logic.model.search.providers.GridCreateHandlerByExpression;

/**
 * {@link DocumentRewrite} migrating {@link GridCreateHandlerByExpression}
 * <code>create-operation</code> configuration.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class UpgradeGridCreateHandlerByExpression extends DescendingDocumentRewrite {

	private static final String S = "\\s*";

	private Log _log = new LogProtocol(UpgradeGridCreateHandlerByExpression.class);

	/**
	 * Creates a {@link UpgradeGridCreateHandlerByExpression} logging errors to the application log.
	 */
	public UpgradeGridCreateHandlerByExpression() {
		super();
	}

	@Override
	public void init(Log log) {
		_log = log;
	}

	@Override
	protected boolean rewriteElement(Element layout) {
		if ("com.top_logic.model.search.providers.GridCreateHandlerByExpression".equals(layout.getAttribute("class"))) {
			String createType = layout.getAttribute("createType");
			String createOperation = layout.getAttribute("create-operation").trim();
			if (!StringServices.isEmpty(createOperation)) {
				Pattern createPattern =
					Pattern.compile(
						"new" + S + "\\(" + S + "`" + Pattern.quote(createType) + "`" + S + "\\)" + "|" +
							"`" + Pattern.quote(createType) + "`" + S + "." + S + "new" + S + "\\(" + S + "\\)");

				Matcher matcher = createPattern.matcher(createOperation);
				if (matcher.find()) {
					String newVar = chooseVar("result", createOperation);
					String contextVar = chooseVar("context", createOperation);

					int start = matcher.start();
					int end = matcher.end();
					if (start > 0 || end < createOperation.length()) {
						String initOperation = newVar + " -> " + contextVar + " -> " +
							"(" +
							createOperation.substring(0, start) +
							"$" + newVar +
							createOperation.substring(end) +
							").apply($" + contextVar + ", null)";
						layout.setAttribute("initOperation", initOperation);
					}
				} else {
					_log.error("Cannot upgrade 'create-operation' because new statement new(`"
						+ createType + "`) is not found in: " + createOperation);
				}

				layout.removeAttribute("create-operation");
				return true;
			}
		}
		return false;
	}

	private String chooseVar(String base, String createOperation) {
		String result = base;
		int id = 1;
		while (createOperation.contains(result)) {
			result = base + (id++);
		}
		return result;
	}

}
