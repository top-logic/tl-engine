/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.v5.export;

import com.top_logic.basic.col.Filter;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperResolver;

/**
 * {@link Filter} that accepts structure node objects of a certain node type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NodeFilter implements Filter<Wrapper> {

	private final String nodeName;
	private final WrapperResolver wrapperResolver;

	public NodeFilter(String nodeName, WrapperResolver wrapperResolver) {
		this.nodeName = nodeName;
		this.wrapperResolver = wrapperResolver;
	}

	@Override
	public boolean accept(Wrapper anObject) {
		{
			// Note: Quirks code follows for factory / type / element name quirks.
			final String elementNameQualifiedOrNot = wrapperResolver.getDynamicType((anObject).tHandle());
			if (elementNameQualifiedOrNot.endsWith(nodeName)) {
				final int theLength = elementNameQualifiedOrNot.length();
				if (theLength == nodeName.length()) {
					// Not qualified.
					return true;
				} else {
					// Qualified.
					return elementNameQualifiedOrNot.charAt(theLength - nodeName.length() - 1) == '.';
				}
			} else {
				return false;
			}
		}
	}

}
