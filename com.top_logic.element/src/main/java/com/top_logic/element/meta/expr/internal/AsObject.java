/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.expr.internal;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocator;
import com.top_logic.element.meta.kbbased.filtergen.CustomSingleSourceValueLocator;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.model.TLObject;

/**
 * {@link AttributeValueLocator} that wraps a {@link KnowledgeItem} back to a {@link TLObject}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AsObject extends CustomSingleSourceValueLocator {

	/**
	 * Configuration options for {@link AsObject}.
	 */
	@TagName("as-object")
	public interface Config extends PolymorphicConfiguration<AsObject> {
		// Pure marker interface.
	}

	/**
	 * Singleton {@link AsObject} instance.
	 */
	public static final AsObject INSTANCE = new AsObject();

	/**
	 * Creates a {@link AsObject} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AsObject(InstantiationContext context, Config config) {
		this();
	}

	private AsObject() {
		// Singleton constructor.
	}

	@Override
	public Object internalLocateAttributeValue(Object obj) {
		if (!(obj instanceof KnowledgeItem)) {
			return null;
		}

		KnowledgeItem item = (KnowledgeItem) obj;
		return item.getWrapper();
	}

}
