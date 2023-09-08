/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge;

import com.top_logic.dob.AttributeStorage;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ReferenceStorage;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.search.Expression;

/**
 * {@link AttributeStorage} for {@link KIReference}
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface KIReferenceStorage extends ReferenceStorage {

	/**
	 * Creates an {@link Expression} which finds each object which has a specific
	 * {@link KnowledgeItem} as value for the given reference.
	 * 
	 * @param attribute
	 *        the attribute to get refers for.
	 * @param searchType
	 *        the type to search in.
	 * @param targetType
	 *        the type of the item later filled into the parameter
	 * @param itemParam
	 *        the name of the parameter which is later filled with the specific
	 *        {@link KnowledgeItem}
	 */
	Expression getRefererExpression(MOReference attribute, MOClass searchType, MetaObject targetType,
			String itemParam);

	/**
	 * Creates an {@link Expression} which finds each object which value for the given reference is
	 * contained in a specific set.
	 * 
	 * @param attribute
	 *        the attribute to get refers for.
	 * @param searchType
	 *        the type to search in.
	 * @param targetType
	 *        the type of the item later filled into the parameter
	 * @param itemSetParam
	 *        the name of the parameter which is later filled with the specific set of
	 *        {@link KnowledgeItem}
	 */
	Expression getReferenceInSetExpression(MOReference attribute, MOClass searchType, MetaObject targetType,
			String itemSetParam);

}
