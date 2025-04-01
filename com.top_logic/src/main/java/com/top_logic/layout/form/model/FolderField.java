/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import java.util.List;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.common.folder.FolderDefinition;
import com.top_logic.common.webfolder.WebFolderUtils;
import com.top_logic.common.webfolder.model.WebFolderTreeBuilder;
import com.top_logic.common.webfolder.ui.WebFolderColumnDescriptionBuilder;
import com.top_logic.common.webfolder.ui.WebFolderComponent;
import com.top_logic.common.webfolder.ui.WebFolderComponent.WebFolderUploadExecutor;
import com.top_logic.common.webfolder.ui.WebFolderUIFactory;
import com.top_logic.common.webfolder.ui.commands.UploadDialog;
import com.top_logic.knowledge.analyze.DefaultAnalyzeService;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.layout.ResourceView;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.folder.FolderData;
import com.top_logic.layout.folder.FolderDataOwner;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.FormMemberVisitor;
import com.top_logic.layout.form.ImmutablePropertyListener;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.tree.breadcrumb.BreadcrumbRenderer;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Field that represents a {@link WebFolder} on the client in a form.
 * 
 * TODO: Rename this class to WebFolderField. Create abstract superclass and a new class FolderField
 * for {@link FolderDefinition} (Talk to FMA)
 * 
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class FolderField extends FormGroup implements FolderDataOwner {

	private final BreadcrumbRenderer _breadcrumbRenderer;

	private FolderData data;

	private boolean _withDescription;

	/**
	 * @param name
	 *        the name of the {@link FormField}
	 * @param resourceView
	 *        used for I18N
	 * @param folder
	 *        the WebFolder to display
	 * @param rootNodeText
	 *        the text to be shown in the Breadcrumb if the folder is dispalyed in an own tab
	 * @param canAddToClipboard
	 *        can the content of the folder be added to the clipboard
	 * @param canUpload
	 *        can data be uploaded to the folder
	 * @param canZipDownload
	 *        is a zip download offered
	 * @param canCreateFolder
	 *        can a subfolder be created
	 * @param tableConfiguration
	 *        the configuration for displaying the contents of the folder, i.e. the documents and
	 *        subfolders
	 * @param configNameMapping
	 *        used for storing personal settings
	 * @param folderColumns
	 *        the columns to display, redundant to the tableConfiguration
	 * @param withDescription
	 *        will a description field be shown in upload dialog
	 */
	protected FolderField(String name, ResourceView resourceView, WebFolder folder, String rootNodeText,
			ExecutableState canAddToClipboard, ExecutableState canUpload, ExecutableState canZipDownload,
			ExecutableState canCreateFolder, TableConfiguration tableConfiguration,
			Mapping<FormMember, String> configNameMapping, List<String> allowedFileTypes, String[] folderColumns,
			boolean withDescription) {
		super(name, resourceView);
		
		_breadcrumbRenderer = getUIFactory().createBreadcrumbRenderer(rootNodeText);
		_withDescription = withDescription;
		this.data =
			getUIFactory().createFolderData(this, folder, WebFolderTreeBuilder.INSTANCE, tableConfiguration,
				this,
				folderColumns, ConfigKey.field(configNameMapping, this));

		SingleSelectionModel selectionModel = getFolderSelectionModel();

		getUIFactory().addZipDownloadFolderCommand(this, selectionModel, canZipDownload);
		getUIFactory().addClipboardCommand(this, selectionModel, canAddToClipboard);
		getUIFactory().addUploadCommand(this, selectionModel, canUpload, createUploadExecutor(allowedFileTypes));
		getUIFactory().addNewFolderCommand(this, selectionModel, canCreateFolder);

		addListener(FormMember.IMMUTABLE_PROPERTY, new ImmutablePropertyListener() {

			@Override
			public Bubble handleImmutableChanged(FormMember sender, Boolean oldValue, Boolean newValue) {
				if (sender == FolderField.this) {
					// No change in bubbling events
					WebFolderColumnDescriptionBuilder.applyImmutableProperty(tableConfiguration, newValue);
				}
				return Bubble.BUBBLE;
			}
		});
	}

	private SingleSelectionModel getFolderSelectionModel() {
		return data.getSingleSelectionModel();
	}

	/**
	 * @return the Factory for creating the UI
	 */
	protected WebFolderUIFactory getUIFactory() {
		return WebFolderUIFactory.getInstance();
	}


	private WebFolderUploadExecutor createUploadExecutor(List<String> allowedFileTypes) {
		WebFolderUploadExecutor executor =
			new WebFolderUploadExecutor(getFolderSelectionModel(), getUIFactory().getMaxUploadSize(), _withDescription);
		executor.setAllowedFileTypes(allowedFileTypes);
		return executor;
	}


	public static TableConfiguration createTableConfiguration(ExecutableState canAddToClipboard,
			ExecutableState canUpdate,
			ExecutableState canDelete,
			boolean withAnalysis, ResourceView resources) {
		WebFolderUIFactory factory = WebFolderUIFactory.getInstance();
		boolean manualLocking = factory.getManualLocking();
		WebFolderColumnDescriptionBuilder descriptionBuilder =
			new WebFolderColumnDescriptionBuilder(canAddToClipboard, canUpdate, canDelete, manualLocking);
		descriptionBuilder.setAnalysis(withAnalysis);
		TableConfiguration tableConfiguration = descriptionBuilder.createWebFolderColumns();
		tableConfiguration.setResPrefix(resources);
		return tableConfiguration;
	}

	public BreadcrumbRenderer getBreadcrumbRenderer() {
		return _breadcrumbRenderer;
	}

	@Override
	public FolderData getFolderData() {
		return data;
	}

	public boolean updateViewState(Object aDocument) {
		return this.data.updateUserObject(aDocument);
	}

	/**
	 * special visit method for {@link FolderField}
	 * 
	 * @see FormGroup#visit(FormMemberVisitor, Object)
	 */
	@Override
	public Object visit(FormMemberVisitor v, Object arg) {
		return v.visitFolderField(this, arg);
	}

	public static FolderField createFolderField(String name, WebFolder aFolder) {
		return createFolderField(name, aFolder, null);
	}

	public static FolderField createFolderField(String name, WebFolder aFolder, String rootNodeText) {
		return createFolderField(name, aFolder, rootNodeText, FormMember.QUALIFIED_NAME_MAPPING);
	}

	public static FolderField createFolderField(String name, WebFolder aFolder, String rootNodeText,
			Mapping<FormMember, String> configNameMapping) {

		ExecutableState defaultClipabilioty = WebFolderComponent.defaultClipability(aFolder);
		ExecutableState defaultModifyability = WebFolderComponent.defaultModifiability(aFolder);

		ExecutableState canUpload = defaultModifyability;
		ExecutableState canZipDownload = ExecutableState.EXECUTABLE;
		ExecutableState canAddToClipboard = defaultClipabilioty;
		ExecutableState canCreateFolder = defaultModifyability;
		List<String> allowedFileTypes = UploadDialog.ALL_TYPES;
		boolean withAnalysis = DefaultAnalyzeService.isAvailable();
		ExecutableState canUpdateDocument = defaultModifyability;
		ExecutableState canDeleteDocument = defaultModifyability;
		ResourceView resources = WebFolderUtils.DEFAULT_WEBFOLDER_RESOURCES;
		TableConfiguration tableConfig =
			createTableConfiguration(canAddToClipboard, canUpdateDocument, canDeleteDocument, withAnalysis, resources);
		String[] folderColumns = null;
		boolean withDescription = true;
		
		return createFolderField(name, resources, aFolder, rootNodeText, canAddToClipboard, canUpload,
			canZipDownload, canCreateFolder, tableConfig,
			configNameMapping, allowedFileTypes, folderColumns, withDescription);
	}

	public static FolderField createFolderField(String name, ResourceView resources, WebFolder aFolder,
			String rootNodeText,
			ExecutableState canAddToClipboard, ExecutableState canUpload, ExecutableState canZipDownload,
			ExecutableState canCreateFolder, TableConfiguration tableConfig,
			Mapping<FormMember, String> configNameMapping, List<String> allowedFileTypes, String[] folderColumns,
			boolean withDescription) {


		return new FolderField(name, resources, aFolder, rootNodeText,
			canAddToClipboard, canUpload, canZipDownload, canCreateFolder, tableConfig, configNameMapping,
			allowedFileTypes, folderColumns, withDescription);
	}

}
