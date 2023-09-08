/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.genericimport;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.element.genericimport.GenericDataImportConfiguration;
import com.top_logic.element.genericimport.interfaces.GenericDataImportConfigurationAware;
import com.top_logic.element.genericimport.interfaces.GenericImporter.GenericFileImporter;
import com.top_logic.element.layout.genericimport.GenericDataImportConfigComponent.GenericDataImportConverterConfigHandler;
import com.top_logic.element.layout.genericimport.GenericDataImportProgressComponent.StartGenericImportTaskCommand;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.assistent.AssistentComponent;
import com.top_logic.tool.boundsec.assistent.AssistentFileUploadComponent;
import com.top_logic.tool.boundsec.assistent.CommandChain;
import com.top_logic.tool.boundsec.assistent.CommandHolder;
import com.top_logic.tool.boundsec.assistent.StepAssistantComponent;
import com.top_logic.tool.boundsec.commandhandlers.UploadHandler;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.util.error.TopLogicException;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class GenericDataImportController extends StepAssistantComponent implements GenericDataImportConfigurationAware {

//    private static final String RESULT = "genericImportResult";
//
//    private static final String WAIT_PROGRESS = "genericImportWaitProgress";
//
//    private static final String PREVIEW       = "genericImportPreview";

	public interface Config extends StepAssistantComponent.Config {

		String UPLOAD_STEP = "uploadStep";

		String CONFIG_STEP = "configStep";

		String PROGRESS_STEP = "progressStep";

		@Name(UPLOAD_STEP)
		ComponentName getUploadStep();

		@Name(CONFIG_STEP)
		ComponentName getConfigStep();

		@Name(PROGRESS_STEP)
		ComponentName getProgressStep();

	}

    private CommandHolder uploadCommand;

    private CommandHolder importCommand;

	public GenericDataImportController(InstantiationContext context, Config config) {
		super(context, config);
    }

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

    private ComponentName getUploadStepName() {
		return getConfig().getUploadStep();
    }

    private ComponentName getConfigStepName() {
		return getConfig().getConfigStep();
    }
    
    private ComponentName getProgressStepName() {
		return getConfig().getProgressStep();
    }
    
    @Override
	protected SimpleStepInfo[] getSteps() {
        SimpleStepInfo[] theSteps = new SimpleStepInfo[3];

        theSteps[0] = new SimpleStepInfo(this.getUploadStepName(), this.getConfigStepName(), getUploadCommand(), true, false, false, false);
        theSteps[1] = new SimpleStepInfo(this.getConfigStepName(), this.getProgressStepName(), getImportCommand(), false, false, false, false);
        theSteps[2] = new SimpleStepInfo(this.getProgressStepName(), this.getUploadStepName(), null, false, false, false, true);
        //theSteps[3] = new SimpleStepInfo(RESULT, UPLOAD, null, false, true,  false, true);
        return theSteps;
    }

    private CommandHolder getUploadCommand() {
        if (this.uploadCommand == null) {
            MainLayout            theMain = this.assistent.getMainLayout();
			this.uploadCommand =
				new CommandHolder(theMain, GenericDataImportUploadHandler.INSTANCE, this.getUploadStepName());
        }
        return this.uploadCommand;
    }

    private CommandHolder getImportCommand() {
        if (this.importCommand == null) {
            MainLayout            theMain = this.assistent.getMainLayout();
            
			LayoutComponent theComponent = theMain.getComponentByName(getConfigStepName());
            List theCommands = new ArrayList(2);
			theCommands.add(new CommandHolder(GenericDataImportConverterConfigHandler.INSTANCE, theComponent));
			theCommands.add(new CommandHolder(CommandHandlerFactory.getInstance().getHandler(
				StartGenericImportTaskCommand.COMMAND_ID), theComponent));
			CommandChain theChain =
				CommandChain.newInstance(CommandChain.generateID(theCommands), SimpleBoundCommandGroup.READ,
					theCommands, theComponent);
			this.importCommand = new CommandHolder(theChain, theComponent);
        }
        return this.importCommand;
    }


    @Override
	public boolean showBackwardButton(ComponentName aCurrentStepName) {
        if ((this.getProgressStepName()).equals(aCurrentStepName) /*|| RESULT.equals(aCurrentStepName)*/) {
            return false;
        }
        return super.showBackwardButton(aCurrentStepName);
    }

    @Override
	public boolean showCancelButton(ComponentName aCurrentStepName) {
        if ((this.getProgressStepName()).equals(aCurrentStepName) /*|| RESULT.equals(aCurrentStepName)*/) {
            return false;
        }
        return super.showCancelButton(aCurrentStepName);
    }

    @Override
	public GenericDataImportConfiguration getImportConfiguration() {
        return ((GenericDataImportConfigurationAware) this.assistent).getImportConfiguration();
    }

    @Override
	public boolean setImportConfiguration(GenericDataImportConfiguration aConfig, String anInternalType) {
        return false;
    }

    public boolean checkImportConfiguration(GenericDataImportConfiguration aConfig) throws Exception {
        return true;
    }

    public static class GenericDataImportUploadHandler extends UploadHandler {

		public static final String COMMAND_ID = "genericImportUpload";

		public static final CommandHandler INSTANCE = newInstance(GenericDataImportUploadHandler.class, COMMAND_ID);

        public GenericDataImportUploadHandler(InstantiationContext context, Config config) {
            super(context, config);
        }

        @Override
		public HandlerResult handleCommand(DisplayContext aContext,
                LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
            HandlerResult theResult = super.handleCommand(aContext, aComponent, model, someArguments);

            if (theResult.isSuccess()) {
                AssistentFileUploadComponent        theComp = (AssistentFileUploadComponent) aComponent;
                GenericDataImportAssistentComponent theAssi = (GenericDataImportAssistentComponent) AssistentComponent.getEnclosingAssistentComponent(theComp);
                File theFile = (File) theAssi.getData(AssistentFileUploadComponent.FILE);
                if (theFile != null) {
                    String              theFilename = theFile.getName().toUpperCase();
                    GenericDataImportConfiguration theConf = theAssi.getImportConfiguration();
                    GenericFileImporter theImporter = (GenericFileImporter) theConf.getImporter();
                    String[]            theExts     = theImporter.getSupportedFileExtensions();
                    for (int i=0; i<theExts.length; i++) {
                        if (theFilename.endsWith((theExts[i].toUpperCase()))) {
							theImporter.setImportFile(BinaryDataFactory.createBinaryData(theFile));
                            return theResult;
                        }
                    }
                    theResult.setException(new TopLogicException(this.getClass(), "genericImport.genericImportUpload.invalidFileType", new Object[] {theFilename}));
                }
                else {
                    theAssi.setData(AssistentComponent.SHOW_AGAIN, Boolean.TRUE);
                    theResult.addErrorMessage(I18NConstants.UPLOAD_NOFILE);
                }
            }

            return theResult;
        }
    }
}

