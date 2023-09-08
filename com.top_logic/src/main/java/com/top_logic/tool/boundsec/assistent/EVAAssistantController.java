/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.assistent;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.base.services.simpleajax.AJAXCommandHandler;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.sched.BatchImpl;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.thread.InContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.progress.ProgressInfo;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.commandhandlers.UploadHandler;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * The simple process of importing a file to the system can be handled by 
 * this controller.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class EVAAssistantController extends StepAssistantComponent {

	public static final String IMPORT_HANDLER = "importHandler";
    
    public static final String UPLOAD_HANDLER = "uploadHandler";

    public static final String EXTENSION_NAME = "extension";
    
    public static final String VALIDATION_RESULT = "validationResult";

	public interface Config extends StepAssistantComponent.Config {

		String INPUT_STEP = "inputStep";

		String PROCESS_STEP = "processStep";

		String OUTPUT_STEP = "outputStep";

		String ERROR_STEP = "errorStep";

		@Name(INPUT_STEP)
		ComponentName getInputStep();

		@Name(PROCESS_STEP)
		ComponentName getProcessStep();

		@Name(OUTPUT_STEP)
		ComponentName getOutputStep();

		@Name(ERROR_STEP)
		ComponentName getErrorStep();

		@Name(EXTENSION_NAME)
		@Format(CommaSeparatedStrings.class)
		List<String> getExtension();

		@Name(IMPORT_HANDLER)
		@Mandatory
		@InstanceFormat
		ProcessFileHandler getImportHandler();
		
		@Name(UPLOAD_HANDLER)
		@InstanceFormat
		@InstanceDefault(UploadHandler.class)
		UploadHandler getUploadHandler();
	}

    private ProcessFileHandler importHandler;
    
    private UploadHandler validationHandler;

    private CommandHolder uploadCommand;

    private CommandHolder importCommand;

	final List<String> _extensions;

	public EVAAssistantController(InstantiationContext context, Config config) {
		super(context, config);
		this._extensions = config.getExtension();
		this.importHandler = config.getImportHandler();
		this.validationHandler = config.getUploadHandler();
    }

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

    /**
     * @see com.top_logic.tool.boundsec.assistent.StepAssistantComponent#showBackwardButton(ComponentName)
     */
    @Override
	public boolean showBackwardButton(ComponentName aCurrentStepName) {
		if (getConfig().getOutputStep().equals(aCurrentStepName) /* || RESULT.equals(
																	 * aCurrentStepName) */) {
            return false;
        }

        return super.showBackwardButton(aCurrentStepName);
    }

    /**
     * @see com.top_logic.tool.boundsec.assistent.StepAssistantComponent#showCancelButton(ComponentName)
     */
    @Override
	public boolean showCancelButton(ComponentName aCurrentStepName) {
		if (getConfig().getOutputStep().equals(aCurrentStepName) /* || RESULT.equals(
																	 * aCurrentStepName) */) {
            return false;
        }

        return super.showCancelButton(aCurrentStepName);
    }

    /**
     * @see com.top_logic.tool.boundsec.assistent.StepAssistantComponent#getSteps()
     */
    @Override
	protected SimpleStepInfo[] getSteps() {
        SimpleStepInfo[] theSteps = new SimpleStepInfo[4];

		theSteps[0] = new SimpleStepInfo(getConfig().getInputStep(), getConfig().getProcessStep(),
			getConfig().getErrorStep(), this.getUploadCommand(), true, false, false, false);
		theSteps[1] = new SimpleStepInfo(getConfig().getProcessStep(), getConfig().getOutputStep(),
			getConfig().getErrorStep(), this.getImportCommand(), false, false, false, false);
		theSteps[2] = new SimpleStepInfo(getConfig().getOutputStep(), null, getConfig().getErrorStep(), null, false,
			true, true, true);
		theSteps[3] = new SimpleStepInfo(getConfig().getErrorStep(), null, null, null, false, true, false, true);

        return theSteps;
    }

	/**
	 * Return the supported file extensions.
	 * 
	 * @return The requested file extensions.
	 */
	public List<String> getExtensions() {
		return this._extensions;
	}

	/**
	 * Create a new import task from the given parameters.
	 * 
	 * @param component
	 *        The component containing the form context.
	 * @param assistant
	 *        The assistant component handling the EVA process.
	 * @param importFile
	 *        The file to be processed.
	 * 
	 * @return The requested import task.
	 */
	protected Runnable createImportTask(LayoutComponent component, AssistentComponent assistant, File importFile) {
		ProcessFileHandler  theHandler = this.getProcessFileHandler();
		Map<String, Object> theMap     = this.getDataMap(assistant);

		ImportTask importTask = new ImportTask(theHandler, theMap, importFile, TLContext.getContext());

		((Selectable) component).setSelected(theHandler);

		return importTask;
	}

	/** 
	 * Extract the data of data set items from the given component.
	 * 
	 * @param    aComponent    The component to get the data from.
	 * @return   The values in a new data.
	 */
	protected Map<String, Object> getDataMap(AssistentComponent aComponent) {
		Map<String, Object> theMap = new HashMap<>();

		for (String theString : aComponent.getDataKeys()) {
			theMap.put(theString, aComponent.getData(theString));
		}
		return theMap;
	}

    /** 
     * Return the class doing the work in a separate thread.
     * 
     * @return    The class doing the work.
     */
    protected ProcessFileHandler getProcessFileHandler() {
        return this.importHandler;
    }

    /** 
     * Return the import command for the last step.
     * 
     * @return    The requested import command encapsulated in a {@link CommandHolder}.
     * @see       ImportCommandHandler
     */
    protected CommandHolder getImportCommand() {
        if (this.importCommand == null) {
            MainLayout theMain = this.assistent.getMainLayout();

			this.importCommand =
				new CommandHolder(theMain, ImportCommandHandler.newInstance(this), getConfig().getProcessStep());
        }

        return this.importCommand;
    }

    /** 
     * Return the upload handler to be used in first assistant step.
     * 
     * @return    The defined upload handler (may be {@link ValidatingUploadHandler}).
     * @see       #getUploadCommand()
     */
    protected UploadHandler getUploadHandler() {
        return this.validationHandler;
    }

    /** 
     * Return the upload command for the first step.
     * 
     * @return    The requested upload command encapsulated in a {@link CommandHolder}.
     * @see       #getUploadHandler()
     */
    private CommandHolder getUploadCommand() {
        if (this.uploadCommand == null) {
            MainLayout theMain = this.assistent.getMainLayout();

			this.uploadCommand = new CommandHolder(theMain, this.getUploadHandler(), getConfig().getInputStep());
        }

        return this.uploadCommand;
    }

    /**
     * Importer step in the assistant.
     * 
     * This step will be called when the upload has been finished and 
     * the options have been chosen. This handler will start the import
     * thread an hand over the {@link EVAAssistantController#getProcessFileHandler() 
     * process file handler} to it for doing the work.
     * 
     * @author    <a href=mailto:mga@top-logic.com>Michael Gänsler</a>
     */
	public static class ImportCommandHandler extends AJAXCommandHandler {

        public static final String COMMAND_ID = "processImportFile";

		private EVAAssistantController _controller;

        // Constructors

        /** 
         * Creates a {@link ImportCommandHandler}.
         */
        public ImportCommandHandler(InstantiationContext context, Config config) {
            super(context, config);
        }

        // Overridden methods from AJAXCommandHandler

		public static CommandHandler newInstance(EVAAssistantController controller) {
			ImportCommandHandler result = newInstance(ImportCommandHandler.class, COMMAND_ID);
			result._controller = controller;
			return result;
		}

		/**
		 * @see com.top_logic.base.services.simpleajax.AJAXCommandHandler#handleCommand(com.top_logic.layout.DisplayContext,
		 *      com.top_logic.mig.html.layout.LayoutComponent, Object, Map)
		 */
        @Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
            AssistentComponent theAssistent = AssistentComponent.getEnclosingAssistentComponent(aComponent);
            File               theFile      = (File) theAssistent.getData(AssistentFileUploadComponent.FILE);

            try {
                if (isCorrectFile(theFile)) {
					Runnable importer = _controller.createImportTask(aComponent, theAssistent, theFile);

					SchedulerService.getInstance().execute(importer);
                    
                    return HandlerResult.DEFAULT_RESULT;
                }
            }
            catch (Exception ex) {
                throw new TopLogicException(ImportCommandHandler.class, "import.file", ex);
            }

            return HandlerResult.DEFAULT_RESULT;
        }

		private boolean isCorrectFile(File file) {
			String fileName = file.getName().toLowerCase();
			for (String ext : _controller.getExtensions()) {
				if (fileName.endsWith(ext)) {
					return true;
				}
			}
			return false;
		}
    }

	/**
	 * Import data from a file by using a {@link ProcessFileHandler} as importer.
	 * 
	 * @author     <a href=mailto:mga@top-logic.com>Michael Gänsler</a>
	 */
	public static class ImportTask extends BatchImpl implements InContext {

        // Attributes

        private final File file;
        private final TLContext context;
        private final ProcessFileHandler importer;
		private final Map<String, Object> data;

        // Constructors


        /**
		 * Creates a {@link ImportTask}.
		 * 
		 * @param aHandler
		 *        The handler to be called for the real import.
		 * @param someData
		 *        Data provided by the assistant component calling this.
		 * @param aFile
		 *        The excel file to take the values from.
		 * @param aContext
		 *        The context this thread has been called from. May be <code>null</code>.
		 */
		public ImportTask(ProcessFileHandler aHandler, Map<String, Object> someData, File aFile, TLContext aContext) {
			super(aHandler.getClass().getSimpleName() + ':' + aFile.getName());

			this.importer  = aHandler;
			this.data      = someData;
            this.file      = aFile;
            this.context   = aContext;
			if (this.context != null) {
				this.context.lock();
			}
        }

        // Overridden methods from TaskImpl

        @Override
		public final void run() {
			if (context == null) {
				inContext();
			} else {
				try {
					context.inContext(this);
				} finally {
					context.unlock();
				}
			}
		}

		@Override
		public void inContext() {
            try {
				this.processFile(this.data, this.file);
			} catch (Throwable ex) {
                Logger.error("Failed to import!", ex, this);
            }
        }

        // Public methods

        /** 
         * Return the handler assigned to this task for doing the work.
         * 
         * @return    The requested performer.
         */
        public ProcessFileHandler getImporter() {
            return this.importer;
        }

        // Protected methods

        /** 
         * Do the real work in the assistant by calling the inner {@link #importer}.
         * 
         * @param    someData    The data to be used for handling.
         * @param    aFile       The file to be processed.
         * @throws   Exception    If processing fails for a reason.
         */
		protected void processFile(Map<String, Object> someData, File aFile) throws Exception {
			this.getImporter().processFile(someData, aFile);
        }
    }

    /**
     * Handler used in the {@link ImportTask} to do the work. 
     * 
     * @author    <a href=mailto:mga@top-logic.com>Michael Gänsler</a>
     */
    public interface ProcessFileHandler extends ProgressInfo {

        /**
		 * Do the real work in the assistant.
		 * 
		 * @param someData
		 *        The component calling this.
		 * @param aFile
		 *        The file to be processed.
		 * @throws Exception
		 *         If processing fails for a reason.
		 */
		public void processFile(Map<String, Object> someData, File aFile) throws Exception;
    }
}

