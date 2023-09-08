/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.dsa;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.fileupload.FileItem;

import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.dsa.DatabaseAccessException;


/**
 * Handler for multipart-uploads
 * 
 * @author    <a href="mailto:hbo@top-logic.com">Holger Borchard</a>
  */
public interface UploadHandler {
    
    /**
    * Create a new entry with the given DataAccessProxy that represents the given FileItem.
    *
    * @param    aProxy       the DataAccessProxy
    * @param    anItem       an Instances of FileItem, that contains the uploaded client-file
    *
    * @throws   DatabaseAccessException if the createEntry failed
    * @throws   IOException             if the createEntry failed 
    *                                
    */   
    public DataAccessProxy createEntry(DataAccessProxy aProxy, FileItem anItem)
                                  throws IOException, DatabaseAccessException;

	/**
	 * Create a new entry with the given DataAccessProxy with the given name and the contents of a
	 * stream.
	 * 
	 * @param aProxy
	 *        the DataAccessProxy
	 * @param aStream
	 *        contains the content of the uploaded file. must not be null
	 * @param aFileName
	 *        the name of the entry. If this name contains file separators, only the last part of
	 *        the name is used
	 * 
	 * @return the DataAccessProxy of the uploaded file
	 * 
	 * @throws DatabaseAccessException
	 *         if the createEntry failed
	 * @throws IOException
	 *         if the createEntry failed
	 * 
	 */
    public DataAccessProxy createEntry(DataAccessProxy aProxy, InputStream aStream,
                              String aFileName) throws DatabaseAccessException, IOException;
    
    /**
    * Put an entry with the given proxy.
    * The content of the entry is determine by the given FileItem.
    * 
    * @param   aProxy  the DataAccessProxy
    * @param   anItem a FileItem that represents the content of the entry.
    * 
    * @return  true, if the update takes place
    * 
    * @throws   DatabaseAccessException if the putEntry-method failed
    * @throws   IOException             if the putEntry-method failed 
    */                            
    public boolean putEntry(DataAccessProxy aProxy, FileItem anItem)
                                        throws IOException, DatabaseAccessException;

	/**
	 * Put an entry with the given proxy. The content of the entry is determine by the given
	 * InputStream.
	 * 
	 * @param aProxy
	 *        the DataAccessProxy
	 * @param aStream
	 *        some InputStream that represents the content of the entry. must not be
	 *        <code>null</code> .
	 * 
	 * 
	 * @throws DatabaseAccessException
	 *         if the putEntry-method failed
	 * @throws IOException
	 *         if the putEntry-method failed
	 */
    public boolean putEntry(DataAccessProxy aProxy, InputStream aStream)
                                      throws DatabaseAccessException, IOException ;
    
    /**
    * Try to update a child entry of the given DataAccessProxy.
    * 
    * @param   aProxy the DataAccessProxy (container)
    * @param   anItem aFileItem the contains the uploaded File 
    * 
    * @throws   DatabaseAccessException if the putEntry-method failed
    * @throws   IOException             if the putEntry-method failed  
    */
    public  boolean putEntryInContainer(DataAccessProxy aProxy, FileItem anItem) 
                                              throws DatabaseAccessException, IOException;
    
    /**
    * Invoke the creation of a new directory.
    * 
    * @param   aProxy the DataAccessProxy
    * @param   aDirName the name of new directory 
    * @throws DatabaseAccessException if creation failed
    */
    public void mkdir(DataAccessProxy aProxy, String aDirName)
                                                throws DatabaseAccessException;
                                                
    
}
