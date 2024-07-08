/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.genericimport;

import java.util.Map;

import com.top_logic.base.services.simpleajax.AJAXCommandHandler;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.genericimport.GenericDataImportConfiguration;
import com.top_logic.element.genericimport.GenericDataImportTask;
import com.top_logic.element.genericimport.interfaces.GenericDataImportConfigurationAware;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.progress.AJAXProgressComponent;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.assistent.AssistentComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class GenericDataImportProgressComponent extends AJAXProgressComponent {

    private static final String IMPORT_PROGRESS_INFO = "IPI";

    /**
     * Creates a {@link GenericDataImportProgressComponent}.
     */
    public GenericDataImportProgressComponent(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
    }


    @Override
	protected boolean supportsInternalModel(Object anObject) {
        return anObject instanceof GenericDataImportConfiguration;
    }

    public static class StartGenericImportTaskCommand extends AJAXCommandHandler {

        public static final String COMMAND_ID = "StartImport";

        public StartGenericImportTaskCommand(InstantiationContext context, Config config) {
            super(context, config);
        }

        @Override
		public HandlerResult handleCommand(DisplayContext aContext,
                LayoutComponent aComponent, Object model, Map<String, Object> aSomeArguments) {
            AssistentComponent                  theAssi = AssistentComponent.getEnclosingAssistentComponent(aComponent);
            GenericDataImportConfigurationAware theComp = (GenericDataImportConfigurationAware) aComponent;
            final GenericDataImportConfiguration      theConf = theComp.getImportConfiguration();

            final GenericDataImportTask theTask = new GenericDataImportTask("GenericImport");
            theTask.setImportConfiguration(theConf, null);

            theAssi.setData(IMPORT_PROGRESS_INFO, theTask);

            new Thread(theTask.getName()) {
                @Override
				public void run() {
                	theTask.init();
                	theTask.doImport();
//                	theTask.doImport(theConf.getCache());
                }
            }.start();

            return HandlerResult.DEFAULT_RESULT;
        }

        @Override
		public ExecutabilityRule createExecutabilityRule() {
            return StartImportRule.INSTANCE;
        }
    }

    public static class StartImportRule implements ExecutabilityRule {

        public static final ExecutabilityRule INSTANCE = new StartImportRule();

        @Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> aSomeValues) {
            if (aComponent instanceof AJAXProgressComponent) {
                AJAXProgressComponent theComponent = (AJAXProgressComponent) aComponent;

                if (theComponent.getState() == AJAXProgressComponent.NO_PROGRESS) {
                    return ExecutableState.EXECUTABLE;
                }
            }
            return ExecutableState.EXECUTABLE;
        }
    }
}

