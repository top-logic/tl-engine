/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.assistent.eva;

import java.util.Map;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.progress.AJAXProgressComponent;
import com.top_logic.layout.progress.ProgressInfo;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.assistent.AssistentComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class OutputComponent extends AJAXProgressComponent {

    private static final String IMPORT_PROGRESS_INFO = "IPI";

    /** 
     * Creates a {@link OutputComponent}.
     */
    public OutputComponent(InstantiationContext context, Config someAtts) throws ConfigurationException {
        super(context, someAtts);
    }

    @Override
	protected boolean supportsInternalModel(Object anObject) {
        return anObject instanceof ProgressInfo;
    }

    /**
     * @see com.top_logic.layout.progress.AJAXProgressComponent#progressFinished(com.top_logic.layout.DisplayContext, java.util.Map)
     */
    @Override
	protected void progressFinished(DisplayContext aSomeContext, Map aSomeArguments) {
        super.progressFinished(aSomeContext, aSomeArguments);

        if (this.getModel() != null) {
            AssistentComponent theComp   = AssistentComponent.getEnclosingAssistentComponent(this);
            LayoutComponent    theParent = theComp.getDialogParent();

            theComp.setData(IMPORT_PROGRESS_INFO, null);

            if (theParent != null) {
                theParent.invalidate();
            }
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

