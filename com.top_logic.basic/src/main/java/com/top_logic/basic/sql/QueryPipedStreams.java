/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.sql.SQLException;

import com.top_logic.basic.Logger;

/**
 * This convert an OutputStream into an InputStream for a PreparedQuery.
 *
 * Some people would like to use an OutputStream to write to a Database BLOB 
 * whereas a {@link java.sql.PreparedStatement} only allows an InputStream. 
 * This class therefore
 * uses a Thread to allow the Prepared statement to use an InputStream
 * and allows the main-Thread to use an OutPutStream:<pre>
 *
 *    MainThread             QueryPipedStreams-Thread
 * (Piped)OutPutStream          (Piped)InputStream
 *       |                            |
 *   T   | -> write ....... ......  read 
 *   i   |                            |
 *   m   | -> close ....... ....... close -> PreparedStatement.executeUpdate ...
 *   e   |                            |
 *       V                            V
 * </pre>
 *
 * @author <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class QueryPipedStreams extends Thread {

    /** Encapsulated the Prepared Statement */
    protected PreparedQuery        pQuery;
    
    /** OutputStream handed to main (calling) Thread */
    protected PipedOutputStream    pOut;
    
    /** InputStream use by Prepared Statement */
    protected PipedInputStream     pIn;
    
    /** for PreparedStatement setBinaryStream() */
    protected int                  index;

    /** for PreparedStatement setBinaryStream() */
    protected int                  length;
    
    /** An exception that was caught internally.
        It is revealed on close() of the OutputStream returned. */
    protected Throwable             caughInTheAct;

    /** Construct the Thread from PreparedQuery and Paramters needed for {@link java.sql.PreparedStatement#setBinaryStream}.
     *
     * @param theIndex  needed by {@link java.sql.PreparedStatement#setBinaryStream}
     * @param theLength needed by {@link java.sql.PreparedStatement#setBinaryStream}
     *
     * @throws IOException in case construction of PipedStreams fails.
     */
    public QueryPipedStreams(PreparedQuery theQuery, int theIndex, int theLength) 
        throws IOException 
    {   
        super("QueryPipedStreams [" + Thread.currentThread().getName() + "]");
        setDaemon(true);
        pQuery = theQuery;
        index  = theIndex;
        length = theLength;
        
        pOut   = new JoiningPipedOutputStream();
        pIn    = new PipedInputStream(pOut);
    }
    
    /** Return the OutputStream and start the Thread using the attached InputStream. */
    public OutputStream getOutputStream()
    {
        this.start();
        return pOut;
    }
    
    /** Main Thread function */
    @Override
	public void run() {
        
        try {
            pQuery.getPreparedStatement().setBinaryStream(index, pIn, length);
			Thread.yield(); // Give OutputStream time to start sending data.
            // Will (hopefully) block until InputStream is closed via OutputStream
            pQuery.executeUpdate();
        }
        catch (SQLException sqlx) {
            this.caughInTheAct = sqlx;
        }
        finally {
            try {
                pQuery.close();
            }
            catch (SQLException sqlx) {
                Logger.error("close() in run() failed", sqlx, this);
            }
        }
    }

	/**
	 * Hook for adding cleanup actions that must be called after the
	 * {@link #getOutputStream()} has been closed.
	 */
    protected void close() throws InterruptedException {
    	this.join();
    }
    
    /** Inner Subclass of OutputStream that will defer the close until the Thread is done */
    public class JoiningPipedOutputStream extends PipedOutputStream {
    
        /** Close superclass and then wait for the Thread to finish() */
        @Override
		public void close() throws IOException {
            
            Throwable   finalEx = null;            
        
            try {
                super.close();
                QueryPipedStreams.this.close();
            }
            catch (Exception ex) {
                finalEx = ex;
            }
            if (finalEx == null) {
                finalEx = QueryPipedStreams.this.caughInTheAct;
            }

            if (finalEx != null) {
                if (finalEx instanceof IOException) {
                    throw (IOException) finalEx;
                }
                
                throw new IOException(finalEx.getMessage());
            }
        }
    }
}
