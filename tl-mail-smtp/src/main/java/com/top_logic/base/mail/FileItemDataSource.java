
/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.mail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

import org.apache.commons.fileupload2.core.FileItem;

/**
 * A DataSource based on a FileItem.
 *
 * This is used to feed the FileItem (Web-MultiPart) into a (Mime)Mutipart.
 *
 * @author    Klaus Halfmann
 */
public class FileItemDataSource implements DataSource {

    /** Object wrapped by this class. */
	protected FileItem<?> item;

    /** Create a MultiPartDataSource wrapping the given Multi-Part.
     */
	public FileItemDataSource(FileItem<?> anItem) {
        item = anItem;
    }

    // Implemenatation of Interface DataSource    

    /** This method returns the MIME type as found in the (Web)MultiPart.
     */
    @Override
	public String getContentType() {
        return item.getContentType();
    } 
    
    
    /** Returns the InputStream type as found in the (Web)MultiPart.
     */
    @Override
	public InputStream getInputStream() throws IOException  {
        return item.getInputStream();
    }

    /** Return the _file_ name of the MultiPart.
     * 
     * The _name_ of a (Web)MultiPart is the name of the inputfield used to submit it.
     */
    @Override
	public String getName()  {
        return  item.getName(); 
    }
          
    /** Not supported.
     *
     * @throws IOException always.
     */
    @Override
	public OutputStream getOutputStream() throws IOException {
        throw new IOException("getOutputStream() is not supported by MultiPartDataSource");
    }

}

