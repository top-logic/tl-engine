/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.db.transformers;

import java.util.Set;

import com.top_logic.basic.config.CommaSeparatedStringSet;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.knowledge.service.db2.migration.db.RowValue;
import com.top_logic.knowledge.service.db2.migration.db.RowWriter;

/**
 * {@link AbstractRowTransformer} transforming the column values of a given set of columns.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractColumnTransformer<C extends AbstractColumnTransformer.Config<?>>
		extends AbstractRowTransformer<C> {

	private Set<String> _columns;

	/**
	 * Configuration of an {@link AbstractColumnTransformer}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config<I extends AbstractColumnTransformer<?>> extends AbstractRowTransformer.Config<I> {

		/** Name of {@link #getColumns()}. */
		String COLUMNS = "attributes";

		/**
		 * The name of the columns which values must be transformed.
		 */
		@Name(COLUMNS)
		@Format(CommaSeparatedStringSet.class)
		Set<String> getColumns();

	}

	/**
	 * Creates a new {@link AbstractColumnTransformer}.
	 */
	public AbstractColumnTransformer(InstantiationContext context, C config) {
		super(context, config);
		_columns = config.getColumns();
	}

	/**
	 * The configured columns.
	 */
	public Set<String> getColumns() {
		return _columns;
	}

	@Override
	protected void internalTransform(RowValue row, RowWriter out) {
		for (String column : _columns) {
			internalTransform(row, column);
		}
		out.write(row);
	}

	/**
	 * Transforms the given {@link RowValue} for the given column.
	 */
	protected abstract void internalTransform(RowValue row, String column);
}

