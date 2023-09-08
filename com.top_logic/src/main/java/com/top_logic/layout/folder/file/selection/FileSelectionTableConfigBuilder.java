/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.folder.file.selection;

import static java.util.Collections.*;

import java.io.File;
import java.text.DecimalFormat;

import com.top_logic.common.folder.LogColumnComparator;
import com.top_logic.common.folder.model.FolderNode;
import com.top_logic.common.folder.util.FolderNodeToFileMapping;
import com.top_logic.common.webfolder.WebFolderUtils;
import com.top_logic.common.webfolder.model.WebFolderAccessor;
import com.top_logic.common.webfolder.ui.WebFolderMimetypeProperty;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.PropertyAccessor;
import com.top_logic.layout.SelfPropertyAccessor;
import com.top_logic.layout.accessors.MappingPropertyAccessor;
import com.top_logic.layout.accessors.PropertyAccessorAdaptor;
import com.top_logic.layout.provider.FormatLabelProvider;
import com.top_logic.layout.table.SortConfigFactory;
import com.top_logic.layout.table.filter.SimpleComparableFilterProvider;
import com.top_logic.layout.table.filter.SimpleDateFilterProvider;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.util.log.FileAccessor;

/**
 * Builds the {@link TableConfiguration} for the {@link FileSelectionComponent}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
final class FileSelectionTableConfigBuilder {

	TableConfiguration build() {
		TableConfiguration tableConfig = TableConfigurationFactory.table();
		tableConfig.setResPrefix(WebFolderUtils.DEFAULT_WEBFOLDER_TABLE_RESOURCES);

		declareNameColumn(tableConfig);
		declareTypeColumn(tableConfig);
		declareDateColumn(tableConfig);
		declareSizeColumn(tableConfig);

		tableConfig.setFixedColumnCount(1);
		tableConfig.setDefaultSortOrder(singletonList(SortConfigFactory.ascending(WebFolder.NAME)));
		return tableConfig;
	}

	private void declareNameColumn(TableConfiguration tableConfig) {
		ColumnConfiguration column = tableConfig.declareColumn(WebFolder.NAME);
		column.setAccessor(toAccessor(SelfPropertyAccessor.INSTANCE));
		column.setComparator(LogColumnComparator.INSTANCE);
		column.setDefaultColumnWidth("300px");
	}

	private void declareTypeColumn(TableConfiguration tableConfig) {
		ColumnConfiguration column = tableConfig.declareColumn(WebFolderAccessor.TYPE);
		column.setAccessor(toAccessor(mapFolderNodeToFile(WebFolderMimetypeProperty.INSTANCE)));
		column.setDefaultColumnWidth("100px");
	}

	private void declareDateColumn(TableConfiguration tableConfig) {
		ColumnConfiguration column = tableConfig.declareColumn(WebFolderAccessor.DATE);
		column.setAccessor(toAccessor(mapFolderNodeToFile(FileAccessor.FILE_DATE)));
		column.setLabelProvider(new FormatLabelProvider(HTMLFormatter.getInstance().getDateTimeFormat()));
		column.setFilterProvider(SimpleDateFilterProvider.INSTANCE);
		column.setDefaultColumnWidth("120px");
	}

	private void declareSizeColumn(TableConfiguration tableConfig) {
		ColumnConfiguration column = tableConfig.declareColumn(WebFolderAccessor.SIZE);
		column.setAccessor(toAccessor(mapFolderNodeToFile(FileAccessor.FILE_LENGTH)));
		column.setLabelProvider(new FormatLabelProvider(new DecimalFormat("#,##0 Byte")));
		column.setFilterProvider(SimpleComparableFilterProvider.INSTANCE);
		column.setCssClass("tblRight");
		column.setDefaultColumnWidth("80px");
	}

	private <T> Accessor<T> toAccessor(PropertyAccessor<T> propertyAccessor) {
		return new PropertyAccessorAdaptor<>(propertyAccessor);
	}

	private PropertyAccessor<FolderNode> mapFolderNodeToFile(PropertyAccessor<? super File> innerAccessor) {
		return new MappingPropertyAccessor<>(FolderNodeToFileMapping.INSTANCE, innerAccessor);
	}

}
