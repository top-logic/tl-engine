/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.expr.internal;

import java.util.List;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.element.meta.expr.parser.ExpressionParser;
import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocator;
import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocatorFactory;

/**
 * Factory for atomic {@link AttributeValueLocator} configurations triggered directly from the
 * {@link ExpressionParser}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class OperationFactory {

	static final Class<?>[] NO_ARGS = {};

	/**
	 * Creates a {@link OperationFactory}.
	 */
	public OperationFactory() {
		super();
	}

	/**
	 * Creates {@link NavigateBackwards}.
	 */
	public PolymorphicConfiguration<? extends AttributeValueLocator> backwards(String module, String typeName,
			String attributeName) {
		return NavigateBackwards.createNavigateBackwards(module, typeName, attributeName);
	}

	/**
	 * Creates {@link TypeOf}.
	 */
	public PolymorphicConfiguration<? extends AttributeValueLocator> typeOf(String module, String typeName) {
		return TypeOf.newInstance(module, typeName);
	}

	/**
	 * Creates {@link MethodCall}.
	 */
	public PolymorphicConfiguration<? extends AttributeValueLocator> method(String className, String methodName) {
		return MethodCall.newInstance(className, methodName);
	}

	/**
	 * Creates {@link AssociationDestinations}.
	 */
	public PolymorphicConfiguration<? extends AttributeValueLocator> associationDestinations(String associationName) {
		return AssociationDestinations.newInstance(associationName);
	}

	/**
	 * Creates {@link AssociationSources}.
	 */
	public PolymorphicConfiguration<? extends AttributeValueLocator> associationSources(String associationName) {
		return AssociationSources.newInstance(associationName);
	}

	/**
	 * Creates {@link IndexAccess}.
	 */
	public PolymorphicConfiguration<? extends AttributeValueLocator> index(int index) {
		return IndexAccess.newInstance(index);
	}

	/**
	 * Looks up an {@link AttributeValueLocator} from the {@link AttributeValueLocatorFactory}.
	 */
	public PolymorphicConfiguration<? extends AttributeValueLocator> locator(String name, String spec) {
		return FactoryDispatch.newInstance(name, unquote(spec));
	}

	private String unquote(String spec) {
		if (spec == null) {
			return null;
		}
		return spec.substring(1, spec.length() - 1).replace("\\\"", "\"").replace("\\\'", "'").replace("\\\\", "\\");
	}

	/**
	 * Creates {@link GetValue}.
	 */
	public PolymorphicConfiguration<? extends AttributeValueLocator> value(String attributeName) {
		return GetValue.newInstance(attributeName);
	}

	/**
	 * Creates {@link GetValue}.
	 */
	public PolymorphicConfiguration<? extends AttributeValueLocator> data(String attributeName) {
		return GetValue.newInstance(attributeName);
	}

	/**
	 * Creates {@link Chain}.
	 */
	public PolymorphicConfiguration<? extends AttributeValueLocator> chain(
			List<PolymorphicConfiguration<? extends AttributeValueLocator>> locators) {
		return Chain.newInstance(locators);
	}

}
