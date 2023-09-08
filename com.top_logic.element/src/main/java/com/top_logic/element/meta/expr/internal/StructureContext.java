/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
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
import com.top_logic.element.structured.StructuredElement;

/**
 * {@link AttributeValueLocator} that returns the {@link StructuredElement#getStructureContext() the
 * structure context} of a {@link StructuredElement}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class StructureContext extends CustomSingleSourceValueLocator {

	/**
	 * Configuration options for {@link StructureContext}.
	 */
	@TagName("structure-context")
	public interface Config extends PolymorphicConfiguration<StructureContext> {
		// Pure marker interface.
	}

	/**
	 * Singleton {@link StructureContext} instance.
	 */
	public static final StructureContext INSTANCE = new StructureContext();

	/**
	 * Creates a {@link StructureContext} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public StructureContext(InstantiationContext context, Config config) {
		this();
	}

	private StructureContext() {
		// Singleton constructor.
	}

	@Override
	public Object internalLocateAttributeValue(Object anObject) {
		return ((StructuredElement) anObject).getStructureContext();
	}
}