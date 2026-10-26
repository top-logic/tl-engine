/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.migration;

import static com.top_logic.basic.core.xml.DOMUtil.*;
import static com.top_logic.basic.shared.collection.iterator.IteratorUtilShared.*;

import java.util.List;
import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.layout.tools.rewrite.LayoutRewrite;

/**
 * Adds the session log dialog to the <code>mainTabbar.layout.xml</code>.
 *
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class Ticket27736UpgradeLayout implements LayoutRewrite {

	private static final String TARGET_LAYOUT_KEY = "mainTabbar.layout.xml";

	private static final String TARGET_TEMPLATE = "com.top_logic/maintabbar.template.xml";

	private static final String TAG_DIALOGS = "dialogs";

	private static final String TAG_LAYOUT_REFERENCE = "layout-reference";

	private static final String ATTRIBUTE_RESOURCE = "resource";

	private static final String SESSION_LOG_DIALOG =
		"com.top_logic.monitoring/admin/technical/logs/logLines.sessionDialog.layout.xml";

	private Log _log = new LogProtocol(Ticket27736UpgradeLayout.class);

	@Override
	public void init(Log log) {
		_log = log;
	}

	@Override
	public boolean rewriteLayout(String layoutKey, String templatePath, Document document) {
		if (!Objects.equals(layoutKey, TARGET_LAYOUT_KEY)) {
			return false;
		}
		if (!Objects.equals(templatePath, TARGET_TEMPLATE)) {
			return false;
		}
		return rewriteDocument(layoutKey, templatePath, document);
	}

	private boolean rewriteDocument(String layoutKey, String templatePath, Document document) {
		Element rootElement = document.getDocumentElement();
		List<Element> dialogsTags = getDialogsTags(rootElement);
		if (dialogsTags.size() > 1) {
			logErrorMultipleDialogsTags(layoutKey, templatePath);
			return false;
		}
		boolean hasDialogsTag = dialogsTags.size() == 1;
		Element dialogsTag = hasDialogsTag ? dialogsTags.get(0) : createDialogsTag(rootElement);
		boolean hasSessionLogDialog = hasDialogsTag && containsSessionLogDialog(dialogsTag);
		if (hasSessionLogDialog) {
			return false;
		}
		createSessionDialogTag(dialogsTag);
		return true;
	}

	private List<Element> getDialogsTags(Element rootElement) {
		return toListIterable(elementsNS(rootElement, null, TAG_DIALOGS));
	}

	private void logErrorMultipleDialogsTags(String layoutKey, String templatePath) {
		String message = "Multiple '" + TAG_DIALOGS + "' tags. Layout key: '" + layoutKey + "'. Template: " + templatePath;
		RuntimeException exception = new RuntimeException(message);
		_log.error(message, exception);
	}

	private Element createDialogsTag(Element parent) {
		Element dialogsTag = parent.getOwnerDocument().createElement(TAG_DIALOGS);
		parent.appendChild(dialogsTag);
		return dialogsTag;
	}

	private boolean containsSessionLogDialog(Element dialogsTag) {
		List<Element> layoutReferences = getLayoutReferences(dialogsTag);
		return layoutReferences.stream().anyMatch(this::isSessionLogDialog);
	}

	private List<Element> getLayoutReferences(Element dialogsTag) {
		return toListIterable(elementsNS(dialogsTag, null, TAG_LAYOUT_REFERENCE));
	}

	private boolean isSessionLogDialog(Element element) {
		return Objects.equals(element.getAttribute(ATTRIBUTE_RESOURCE), SESSION_LOG_DIALOG);
	}

	private void createSessionDialogTag(Element dialogsTag) {
		Element sessionLogDialog = dialogsTag.getOwnerDocument().createElement(TAG_LAYOUT_REFERENCE);
		sessionLogDialog.setAttribute(ATTRIBUTE_RESOURCE, SESSION_LOG_DIALOG);
		dialogsTag.appendChild(sessionLogDialog);
		_log.info("Added session log dialog to the file '" + TARGET_LAYOUT_KEY + "'.");
	}

}
