/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.layout.ResourceProvider;


/**
 * Extension for the {@link KnowledgeBase}, which enables the creation of different wrappers for
 * items of the same static type (table).
 * 
 * <p>
 * The dynamic type name of a {@link KnowledgeItem} is defined by a {@link WrapperResolver} instance
 * for all item from the same table based on certain attribute values of the given item, see
 * {@link #getDynamicType(KnowledgeItem)}.
 * </p>
 * 
 * <p>
 * A UI-centric resource name for a dynamic type is constructed by {@link #getResourceType(String)}.
 * </p>
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public interface WrapperResolver {

    /**
	 * Convert the dynamic type name into a type specification for the UI (compatible with
	 * {@link ResourceProvider#getType(Object)}).
	 * 
	 * @param dynamicType
	 *        The dynamic type name produced by {@link #getDynamicType(KnowledgeItem)}.
	 * @return The resource type for an object with the given dynamic type.
	 */
    public String getResourceType(String dynamicType);

    /**
	 * Resolve the dynamic type name for the given persistent item.
	 * 
	 * @param item
	 *        The persistent item to resolve its dynamic type for.
	 * @return The dynamic type name.
	 */
    public String getDynamicType(KnowledgeItem item);
}
