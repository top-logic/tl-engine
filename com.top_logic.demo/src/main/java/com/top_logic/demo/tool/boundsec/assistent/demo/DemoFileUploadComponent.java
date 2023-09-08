/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.tool.boundsec.assistent.demo;

import java.io.IOException;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.mig.html.layout.FileUploadComponent;
import com.top_logic.tool.boundsec.assistent.AssistentComponent;

/**
 * For testing!
 * 
 * @author    <a href="mailto:skr@top-logic.com">Sylwester Kras</a>
 */
public class DemoFileUploadComponent extends FileUploadComponent {

    public DemoFileUploadComponent(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
    }

    @Override
	public void receiveFile(BinaryData aFileItem) throws IOException {
        
		String theName = aFileItem.getName();
        AssistentComponent theAssistent = AssistentComponent.getEnclosingAssistentComponent(this);
        theAssistent.setData("FILE_NAME", theName);
        
        if(! theName.endsWith("doc")){
            theAssistent.setData(AssistentComponent.SHOW_FAILTURE,Boolean.TRUE);
        }
    }
}
