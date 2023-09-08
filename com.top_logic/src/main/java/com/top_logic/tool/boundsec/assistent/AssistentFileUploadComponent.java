/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.assistent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.top_logic.basic.Settings;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.mig.html.layout.FileUploadComponent;

/**
 * This component uploads a single File and stores it into the {@link AssistentComponent}s
 * data.
 *
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class AssistentFileUploadComponent extends FileUploadComponent {

	/** The file data in the assistent controller. */
    public static final String FILE = "FILE";

	/**
	 * Creates a {@link AssistentFileUploadComponent}.
	 */
    public AssistentFileUploadComponent(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
    }

    @Override
	public void receiveFile(BinaryData aFileItem) throws IOException {
        AssistentComponent theAssistant = AssistentComponent.getEnclosingAssistentComponent(this);
		String theFilename = aFileItem.getName();
        int theIndex = theFilename.lastIndexOf(".");
        String theExt = theIndex < 0 ? "" : theFilename.substring(theIndex);

        File theFile = File.createTempFile("assistantUpload", theExt, Settings.getInstance().getTempDir());
		try (InputStream theIS = aFileItem.getStream()) {
			try (FileOutputStream theOS = new FileOutputStream(theFile)) {
				StreamUtilities.copyStreamContents(theIS, theOS);
				theOS.flush();
			}
		}
        theAssistant.setData(FILE, theFile);
    }
}
