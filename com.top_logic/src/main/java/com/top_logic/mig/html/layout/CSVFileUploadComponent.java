/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.top_logic.basic.Settings;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.tool.boundsec.assistent.AssistentComponent;

/**
 * Upload a CSV-File as Temp-file assuming this is part of an Assistant.
 * 
 * @author    <a href=mailto:kha@top-logic.com>kha</a>
 */
public class CSVFileUploadComponent extends FileUploadComponent  {

    /** The default extensions wil will accept */
    protected static final String[] DEFAULT_EXTENSIONS = new String[] {".csv", ".zip"};

    /** Name where uploaded file is saved in AssistentComponent */
    public static final String FILE_ATTR = CSVFileUploadComponent.class.getName() + ".FILE";
    
    /** 
     * Create a new CSVFileUploadComponent from XML.
     */
    public CSVFileUploadComponent(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);

        if (StringServices.isEmpty(this.allowedExts)) {
            this.allowedExts = DEFAULT_EXTENSIONS;
        }
    }

    /** 
     * Save aFileItem into a tempFile and store it at an enclosing AssistentComponent.
     * 
     * Try to remove the file in case the wizard is canceld etc. 
     */
    @Override
	public void receiveFile(BinaryData aFileItem) throws IOException {
        
        AssistentComponent assi = AssistentComponent.getEnclosingAssistentComponent(this);
        if (assi == null) {
            throw new IOException("This componenet must be child of an AssistentComponent");
        }
		String cleanName = aFileItem.getName();
		File tmpFile = File.createTempFile("FileUpload", cleanName, Settings.getInstance().getTempDir());
		try (InputStream input = aFileItem.getStream()) {
			FileUtilities.copyToFile(input, tmpFile);
		}
        
        assi.setData(FILE_ATTR, tmpFile);
        assi.setData(DATE_FIELD, getDate());
    }

}
