/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.webfolder;

import java.io.IOException;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.knowledge.wrap.DocumentJournalSupport;

/**
 * DownloadHandler with journaling facility
 * 
 * @author    <a href="mailto:tri@top-logic.com>Thomas Richter</a>
 */
public class JournalingDownloadHandler extends DownloadHandler {

	public JournalingDownloadHandler(InstantiationContext context, Config config) {
		super(context, config);
	}
	
	/**
	 * If super implementation delivers a valid input stream, this method creates an 
	 * appropriate journal entry before returning it.
	 * 
     * @see com.top_logic.knowledge.gui.layout.webfolder.DownloadHandler#getDownloadData(java.lang.Object)
     */
    @Override
	public BinaryDataSource getDownloadData(Object aPrepareResult) throws IOException {
		BinaryDataSource theData = super.getDownloadData(aPrepareResult);
		DownloadPackage thePack   = (DownloadPackage) aPrepareResult;

        if ((thePack.wrapper instanceof Document) && (theData != null)) {
            DocumentJournalSupport.journalDocumentEvent((Document) thePack.wrapper, null, "downloaded", thePack.version);
        }

		return (theData);
	}

}