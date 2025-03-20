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
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.table.export.ModelDownloadName;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.util.Resources;

/**
 * {@link ModelDownloadName} computing the filename for the component by applying an {@link Expr} on
 * the channel of the component.
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
		 * Function computing the download-name for the {@link #getModel()} object.
		 * 
		 * <p>
		 * The function is expected to take the {@link #getModel()} object as argument and return a
		 * filename for the given object. The computed name can either be a literal {@link String}
		 * value or a {@link ResKey} for internationalization.
		 * </p>
		 * 
		 * <p>
		 * The computed label is passed as single argument value to the configured
		 * {@link com.top_logic.layout.table.export.ExcelExportHandler.Config#getDownloadNameKey()}
		 * for dynamic embedding. The text in
		 * {@link com.top_logic.layout.table.export.ExcelExportHandler.Config#getDownloadNameKey()}
		 * is expected to contain a placeholder <code>{0}</code> where the computed name is to be
		 * inserted.
		 * </p>
		 */
		@Mandatory
		Expr getExpr();

	}

	final QueryExecutor _expr;

	/**
	 * Creates a new DownloadNameByExpression.
	 */
	public DownloadNameByExpression(InstantiationContext context, C config) {
		super(context, config);
		_expr = QueryExecutor.compile(config.getExpr());
	}

	@Override
	protected String createDownloadName(ComponentChannel channel, ResKey key) {
		Object channelValue = channel.get();
		Object res = _expr.execute(channelValue);
		return Resources.getInstance().getMessage(key, res);
	}

}