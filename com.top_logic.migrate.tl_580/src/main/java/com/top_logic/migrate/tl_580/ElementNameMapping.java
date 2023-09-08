/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl_580;

import com.top_logic.basic.col.Mapping;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.model.TLClass;

/**
 * Mapping from the {@link KnowledgeItem} representing a {@link TLClass} to the classes name.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ElementNameMapping implements Mapping<Object, String> {

	@Override
	public String map(Object input) {
		TLClass tlClass = (TLClass) ((KnowledgeItem) input).getWrapper();
		return tlClass.getName();
	}

}

