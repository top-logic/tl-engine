/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui.commands;

import static com.top_logic.layout.basic.fragments.Fragments.*;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.Named;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.util.ResKey;
import com.top_logic.common.folder.FolderDefinition;
import com.top_logic.common.webfolder.WebFolderUtils;
import com.top_logic.knowledge.gui.layout.upload.SimpleFileNameStrategy;
import com.top_logic.knowledge.wrap.DocumentVersion;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.control.IconControl;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.listener.EnableButtonOnValue;
import com.top_logic.layout.form.model.DataField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.messagebox.AbstractFormPageDialog;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.SimpleFormDialog;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.model.form.ReactiveFormCSS;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * Upload dialog providing the {@link DataField} for identifying the file to be uploaded.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class UploadDialog extends AbstractFormPageDialog {

	/**
	 * Factory for {@link UploadDialog}s.
	 * 
	 * <p>
	 * For special types of folders special {@link UploadExecutor}s must create customized
	 * {@link UploadDialog}s with custom view, and custom {@link UploadCommand}s.
	 * </p>
	 * 
	 * @see UploadDialog#fillFormContext(FormContext)
	 * @see UploadDialog#createBodyContent()
	 * @see UploadDialog#createUploadCommand(UploadDialog)
	 * @see UploadCommand#executeUpload(DisplayContext, List)
	 */
	public static interface UploadExecutor {

		/**
		 * Create the actual dialog for performing the upload.
		 * 
		 * @param resPrefix
		 *        The resource prefix using for I18N.
		 * @param folderDefinition
		 *        Description of the folder to upload to.
		 * @return The dialog for performing the upload.
		 */
		public UploadDialog createUploadDialog(ResPrefix resPrefix, FolderDefinition folderDefinition);

	}

	/**
	 * Command for doing the real upload.
	 */
	public static abstract class UploadCommand implements Command {
	    
		/** The folder to upload the documents to. */
		private final UploadDialog _dialog;
		
		/**
		 * Creates a {@link UploadCommand}.
		 * 
		 * @param dialog
		 *        The surrounding {@link UploadDialog}.
		 */
		public UploadCommand(UploadDialog dialog) {
			_dialog = dialog;
	    }
	
		/**
		 * The {@link FormContext} of the upload dialog.
		 */
		protected FormContext getFormContext() {
			return getDialog().getFormContext();
		}

		/**
		 * The upload dialog.
		 */
		protected final UploadDialog getDialog() {
			return _dialog;
		}
	
		@Override
		public final HandlerResult executeCommand(DisplayContext aContext) {
			FormContext theContext = getFormContext();
			if (!theContext.checkAll()) {
				return AbstractApplyCommandHandler.createErrorResult(theContext);
			}
			FormField dataField = theContext.getField(SimpleFormDialog.INPUT_FIELD);

			if (dataField instanceof DataField) {
				List<BinaryData> data = ((DataField) dataField).getDataItems();
				if (data == null) {
					return UploadDialog.errorNoDocumentSelected();
				}
				return executeUpload(aContext, data);
			}

			return HandlerResult.DEFAULT_RESULT;
		}

		/**
		 * Perform the storage operation.
		 * 
		 * @param displayContext
		 *        The current context.
		 * @param data
		 *        The data to store.
		 * @return The success.
		 */
		protected abstract HandlerResult executeUpload(DisplayContext displayContext, List<BinaryData> data);
		
	}

	/**
	 * Empty list indicates all types
	 */
	public static final List<String> ALL_TYPES = null;

	private final CommandModel _uploadButton;

	private final FolderDefinition _folder;

	private ResPrefix _resourcePrefix;

	private long _maxUploadSize;

	private List<String> _allowedFileTypes;

	private boolean _withDescription;

	/**
	 * Creates a new {@link UploadDialog}.
	 * 
	 * @param maxUploadSize
	 *        Maximum size of a single file that can be uploaded. A value of <code>0</code> means no
	 *        limit.
	 */
	protected UploadDialog(ResPrefix aPrefix, FolderDefinition folder, DisplayDimension width, DisplayDimension height, long maxUploadSize) {
		this(aPrefix, folder, width, height, maxUploadSize, ALL_TYPES, true);
	}

	protected UploadDialog(ResPrefix aPrefix, FolderDefinition folder, DisplayDimension width, DisplayDimension height,
			long maxUploadSize, List<String> allowedFileTypes, boolean withDescription) {
		super(aPrefix, width, height);
		_withDescription = withDescription;
		_resourcePrefix = aPrefix;
		_folder = folder;
		_maxUploadSize = maxUploadSize;
		_allowedFileTypes = allowedFileTypes;
		Command uploadCommand = new Command.CommandChain(createUploadCommand(this), getDiscardClosure());
		getDialogModel().setDefaultCommand(uploadCommand);
		CommandModel uploadButton = MessageBox.forwardStyleButton(I18NConstants.UPLOAD_DOCUMENT, uploadCommand);
		_uploadButton = uploadButton;
	}

	/**
	 * Returns the {@link CommandModel} which actually executes the upload.
	 */
	protected CommandModel getUploadButton() {
		return _uploadButton;
	}

	/**
	 * Returns the folder to upload file to.
	 */
	protected FolderDefinition getFolder() {
		return _folder;
	}

	/**
	 * Create the {@link Command} that is invoked, when the user presses the upload button.
	 * 
	 * <p>
	 * Note: Since the command needs a reference to the surrounding dialog (for accessing the form
	 * data), the command cannot be created before the dialog and passed as argument to its
	 * constructor.
	 * </p>
	 * 
	 * @param dialog
	 *        The active {@link UploadDialog}.
	 * @return The command performing the upload.
	 */
	protected abstract UploadCommand createUploadCommand(UploadDialog dialog);

	@Override
	protected HTMLFragment createSubtitleContent() {
		return Fragments.empty();
	}

	protected HTMLFragment createBodyContentWith() {
		return div(FormConstants.FORM_BODY_CSS_CLASS,
			div(ReactiveFormCSS.RF_COLUMNS_LAYOUT + " cols1",
				div(ReactiveFormCSS.RF_INPUT_CELL,
					div(ReactiveFormCSS.RF_LABEL,
						label(SimpleFormDialog.INPUT_FIELD),
						errorIcon(SimpleFormDialog.INPUT_FIELD)),
					div(ReactiveFormCSS.RF_CELL,
						input(SimpleFormDialog.INPUT_FIELD))),
				div(ReactiveFormCSS.RF_INPUT_CELL + " " + ReactiveFormCSS.RF_LABEL_ABOVE,
					div(ReactiveFormCSS.RF_LABEL,
						label(DocumentVersion.DESCRIPTION),
						errorIcon(DocumentVersion.DESCRIPTION)),
					div(ReactiveFormCSS.RF_CELL,
						input(DocumentVersion.DESCRIPTION)))));
	}

	protected HTMLFragment createBodyContentWithout() {
		return span(input(SimpleFormDialog.INPUT_FIELD), text(HTMLConstants.NBSP),
			errorIcon(SimpleFormDialog.INPUT_FIELD), br(), br(),
			text(Resources.getInstance().getString(I18NConstants.UPLOAD_INFO)));
	}

	@Override
	protected HTMLFragment createBodyContent() {
		if (_withDescription) {
			return createBodyContentWith();
		} else {
			return createBodyContentWithout();
		}

	}

	@Override
	protected ResPrefix getResourcePrefix() {
		return _resourcePrefix;
	}

    @Override
    protected void fillFormContext(FormContext context) {
		List<String> whiteList = null;
		List<String> blackList = new ArrayList<>();
		for (Named content : getFolder().getContents()) {
			blackList.add(content.getName());
		}

		DataField dataField = createDataField(blackList, whiteList);

		context.addMember(dataField);
		EnableButtonOnValue.enableButtonOnNonEmptyValue(dataField, getUploadButton());

		StringField descriptionField = WebFolderUtils.createDescriptionField(DocumentVersion.DESCRIPTION, 5);
		context.addMember(descriptionField);

		descriptionField.setVisible(showDescriptionField());

    }

	private boolean showDescriptionField() {
		return true;
	}

	private DataField createDataField(List<String> blackList, List<String> whiteList) {
		SimpleFileNameStrategy fileNamesStrategy =
			createFileNameStrategy(blackList, whiteList);
		DataField dataField = FormFactory.newDataField(SimpleFormDialog.INPUT_FIELD, true, fileNamesStrategy);
		dataField.setLabel(StringServices.EMPTY_STRING);

		dataField.setLabel(Resources.getInstance().getString(I18NConstants.UPLOAD_DIALOG_FILES));
		dataField.setTooltip(Resources.getInstance().getString(I18NConstants.UPLOAD_DIALOG_FILES.tooltip()));

		dataField.setDownload(false);
		dataField.setMaxUploadSize(_maxUploadSize);
		return dataField;
	}


	private SimpleFileNameStrategy createFileNameStrategy(List<String> blackList,
			List<String> whiteList) {
		final List<String> fileTypesWhiteList =
			_allowedFileTypes == null ? ALL_TYPES : new ArrayList<>(_allowedFileTypes);
		return new SimpleFileNameStrategy(blackList, whiteList) {

			@Override
			public ResKey checkFileName(String aName) {
				ResKey theResult = super.checkFileName(aName);

				if (theResult == null) {
					if (ALL_TYPES == fileTypesWhiteList) {
						return null;
					}
					String theName = aName.toLowerCase();

					for (String theExtension : fileTypesWhiteList) {
						if (theName.endsWith(theExtension.toLowerCase())) {
							return null;
						}
					}

					return ResKey.message(I18NConstants.MESSAGE_KEY_FILENAME_MUST_END_WITH, aName,
						StringServices.join(fileTypesWhiteList, "; "));
				}

				return theResult;
			}
		};
	}

    @Override
    protected void fillButtons(List<CommandModel> buttons) {
		buttons.add(getUploadButton());
    	addCancel(buttons);
    }

	static HandlerResult errorNoDocumentSelected() {
		HandlerResult error = new HandlerResult();
		error.addErrorMessage(I18NConstants.UPLOAD_DOCUMENT_NO_DOCUMENT_SELECTED);
		return error;
	}

	public static HandlerResult errorDocumentExists(DisplayContext aContext, BinaryData theItem) {
		HandlerResult theResult = new HandlerResult();
		theResult.addError(I18NConstants.ERROR_DOCUMENT_EXISTS__NAME.fill(theItem.getName()));
		return theResult;
	}

	@Override
	protected IconControl createTitleIcon() {
		return IconControl.icon(Icons.UPLOAD60);
	}

}