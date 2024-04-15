/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.image.gallery;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.SimpleAccessor;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.form.control.DataItemControl;
import com.top_logic.layout.form.control.ErrorControl;
import com.top_logic.layout.form.model.DataField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.image.gallery.scripting.GalleryDialogHandler;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.structure.DefaultDialogModel;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DialogClosedListener;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.control.EditableTableControl;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnConfiguration.DisplayMode;
import com.top_logic.layout.table.model.ColumnCustomization;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.renderer.DefaultTableRenderer;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link Command}, that is executed, when the gallery edit button has been pressed. It generates a
 * dialog, that displays a table. This table can be used to rearrange the gallery images, as well as
 * add new ones or remove them.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
class ManageGalleryCommand implements Command {

	private static final String GALLERY_MANAGEMENT_CONTEXT = "galleryManagement";
	private static final int DIALOG_HEIGHT = 440;
	private static final int DIALOG_WIDTH = 476;
	private static final String ROW_HEIGHT_STYLE = "height: 70px";
	private static final String IMAGE_COLUMN_WIDTH = "440px";

	private static final String GALLERY_IMAGES_FIELD_NAME = "galleryImagesField";
	private static final String UPLOAD_FIELD_NAME = "uploadField";
	private static final String IMAGE_COLUMN = "image";

	private GalleryModel _galleryModel;

	/**
	 * Create a new {@link ManageGalleryCommand}.
	 */
	public ManageGalleryCommand(GalleryModel galleryModel) {
		_galleryModel = galleryModel;
	}

	@Override
	public HandlerResult executeCommand(DisplayContext context) {
		DialogModel dialogModel = createDialogModel();
		ObjectTableModel tableModel = createTableModel();
		FormContext dialogFormContext = createDialogFormContext(tableModel);
		List<CommandModel> dialogCommands = createDialogCommands(dialogModel, tableModel);
		EditableTableControl tableControl = createTableControl(dialogFormContext);
		return openDialog(context, dialogModel, dialogFormContext, dialogCommands, tableControl);
	}

	private DialogModel createDialogModel() {
		final DefaultDialogModel dialogModel =
			new DefaultDialogModel(new DefaultLayoutData(DisplayDimension.dim(DIALOG_WIDTH, DisplayUnit.PIXEL), 100,
				DisplayDimension.dim(DIALOG_HEIGHT, DisplayUnit.PIXEL), 100, Scrolling.AUTO),
				new ResourceText(I18NConstants.IMAGE_DIALOG_TITLE), true, true, null);
		return dialogModel;
	}

	private ObjectTableModel createTableModel() {
		TableConfiguration tableConfiguration = createTableConfiguration();
		final ObjectTableModel tableModel =
			new ObjectTableModel(new String[] {}, tableConfiguration, _galleryModel.getImages(), true);
		return tableModel;
	}

	@SuppressWarnings("deprecation")
	private EditableTableControl createTableControl(FormContext dialogFormContext) {
		TableField tableField = (TableField) dialogFormContext.getFirstMemberRecursively(GALLERY_IMAGES_FIELD_NAME);
		DataField uploadField = (DataField) dialogFormContext.getFirstMemberRecursively(UPLOAD_FIELD_NAME);
		EditableTableControl tableControl =
			new EditableTableControl(tableField, tableField.getTableModel().getTableConfiguration().getTableRenderer(),
				true);
		tableControl.addTitleBarControl(0, new ErrorControl(uploadField, true));
		tableControl.addTitleBarControl(0, new DataItemControl(uploadField));
		return tableControl;
	}

	private FormContext createDialogFormContext(ObjectTableModel tableModel) {
		FormContext dialogContext =
			new FormContext(GALLERY_MANAGEMENT_CONTEXT, ResPrefix.forClass(ManageGalleryCommand.class));

		TableField tableField = createTableField(tableModel);
		DataField uploadField = createImageUploadField(tableField);

		dialogContext.addMember(tableField);
		dialogContext.addMember(uploadField);

		return dialogContext;
	}

	private TableField createTableField(ObjectTableModel tableModel) {
		TableField tableField = FormFactory.newTableField(GALLERY_IMAGES_FIELD_NAME);
		tableField.setTableModel(tableModel);
		return tableField;
	}

