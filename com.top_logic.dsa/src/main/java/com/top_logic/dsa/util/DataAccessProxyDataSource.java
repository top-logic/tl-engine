/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa.util;

import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

import com.top_logic.dsa.DataAccessProxy;

/** 
 * An activation DataSource based on an DataAccessProxy objects.
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 * @author    Andreas Röse
 */
public class DataAccessProxyDataSource implements DataSource {

    /** the DataAccessProxy that act as the data source */
    private DataAccessProxy dataAccessProxy;
    
    /** the name of the object this is represented by this dataSource. */
    private String name;
               
    /**
     * This DataSource must be initialized with a DataAccessProxy,
     * 
     * @param aDataAccessProxy the object to act as DataSource. 
     */
    public DataAccessProxyDataSource(DataAccessProxy aDataAccessProxy){
        this.dataAccessProxy = aDataAccessProxy;
    }
    
    /**
     * A constructor to initialize the DataSource and use a different Name.
     */
    public DataAccessProxyDataSource(DataAccessProxy aDataAccessProxy, String aName) {
        this(aDataAccessProxy);
        this.name = aName;
    }
        
	/**
	 * Gets the mime type of the underlying {@link DataAccessProxy}.
	 * 
	 * @see DataSource#getContentType()
	 */
    @Override
	public String getContentType() {
		return dataAccessProxy.getMimeType();
    }

    /**
     * Gets the entry of the underlying DataAccessProxy.
     * If a DatabaseAccessException is thrown by the DataAccessProxy,
     * null will be returned.
     * 
     * @return the entry of the  
     */
    @Override
	public InputStream getInputStream() {
		return dataAccessProxy.getEntry();
    }
    
    /**
     * Gets the full qualified name of the underlying DataAccessProxy.
     * e.g.: file-protocol://folder/filename
     *       mail-protocol://folder/message-id
     * 
     * KHA: the semantic of this fucntion may be wrong.
     *  
     * @return the full qualified name of the underlying DataAccessProxy.
     * 
     */
    @Override
	public String getName(){
        if (this.name != null){
            return this.name;
		} else {
			return this.name = dataAccessProxy.getDisplayName();
        } 
    }
    
    /**
     * Gets the entry output stream of the underlying DataAccessProxy.
     * 
     * If a DatabaseAccessException is thrown by the DataAccessProxy,
     * null will be returned.
     * 
     * @return the entry output stream of the DataAccessProxy.
     * 
     */
    @Override
	public OutputStream getOutputStream(){
		return dataAccessProxy.getEntryOutputStream();
    }
        
    /**
     * Gets the held DataAccessProxy object.
     *  
     * @return the held DataAccessProxy.
     */
    public DataAccessProxy getDataAccessProxy(){
        return this.dataAccessProxy;    
    }

}

