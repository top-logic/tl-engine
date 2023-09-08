/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.persistency.attribute.macro;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.kbbased.storage.AbstractDerivedStorage;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.persistency.attribute.AbstractExpressionAttribute;
import com.top_logic.model.search.providers.AttributeByExpression;

/**
 * {@link AbstractDerivedStorage} that renders the expansion of a search expression macro.
 * 
 * <p>
 * The value of a {@link MacroAttribute} is a {@link SearchExpression} that invokes the configured
 * {@link Config#getExpr() expression} on the object defining the attribute.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MacroAttribute<C extends MacroAttribute.Config<?>> extends AbstractExpressionAttribute<C> {

	/**
	 * Configuration options for {@link AttributeByExpression}.
	 */
	@TagName("macro")
	public interface Config<I extends MacroAttribute<?>> extends AbstractExpressionAttribute.Config<I> {
		// Pure marker interface.
	}

	/**
	 * Creates a {@link MacroAttribute} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MacroAttribute(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public Object getAttributeValue(TLObject object, TLStructuredTypePart attribute) throws AttributeException {
		return getExpr();
	}

}
