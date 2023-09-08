/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.bookmark;

import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.structured.StructuredElementFactory;
import com.top_logic.mig.util.net.URLUtilities;
import com.top_logic.tool.boundsec.ObjectNotFound;
import com.top_logic.tool.boundsec.commandhandlers.BookmarkHandler;
import com.top_logic.tool.boundsec.commandhandlers.DefaultBookmarkHandler;

/**
 * {@link DefaultBookmarkHandler} that resolves {@link StructuredElement}s by a <code>/</code>
 * -separated path of {@link StructuredElement#getName() names}.
 * 
 * <p>
 * The first element in the path is the structure name (instead of the name of the structure root
 * object).
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DemoBookmarkHandler implements BookmarkHandler {

	private static final String BOOKMARK_PARAMETER = "bookmark";

	@Override
	public Object getBookmarkObject(Map<String, Object> someArguments) {
		String bookmark = (String) someArguments.get(BOOKMARK_PARAMETER);
		if (bookmark != null) {
			String[] path = StringServices.split(bookmark, '/');
			if (path.length < 1) {
				return null;
			}

			String structureName = path[0];
			StructuredElementFactory factory = StructuredElementFactory.getInstanceForStructure(structureName);
			StructuredElement result = factory.getRoot();
			for (int n = 1, cnt = path.length; n < cnt; n++) {
				Object name = path[n].replace("+", " ");

				findChild:
				{
					for (StructuredElement child : result.getChildren()) {
						if (name.equals(child.getName())) {
							result = child;
							break findChild;
						}
					}
					
					// Object not found.
					throw new ObjectNotFound(I18NConstants.BOOKMARK_OBJECT_NOT_FOUND__BOOKMARK.fill(bookmark));
				}
			}
			return result;
		}
		return null;
	}

	@Override
	public boolean appendIdentificationArguments(StringBuilder url, boolean first, Object targetObject) {
		if (targetObject instanceof StructuredElement) {
			URLUtilities.appendParamName(url, first, BOOKMARK_PARAMETER);
			StructuredElement node = (StructuredElement) targetObject;
			url.append(URLUtilities.urlEncode(node.getStructureName()));
			appendPathTo(url, node);

			// A parameter was appended.
			return false;
		}
		return first;
	}

	private static void appendPathTo(StringBuilder url, StructuredElement node) {
		StructuredElement parent = node.getParent();
		if (parent != null) {
			appendPathTo(url, parent);

			// Note: The name of the root object must not be appended. It is identified by the
			// structure name.
			url.append('/');
			url.append(URLUtilities.urlEncode(node.getName()));
		}
	}

}
