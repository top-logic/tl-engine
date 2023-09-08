/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.MetaResourceProvider;

/**
 * {@link LabelProvider}, that provides label base on objects type (e. g. objects class name}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TypeBasedLabelProvider implements LabelProvider {

	/** Singleton instance of {@link TypeBasedLabelProvider} */
	public static final LabelProvider INSTANCE = new TypeBasedLabelProvider();

	private TypeBasedLabelProvider() {
		// Singleton
	}

	@Override
	public String getLabel(Object object) {
		if (object instanceof Wrapper) {
			return getWrapperTypeLabel((Wrapper) object);
		} else if (object instanceof KnowledgeItem) {
			return getWrapperTypeLabel((Wrapper) ((KnowledgeItem) object).getWrapper());
		} else {
			return object.getClass().getName();
		}
	}

	private String getWrapperTypeLabel(Wrapper wrapper) {
		return MetaResourceProvider.INSTANCE.getLabel(wrapper.tType());
	}

}
