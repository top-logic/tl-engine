/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui.commands;

import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.common.webfolder.ui.WebFolderColumnDescriptionBuilder;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.DocumentVersion;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.Renderer;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.control.IconControl;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.messagebox.AbstractFormPageDialog;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.SimpleFormDialog;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.FieldProvider;
import com.top_logic.layout.table.model.FormTableModel;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;

/**
 * Clipboard dialog providing the {@link TableField} for adding objects to the folder.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class VersionDialog extends AbstractFormPageDialog {

	/**
	 * Global configuration of {@link VersionDialog}.
	 */
	public interface Config extends ConfigurationItem {

		/**
		 * The columns to display in the {@link VersionDialog}.
		 */
		@Format(CommaSeparatedStrings.class)
		List<String> getColumns();
		
	}
	
	private final Document document;

	/**
	 * Name of the column which displays the date of the document of the displayed
	 * {@link DocumentVersion}.
	 */
	public static final String VERSION_DATE = "_versionDate";

	/**
	 * Name of the column which displays the size of the document of the displayed
	 * {@link DocumentVersion}.
	 */
	public static final String VERSION_SIZE = "_versionSize";

	/**
	 * Name of the column which offers the user a download button for the document of the displayed
	 * {@link DocumentVersion}.
	 */
	public static final String VERSION_DOWNLOAD = "_versionDownload";

	/**
	 * Name of the column which displays the author of the document of the displayed
	 * {@link DocumentVersion}.
	 */
	public static final String VERSION_AUTHOR = "_versionAuthor";

	/**
	 * Name of the column which displays the description of the document of the displayed
	 * {@link DocumentVersion}.
	 */
	public static final String VERSION_DESCRIPTION = "_versionDescription";

	/**
	 * Creates a {@link VersionDialog}.
	 * 
	 * @param aPrefix
	 *        resource prefix for the new dialog
	 * @param document
	 *        the underlying document
	 */
	public VersionDialog(ResPrefix aPrefix, Document document) {
		super(I18NConstants.VERSION_DIALOG, DisplayDimension.dim(900, DisplayUnit.PIXEL),
			DisplayDimension.dim(500, DisplayUnit.PIXEL));
		this.document = document;
    }

	@Override
	protected IconControl createTitleIcon() {
		return IconControl.icon(Icons.DOCUMENT_VERSIONS_60);
	}

	@Override
	protected HTMLFragment createBodyContent() {
		return input(SimpleFormDialog.INPUT_FIELD);
	}

	@Override
	protected ResPrefix getResourcePrefix() {
		return I18NConstants.VERSION_DIALOG;
	}

    @Override
    protected void fillFormContext(FormContext context) {
		List<? extends DocumentVersion> theContent = this.getContent();
		List<String> theColumns = getVersionColumns();
        TableConfiguration theManager  = WebFolderColumnDescriptionBuilder.FULL_ACCESS_INSTANCE.createVersionTableColumns();
		theManager.setResPrefix(I18NConstants.VERSION_DIALOG.append(SimpleFormDialog.INPUT_FIELD));
		ObjectTableModel innerTableModel = new ObjectTableModel(theColumns, theManager, theContent);
		TableModel theModel = new FormTableModel(innerTableModel, context);
		TableField theField = FormFactory.newTableField(SimpleFormDialog.INPUT_FIELD, theModel, false);

        context.addMember(theField);
    }

    /**
     * The columns to display.
     */
	protected List<String> getVersionColumns() {
		return ApplicationConfig.getInstance().getConfig(Config.class).getColumns();
	}

    @Override
    protected void fillButtons(List<CommandModel> buttons) {
    	buttons.add(MessageBox.button(ButtonType.CLOSE, getDiscardClosure()));
    }

	/**
	 * Creates a new {@link ColumnConfiguration} and adds it to the given {@link TableConfiguration}
	 * 
	 * @param aManager
	 *        the managed to be informed about the new column description
	 * @param aName
	 *        name of the new column
	 * @param aFieldProvider
	 *        {@link FieldProvider} of the new column.
	 * @param aControlProvider
	 *        {@link ControlProvider} of the new column.
	 * @param labelProvider
	 *        used to create the new {@link Renderer} of the column. see
	 *        {@link ColumnConfiguration#setLabelProvider(LabelProvider)} and
	 *        {@link ColumnConfiguration#setResourceProvider(ResourceProvider)} of the new column.
	 * 
	 * @return the new {@link ColumnConfiguration}
	 */
    protected ColumnConfiguration addDescription(TableConfiguration aManager, String aName,
			FieldProvider aFieldProvider, ControlProvider aControlProvider, LabelProvider labelProvider) {
		ColumnConfiguration column = aManager.declareColumn(aName);
		column.setFieldProvider(aFieldProvider);
		column.setControlProvider(aControlProvider);
		if (labelProvider instanceof ResourceProvider) {
			column.setResourceProvider((ResourceProvider) labelProvider);
        }
        else { 
			column.setLabelProvider(labelProvider);
        }
        return column;
    }

	/**
	 * Creates the list of {@link DocumentVersion} displayed in this dialog
	 * 
	 * @return the list of displayed {@link DocumentVersion}
	 */
	protected List<? extends DocumentVersion> getContent() {
		return document.getDocumentVersions();
	}
}