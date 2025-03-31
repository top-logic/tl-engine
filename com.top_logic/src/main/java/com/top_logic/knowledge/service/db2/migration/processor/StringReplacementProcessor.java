/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.processor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.top_logic.basic.Log;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;

/**
 * {@link StringColumnTransformProcessor} that uses a (list of) configured {@link Mapping}s that are
 * applied in given order to the value in the configured column.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class StringReplacementProcessor extends StringColumnTransformProcessor<StringReplacementProcessor.Config> {

	/**
	 * Configuration options for {@link StringReplacementProcessor}.
	 */
	@TagName("string-column-transform")
	public interface Config extends StringColumnTransformProcessor.Config<StringReplacementProcessor> {

		/** Configuration name of {@link #getReplacements()}. */
		String REPLACEMENTS = "replacements";

		/**
		 * Replacements the are applied to the value of {@link #getColumn()} in each row of
		 * {@link #getTable()}.
		 * 
		 * @see RegExpReplacement
		 * @see StringReplacement
		 */
		@Name(REPLACEMENTS)
		@DefaultContainer
		List<PolymorphicConfiguration<Mapping<? super String, String>>> getReplacements();

	}

	private List<Mapping<? super String, String>> _replacements;

	/**
	 * This constructor creates a new {@link StringReplacementProcessor}.
	 */
	public StringReplacementProcessor(InstantiationContext context, Config config) {
		super(context, config);
		_replacements = TypedConfiguration.getInstanceListReadOnly(context, config.getReplacements());
	}

	@Override
	protected void processRows(Log log, ResultSet rows, String table, String column, List<String> keyColumns)
			throws SQLException {
		
		log.info("Updating values in column '" + column + "' of table '" + table + "'.");

		int cnt = 0;
		while (rows.next()) {
			String value = rows.getString(1);
			String original = value;
			for (Mapping<? super String, String> replacement : _replacements) {
				value = replacement.map(value);
			}
			if (StringServices.equals(value, original)) {
				continue;
			}

			cnt++;
			rows.updateString(1, value);
			rows.updateRow();
		}

		log.info("Updated values in column '" + column + "' of " + cnt + " rows of table '" + table + "'.");
	}

}

