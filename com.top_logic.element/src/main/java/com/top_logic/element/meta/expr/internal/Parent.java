/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.expr.internal;

import java.util.HashSet;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocator;
import com.top_logic.element.meta.kbbased.filtergen.CustomSingleSourceValueLocator;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.model.TLObject;

/**
 * {@link AttributeValueLocator} that returns the {@link StructuredElement#getParent() parent
 * object} of a {@link StructuredElement}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class Parent extends CustomSingleSourceValueLocator {

	/**
	 * Configuration options for {@link Parent}
	 */
	@TagName("structure-parent")
	public interface Config extends PolymorphicConfiguration<Parent> {
		// Pure marker interface.
	}

	/**
	 * Creates a {@link Parent} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public Parent(InstantiationContext context, Config config) {
		this();
	}

	/**
	 * Singleton {@link Parent} instance.
	 */
	public static final Parent INSTANCE = new Parent();

	private Parent() {
		// Singleton constructor.
	}

	@Override
	public Object internalLocateAttributeValue(Object anObject) {
		return ((StructuredElement) anObject).getParent();
	}

	@Override
	public Set<? extends TLObject> locateReferers(Object value) {
		return new HashSet<>(((StructuredElement) value).getChildren());
	}
}