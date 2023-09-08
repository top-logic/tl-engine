/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.layout.LabelProvider;

/**
 * {@link LabelProvider} for {@link KnowledgeObject} that actually displays the {@link Wrapper}
 * implementation.
 * 
 * @see KOResourceProvider
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class KOLabelProvider implements LabelProvider {

	/** Singleton {@link KOLabelProvider} instance. */
	public static final KOLabelProvider INSTANCE = new KOLabelProvider();

	private KOLabelProvider() {
		// singleton instance
	}

	private LabelProvider impl(Object object, Object orig) {
		if (object == orig) {
			// avoid recursive call. This may happen because some KO's are their own wrapper.
			return DefaultLabelProvider.INSTANCE;
		}
		return MetaLabelProvider.INSTANCE;
	}

	private Object toWrapper(Object object) {
		if (object == null) {
			return object;
		}
		return WrapperFactory.getWrapper((KnowledgeObject) object);
	}

	@Override
	public String getLabel(Object object) {
		Object mapped = toWrapper(object);
		return impl(mapped, object).getLabel(mapped);
	}

}

