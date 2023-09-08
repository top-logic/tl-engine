/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.binding;

import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.model.TLObject;

/**
 * Algorithm determining the concrete {@link TLObject} type of a {@link KnowledgeItem}.
 * 
 * @see #createBinding(KnowledgeItem)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ImplementationBinding {

	/**
	 * Initialization after construction.
	 * 
	 * @param staticType
	 *        The table type this {@link ImplementationBinding} is used for.
	 */
	void initTable(MOClass staticType);

	/**
	 * Creates the application wrapper for the given persistent item.
	 */
	TLObject createBinding(KnowledgeItem item);

	/**
	 * The default implementation class for objects that are stored in the represented table. May be
	 * <code>null</code> when there is no useful default.
	 */
	Class<? extends TLObject> getDefaultImplementationClassForTable();

}
