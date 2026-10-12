/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.component.title.ModelTitle;
import com.top_logic.mig.html.layout.DisplayChannelValue;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link ModelTitle} computing the title for the component by applying an {@link Expr} on the
 * channel of the component.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp
public class TitleByExpression<C extends TitleByExpression.Config<?>> extends ModelTitle<C> {

	/**
	 * Configuration options for {@link TitleByExpression}.
	 */
	@TagName("title-by-expression")
	public interface Config<I extends TitleByExpression<?>> extends ModelTitle.Config<I> {

		/**
		 * Function computing the label for the {@link #getModel()} object.
		 * 
		 * <p>
		 * The function is expected to take the {@link #getModel()} object as argument and return a
		 * label for the given object. The computed label can either be a literal {@link String}
		 * value or a {@link ResKey} for internationalization.
		 * </p>
		 * 
		 * <p>
		 * If {@link #getKey()} is also given, the computed label is passed as single argument value
		 * to the configured {@link ResKey} for dynamic embedding. In that case, the text in
		 * {@link #getKey()} is expected to contain a placeholder <code>{0}</code> where the
		 * computed label is to be inserted.
		 * </p>
		 */
		@Mandatory
		Expr getExpr();

	}

	final QueryExecutor _expr;

	/**
	 * Creates a new TitleByExpression.
	 */
	public TitleByExpression(InstantiationContext context, C config) {
		super(context, config);
		_expr = QueryExecutor.compile(config.getExpr());
	}

	@Override
	protected HTMLFragment createTitle(ComponentChannel channel) {
		DisplayChannelValue control = new DisplayChannelValue(channel) {

			@Override
			protected String label(DisplayContext context, Object channelValue) {
				return super.label(context, _expr.execute(channelValue));
			}

		};
		control.setKey(getConfig().getKey());
		return control;
	}

}

