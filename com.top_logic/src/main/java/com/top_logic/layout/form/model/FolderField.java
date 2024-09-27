/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.util.ResKey;
import com.top_logic.common.folder.FolderDefinition;
import com.top_logic.common.webfolder.WebFolderUtils;
import com.top_logic.common.webfolder.model.WebFolderTreeBuilder;
import com.top_logic.common.webfolder.ui.WebFolderColumnDescriptionBuilder;
import com.top_logic.common.webfolder.ui.WebFolderComponent;
import com.top_logic.common.webfolder.ui.WebFolderComponent.WebFolderUploadExecutor;
import com.top_logic.common.webfolder.ui.WebFolderUIFactory;
import com.top_logic.common.webfolder.ui.commands.UploadDialog.UploadExecutor;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.layout.ResourceView;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.folder.FolderData;
import com.top_logic.layout.folder.FolderDataOwner;
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

	private FolderField(String name, ResourceView aLabelRessource, Object rootUserObject, ResKey rootNodeText,
			ExecutableState canAddToClipboard, ExecutableState canUpload, ExecutableState canZipDownload,
			ExecutableState canCreateFolder, ExecutableState canUpdate, ExecutableState canDelete,
			Mapping<FormMember, String> configNameMapping) {
		super(name, aLabelRessource);
		
		WebFolderUIFactory factory = WebFolderUIFactory.getInstance();
		boolean manualLocking = factory.getManualLocking();
		final TableConfiguration aManager =
			new WebFolderColumnDescriptionBuilder(canAddToClipboard, canUpdate, canDelete, manualLocking).createWebFolderColumns();
		aManager.setResPrefix(aLabelRessource);
		_breadcrumbRenderer = factory.createBreadcrumbRenderer(rootNodeText);
		this.data =
			WebFolderUIFactory.createFolderData(this, rootUserObject, WebFolderTreeBuilder.INSTANCE, aManager, this,
				null, ConfigKey.field(configNameMapping, this));
		SingleSelectionModel selectionModel = data.getSingleSelectionModel();
		
		WebFolderUIFactory.addZipDownloadFolderCommand(this, selectionModel, canZipDownload);
		WebFolderUIFactory.addClipboardCommand(this, selectionModel, canAddToClipboard);
		UploadExecutor executer = new WebFolderUploadExecutor(selectionModel, factory.getMaxUploadSize());

		WebFolderUIFactory.addUploadCommand(this, selectionModel, canUpload, executer);
		WebFolderUIFactory.addNewFolderCommand(this, selectionModel, canCreateFolder);

		addListener(FormMember.IMMUTABLE_PROPERTY, new ImmutablePropertyListener() {

			@Override
			public Bubble handleImmutableChanged(FormMember sender, Boolean oldValue, Boolean newValue) {
				if (sender == FolderField.this) {
					// No change in bubbling events
					WebFolderColumnDescriptionBuilder.applyImmutableProperty(aManager, newValue);
				}
				return Bubble.BUBBLE;
			}
		});
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

	public static FolderField createFolderField(String name, WebFolder aFolder, ResKey rootNodeText) {
		return createFolderField(name, aFolder, rootNodeText, FormMember.QUALIFIED_NAME_MAPPING);
	}

	public static FolderField createFolderField(String name, WebFolder aFolder, ResKey rootNodeText,
			Mapping<FormMember, String> configNameMapping) {
		ExecutableState canZipDownload = ExecutableState.EXECUTABLE;
		ExecutableState canAddToClipboard = WebFolderComponent.defaultClipability(aFolder);
		ExecutableState defaultModifiability = WebFolderComponent.defaultModifiability(aFolder);
		return createFolderField(name, aFolder, rootNodeText, canAddToClipboard, defaultModifiability,
			canZipDownload, defaultModifiability, defaultModifiability, defaultModifiability, configNameMapping);
	}

	public static FolderField createFolderField(String name, WebFolder aFolder, ResKey rootNodeText,
			ExecutableState canAddToClipboard, ExecutableState canUpload, ExecutableState canZipDownload,
			ExecutableState canCreateFolder, ExecutableState canUpdate, ExecutableState canDelete,
			Mapping<FormMember, String> configNameMapping) {
		return new FolderField(name, WebFolderUtils.DEFAULT_WEBFOLDER_RESOURCES, aFolder, rootNodeText,
			canAddToClipboard, canUpload, canZipDownload, canCreateFolder, canUpdate, canDelete, configNameMapping);
	}

}
