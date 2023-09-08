/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui;

import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.provider.AbstractMappingResourceProviderBase;
import com.top_logic.layout.provider.MetaResourceProvider;

/**
 * {@link ResourceProvider} for {@link KnowledgeItem} based on their
 * {@link KnowledgeItem#getWrapper() business objects}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class KnowledgeItemResourceProvider extends AbstractMappingResourceProviderBase {

	/**
	 * Creates a {@link KnowledgeItemResourceProvider}.
	 */
	public KnowledgeItemResourceProvider() {
		super(MetaResourceProvider.INSTANCE);
	}

	@Override
	protected Object mapValue(Object anObject) {
		if (anObject instanceof KnowledgeItem) {
			return ((KnowledgeItem) anObject).getWrapper();
		}
		return anObject;
	}

}
