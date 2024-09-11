/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui.commands;

import static com.top_logic.basic.util.Utils.*;
import static com.top_logic.layout.basic.fragments.Fragments.*;

import java.util.Collections;
import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.Logger;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.common.webfolder.WebFolderUtils;
import com.top_logic.common.webfolder.ui.WebFolderUIFactory;
import com.top_logic.knowledge.gui.layout.upload.SimpleFileNameStrategy;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.DocumentVersion;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.control.IconControl;
import com.top_logic.layout.component.ComponentUtil;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.listener.EnableButtonOnValue;
import com.top_logic.layout.form.model.DataField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.messagebox.AbstractFormPageDialog;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.SimpleFormDialog;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.error.TopLogicException;

/**
 * Update dialog providing the {@link DataField} for identifying the file to be uploaded (and updated).
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class UpdateDialog extends AbstractFormPageDialog {

	private static final Class<UpdateDialog> THIS_CLASS = UpdateDialog.class;

    /**
	 * Command for doing the real update.
	 * 
	 * When updating a {@link Document} will become new version.
	 * Moreover the stuff from the {@link DataField} will be used as content for that new version.
	 * 
	 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public static class UpdateCommand implements Command {
	
	    // Attributes
	    
	    /** The document to be updated. */
	    private final Document document;
		private final FormHandler handler;
		private final Command closeAction;
	
	    // Constructors
	
		public UpdateCommand(FormHandler handler, Document aDocument, Command closeAction) {
	        this.handler = handler;
			this.document = aDocument;
			this.closeAction = closeAction;
	    }
	
	    // Implementation of interface Executable
	
	    @Override
		public HandlerResult executeCommand(DisplayContext aContext) {
			if (!ComponentUtil.isValid(document)) {
				closeAction.executeCommand(aContext);
				return ComponentUtil.errorObjectDeleted(aContext);
			}

	        HandlerResult theResult  = new HandlerResult();
	        FormContext   formContext = handler.getFormContext();
			if (!formContext.checkAll()) {
				return AbstractApplyCommandHandler.createErrorResult(formContext);
			}
	        FormField     dataInputField   = formContext.getField(SimpleFormDialog.INPUT_FIELD);
	
	        if (dataInputField instanceof DataField) {
	            BinaryData    theItem = ((DataField) dataInputField).getDataItem();
				if (theItem == null) {
					return UploadDialog.errorNoDocumentSelected();
				}
	            Transaction theTX   = PersistencyLayer.getKnowledgeBase().beginTransaction();
	
	            try {
					document.update(theItem);
					WebFolderUtils.updateDescription(document, formContext);

	                theTX.commit();
	
	                return closeAction.executeCommand(aContext);
				} catch (KnowledgeBaseException ex) {
					throw errorUpdateFailed(theItem, ex);
				}
	            finally {
	                theTX.rollback();
	            }
	        }
	
	        return theResult;
	    }
	}

	private final Document document;

	private final CommandModel _updateButton;

	private final boolean _manualLocking;

	public UpdateDialog(Document aModel, boolean manualLocking) {
		super(I18NConstants.UPDATE_DIALOG, DisplayDimension.dim(500, DisplayUnit.PIXEL),
			DisplayDimension.dim(400, DisplayUnit.PIXEL));
        
		this.document = aModel;
		UpdateDialog.UpdateCommand updateCommand = new UpdateDialog.UpdateCommand(this, document, getDiscardClosure());
		getDialogModel().setDefaultCommand(updateCommand);
		_updateButton = MessageBox.forwardStyleButton(I18NConstants.UPDATE_DOCUMENT, updateCommand);
		_manualLocking = manualLocking;
		getDialogModel().addListener(DialogModel.CLOSED_PROPERTY, this::onDialogClose);
    }

	/**
	 * @param sender
	 *        Ignored, but declared by the functional interface.
	 * @param oldValue
	 *        Ignored, but declared by the functional interface.
	 * @param newValue
	 *        Whether the dialog was closed.
	 */
	private void onDialogClose(Object sender, Boolean oldValue, Boolean newValue) {
		if (!newValue) {
			return;
		}
		if (_manualLocking) {
			return;
		}
		unlockDocument();
	}

	private void unlockDocument() {
		boolean success = document.getDAP().unlock();
		if (!success) {
			logError("Failed to release lock for document: " + debug(document));
		}
	}

	private void logError(String message) {
		Logger.error(message, THIS_CLASS);
	}

	Document getDocument() {
		return document;
	}

    @Override
	protected IconControl createTitleIcon() {
		return IconControl.icon(Icons.DOCUMENT_UPDATE_60);
	}

	@Override
	protected HTMLFragment createBodyContent() {
		return div(FormConstants.FORM_BODY_CSS_CLASS,
			div(
				input(SimpleFormDialog.INPUT_FIELD),
				text(HTMLConstants.NBSP),
				errorIcon(SimpleFormDialog.INPUT_FIELD)),
			div(
				div(
					label(DocumentVersion.DESCRIPTION),
					div(
						input(DocumentVersion.DESCRIPTION)))));
	}

	@Override
	protected ResPrefix getResourcePrefix() {
		return I18NConstants.UPDATE_DIALOG;
	}

	@Override
    protected void fillFormContext(FormContext context) {
		List<String> theBlack = null;
		List<String> theWhite = Collections.singletonList(this.document.getName());

		DataField updateData =
			FormFactory.newDataField(SimpleFormDialog.INPUT_FIELD, new SimpleFileNameStrategy(theBlack, theWhite));
		context.addMember(updateData);

		StringField stringField = WebFolderUtils.createDescriptionField(DocumentVersion.DESCRIPTION, 5);
		stringField.initializeField(document.getDocumentVersion().getDescription());
		context.addMember(stringField);

		EnableButtonOnValue.enableButtonOnNonEmptyValue(updateData, _updateButton);
    }
    
    @Override
    protected void fillButtons(List<CommandModel> buttons) {
		buttons.add(_updateButton);
    	addCancel(buttons);
    }

	static TopLogicException errorUpdateFailed(BinaryData theItem, Throwable ex) {
		return new TopLogicException(THIS_CLASS, "updateFailed(name)", new Object[] { theItem.getName() }, ex);
	}

	/**
	 * @see WebFolderUIFactory#getManualLocking()
	 */
	protected boolean getManualLocking() {
		return _manualLocking;
	}

}