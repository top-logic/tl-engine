/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.imp;

import java.io.File;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.layout.progress.ProgressInfo;
import com.top_logic.mig.html.layout.CSVFileUploadComponent;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.assistent.AbstractAssistentController;
import com.top_logic.tool.boundsec.assistent.CommandHolder;
import com.top_logic.tool.boundsec.commandhandlers.UploadHandler;

/**
 * Control the SAP Supplier Assitent Importer.
 * 
 * This is a Customer specific component and needs modifications for generic 
 * (if possible) usage.
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class ImportSAPController extends AbstractAssistentController {

	/**
	 * Typed configuration interface definition for {@link ImportSAPController}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends AbstractAssistentController.Config {

		@Mandatory
		ComponentName getShowTablesStep();

		@Mandatory
		ComponentName getFinishStep();

		@Mandatory
		ComponentName getUploadStep();

		@Mandatory
		ComponentName getImportProgressStep();
	}

	/** Key to store the Map of imported SAPRecords in the AssistentComponenet */
    public final static String SAP_SUPPLIERS = SAPSupplierRecord.class.getName() + ".list";
    
	public ImportSAPController(InstantiationContext context, Config config) {
		super(context, config);
    }

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

    /** 
     * Any step starting with "finish" will do.
     */
    @Override
	protected boolean isFinishStep(ComponentName aCurrentStepName) {
		return getConfig().getFinishStep().equals(aCurrentStepName);
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
		return getConfig().getUploadStep().equals(aCurrentStepName);
    }

    /** 
     * Main step logic is handled here.
     */
    @Override
	public ComponentName getNameOfNextStep(ComponentName aCurrentStepName) {
		Config config = getConfig();
		if (config.getUploadStep().equals(aCurrentStepName))
			return config.getImportProgressStep();
		if (config.getImportProgressStep().equals(aCurrentStepName))
			return config.getShowTablesStep();
		if (config.getShowTablesStep().equals(aCurrentStepName))
			return config.getFinishStep();
		return config.getUploadStep();
    }
    
    /**
     * Add an Upload Command as part of switching to the next step.
     * 
     * @param aCurrentStepName the step performing the command.
     * @return the commandHanlder to perform before the switch action. May be null.
     */
    @Override
	public CommandHolder getAdditionaleCommandForNext(ComponentName aCurrentStepName) {
		if (getConfig().getUploadStep().equals(aCurrentStepName)) {
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
     * disableForwardButton when when ProgressInfo is not finished.
     */
    @Override
	public boolean disableForwardButton(ComponentName aCurrentStepName) {
		if (getConfig().getImportProgressStep().equals(aCurrentStepName)) {
            ProgressInfo pInfo = (ProgressInfo) assistent.getCurrentStep().getModel();
            return pInfo != null && !pInfo.isFinished();
        }
        return super.disableForwardButton(aCurrentStepName);
    }
    
}
