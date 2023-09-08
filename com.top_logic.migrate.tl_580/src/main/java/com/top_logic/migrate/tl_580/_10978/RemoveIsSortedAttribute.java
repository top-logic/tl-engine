/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl_580._10978;

import com.top_logic.element.meta.kbbased.KBBasedMetaAttribute;
import com.top_logic.knowledge.event.convert.EventRewriter;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.migrate.tl.attribute.RemoveAttributeValue;
import com.top_logic.migrate.tl.util.LazyEventRewriter;

/**
 * Class containing rewriter that fixes Ticket #10978, i.e. it removes the attribute isSorted from
 * type {@link #META_ATTRIBUTE_TYPE}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RemoveIsSortedAttribute {

	private static final String META_ATTRIBUTE_TYPE = KBBasedMetaAttribute.OBJECT_NAME;

	private static final String IS_SORTED_ATTRIBUTE = "isSorted";

	/** {@link LazyEventRewriter} creating {@link #createRewriter()} */
	public static final LazyEventRewriter LAZY_INSTANCE = new LazyEventRewriter() {

		@Override
		public EventRewriter createRewriter(KnowledgeBase srcKB, KnowledgeBase destKb) {
			return RemoveIsSortedAttribute.createRewriter();
		}

	};

	/**
	 * Creates an {@link EventRewriter} removing {@link #IS_SORTED_ATTRIBUTE}.
	 */
	public static EventRewriter createRewriter() {
		return RemoveAttributeValue.removeAllAttributeValues(META_ATTRIBUTE_TYPE, IS_SORTED_ATTRIBUTE);
	}
}

