/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui;

import java.util.Collections;
import java.util.Comparator;

import com.top_logic.base.mail.MailSenderService;
import com.top_logic.common.folder.ui.FolderColumnDescriptionBuilder;
import com.top_logic.common.webfolder.WebFolderUtils;
import com.top_logic.common.webfolder.model.WebFolderAccessor;
import com.top_logic.common.webfolder.ui.commands.VersionDialog;
import com.top_logic.knowledge.analyze.DefaultAnalyzeService;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.layout.LabelComparator;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.table.SortConfigFactory;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.FieldProvider;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.mig.html.NoLinkResourceProvider;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link FolderColumnDescriptionBuilder} which additionally creates and returns table configuration
 * for WebFolder and for a list of versions of a document.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class WebFolderColumnDescriptionBuilder extends FolderColumnDescriptionBuilder{

	/**
	 * Singleton {@link WebFolderColumnDescriptionBuilder} instance that is configured for full
	 * access.
	 * <p>
	 * That includes the manual locking: {@link WebFolderUIFactory#getManualLocking()}.
	 * </p>
	 */
	public static final WebFolderColumnDescriptionBuilder FULL_ACCESS_INSTANCE =
		new WebFolderColumnDescriptionBuilder(ExecutableState.EXECUTABLE, ExecutableState.EXECUTABLE,
			ExecutableState.EXECUTABLE, true);

	private final boolean _manualLocking;

	private boolean _withAnalysis = DefaultAnalyzeService.isAvailable();

	private boolean _withMail = MailSenderService.isConfigured();

	/**
	 * Creates a {@link WebFolderColumnDescriptionBuilder}.
	 * 
	 * @param canUpdate
	 *        Whether update is allowed.
	 * @param canDelete
	 *        Whether content deletion is allowed.
	 */
	public WebFolderColumnDescriptionBuilder(ExecutableState canAddToClipboard, ExecutableState canUpdate,
			ExecutableState canDelete, boolean manualLocking) {
		super(canAddToClipboard, canUpdate, canDelete);
		_manualLocking = manualLocking;
    }

	/**
	 * Whether to compare documents by keyswords.
	 */
	public void setAnalysis(boolean withAnalysis) {
		_withAnalysis = withAnalysis && DefaultAnalyzeService.isAvailable();
	}

	/**
	 * Whether sending documents by mail is enabled.
	 */
	public void setMail(boolean withMail) {
		_withMail = withMail && MailSenderService.isConfigured();
	}

	/**
	 * Creates and returns a table configuration suitable for a WebFolder.
	 */
    public TableConfiguration createWebFolderColumns() {
		TableConfiguration tableConfig = TableConfiguration.table();
		tableConfig.setResPrefix(WebFolderUtils.DEFAULT_WEBFOLDER_TABLE_RESOURCES);
		tableConfig.setDefaultSortOrder(
			Collections.singletonList(SortConfigFactory.ascending(WebFolder.NAME)));
		tableConfig.getDefaultColumn().setAccessor(WebFolderAccessor.INSTANCE);
		tableConfig.getDefaultColumn().setSortable(false);
    	
		createNameColumn(tableConfig.declareColumn(WebFolder.NAME));
		createDescriptionColumn(tableConfig.declareColumn(WebFolderAccessor.DESCRIPTION));
		createDateColumn(tableConfig.declareColumn(WebFolderAccessor.DATE));
		createTypeColumn(tableConfig.declareColumn(WebFolderAccessor.TYPE));
		createSizeColumn(tableConfig.declareColumn(WebFolderAccessor.SIZE));
		createDownloadColumn(tableConfig.declareColumn(WebFolderAccessor.DOWNLOAD));
		createLockColumn(tableConfig.declareColumn(WebFolderAccessor.LOCK));
		createVersionColumn(tableConfig.declareColumn(WebFolderAccessor.VERSION));
		if (_withMail) {
			createMailColumn(tableConfig.declareColumn(WebFolderAccessor.MAIL));
		}
		createClipboardColumn(tableConfig.declareColumn(WebFolderAccessor.CLIPBOARD));
		createDeleteColumn(tableConfig.declareColumn(WebFolderAccessor.DELETE));
		if (_withAnalysis) {
			createSimilarDocsColumn(tableConfig.declareColumn(WebFolderAccessor.SIMILAR_DOCUMENTS));
			createKeywordsColumn(tableConfig.declareColumn(WebFolderAccessor.KEYWORDS));
		}

		return tableConfig;
	}

	/**
	 * Creates and returns a table configuration to display different versions of a document.
	 */
	public TableConfiguration createVersionTableColumns() {
		TableConfiguration columns = TableConfiguration.table();
		columns.getDefaultColumn().setResourceProvider(NoLinkResourceProvider.INSTANCE);
		columns.getDefaultColumn().setAccessor(VersionedDocumentProperty.INSTANCE);
	
		createVersionDownloadColumn(columns.declareColumn(VersionDialog.VERSION_DOWNLOAD));
		createDateColumn(columns.declareColumn(VersionDialog.VERSION_DATE));
		createSizeColumn(columns.declareColumn(VersionDialog.VERSION_SIZE));
		createImmutableDescriptionColumn(columns.declareColumn(VersionDialog.VERSION_DESCRIPTION));
		createClipboardColumn(columns.declareColumn(WebFolderAccessor.CLIPBOARD));
		return columns;
	}
    


	private ColumnConfiguration createVersionDownloadColumn(ColumnConfiguration column) {
		column.setFieldProvider(VersionFieldProvider.INSTANCE);
		column.setControlProvider(DefaultFormFieldControlProvider.INSTANCE);
		column.setComparator(LabelComparator.newCachingInstance());
		
		return column;
	}

	public static void applyImmutableProperty(TableConfiguration aManager, boolean immutable) {
		ColumnConfiguration lockColumn = aManager.getDeclaredColumn(WebFolderAccessor.LOCK);
		if (lockColumn != null && (!lockColumn.isFrozen())) {
			lockColumn.setVisible(!immutable);
		}
		ColumnConfiguration deleteColumn = aManager.getDeclaredColumn(WebFolderAccessor.DELETE);
		if (deleteColumn != null && !deleteColumn.isFrozen()) {
			deleteColumn.setVisible(!immutable);
		}
	}

	@Override
	protected Renderer<Object> getRenderer() {
		return WebFolderNameRenderer.INSTANCE;
	}

	@Override
	protected Comparator getComparator() {
		// not static because depends on TLContext.
		return new WebFolderComparator();
	}

	@Override
	protected FieldProvider createFolderFieldProvider(ExecutableState allowClipboard, ExecutableState allowWrite,
			ExecutableState allowDelete) {
		return new WebFolderFieldProvider(allowClipboard, allowWrite, allowDelete, getManualLocking());
	}

	/**
	 * @see WebFolderUIFactory#getManualLocking()
	 */
	protected boolean getManualLocking() {
		return _manualLocking;
	}

}
