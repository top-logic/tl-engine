/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.log;

import static com.top_logic.layout.table.SortConfigFactory.*;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.top_logic.basic.StringServices;
import com.top_logic.common.folder.FolderFieldProvider;
import com.top_logic.common.folder.LogColumnComparator;
import com.top_logic.common.folder.impl.FileDocument;
import com.top_logic.common.folder.model.FolderNode;
import com.top_logic.common.folder.ui.FolderColumnDescriptionBuilder;
import com.top_logic.common.webfolder.WebFolderUtils;
import com.top_logic.common.webfolder.model.WebFolderAccessor;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.layout.IdentityAccessor;
import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.layout.table.SortConfig;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.FieldProvider;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.log.FileAccessor;

/**
 * {@link FolderColumnDescriptionBuilder} which additionally creates and returns table configuration
 * for a list of log files.
 * 
 * @author <a href="mailto:fma@top-logic.com">fma</a>
 */
public class LogColumnDescriptionBuilder extends FolderColumnDescriptionBuilder {

	/**
	 * Creates a new instance of {@link LogColumnDescriptionBuilder}.
	 */
	public LogColumnDescriptionBuilder(ExecutableState canAddToClipboard, ExecutableState canUpdate,
			ExecutableState canDelete) {
		super(canAddToClipboard, canUpdate, canDelete);
	}

	@Override
	protected FieldProvider createFolderFieldProvider(ExecutableState allowClipboard, ExecutableState allowWrite,
			ExecutableState allowDelete) {
		return new FolderFieldProvider(allowClipboard, allowWrite, allowDelete);
	}

	/**
	 * Creates and returns the TableConfiguration for a folder with log files.
	 */
	public TableConfiguration createLogColumns() {
		TableConfiguration columns = TableConfiguration.table();
		columns.setResPrefix(WebFolderUtils.DEFAULT_WEBFOLDER_TABLE_RESOURCES);
		columns.getDefaultColumn().setAccessor(FileDocumentAccessor.INSTANCE);
		ColumnConfiguration nameColumn = columns.declareColumn(WebFolder.NAME);
		nameColumn.setAccessor(IdentityAccessor.INSTANCE);
		createNameColumn(nameColumn);
		createDateColumn(columns.declareColumn(WebFolderAccessor.DATE));
		createSizeColumn(columns.declareColumn(WebFolderAccessor.SIZE));
		createDownloadColumn(columns.declareColumn(WebFolderAccessor.DOWNLOAD));

		// Sort initially by date
		List<SortConfig> initialSortOrder = Collections.singletonList(descending(WebFolderAccessor.DATE));
		columns.setDefaultSortOrder(initialSortOrder);
		return columns;
	}

	@Override
	protected Comparator getComparator() {
		return LogColumnComparator.INSTANCE;
	}

	/**
	 * Read accessor for {@link FolderNode}
	 */
	public static class FileDocumentAccessor extends ReadOnlyAccessor<FolderNode> {

		/** Singleton instance. */
		public static FileDocumentAccessor INSTANCE = new FileDocumentAccessor();

		@Override
		public Object getValue(FolderNode node, String property) {
			File file = getFile(node);
			if (StringServices.startsWithChar(property, '_'))
				property = property.substring(1);
			return FileAccessor.INSTANCE.getValue(file, property);
		}

		private File getFile(FolderNode node) {
			return getFileDocument(node).getFile();
		}

		private FileDocument getFileDocument(FolderNode node) {
			return (FileDocument) node.getBusinessObject();
		}

	}

}
