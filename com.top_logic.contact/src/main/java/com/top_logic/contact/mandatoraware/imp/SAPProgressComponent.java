/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.imp;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.top_logic.base.services.simpleajax.AJAXCommandHandler;
import com.top_logic.basic.Configuration;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.structured.wrap.Mandator;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.progress.AJAXProgressComponent;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.sched.Scheduler;

/**
 * Show the Progress of the SAPImport.
 * 
 * @author    <a href=mailto:kha@top-logic.com>Klaus Halfmann</a>
 */
public class SAPProgressComponent extends AJAXProgressComponent {

    public interface Config extends AJAXProgressComponent.Config {

		String FINISH_COMMANDS =
			AJAXProgressComponent.Config.FINISH_COMMAND_HANDLERS + ',' + FinishSAPImportCommandHandler.COMMAND_ID;

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Name(ATT_TASKNAME)
		@Mandatory
		String getTaskname();

		@Name(ATT_CREATE)
		@BooleanDefault(false)
		boolean getCreate();

		@Name(ATT_CREATEPROPS)
		String getCreateProperties();

		@FormattedDefault(FINISH_COMMANDS)
		@Override
		List<String> getFinishCommandHandlers();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			AJAXProgressComponent.Config.super.modifyIntrinsicCommands(registry);
			registry.registerButton(StartSAPImportCommandHandler.COMMAND_ID);
		}
	}

	public static final String ATT_TASKNAME    = "taskname";
    public static final String ATT_CREATE      = "create";
    public static final String ATT_CREATEPROPS = "createProperties";

    /** The name of the task to run. */
    protected String taskName;

    /** If true try to create the task if not found in the scheduler. */
    protected boolean create;

    /** Property section to be used when creating a task. */
    protected String createProps;

    /** 
     * Create a new SAPProgressComponent from XML.
     */
    public SAPProgressComponent(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
        
        this.taskName    = atts.getTaskname();
        this.create      = atts.getCreate();
        this.createProps = StringServices.nonEmpty(atts.getCreateProperties());
        
        this.alwaysReloadButtons = true;
    }

	@Override
	public boolean validateModel(DisplayContext context) {
		if (getModel() == null) {
			setModel(initialModel());
		}
		return super.validateModel(context);
	}

	private Object initialModel() {
        Object model;
			model = (DataObjectImportTask) Scheduler.getSchedulerInstance().getTaskByName(this.taskName);
            
            if (model == null && this.create) {
            	String theClazz = null;
                try {
                    Properties theProp = Configuration.getConfigurationByName(this.createProps).getProperties();

                    if (theProp.isEmpty()) {
                         Logger.error("Missing configuration for " + this.taskName + " will be skipped", this);
                         return null;
                    }
                                theClazz = theProp.getProperty("class");
                    Class       theClass = Class.forName(theClazz);
                    Constructor theCTor  = theClass.getConstructor(new Class[] {Properties.class});

                    model = (DataObjectImportTask) theCTor.newInstance(new Object[] { theProp });
                }
                catch (Exception ex) {
                    Logger.error("Failed to create task for " + this.taskName + " class='" + theClazz + "'", ex, this);
                }
            }

            if (model != null) {
                this.invalidateButtons();
            }

        return model;
    }

    /**
     * @author    <a href=mailto:mga@top-logic.com>mga</a>
     */
    public static class StartSAPImportCommandHandler extends AJAXCommandHandler {

        // Constants

        public static final String COMMAND_ID = "startSAPImport";

		/**
		 * Configuration for {@link StartSAPImportCommandHandler}.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		public interface Config extends AJAXCommandHandler.Config {

			@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
			@Override
			CommandGroupReference getGroup();

		}

        /** 
         * Creates a {@link StartSAPImportCommandHandler}.
         */
        public StartSAPImportCommandHandler(InstantiationContext context, Config config) {
			super(context, config);
        }

        /**
         * @see com.top_logic.base.services.simpleajax.AJAXCommandHandler#handleCommand(com.top_logic.layout.DisplayContext, com.top_logic.mig.html.layout.LayoutComponent, Object, Map)
         */
        @Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> aSomeArguments) {
			final DataObjectImportTask theModel = (DataObjectImportTask) model;

            if (theModel != null) {
				final Mandator theMandator = ExcelPersonContactImportProgress.findMandator(aComponent);

                if (aComponent instanceof SAPProgressComponent) {
                    ((SAPProgressComponent) aComponent).resetState();
                    ((SAPProgressComponent) aComponent).invalidate();
                }

                new Thread(theModel.getName()) {
                    @Override
					public void run() {
                        theModel.runOnMandator(theMandator, false);
                    }
                }.start();

                try {
                    Thread.sleep(theModel.getRefreshSeconds() * 1000); // Wait until refresh will work ...
                }
                catch (Exception ex) {
                    
                }
            }

            return HandlerResult.DEFAULT_RESULT;
        }

        @Override
		@Deprecated
		public ResKey getDefaultI18NKey() {
            return I18NConstants.START_IMPORT;
        }

        @Override
		@Deprecated
		public ExecutabilityRule createExecutabilityRule() {
			return new ExecutabilityRule() {

                    @Override
					public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> aSomeValues) {
					DataObjectImportTask theModel = (DataObjectImportTask) model;

                        if (theModel == null) {
						return ExecutableState.createDisabledState(I18NConstants.ERROR_NO_IMPORT_DEFINED);
                        }
                        else if (!theModel.isFinished()){
						return ExecutableState.createDisabledState(I18NConstants.ERROR_IMPORT_IN_PROGRESS);
                        }
                        else {
                            return ExecutableState.EXECUTABLE;
                        }
                    }
                };
        }
    }

    /**
     * Handler for displaying the rest of information provided by the internal importer to
     * the UI. 
     * 
     * @author    <a href=mailto:mga@top-logic.com>Michael Gänsler</a>
     */
    public static class FinishSAPImportCommandHandler extends AJAXCommandHandler {
        
        public static final String COMMAND_ID = "finishSAPImport";

        /**
		 * Creates a {@link FinishSAPImportCommandHandler}.
		 */
        public FinishSAPImportCommandHandler(InstantiationContext context, Config config) {
            super(context, config);
        }

        /**
         * @see com.top_logic.base.services.simpleajax.AJAXCommandHandler#handleCommand(com.top_logic.layout.DisplayContext, com.top_logic.mig.html.layout.LayoutComponent, Object, Map)
         */
        @Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> aSomeArguments) {
            return HandlerResult.DEFAULT_RESULT;
        }
    }
}
