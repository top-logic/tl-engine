/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa.util;

import java.io.InputStream;
import java.io.OutputStream;

import jakarta.activation.DataSource;

import com.top_logic.dsa.DataAccessProxy;

/** 
 * An activation {@link DataSource} based on an {@link DataAccessProxy} objects.
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 * @author    Andreas Röse
 */
public class DataAccessProxyDataSource implements DataSource {

    /** the {@link DataAccessProxy} that act as the data source */
    private DataAccessProxy dataAccessProxy;
    
    /** the name of the object this is represented by this dataSource. */
    private String name;
               
    /**
     * This {@link DataSource} must be initialized with a {@link DataAccessProxy},
     * 
     * @param aDataAccessProxy the object to act as {@link DataSource}. 
     */
    public DataAccessProxyDataSource(DataAccessProxy aDataAccessProxy){
        this.dataAccessProxy = aDataAccessProxy;
    }
    
    /**
     * A constructor to initialize the {@link DataSource} and use a different Name.
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
     * Gets the entry of the underlying {@link DataAccessProxy}.
     * If a {@link com.top_logic.dsa.DatabaseAccessException} is thrown by the {@link
     * DataAccessProxy}, null will be returned.
     * 
     * @return the entry of the  
     */
    @Override
	public InputStream getInputStream() {
		return dataAccessProxy.getEntry();
    }
    
    /**
     * Gets the full qualified name of the underlying {@link DataAccessProxy}.
     * e.g.: file-protocol://folder/filename
     *       mail-protocol://folder/message-id
     * 
     * KHA: the semantic of this fucntion may be wrong.
     *  
     * @return the full qualified name of the underlying {@link DataAccessProxy}.
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
     * Gets the entry output stream of the underlying {@link DataAccessProxy}.
     * 
     * If a {@link com.top_logic.dsa.DatabaseAccessException} is thrown by the {@link
     * DataAccessProxy}, null will be returned.
     * 
     * @return the entry output stream of the {@link DataAccessProxy}.
     * 
     */
    @Override
	public OutputStream getOutputStream(){
		return dataAccessProxy.getEntryOutputStream();
    }
        
    /**
     * Gets the held {@link DataAccessProxy} object.
     *  
     * @return the held {@link DataAccessProxy}.
     */
    public DataAccessProxy getDataAccessProxy(){
        return this.dataAccessProxy;    
    }

}

