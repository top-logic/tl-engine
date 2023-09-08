/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.structured.wrap;

import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.wrap.WrapperResolver;

/**
 * {@link WrapperResolver} that resolves all items to a monomorphic type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class MonomorphicResolver implements WrapperResolver {
	private String _monomorphicType;

	/**
	 * Creates a {@link MonomorphicResolver}.
	 * 
	 * @param monomorphicType
	 *        The only type to resolve all items to.
	 */
	public MonomorphicResolver(String monomorphicType) {
		_monomorphicType = monomorphicType;
	}
	
	@Override
	public String getDynamicType(KnowledgeItem item) {
		return _monomorphicType;
	}

	@Override
	public String getResourceType(String dynamicType) {
		return dynamicType;
	}
}