/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.table.export.DownloadNameProvider;
import com.top_logic.layout.table.export.ModelDownloadName;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link DownloadNameProvider} computing an export file name with TL-Script.
 * 
 * @author <a href="mailto:cca@top-logic.com">Christian Canterino</a>
 */
public class DownloadNameByExpression<C extends DownloadNameByExpression.Config<?>> extends ModelDownloadName<C> {

	/**
	 * Configuration options for {@link DownloadNameByExpression}.
	 */
	@TagName("download-name-by-expression")
	public interface Config<I extends DownloadNameByExpression<?>> extends ModelDownloadName.Config<I> {

		/**
		 * Function computing the download-name for a {@link #getModel()} object.
		 * 
		 * <p>
		 * The function is expected to take the {@link #getModel()} object as argument and return a
		 * file name for the given object. The computed name can either be a literal {@link String}
		 * value or a {@link ResKey} for internationalization.
		 * </p>
		 * 
		 * <p>
		 * The computed value is passed as single argument value to the configured
		 * {@link #getDownloadNameTemplate()}.
		 * </p>
		 */
		@Mandatory
		Expr getExpr();

	}

	final QueryExecutor _expr;

	/**
	 * Creates a new {@link DownloadNameByExpression}.
	 */
	public DownloadNameByExpression(InstantiationContext context, C config) {
		super(context, config);
		_expr = QueryExecutor.compile(config.getExpr());
	}

	@Override
	protected ResKey createDownloadName(Object model) {
		Object result = _expr.execute(model);
		return getConfig().getDownloadNameTemplate().fill(result);
	}

}