/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dsa.sql;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.top_logic.basic.sql.PreparedQuery;
import com.top_logic.basic.sql.SQLQuery;

/**
 * A Stream that closes a Query Object on closage of the Stream.
 *
 * This is needed in case you transparaently want to return a
 * Stream from an open resultset
 *
 * @author <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class QueryInputStream extends InputStream {

    /** The wrapped, actual outputstream */
    InputStream     in;

    /** The Query (ResultSet/Statement) the stream is based on */
    SQLQuery        query;

    /** 
     * Construct the Stream from aStream and aQuery.
     */
    public QueryInputStream(InputStream aStream, SQLQuery aQuery)  
    {   
        this.in    = aStream;
        this.query = aQuery;
    }

    /** 
     * Create an BinaryOutputStream from the open query at the given index.
     *
     *  @param index starting with 1 as usual with JDBC.
     *  @throws SQLException in case {@link ResultSet#getBinaryStream(int)} fails.
     */
    public QueryInputStream(SQLQuery aQuery, int index)  
        throws SQLException
    {   
        this(aQuery.getResultSet().getBinaryStream(index), aQuery);
    }

    /** Create an BinaryInputStream for the Statement and given index/length.
     *
     *  @param index  starting with 1 as susual with JDBC.
     *  @param length length of Data in the InputStream, meaning of JDBC is unclera here.
     *
     *  @throws SQLException in case extracion of Stream fails.
     */
    public QueryInputStream(PreparedQuery aQuery, InputStream anIn, int index, int length)  
        throws SQLException
    {   
        this.query = aQuery;
        this.in    = anIn;
        aQuery.getPreparedStatement().setBinaryStream(index, this, length);
    }

    /** Closeses the stream and the Query */
    @Override
	public void close() throws IOException {
        try  {
            in.close();
        }
        finally {
            try  {
                query.close();
            }
            catch (SQLException sqx)  {
                throw new IOException(sqx.getMessage());
            }
        }
    }
    
    /** Forward to the actual inputstream. */   
    @Override
	public int available() throws IOException  {
        return in.available();
    }

    
    /** Forward to the actual inputstream. */   
    @Override
	public void mark(int readlimit) {
        in.mark(readlimit);
    } 
    
    /** Forward to the actual inputstream. */   
    @Override
	public boolean markSupported() {
        return in.markSupported();
    } 
    
    /** Forward to the actual inputstream. */   
    @Override
	public int read() throws IOException  {
        return in.read();
    }
    
    /** Forward to the actual inputstream. */   
    @Override
	public int read(byte[] b) throws IOException  {
        return in.read(b);
    }
    
    /** Forward to the actual inputstream. */   
    @Override
	public int read(byte[] b, int off, int len) throws IOException  {
        return in.read(b,off,len);
    }
    
    /** Forward to the actual inputstream. */   
    @Override
	public void reset() throws IOException  {
        in.reset();
    }
    
    /** Forward to the actual inputstream. */   
    @Override
	public long skip(long n) throws IOException  {
        return in.skip(n);
    }
    
}
