/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc;

import java.util.Map;

import com.top_logic.dob.DataObject;
import com.top_logic.doc.model.Page;
import com.top_logic.doc.model.TLDocFactory;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.mig.util.net.URLUtilities;
import com.top_logic.tool.boundsec.commandhandlers.BookmarkHandler;
import com.top_logic.tool.boundsec.commandhandlers.DefaultBookmarkHandler;

/**
 * {@link DefaultBookmarkHandler} that resolves {@link StructuredElement}s with the type
 * {@link Page}.
 * 
 * @author <a href="mailto:dpa@top-logic.com">dpa</a>
 */
public class PageBookmarkHandler implements BookmarkHandler {

	/** URL argument containing {@link Page#getUuid()} in the bookmark. */
	public static final String COMMAND_PARAM_UUID = "uuid";

	/** URL argument containing {@link Page#getName()} in the bookmark. */
	public static final String COMMAND_PARAM_PAGE = "page";

	@Override
	public Object getBookmarkObject(Map<String, Object> someArguments) {
		String uuid = (String) someArguments.get(COMMAND_PARAM_UUID);
		if (uuid != null) {
			DataObject dbObject = PersistencyLayer.getKnowledgeBase().getObjectByAttribute(TLDocFactory.KO_NAME_PAGE,
				Page.UUID_ATTR, uuid);
			if (dbObject != null) {
				return ((KnowledgeObject) dbObject).getWrapper();
			}
		}
		String page = (String) someArguments.get(COMMAND_PARAM_PAGE);
		if (page != null) {
			DataObject dbObject = PersistencyLayer.getKnowledgeBase().getObjectByAttribute(TLDocFactory.KO_NAME_PAGE,
				Page.NAME_ATTR, page);
			if (dbObject != null) {
				return ((KnowledgeObject) dbObject).getWrapper();
			}
		}
		return null;
	}


	@Override
	public boolean appendIdentificationArguments(StringBuilder url, boolean first, Object targetObject) {
		if (targetObject instanceof Page) {
			Page page = (Page) targetObject;
			boolean stillFirst = first;
			stillFirst = URLUtilities.appendUrlArg(url, stillFirst, COMMAND_PARAM_PAGE, page.getName());
			stillFirst = URLUtilities.appendUrlArg(url, stillFirst, COMMAND_PARAM_UUID, page.getUuid());
			return stillFirst;
		}
		return first;
	}
}
