/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.folder.file.selection;

import java.io.File;
import java.util.Set;

import com.top_logic.basic.Log;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.common.folder.FolderDefinition;
import com.top_logic.common.folder.impl.FileFolderDefinition;
import com.top_logic.common.folder.model.FolderNode;
import com.top_logic.common.folder.ui.FolderComponent;
import com.top_logic.common.folder.util.FolderNodeToFileMapping;
import com.top_logic.common.webfolder.ui.WebFolderUIFactory;
import com.top_logic.common.webfolder.ui.commands.UploadDialog.UploadExecutor;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.layout.folder.CurrentSelectionStorage;
import com.top_logic.layout.folder.FolderControl;
import com.top_logic.layout.folder.FolderData;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableUtil;
import com.top_logic.layout.tree.breadcrumb.BreadcrumbData;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.tool.execution.ExecutableState;

/**
 * A {@link FolderComponent} that implements {@link Selectable} for selecting {@link File}s on the
 * server.
 * <p>
 * Model channel: The file representing the root directory of this {@link FolderComponent}.
 * </p>
 * <p>
 * Selection channel: The file or directory that the user has selected.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class FileSelectionComponent extends FolderComponent implements Selectable {

	/** {@link ConfigurationItem} for the {@link FileSelectionComponent}. */
	public interface Config extends FolderComponent.Config {
		// nothing needed, yet.
	}

	private static final String CURRENT_SELECTION = FileSelectionComponent.class.getName() + ".currentSelection";

	private FolderControl _folderControl;

	/** {@link TypedConfiguration} constructor for {@link FileSelectionComponent}. */
	public FileSelectionComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	protected void becomingInvisible() {
		super.becomingInvisible();
		/* Load the folder structure anew when this component is displayed again, to be up to date.
		 * Without that, it always displays the state from the login, not the current state. */
		resetFormContext();
		removeControl();
	}

	@Override
	protected boolean doValidateModel(DisplayContext context) {
		boolean valid = super.doValidateModel(context);

		// Ensure that the form context is build before rendering. This is necessary, because events
		// must be processed to restore the last selection.
		getFormContext();

		return valid;
	}

	@Override
	protected FormContext createFormContext() {
		FormContext result = super.createFormContext();
		/* Binding the selection storage has to happen _after_ the FormContext was created: It needs
		 * the FolderData, which is created during construction of the FormContext. */
		FolderData folderData = getFolderData();
		bindSelectionStorage(folderData);
		return result;
	}

	private void bindSelectionStorage(FolderData folderData) {
		BreadcrumbData breadcrumbData = folderData.getBreadcrumbData();
		CurrentSelectionStorage storage = new CurrentSelectionStorage(breadcrumbData, CURRENT_SELECTION);
		storage.restore();
		storage.attach();
	}

	@Override
	protected Control createControlForContext(FormContext context) {
		if (_folderControl == null) {
			_folderControl = initControl(context);
		}
		return _folderControl;
	}

	/** Removes the {@link #getRenderingControl()}. */
	public void removeControl() {
		_folderControl = null;
	}

	private FolderControl initControl(FormContext context) {
		FolderControl folderControl = createFolderControl(context);
		initializeControl(folderControl);
		return folderControl;
	}

	private FolderControl createFolderControl(FormContext context) {
		FolderData folderData = getFolderData();
		return WebFolderUIFactory.createControl(getBreadcrumbRenderer(), folderData, context, null);
	}

	private void initializeControl(FolderControl folderControl) {
		folderControl.setSelectOnlyFiles(false);
		folderControl.setOpenFolderOnSelect(true);
		TableData tableData = folderControl.getTableData();
		SelectionModel tableSelectionModel = tableData.getSelectionModel();
		SingleSelectionModel folderSelectionModel = folderControl.getBreadcrumbSelection();
		clearTableSelectionOnFolderSelection(folderSelectionModel, tableSelectionModel);
		bindFileSelectionToChannel(tableData, folderSelectionModel);
	}

	/**
	 * When the user chooses a different folder, clear the table selection.
	 * <p>
	 * The selection of the folder control represents which folder is displayed. When that changes,
	 * the table selection has to be cleared, as a different folder will be displayed and the old
	 * selection is no longer meaningful.
	 * </p>
	 */
	private void clearTableSelectionOnFolderSelection(SingleSelectionModel folderSelectionModel,
			SelectionModel tableSelectionModel) {
		folderSelectionModel
			.addSingleSelectionListener((model, oldSelection, newSelection) -> tableSelectionModel.clear());
	}

	private void bindFileSelectionToChannel(TableData tableData, SingleSelectionModel folderSelectionModel) {
		bindFolderSelectionToChannel(folderSelectionModel);
		bindTableSelectionToChannel(tableData);
	}

	private void bindFolderSelectionToChannel(SingleSelectionModel folderSelectionModel) {
		folderSelectionModel.addSingleSelectionListener(
			(model, oldSelection, newSelection) -> setSelectedFolderNode((FolderNode) newSelection));
	}

	private void bindTableSelectionToChannel(TableData tableData) {
		tableData.getSelectionModel().addSelectionListener(new SelectionListener() {

			@Override
			public void notifySelectionChanged(SelectionModel model, Set<?> oldSelection, Set<?> newSelection) {
				applySelectionFromTableToChannel(tableData);
			}
		});
	}

	private void applySelectionFromTableToChannel(TableData tableData) {
		FolderNode selectedRow = (FolderNode) getSelectedRow(tableData);
		setSelectedFolderNode(selectedRow);
	}

	private Object getSelectedRow(TableData table) {
		TableViewModel viewModel = table.getViewModel();
		int selectedIndex = TableUtil.getSingleSelectedRow(table);
		if (selectedIndex == TableModel.NO_ROW) {
			return null;
		}
		return viewModel.getRowObject(selectedIndex);
	}

	/**
	 * The {@link SelectionModel} representing which file in the currently displayed folder is
	 * selected.
	 */
	public SelectionModel getTableSelectionModel() {
		FolderData folderData = getFolderData();
		if (folderData == null) {
			return null;
		}
		TableData tableData = folderData.getTableData();
		if (tableData == null) {
			return null;
		}
		return tableData.getSelectionModel();
	}

	/**
	 * The {@link SingleSelectionModel} representing which folder is currently displayed.
	 */
	public SingleSelectionModel getFolderSelectionModel() {
		return getFolderData().getSingleSelectionModel();
	}

	/**
	 * Sets the {@link File} in the {@link FolderNode} as the new selection.
	 * 
	 * @param newSelection
	 *        See {@link FolderNodeToFileMapping} for the type of {@link FolderNode}s that are
	 *        supported. If the value is null, or is mapped to null, the selection is cleared.
	 */
	public boolean setSelectedFolderNode(FolderNode newSelection) {
		File selectedFile = getFile(newSelection);
		return setSelected(selectedFile);
	}

	private File getFile(FolderNode newSelection) {
		if (newSelection == null) {
			return null;
		}
		return FolderNodeToFileMapping.INSTANCE.apply(newSelection);
	}

	@Override
	protected boolean isSupported(Object model) {
		return supportsInternalModel(model);
	}

	@Override
	protected String getFolderSize() {
		int folderSize = getFolderDefinition().getContents().size();
		return Integer.toString(folderSize);
	}

	@Override
	protected ExecutableState getModifyability(FolderDefinition folder) {
		return ExecutableState.NOT_EXEC_HIDDEN;
	}

	@Override
	protected ExecutableState getClipability(FolderDefinition folder) {
		return ExecutableState.NOT_EXEC_HIDDEN;
	}

	@Override
	protected FolderDefinition getFolderDefinition() {
		return new FileFolderDefinition(fileModel());
	}

	/** Type-safe access to {@link #getModel()}. */
	public File fileModel() {
		return (File) getModel();
	}

	@Override
	protected UploadExecutor getUploadExecuter(FolderData folderData) {
		return null;
	}

	@Override
	protected TableConfiguration getTableConfiguration(
			ExecutableState canAddToClipboard, ExecutableState canUpdate, ExecutableState canDelete) {

		if (!(canAddToClipboard.isHidden() && canUpdate.isHidden() && canDelete.isHidden())) {
			throw new UnsupportedOperationException("The " + FileSelectionComponent.class.getSimpleName()
				+ " does not support updating, deleting or adding a file to the clipboard.");
		}
		return new FileSelectionTableConfigBuilder().build();
	}

	@Override
	public ResKey hideReason(Object potentialModel) {
		/* This method is overridden and duplicates the code from the BoundComponent to prevent a
		 * "State modification during rendering" error to be logged: The allow() implementation in
		 * the superclass calls "getModel()" before doing anything else. That causes the model to be
		 * initialized, which sends events. But this is not allowed, as allow() is called during
		 * rendering. To prevent that error, this method is overridden and does not use
		 * "getModel()", as the model is not relevant for the security of this component anyway. The
		 * implementation from the BoundComponent works and is therefore used. */
		return securityReason(potentialModel);
	}

	@Override
	protected ResKey getRootLabel() {
		return ResKey.text(fileModel().getName());
	}

	@Override
	public void linkChannels(Log log) {
		super.linkChannels(log);
		initializeSelection();
	}

	private void initializeSelection() {
		/* The initially selected folder is the initially displayed folder. This is normally the
		 * outermost folder, which this component can display. */
		setSelected(getModel());
	}

}
