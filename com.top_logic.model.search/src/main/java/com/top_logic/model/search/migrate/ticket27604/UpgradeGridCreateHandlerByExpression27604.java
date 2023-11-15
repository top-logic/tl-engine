/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.migrate.ticket27604;

import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.top_logic.basic.Log;
import com.top_logic.basic.StringServices;
import com.top_logic.element.layout.create.ConstantCreateTypeOptions;
import com.top_logic.layout.tools.rewrite.DescendingDocumentRewrite;
import com.top_logic.layout.tools.rewrite.DocumentRewrite;
import com.top_logic.model.search.providers.GridCreateHandlerByExpression;

/**
 * {@link DocumentRewrite} migrating the configuration of {@link GridCreateHandlerByExpression}
 * <code>createType</code> into {@link ConstantCreateTypeOptions}.
 * <p>
 * The migration is the following:
 * </p>
 * <pre>
 * <code>
 * - 	createType="TYPE"
 * + 	&lt;type-options class="com.top_logic.element.layout.create.ConstantCreateTypeOptions"
 * + 		include-subtypes="false"
 * + 		type="TYPE"
 * + 	/&gt;
 * <code>
 * </pre>
 *
 * @implNote The string literals are copied here and not references into the actual code, as that
 *           might change, which would break this migration.
 *
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public final class UpgradeGridCreateHandlerByExpression27604 extends DescendingDocumentRewrite {

	private static final String SEARCHED_TYPE = "com.top_logic.model.search.providers.GridCreateHandlerByExpression";

	private static final String SEARCHED_ATTRIBUTE = "createType";

	/** Creates a {@link UpgradeGridCreateHandlerByExpression27604}. */
	public UpgradeGridCreateHandlerByExpression27604() {
		super();
	}

	@Override
	public void init(Log log) {
		// The log is not used.
	}

	@Override
	protected boolean rewriteElement(Element layout) {
		if (!Objects.equals(layout.getAttribute("class"), SEARCHED_TYPE)) {
			return false;
		}
		String createType = layout.getAttribute(SEARCHED_ATTRIBUTE).strip();
		if (StringServices.isEmpty(createType)) {
			return false;
		}
		layout.removeAttribute(SEARCHED_ATTRIBUTE);
		addTypeOptionsTag(layout, createType);
		return true;
	}

	private void addTypeOptionsTag(Element layout, String type) {
		Document document = layout.getOwnerDocument();
		Element typeOptions = document.createElement("type-options");
		typeOptions.setAttribute("class", "com.top_logic.element.layout.create.ConstantCreateTypeOptions");
		typeOptions.setAttribute("include-subtypes", "false");
		typeOptions.setAttribute("type", type);
		layout.appendChild(typeOptions);
	}

}