	private TableConfiguration createTableConfiguration() {
		TableConfiguration tableConfiguration = TableConfigurationFactory.table();
		createFrozenTable(tableConfiguration);
		excludeSelectColumn(tableConfiguration);
		createImageColumn(tableConfiguration);
		return tableConfiguration;
	}

	private void createFrozenTable(TableConfiguration tableConfiguration) {
		tableConfiguration.setTableRenderer(DefaultTableRenderer.newInstance());
		tableConfiguration.setRowStyle(ROW_HEIGHT_STYLE);
	}

	private void excludeSelectColumn(TableConfiguration tableConfiguration) {
		tableConfiguration.setDefaultFilterProvider(null);
		tableConfiguration.setColumnCustomization(ColumnCustomization.NONE);
		ColumnConfiguration selectColumn = tableConfiguration.declareColumn(TableControl.SELECT_COLUMN_NAME);
		selectColumn.setVisibility(DisplayMode.excluded);
	}

	private void createImageColumn(TableConfiguration tableConfiguration) {
		ColumnConfiguration imageColumn = tableConfiguration.declareColumn(IMAGE_COLUMN);
		imageColumn.setDefaultColumnWidth(IMAGE_COLUMN_WIDTH);
		imageColumn.setAccessor(SimpleAccessor.INSTANCE);
		imageColumn.setCellRenderer(ImageCellRenderer.INSTANCE);
		imageColumn.setFilterProvider(null);
		imageColumn.setSortable(false);
		imageColumn.setColumnLabelKey(I18NConstants.IMAGE_COLUMN_NAME);
	}

	private List<CommandModel> createDialogCommands(final DialogModel dialogModel,
			final ObjectTableModel tableModel) {
		List<CommandModel> dialogCommands = new ArrayList<>();
		CommandModel okButton = MessageBox.button(ButtonType.OK, (Command) context -> {
			_galleryModel.setImages(tableModel.getDisplayedRows());
			return dialogModel.getCloseAction().executeCommand(context);
		});
		dialogCommands.add(okButton);
		dialogModel.setDefaultCommand(okButton);
		dialogCommands.add(MessageBox.button(ButtonType.CANCEL,
			(Command) context -> dialogModel.getCloseAction().executeCommand(context)));
		return dialogCommands;
	}

	private DataField createImageUploadField(TableData tableData) {
		DataField uploadField = FormFactory.newDataField(UPLOAD_FIELD_NAME, FormFactory.MULTIPLE);
		uploadField.addValueListener(new GalleryUploadListener(tableData));
		uploadField.setFileNameConstraint(
			new CombiningFileNameConstraint(ImageTypeConstraint.INSTANCE,
				new NoDuplicateImageConstraint(tableData.getTableModel())));
		uploadField.addConstraint(ImageSizeConstraint.INSTANCE);
		return uploadField;
	}

	private HandlerResult openDialog(DisplayContext displayContext, DialogModel dialogModel, FormContext dialogFormContext,
			List<CommandModel> dialogCommands, EditableTableControl tableControl) {
		if (ScriptingRecorder.isEnabled()) {
			registerDialogHandler(dialogModel, dialogFormContext);
		}
		return MessageBox.open(displayContext, dialogModel, tableControl, dialogCommands);
	}

	private void registerDialogHandler(final DialogModel dialogModel, FormContext dialogFormContext) {
		final GalleryDialogHandler galleryManagementDialogHandler =
			new GalleryDialogHandler(_galleryModel, dialogFormContext);
		dialogFormContext.setOwningModel(galleryManagementDialogHandler);
		galleryManagementDialogHandler.registerToGalleryModel();
		dialogModel.addListener(DialogModel.CLOSED_PROPERTY, new DialogClosedListener() {

			@Override
			public void handleDialogClosed(Object sender, Boolean oldValue, Boolean newValue) {
				galleryManagementDialogHandler.deregisterFromGalleryModel();
				dialogModel.removeListener(DialogModel.CLOSED_PROPERTY, this);
			}
		});
	}
}