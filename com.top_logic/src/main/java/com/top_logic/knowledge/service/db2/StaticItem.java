/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;

/**
 * {@link TLObject} that is its own {@link TLObject#tHandle() item}, resp. a {@link KnowledgeItem}
 * that is its own {@link KnowledgeItem#getWrapper() wrapper}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface StaticItem extends KnowledgeItem, TLObject {

	@Override
	default KnowledgeItem tHandle() {
		return this;
	}

	@Override
	default <T extends TLObject> T getWrapper() {
		@SuppressWarnings("unchecked")
		T wrapper = (T) this;
		return wrapper;
	}

	@Override
	default TLStructuredType tType() {
		return null;
	}

}

