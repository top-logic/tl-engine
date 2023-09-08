/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.EnumerationNameMapping;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.layout.table.SortConfig;
import com.top_logic.layout.table.SortConfigFactory;
import com.top_logic.layout.table.model.TableConfiguration.SortDirection;

/**
 * {@link AbstractConfigurationValueProvider} to parse sort order of table configurations.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultSortOrderFormat extends AbstractConfigurationValueProvider<Iterable<SortConfig>> {

	public static final DefaultSortOrderFormat INSTANCE = new DefaultSortOrderFormat();

	private DefaultSortOrderFormat() {
		super(Iterable.class);
	}

	@Override
	public Iterable<SortConfig> defaultValue() {
		return Collections.emptyList();
	}

	@Override
	protected Iterable<SortConfig> getValueEmpty(String propertyName) throws ConfigurationException {
		return defaultValue();
	}

	@Override
	protected Iterable<SortConfig> getValueNonEmpty(String propertyName, CharSequence propertyValue)
			throws ConfigurationException {
		String configValue = propertyValue.toString();
		if (isEmptyDeclaration(configValue)) {
			return getValueEmpty(propertyName);
		}
		checkForInvalidDeclaration(configValue);
		List<SortConfig> sortConfigs = parseDeclaration(configValue);

		return sortConfigs;
	}

	private List<SortConfig> parseDeclaration(String configValue) throws ConfigurationException {
		String[] columnConfigs = configValue.trim().split(",");
		List<SortConfig> sortConfigs = new LinkedList<>();
		for (String columnConfig : columnConfigs) {
			sortConfigs.add(parseSingleColumnDeclaration(columnConfig));
		}
		return sortConfigs;
	}

	private void checkForInvalidDeclaration(String configValue) throws ConfigurationException {
		String normalizedConfigValue = configValue.trim();
		if (StringServices.startsWithChar(normalizedConfigValue, ',')
			|| StringServices.endsWithChar(normalizedConfigValue, ',')) {
			throwInvalidFormatException();
		}
	}

	private SortConfig parseSingleColumnDeclaration(String columnConfigValue)
			throws ConfigurationException {
		checkForEmptyDeclaration(columnConfigValue);
		checkForInvalidColumnDeclarationFormat(columnConfigValue);

		String[] columnConfig = columnConfigValue.trim().split("\\s*:\\s*");
		checkForEmptyColumnName(columnConfig);

		if (isConvenienceDeclaration(columnConfig)) {
			return createConfigFromConvenienceDeclaration(columnConfig);
		} else {
			return createConfigFromStandardDeclaration(columnConfig);
		}
	}

	private boolean isConvenienceDeclaration(String[] columnConfig) {
		return columnConfig.length == 1;
	}

	private SortConfig createConfigFromConvenienceDeclaration(String[] columnConfig) {
		String columnName = columnConfig[0];
		return SortConfigFactory.ascending(columnName);
	}

	private SortConfig createConfigFromStandardDeclaration(String[] columnConfig) throws ConfigurationException {
		String columnName = columnConfig[0];
		String sortDirectionConfig = columnConfig[1];
		SortDirection sortDirection = ConfigUtil.getEnum(SortDirection.class, sortDirectionConfig);
		boolean ascending = sortDirection == SortDirection.ascending;
		return SortConfigFactory.sortConfig(columnName, ascending);
	}

	private void checkForEmptyColumnName(String[] columnConfig) throws ConfigurationException {
		String columnName = columnConfig[0];
		if (StringServices.isEmpty(columnName)) {
			throw new ConfigurationException("Column name must not be empty!");
		}
	}

	private void checkForEmptyDeclaration(String configValue) throws ConfigurationException {
		if (isEmptyDeclaration(configValue)) {
			throw new ConfigurationException("Column sort configuration must not be empty!");
		}
	}

	private boolean isEmptyDeclaration(String configValue) {
		String normalizedConfigValue = (configValue != null) ? configValue.trim() : configValue;
		return StringServices.isEmpty(normalizedConfigValue);
	}

	private void checkForInvalidColumnDeclarationFormat(String configValue) throws ConfigurationException {
		String normalizedConfigValue = configValue.trim();
		if (normalizedConfigValue.endsWith(":")) {
			throwInvalidFormatException();
		}
		String[] columnConfig = normalizedConfigValue.split("\\s*:\\s*");
		if (columnConfig.length > 2) {
			throwInvalidFormatException();
		}
	}

	private void throwInvalidFormatException() throws ConfigurationException {
		throw new ConfigurationException(
			"Column sort configuration must follow convention: [columnName_1:sortDirection_1, ...]");
	}

	@Override
	protected String getSpecificationNonNull(Iterable<SortConfig> configValue) {
		StringBuilder result = new StringBuilder();
		boolean writeSeparator = false;
		for (SortConfig config : configValue) {
			if (writeSeparator) {
				result.append(',');
			}
			result.append(config.getColumnName());
			result.append(':');
			if (config.getAscending()) {
				result.append(EnumerationNameMapping.INSTANCE.map(TableConfiguration.SortDirection.ascending));
			} else {
				result.append(EnumerationNameMapping.INSTANCE.map(TableConfiguration.SortDirection.descending));
			}
			writeSeparator = true;
		}
		return result.toString();
	}

}

