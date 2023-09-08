/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.db.transformers;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.knowledge.service.db2.migration.db.RowValue;
import com.top_logic.knowledge.service.db2.migration.db.RowWriter;

/**
 * {@link AbstractRowTransformer} skipping rows that matches a configured filter.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TableRowSkip extends AbstractRowTransformer<TableRowSkip.Config> {

	private Filter<? super RowValue> _matcher;

	/**
	 * Typed configuration interface definition for {@link TableRowSkip}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends AbstractRowTransformer.Config<TableRowSkip> {

		/**
		 * Configuration of the filter that a row must match to be skipped.
		 */
		@Mandatory
		PolymorphicConfiguration<Filter<? super RowValue>> getMatcher();

		/**
		 * Setter for {@link #getMatcher()}.
		 */
		void setMatcher(PolymorphicConfiguration<Filter<? super RowValue>> matcher);

	}

	/**
	 * Create a {@link TableRowSkip}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public TableRowSkip(InstantiationContext context, Config config) {
		super(context, config);
		_matcher = context.getInstance(config.getMatcher());
	}

	@Override
	protected void internalTransform(RowValue row, RowWriter out) {
		if (_matcher.accept(row)) {
			// skip row
		} else {
			out.write(row);
		}
	}

}
