/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.folder;

import static java.util.Collections.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.fileupload2.core.FileItem;

import com.top_logic.base.multipart.MultipartRequest;
import com.top_logic.basic.Logger;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.common.folder.FolderDefinition;
import com.top_logic.common.folder.model.FolderNode;
import com.top_logic.knowledge.gui.layout.upload.DefaultDataItem;
import com.top_logic.knowledge.service.binary.FileItemBinaryData;
import com.top_logic.layout.ContentHandler;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.URLParser;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.dnd.DnDFileUtilities;
import com.top_logic.layout.form.control.I18NConstants;
import com.top_logic.layout.scripting.action.SelectAction.SelectionChangeKind;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.table.ITableRenderer;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.layout.tree.breadcrumb.BreadcrumbControl;
import com.top_logic.layout.tree.breadcrumb.BreadcrumbData;
import com.top_logic.layout.tree.breadcrumb.BreadcrumbRenderer;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class FolderControl extends TableControl implements FolderListener, ContentHandler {

	/**
	 * Default value for {@link #getSelectOnlyFolders()}.
	 */
	protected static final boolean DEFAULT_SELECT_ONLY_FOLDERS = true;

	/**
	 * Default value for {@link #getOpenFolderOnSelect()}.
	 */
	protected static final boolean DEFAULT_OPEN_FOLDER_ON_SELECT = true;

    private BreadcrumbControl breadcrumbControl;

	private FolderData folderData;

	private final FileDropHandler _fileDropHandler;

	private final List<BinaryData> _uploadedItems = new ArrayList<>();

	private boolean _selectOnlyFolders = DEFAULT_SELECT_ONLY_FOLDERS;

	private boolean _openFolderOnSelect = DEFAULT_OPEN_FOLDER_ON_SELECT;

	private BreadcrumbRenderer _breadcrumbRenderer;

	/**
	 * Commands for file drops.
	 */
	protected static final Map<String, ControlCommand> FILE_DROP_COMMANDS = createCommandMap(TableControl.TABLE_COMMANDS,
		new ControlCommand[] {
			DropFile.INSTANCE
		});

	/**
	 * Creates a new {@link FolderControl}
	 * 
	 * @param data
	 *        The {@link FolderData}.
	 * @param tableRenderer
	 *        The Renderer.
	 */
	public FolderControl(FolderData data, BreadcrumbRenderer breadcrumbRenderer, ITableRenderer tableRenderer) {
		this(data, breadcrumbRenderer, tableRenderer, NoFileDrop.INSTANCE);
	}

	/**
	 * Creates a new {@link FolderControl}
	 * 
	 * @param data
	 *        The {@link FolderData}.
	 * @param tableRenderer
	 *        The Renderer.
	 * @param fileDropHandler
	 *        A {@link FileDropHandler} for uploads of dropped files/folders. If the
	 *        {@link FileDropHandler} is <code>null</code>, an instance of {@link NoFileDrop} will
	 *        be created. In this case dropping files/folders into the {@link FolderControl} is not
	 *        supported. If the user is dragging a file/folder over a {@link FolderControl} with
	 *        {@link NoFileDrop} he will receive the information that the upload is not possible
	 *        before actually dropping it.
	 */
	public FolderControl(FolderData data, BreadcrumbRenderer breadcrumbRenderer, ITableRenderer tableRenderer,
			FileDropHandler fileDropHandler) {
		super(data.getTableData(), FILE_DROP_COMMANDS, tableRenderer);
		folderData = data;
		_breadcrumbRenderer = breadcrumbRenderer;
		_fileDropHandler = (fileDropHandler == null) ? NoFileDrop.INSTANCE : fileDropHandler;
		setSelectable(true);
	}

	/**
	 * Whether it is possible to select only folders, or files too.
	 */
	public boolean getSelectOnlyFolders() {
		return _selectOnlyFolders;
	}

	/**
	 * @see #getSelectOnlyFolders()
	 */
	public void setSelectOnlyFiles(boolean selectOnlyFolders) {
		_selectOnlyFolders = selectOnlyFolders;
	}

	/**
	 * Whether folders should be opened when the user selects them.
	 */
	public boolean getOpenFolderOnSelect() {
		return _openFolderOnSelect;
	}

	/**
	 * @see #getOpenFolderOnSelect()
	 */
	public void setOpenFolderOnSelect(boolean openFolderOnSelect) {
		_openFolderOnSelect = openFolderOnSelect;
	}

    @Override
    protected void attachRevalidated() {
    	super.attachRevalidated();
    	folderData.addFolderListener(this);
    }
    
    @Override
    protected void detachInvalidated() {
    	folderData.removeFolderListener(this);
    	super.detachInvalidated();
    }
    
    public BreadcrumbControl getBreadcrumbControl() {
        if (this.breadcrumbControl == null) {
			this.breadcrumbControl = new BreadcrumbControl(_breadcrumbRenderer, folderData.getBreadcrumbData());
            
        }

        return this.breadcrumbControl;
    }

	/**
	 * {@link SingleSelectionModel} of the breadcrumb.
	 * 
	 * @see BreadcrumbData#getSelectionModel()
	 */
	public SingleSelectionModel getBreadcrumbSelection() {
        return folderData.getBreadcrumbData().getSelectionModel();
    }

	@Override
	protected boolean acceptUISelection(int aNewSelectedRow, SelectionType selectionType) {
		int modelRow = getViewModel().getApplicationModelRow(aNewSelectedRow);
		FolderNode selectedNode = (FolderNode) getApplicationModel().getRowObject(modelRow);

		if (getSelectOnlyFolders() && !selectedNode.isFolder()) {
			return false;
		}
		if (getOpenFolderOnSelect() && (selectedNode.getBusinessObject() instanceof FolderDefinition)) {
			if (ScriptingRecorder.isRecordingActive()) {
				ScriptingRecorder.recordSelection(folderData, selectedNode, true, SelectionChangeKind.ABSOLUTE);
			}
			getBreadcrumbSelection().setSingleSelection(selectedNode);

			return true;
		}
		return super.acceptUISelection(aNewSelectedRow, selectionType);
	}

	@Override
	public boolean isRowSelectable(int row) {
		if (!getSelectOnlyFolders()) {
			return true;
		}
		int modelRow = getViewModel().getApplicationModelRow(row);
		FolderNode selectedNode = (FolderNode) getApplicationModel().getRowObject(modelRow);

		return selectedNode.isFolder();
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		super.internalWrite(context, out);
		FileDropHandler fileDropHandler = this.getFileDropHandler();
		DnDFileUtilities.initFileDragAndDrop(context, out, this, this, fileDropHandler.getuploadPossible(),
			fileDropHandler.getMaxUploadSize());
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();
		final FrameScope urlContext = getScope().getFrameScope();
		urlContext.registerContentHandler(getID(), this);
	}

	@Override
	protected void internalDetach() {
		getScope().getFrameScope().deregisterContentHandler(this);
		// Control is detached
		super.internalDetach();
	}

	@Override
	public void handleContent(DisplayContext context, String id, URLParser url) {
		DnDFileUtilities.handleContent(context, this::performUpload);
	}

	private void performUpload(MultipartRequest request) {
		clearUploadedItems();
		var receivedFiles = request.getFiles();
		if (receivedFiles != null) {
			for (int i = 0; i < receivedFiles.size(); i++) {
				FileItem<?> fileItem = receivedFiles.get(i);
				String fileName = DnDFileUtilities.getFileName(fileItem);
				int splitNameIndex = fileItem.getName().lastIndexOf(':');
				String filePath = fileItem.getName().substring(0, splitNameIndex + 1);

				if (Logger.isDebugEnabled(FolderControl.class)) {
					Logger.debug("Upload file named '" + fileName + "'", FolderControl.class);
				}

				BinaryData data = new FileItemBinaryData(fileItem);
				String contentType = DnDFileUtilities.getContentType(fileItem, fileName);
				addUploadedItem(new DefaultDataItem(filePath + fileName, data, contentType));
			}
		}
	}

	FileDropHandler getFileDropHandler() {
		return _fileDropHandler;
	}

	List<BinaryData> getUploadedItems() {
		return unmodifiableList(_uploadedItems);
	}

	private void addUploadedItem(BinaryData item) {
		_uploadedItems.add(item);
	}

	void clearUploadedItems() {
		_uploadedItems.clear();
	}

	/**
	 * Command to trigger upload of dropped files.
	 *
	 * @author <a href="mailto:Diana.Pankratz@top-logic.com">Diana Pankratz</a>
	 */
	protected static class DropFile extends ControlCommand {

		/**
		 * ID of the command.
		 */
		protected static final String COMMAND_ID = "dropFile";

		/**
		 * Single instance of the {@link DropFile}.
		 */
		public static final ControlCommand INSTANCE = new DropFile();


		/**
		 * Creates a new {@link DropFile}.
		 */
		public DropFile() {
			super(COMMAND_ID);
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			FolderControl folderControl = (FolderControl) control;
			FileDropHandler fileDropHandler = folderControl.getFileDropHandler();
			try {
				if (arguments.get(DnDFileUtilities.CLIENT_UPLOAD_FAILED_MESSAGE) != null) {
					DnDFileUtilities.showClientInfoMessages(arguments);
				} else {
					FolderNode selectedFolder =
						(FolderNode) folderControl.getBreadcrumbSelection().getSingleSelection();
					List<BinaryData> files = folderControl.getUploadedItems();
					fileDropHandler.uploadFiles(commandContext, selectedFolder, files);
				}
			} finally {
				folderControl.clearUploadedItems();
				DnDFileUtilities.hideProgressDialog(folderControl.getScope().getFrameScope(), folderControl.getID());
			}

			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.DROP_FILE_UPLOAD;
		}
	}

}
