/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.assistent.eva;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.tool.boundsec.assistent.ValidatingUploadHandler;

/**
 * Error component to be displayed when the {@link ValidatingUploadHandler} 
 * has found at least one error in the uploaded file. 
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ErrorComponent extends ProcessingComponent {

    /** 
     * Creates a {@link ErrorComponent}.
     */
    public ErrorComponent(InstantiationContext context, Config aSomeAtts) throws ConfigurationException {
        super(context, aSomeAtts);
    }

}

