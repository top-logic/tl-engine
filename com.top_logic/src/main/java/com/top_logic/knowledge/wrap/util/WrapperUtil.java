/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.util;

import com.top_logic.dob.MetaObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.model.TLObject;

/**
 * Static utilities for persistent objects.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class WrapperUtil {

	/**
	 * Short-cut for {@link TLObject#tTable()}.{@link MetaObject#getName() getName()}.
	 */
	public static String getMetaObjectName(TLObject abstractWrapper) {
		return abstractWrapper.tTable().getName();
	}
	
	/**
	 * Returns the {@link KnowledgeBase} of the given wrapper.
	 * 
	 * @param wrapper
	 *        The Wrapper to get {@link KnowledgeBase} for.
	 * @return {@link TLObject#tKnowledgeBase()}.
	 */
	public static KnowledgeBase getKnowledgeBase(TLObject wrapper) {
		return wrapper.tKnowledgeBase();
	}

}
