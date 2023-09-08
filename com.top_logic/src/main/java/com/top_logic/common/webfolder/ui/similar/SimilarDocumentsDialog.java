/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui.similar;

import java.util.List;
import java.util.Map;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.col.MapBuilder;
import com.top_logic.common.webfolder.WebFolderUtils;
import com.top_logic.common.webfolder.model.WebFolderAccessor;
import com.top_logic.common.webfolder.ui.WebFolderColumnDescriptionBuilder;
import com.top_logic.common.webfolder.ui.WebFolderDateProperty;
import com.top_logic.common.webfolder.ui.WebFolderDescriptionProperty;
import com.top_logic.common.webfolder.ui.WebFolderMimetypeProperty;
import com.top_logic.common.webfolder.ui.WebFolderSizeProperty;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.knowledge.wrap.WrapperAccessor;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.DispatchingAccessor;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.IdentityAccessor;
import com.top_logic.layout.PropertyAccessor;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ResourceRenderer;
import com.top_logic.layout.basic.control.IconControl;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.form.tag.FormPageTag;
import com.top_logic.layout.messagebox.AbstractFormPageDialog;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.EditableRowTableModel;
import com.top_logic.layout.table.model.FormTableModel;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.mig.html.NoLinkResourceProvider;

/**
 * {@link SimilarDocumentsDialog} displays a {@link List} of {@link Document}s
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SimilarDocumentsDialog extends AbstractFormPageDialog {

	/**
	 * Name of the technical group needed to get correct resources in the table and its content
	 * fields.
	 */
	private static final String TECHNICAL_GROUP_NAME = "g";

	/** Name of the table in the GUI */
	private static final String TABLE_NAME = "table";

	/** Displayed columns in the dialog displaying similar documents */
	private static final String[] SIMILAR_DOC_DEFAULT_COLUMNS = new String[] {
		WebFolder.NAME,
		WebFolderAccessor.DOWNLOAD,
		WebFolderAccessor.TYPE,
		WebFolderAccessor.SIZE,
		WebFolderAccessor.DATE,
		WebFolderAccessor.VERSION,
		WebFolderAccessor.CLIPBOARD,
		WebFolderAccessor.DESCRIPTION };

	/**
	 * The list of documents to display.
	 */
	private final List<? extends Document> _contents;

	private static final Accessor<Object> SIMILAR_DOC_DIALOG_ACCESSOR;

	static {
		Map<String, PropertyAccessor<? super Object>> properties =
			new MapBuilder<String, PropertyAccessor<? super Object>>()
				.put(WebFolderAccessor.SIZE, WebFolderSizeProperty.INSTANCE)
				.put(WebFolderAccessor.TYPE, WebFolderMimetypeProperty.INSTANCE)
				.put(WebFolderAccessor.DATE, WebFolderDateProperty.INSTANCE)
				.put(WebFolderAccessor.DESCRIPTION, WebFolderDescriptionProperty.INSTANCE)
				.toMap();

		SIMILAR_DOC_DIALOG_ACCESSOR = new DispatchingAccessor<>(properties,
				WrapperAccessor.INSTANCE);
	}

	/**
	 * Creates a new {@link SimilarDocumentsDialog}.
	 * 
	 * @param contents
	 *        the {@link Document}s to display
	 */
	public SimilarDocumentsDialog(List<? extends Document> contents, DisplayDimension width,
			DisplayDimension height) {
		super(I18NConstants.DIALOG, width, height);
		_contents = contents;
	}

	@Override
	protected IconControl createTitleIcon() {
		return IconControl.icon(Icons.DOCUMENT_SIMILAR_60);
	}

	@Override
	protected HTMLFragment createBodyContent() {
		return FormPageTag.input(getControlProvider(),
			getFormContext().getContainer(TECHNICAL_GROUP_NAME).getMember(TABLE_NAME));
	}

	@Override
	protected void fillButtons(List<CommandModel> buttons) {
		addClose(buttons, ButtonType.CLOSE);
	}

	@Override
	protected ResPrefix getResourcePrefix() {
		return I18NConstants.DIALOG;
	}

	@Override
	protected void fillFormContext(FormContext context) {
		/* FormContext has resource prefix of the dialog which need to be unique to have individual
		 * title for the dialog. To ensure that the same resources are used as in WebFolder view, a
		 * technical form group is needed with the same resources as in the web folder view. */

		/* Group used as container for the fields within the table. The Resources are used to
		 * resolve resources for fields within the table */
		FormGroup wrappingGroup = new FormGroup(TECHNICAL_GROUP_NAME, WebFolderUtils.DEFAULT_WEBFOLDER_RESOURCES);
		context.addMember(wrappingGroup);

		TableConfiguration table = createTableConfig();
		final ObjectTableModel anInner =
			new ObjectTableModel(SIMILAR_DOC_DEFAULT_COLUMNS, table, _contents);

		EditableRowTableModel tableModel = new FormTableModel(anInner, wrappingGroup);
		final TableField tableField = FormFactory.newTableField(TABLE_NAME, tableModel);

		wrappingGroup.addMember(tableField);
	}

	private TableConfiguration createTableConfig() {
		TableConfiguration table = TableConfiguration.table();
		table.setResPrefix(WebFolderUtils.DEFAULT_WEBFOLDER_RESOURCES);
		table.getDefaultColumn().setAccessor(SimilarDocumentsDialog.SIMILAR_DOC_DIALOG_ACCESSOR);

		ColumnConfiguration nameColumn = table.declareColumn(WebFolder.NAME);
		nameColumn.setAccessor(IdentityAccessor.INSTANCE);
		nameColumn.setRenderer(ResourceRenderer.newResourceRenderer(NoLinkResourceProvider.INSTANCE));
		nameColumn.setDefaultColumnWidth("100%");

		WebFolderColumnDescriptionBuilder tableBuilder = WebFolderColumnDescriptionBuilder.FULL_ACCESS_INSTANCE;
		tableBuilder.createDownloadColumn(table.declareColumn(WebFolderAccessor.DOWNLOAD));
		tableBuilder.createTypeColumn(table.declareColumn(WebFolderAccessor.TYPE));
		tableBuilder.createSizeColumn(table.declareColumn(WebFolderAccessor.SIZE));
		tableBuilder.createDateColumn(table.declareColumn(WebFolderAccessor.DATE));
		tableBuilder.createVersionColumn(table.declareColumn(WebFolderAccessor.VERSION));
		tableBuilder.createClipboardColumn(table.declareColumn(WebFolderAccessor.CLIPBOARD));
		tableBuilder.createImmutableDescriptionColumn(table.declareColumn(WebFolderAccessor.DESCRIPTION));

		return table;
	}

}
