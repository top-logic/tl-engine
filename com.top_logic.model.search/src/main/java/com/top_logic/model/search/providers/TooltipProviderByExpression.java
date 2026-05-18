/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.Logger;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.TooltipProvider;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.model.search.expr.ToString;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.util.Resources;

/**
 * {@link TooltipProvider} that can be implemented with a <i>TL-Script</i> expression.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Label("Tooltip via TL-Script")
@InApp
public class TooltipProviderByExpression<C extends TooltipProviderByExpression.Config<?>>
		extends AbstractConfiguredInstance<C> implements TooltipProvider {

	/**
	 * Configuration options for {@link TooltipProviderByExpression}.
	 */
	public interface Config<I extends TooltipProviderByExpression<?>> extends PolymorphicConfiguration<I> {

		/**
		 * Function computing the tooltip of a given object.
		 * 
		 * <p>
		 * The function expects the object for which a tooltip should be computed as single
		 * argument. The result of the function is displayed as tooltip.
		 * </p>
		 * 
		 * <p>
		 * The result may either be a plain string, an internationalized string, or a HTML fragment.
		 * </p>
		 */
		@Mandatory
		Expr getTooltip();

	}

	private QueryExecutor _tooltipExpr;

	/**
	 * Creates a new {@link TooltipProviderByExpression}.
	 */
	public TooltipProviderByExpression(InstantiationContext context, C config) {
		super(context, config);
		_tooltipExpr = QueryExecutor.compile(config.getTooltip());
	}

	/**
	 * Evaluates the configured {@link Expr label expression} with the given object and routes the
	 * result through the inner {@link ResourceProvider}.
	 */
	@Override
	public String getTooltip(Object object) {
		if (object == null) {
			return null;
		}
		Object rawResult = _tooltipExpr.execute(object);
		if (rawResult == null) {
			return null;
		}
		if (rawResult instanceof HTMLFragment html) {
			try (TagWriter out = new TagWriter()) {
				html.write(DefaultDisplayContext.getDisplayContext(), out);

				return out.toString();
			} catch (IOException ex) {
				Logger.error("Tooltip creation failed for '" + object + "'.", ex, TooltipProviderByExpression.class);
				return TagUtil.encodeXML("ERROR: " + ex.getMessage());
			}
		}
		if (rawResult instanceof ResKey key) {
			return TagUtil.encodeXML(Resources.getInstance().getString(key));
		}
		return ToString.toString(rawResult);
	}

}
