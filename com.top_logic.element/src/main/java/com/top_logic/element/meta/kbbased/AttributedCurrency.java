/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.db2.KnowledgeItemImpl;
import com.top_logic.knowledge.wrap.currency.Currency;
import com.top_logic.model.TLObject;

/**
 * Group implementation that returns the dynamic currency type as {@link TLObject#tType()}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AttributedCurrency extends Currency implements AttributedObject {

	/**
	 * @see KnowledgeItemImpl#createWrapper(KnowledgeItem)
	 */
	@CalledByReflection
	public AttributedCurrency(KnowledgeObject ko) {
		super(ko);
	}

}
