/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools.ticket26117;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.StringServices;
import com.top_logic.layout.tools.rewrite.LayoutRewrite;

/**
 * {@link LayoutRewrite} migrating <code>securityProviderClass</code> configurations to
 * <code>securityObject</code> configurations.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class SecurityProviderRewriter implements LayoutRewrite {

	private static final String SECURITY_PROVIDER_CLASS = "securityProviderClass";

	private Log _log = new LogProtocol(SecurityProviderRewriter.class);

	@Override
	public void init(Log log) {
		_log = log;
	}

	private boolean isRelevantTemplate(String template) {
		return "com.top_logic/table.template.xml".equals(template)
			|| "com.top_logic/treetable.template.xml".equals(template)
			|| "com.top_logic.chart.chartjs/chartjs.template.xml".equals(template)
			|| "com.top_logic.element/form.template.xml".equals(template)
			|| "com.top_logic.element/grid.template.xml".equals(template)
			|| "com.top_logic.element/treegrid.template.xml".equals(template)
			|| "com.top_logic.model.search/genericCreateDialog.template.xml".equals(template)
			|| "com.top_logic.model.search/transaction.template.xml".equals(template)
			|| "com.top_logic/selector.template.xml".equals(template)
			|| "com.top_logic/tree.template.xml".equals(template);
	}

	@Override
	public boolean rewriteLayout(String layoutKey, String template, Document document) {
		if (!isRelevantTemplate(template)) {
			return false;
		}
		Element docElement = document.getDocumentElement();
		boolean rewriteAttribute = rewriteAttribute(layoutKey, docElement);
		if (rewriteAttribute) {
			return true;
		}
		return rewriteTag(layoutKey, docElement);
	}

	private boolean rewriteTag(String layoutKey, Element docElement) {
		NodeList subTags = docElement.getElementsByTagName(SECURITY_PROVIDER_CLASS);
		switch (subTags.getLength()) {
			case 0:
				return false;
			case 1:
				Node child = subTags.item(0);
				setSecurityObject(docElement, layoutKey, child.getTextContent());
				docElement.removeChild(child);
				return true;
			default:
				_log.error("Multiple tags with name " + SECURITY_PROVIDER_CLASS + " unsupported.");
				return false;
		}
	}

	private boolean rewriteAttribute(String layoutKey, Element docElement) {
		String secObject = docElement.getAttribute(SECURITY_PROVIDER_CLASS);
		if (secObject == null) {
			return false;
		}
		docElement.removeAttribute(SECURITY_PROVIDER_CLASS);
		setSecurityObject(docElement, layoutKey, secObject);
		return true;
	}

	private void setSecurityObject(Element docElement, String layoutKey, String secObject) {
		if (StringServices.isEmpty(secObject)) {
			return;
		}
		String newSecObj;
		if ("securityRoot".equals(secObject)) {
			newSecObj = "securityRoot";
		} else if ("model".equals(secObject)) {
			newSecObj = "model";
		} else if ("master".equals(secObject)) {
			newSecObj = "path:master.currentobject";
		} else if ("slave".equals(secObject)) {
			newSecObj = "selection(self())";
		} else {
			_log.error("Unexpected securityProviderClass: " + secObject);
			newSecObj = secObject;
		}
		docElement.setAttribute("securityObject", newSecObj);
		_log.info("Updated security object from " + secObject + " to " + newSecObj + " in layout" + layoutKey + ".");
	}

}
