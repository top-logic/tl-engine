/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.imp; // import is a reserved keyword ..

import java.io.File;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.progress.ProgressInfo;
import com.top_logic.mig.html.layout.CSVFileUploadComponent;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.assistent.AbstractAssistentController;
import com.top_logic.tool.boundsec.assistent.CommandHolder;
import com.top_logic.tool.boundsec.commandhandlers.UploadHandler;

/**
 * Control the Excel Supplier Importer.
 * 
 * Allow importing Supplier Data from Excel (when Mandtor is not
 * configured to use SAP as input source)
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class ExcelSupplierImportController extends AbstractAssistentController {

	/**
	 * Typed configuration interface definition for {@link ExcelSupplierImportController}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends AbstractAssistentController.Config {

		@Name("uploadStep")
		ComponentName getUploadStep();

		@Name("uploadStep")
		ComponentName getImportStep();
	}

	/**
	 * Create a {@link ExcelSupplierImportController}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public ExcelSupplierImportController(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

    /** 
     * "importProgressVolume" is our second and last step.
     */
    @Override
	protected boolean isFinishStep(ComponentName aCurrentStepName) {
    	return this.isImportStepName(aCurrentStepName);
    }

    /** 
     * Same as {@link #isFinishStep(ComponentName)}.
     */
    @Override
	public boolean isLastStep(ComponentName aCurrentStepName) {
        return isFinishStep(aCurrentStepName);
    }

    /** 
     * No single close step here for now.
     * 
     * @return always false for now.
     */
    @Override
	protected boolean isCloseInfoStep(ComponentName aCurrentStepName) {
        return false;
    }

    /** 
     * My first step is named upload.
     */
    @Override
	protected boolean isFirstStep(ComponentName aCurrentStepName) {
        return this.isUploadStepName(aCurrentStepName);
    }

    /** 
     * Main step logic is handled here.
     */
    @Override
	public ComponentName getNameOfNextStep(ComponentName aCurrentStepName) {
        if (this.isUploadStepName(aCurrentStepName))
			return importStep();
		return uploadStep();
    }

	private ComponentName importStep() {
		return getConfig().getImportStep();
	}

	private ComponentName uploadStep() {
		return getConfig().getUploadStep();
	}
    
    /**
     * Add an Upload Command as part of switching to the next step.
     * 
     * @param aCurrentStepName the step performing the command.
     * @return the commandHanlder to perform before the switch action. May be null.
     */
    @Override
	public CommandHolder getAdditionaleCommandForNext(ComponentName aCurrentStepName) {
        if (this.isUploadStepName(aCurrentStepName)) {
            CommandHandlerFactory theFactory = CommandHandlerFactory.getInstance();
            CommandHandler theHandler = theFactory.getHandler(UploadHandler.COMMAND);
            return new CommandHolder(this.assistent.getMainLayout(), theHandler, aCurrentStepName);
        }
        return super.getAdditionaleCommandForNext(aCurrentStepName);
    }
 
    /** 
     * Check if CSVFileUploadComponent created a file an eventually delete it.
     */
    @Override
	public void dialogFinished() {
        File theFile = (File) assistent.getData(CSVFileUploadComponent.FILE_ATTR);
        if (theFile != null && theFile.exists()) {
            theFile.delete();
        }
    }
    
    /** 
     * true for upload step only.
     */
    @Override
	public boolean showCancelButton(ComponentName aCurrentStepName) {
        return this.isUploadStepName(aCurrentStepName);
    }
    
    /** 
     * false - not needed here.
     */
    @Override
	public boolean showBackwardButton(ComponentName aCurrentStepName) {
        return this.isImportStepName(aCurrentStepName);
    }

    /** 
     * DisableForwardButton when whie ProgressInfo is not finished.
     */
    @Override
	public boolean disableForwardButton(ComponentName aCurrentStepName) {
        if(this.isImportStepName(aCurrentStepName)){
            ProgressInfo pInfo = (ProgressInfo) assistent.getCurrentStep().getModel();
            return pInfo != null && !pInfo.isFinished();
        }
        return false;
    }
    
    /**
     * @param aStepName a step name
     * @return true if the step name is the import step name
     */
    public boolean isImportStepName(ComponentName aStepName) {
		return aStepName != null && aStepName.equals(importStep());
    }
    
    /**
     * @param aStepName a step name
     * @return true if the step name is the upload step name
     */
    public boolean isUploadStepName(ComponentName aStepName) {
		return aStepName != null && aStepName.equals(uploadStep());
    }
    
}