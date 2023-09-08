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
 * {@link AttributeValueLocator} that returns the {@link StructuredElement#getRoot() root object} of
 * a {@link StructuredElement}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class Root extends CustomSingleSourceValueLocator {

	/**
	 * Configuration options for {@link Root}.
	 */
	@TagName("structure-root")
	public interface Config extends PolymorphicConfiguration<Root> {
		// Pure marker interface.
	}

	/**
	 * Singleton {@link Root} instance.
	 */
	public static final Root INSTANCE = new Root();

	/**
	 * Creates a {@link Root} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public Root(InstantiationContext context, Config config) {
		this();
	}

	private Root() {
		// Singleton constructor.
	}

	@Override
	public Object internalLocateAttributeValue(Object anObject) {
		return ((StructuredElement) anObject).getRoot();
	}
}