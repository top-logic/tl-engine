/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.expr.internal;

import java.util.Collections;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocator;
import com.top_logic.element.meta.kbbased.filtergen.CustomSingleSourceValueLocator;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.model.TLObject;

/**
 * {@link AttributeValueLocator} that unwraps a {@link TLObject}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AsData extends CustomSingleSourceValueLocator {

	/**
	 * Configuration options for {@link AsData}.
	 */
	@TagName("as-data")
	public interface Config extends PolymorphicConfiguration<AsData> {
		// Pure marker interface.
	}

	/**
	 * Creates a {@link AsData} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AsData(InstantiationContext context, Config config) {
		this();
	}

	/**
	 * Singleton {@link AsData} instance.
	 */
	public static final AsData INSTANCE = new AsData();

	private AsData() {
		// Singleton constructor.
	}

	@Override
	public Object internalLocateAttributeValue(Object obj) {
		if (!(obj instanceof TLObject)) {
			return null;
		}

		TLObject wrapper = (TLObject) obj;
		return wrapper.tHandle();
	}

	@Override
	public Set<? extends TLObject> locateReferers(Object value) {
		if (!(value instanceof KnowledgeItem)) {
			return Collections.emptySet();
		}
		KnowledgeItem ki = (KnowledgeItem) value;
		return Collections.singleton(ki.getWrapper());
	}

}
