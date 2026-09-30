/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.dataImport.layout;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.progress.AJAXProgressComponent;

/**
 * The DataImportProgressStepComponent is a progress step of the DataImportAssistant.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class DataImportProgressStepComponent extends AJAXProgressComponent {

    /**
     * Creates a new instance of this class.
     */
    public DataImportProgressStepComponent(InstantiationContext context, Config aAtts) throws ConfigurationException {
        super(context, aAtts);
    }

    @Override
	protected void becomingVisible() {
        resetState();
        super.becomingVisible();
    }

}
