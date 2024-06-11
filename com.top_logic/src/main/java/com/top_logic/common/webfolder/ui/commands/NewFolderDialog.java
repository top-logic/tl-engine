/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui.commands;

import static com.top_logic.layout.basic.fragments.Fragments.*;

import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.Logger;
import com.top_logic.common.webfolder.WebFolderUtils;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.control.IconControl;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.component.ComponentUtil;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.tag.Icons;
import com.top_logic.layout.messagebox.AbstractFormPageDialog;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.SimpleFormDialog;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.error.TopLogicException;

/**
 * New folder dialog providing the {@link StringField} for identifying the name of the new folder.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class NewFolderDialog extends AbstractFormPageDialog {

	/**
	 * Name of the folder name field.
	 * 
	 * <p>
	 * For backwards compatibility with existing scripts, {@link SimpleFormDialog#INPUT_FIELD} is
	 * used as name.
	 * </p>
	 */
	static final String FOLDER_NAME_FIELD = SimpleFormDialog.INPUT_FIELD;

	static final String FOLDER_DESCRIPTION_FIELD = WebFolder.DESCRIPTION;

    /**
	 * Command for doing the folder create.
	 * 
	 * When creating a new {@link WebFolder} will be created in the folder defined in this instance.
	 * 
	 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public static class NewFolderCommand implements Command {
	    
	    /** The web folder to create the new folder in. */
	    private final WebFolder folder;
		private final FormHandler handler;
		private final Command closeAction;
	    
	    // Constructors
	    
	    /** 
	     * Creates a {@link NewFolderCommand}.
	     * 
	     * @param    aFolder    The web folder to create the new folder in, must not be <code>null</code>.
	     */
	    public NewFolderCommand(FormHandler handler, WebFolder aFolder, Command closeAction) {
	        this.handler = handler;
			this.folder = aFolder;
			this.closeAction = closeAction;
	    }
	
	    // Implementation of interface Executable
	
	    @Override
		public HandlerResult executeCommand(DisplayContext aContext) {
			if (!ComponentUtil.isValid(folder)) {
				closeAction.executeCommand(aContext);
				return ComponentUtil.errorObjectDeleted(aContext);
			}

	        HandlerResult theResult  = new HandlerResult();
	        FormContext   theContext = handler.getFormContext();
	        FormField     theField   = theContext.getField(FOLDER_NAME_FIELD);
	
	        if (theField instanceof StringField) {
	            String theName = (String) ((StringField) theField).getValue();
	
	            try {
	                if (this.folder.hasChild(theName)) {
						theResult.addError(I18NConstants.ERROR_FOLDER_EXISTS__NAME.fill(theName));
					} else {
	                    Transaction theTX = PersistencyLayer.getKnowledgeBase().beginTransaction();
	                    try {
							WebFolder subFolder = folder.createSubFolder(theName);
							FormField descriptionField = theContext.getField(WebFolder.DESCRIPTION);
							subFolder.setDescription(descriptionField.getValue().toString());
							theTX.commit();

							return closeAction.executeCommand(aContext);
						} catch (IllegalArgumentException ex) {
							throw new TopLogicException(NewFolderDialog.class, "invalidName",
								new Object[] { ex.getMessage() });
						} catch (RuntimeException ex) {
							throw ex;
						} catch (Exception ex) {
							Logger.error("Folder creation failed with internal error.", ex, NewFolderDialog.class);
							theResult.addError(I18NConstants.ERROR_FOLDER_CREATE__NAME.fill(theName));
						} finally {
	                        theTX.rollback();
	                    }
	                }
				} catch (RuntimeException ex) {
					throw ex;
				} catch (Exception ex) {
					Logger.error("Folder creation failed with internal error.", ex, NewFolderDialog.class);
					theResult.addError(I18NConstants.ERROR_FOLDER_CREATE__NAME.fill(theName));
	            }
	        }
	
	        return theResult;
	    }
	}

	private final WebFolder webFolder;

	/** 
     * Creates a {@link NewFolderDialog}.
     */
	public NewFolderDialog(WebFolder webFolder) {
		super(I18NConstants.NEW_FOLDER_DIALOG, DisplayDimension.dim(400, DisplayUnit.PIXEL),
			DisplayDimension.dim(310, DisplayUnit.PIXEL));
		this.webFolder = webFolder;
    }

	@Override
	protected ResPrefix getResourcePrefix() {
		return I18NConstants.NEW_FOLDER_DIALOG;
	}

    @Override
    protected void fillFormContext(FormContext context) {
		StringField nameField = FormFactory.newStringField(FOLDER_NAME_FIELD, "", false);
		StringField descriptionField = WebFolderUtils.createDescriptionField(FOLDER_DESCRIPTION_FIELD, 5);

        context.addMember(nameField);
		context.addMember(descriptionField);
    }
    
    @Override
    protected void fillButtons(List<CommandModel> buttons) {
		Command createCommand = new NewFolderCommand(this, webFolder, getDiscardClosure());
		getDialogModel().setDefaultCommand(createCommand);
		buttons.add(MessageBox.button(I18NConstants.CREATE_FOLDER, createCommand));

    	addCancel(buttons);
    }

	@Override
	protected HTMLFragment createSubtitleContent() {
		return Fragments.empty();
	}

	@Override
	protected IconControl createTitleIcon() {
		return IconControl.iconTheme(ThemeImage.typeIconLarge(WebFolder.OBJECT_NAME));
	}

	@Override
	protected IconControl createTitleIconOverlay() {
		return IconControl.icon(Icons.PLUS48);
	}

	@Override
	protected HTMLFragment createBodyContent() {
		HTMLFragment nameInput = input(FOLDER_NAME_FIELD);
		HTMLFragment errorName = error(member(FOLDER_NAME_FIELD));
		HTMLFragment nameWithError = concat(nameInput, nbsp(), errorName);

		HTMLFragment descriptionInput = input(FOLDER_DESCRIPTION_FIELD);

		return div(label(FOLDER_NAME_FIELD), div(nameWithError), label(FOLDER_DESCRIPTION_FIELD),
			div(descriptionInput));
	}

}
