/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui.commands;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.Logger;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.common.webfolder.WebFolderUtils;
import com.top_logic.common.webfolder.ui.WebFolderUIFactory;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.knowledge.gui.layout.upload.DefaultDataItem;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.control.IconControl;
import com.top_logic.layout.component.ComponentUtil;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.messagebox.AbstractFormPageDialog;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * New zip download dialog...
 * 
 * @author <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 */
public class ZipDownloadDialog extends AbstractFormPageDialog {

	/**
	 * Command for doing the zip download.
	 * 
	 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public static class ZipDownloadCommand implements Command {
	    
	    // Attributes
	
	    /** The web folder to create the new folder in. */
	    private final WebFolder folder;
		private final FormHandler handler;
		private final Command closeAction;
	    // Constructors

		/**
		 * Creates a {@link ZipDownloadCommand}.
		 * 
		 * @param aFolder
		 *        The web folder to zip and download, must not be <code>null</code>.
		 */
	    public ZipDownloadCommand(FormHandler handler, WebFolder aFolder, Command closeAction) {
	        this.handler = handler;
			this.folder = aFolder;
			this.closeAction = closeAction;
	    }

		@Override
		public HandlerResult executeCommand(DisplayContext displayContext) {
			try {
				if (!ComponentUtil.isValid(folder)) {
					closeAction.executeCommand(displayContext);
					return ComponentUtil.errorObjectDeleted(displayContext);
				}

				File zipFile =
					WebFolderUtils.zipWebfolders(WebFolderUIFactory.getInstance().getZipFolderNameProvider(),
						this.folder);
				DefaultDataItem dataItem =
					new DefaultDataItem(getDownloadName() + ".zip", BinaryDataFactory.createBinaryData(zipFile),
					MimeTypes.getInstance().getMimeType(".zip"));
				displayContext.getWindowScope().deliverContent(dataItem);

				closeAction.executeCommand(displayContext);

				return HandlerResult.DEFAULT_RESULT;
			} catch (IOException ex) {
				Logger.warn("Zip download failed.", ex, ZipDownloadDialog.class);
				throw new TopLogicException(ZipDownloadCommand.class, "ioException", ex);
			}
	    }

		private String getDownloadName() {
			return WebFolderUIFactory.getInstance().getZipDownloadFileNameProvider().getLabel(this.folder);
		}
	}

	private final FolderInfo info;

	/** 
     * Creates a {@link NewFolderDialog}.
     */
	public ZipDownloadDialog(WebFolder webFolder) {
		super(I18NConstants.ZIP_DOWNLOAD_FOLDER_DIALOG, DisplayDimension.dim(450, DisplayUnit.PIXEL),
			DisplayDimension.dim(240, DisplayUnit.PIXEL));
		this.info = new FolderInfo(webFolder);
	}

	@Override
	protected void fillFormContext(FormContext context) {
		// No form at all.
	}
	
	@Override
	protected HTMLFragment createBodyContent() {
		return new HTMLFragment() {
			@Override
			public void write(DisplayContext context, TagWriter out) throws IOException {
				Resources resources = context.getResources();

				out.beginTag(PARAGRAPH);
				out.writeText(resources.getString(I18NConstants.ZIP_DOWNLOAD_FOLDER_DIALOG_MESSAGE__DOCUMENTS_SIZE.fill(
					info.getDocumentCount(), info.getContentSize())));
				out.endTag(PARAGRAPH);

				if (info.isTooLarge()) {
					String disabledMessage =
						resources.getString(I18NConstants.ZIP_DOWNLOAD_FOLDER_DIALOG_DISABLED__LIMIT
							.fill(WebFolderUIFactory.getInstance().getZipDownloadSizeLimit()));
					out.beginTag(PARAGRAPH);
					out.writeText(disabledMessage);
					out.endTag(PARAGRAPH);
				}
			}
		};
    }

	@Override
	protected IconControl createTitleIcon() {
		return IconControl.icon(Icons.DOWNLOAD60);
	}
    
    @Override
    protected void fillButtons(List<CommandModel> buttons) {
		buttons.add(createDownloadButton());
		addCancel(buttons);
	}

	private CommandModel createDownloadButton() {
		Command command = new ZipDownloadDialog.ZipDownloadCommand(this, info.getWebFolder(), getDiscardClosure());
		getDialogModel().setDefaultCommand(command);
		CommandModel button = MessageBox.button(I18NConstants.ZIP_DOWNLOAD_FOLDER, command);
		if (info.isTooLarge()) {
			button.setNotExecutable(I18NConstants.ZIP_DOWNLOAD_FOLDER_DIALOG_DISABLED__LIMIT.fill(
				WebFolderUIFactory.getInstance().getZipDownloadSizeLimit()));
		}
		return button;
	}

}