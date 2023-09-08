/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io;

// InputStream, Reader, BufferedReader, InputStreamReader, IOException
import java.io.IOException;
import java.io.OutputStream;

/**
 * A Stream that writes two two other OutputStreams at once.
 *
 * @author <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TOutputStream extends OutputStream {

    /** The first of the actual outputstream */
    OutputStream out1;

    /** The second of the actual outputstream */
    OutputStream out2;


    /** 
     * Construct the T-junction from two given OutputStreams.
     */
    public TOutputStream(OutputStream aOut1, OutputStream aOut2)  
    {   
        this.out1 = aOut1;
        this.out2 = aOut2;
    }

    /** forwards to close() on the two actual outputstreams. */
    @Override
	public void close() throws IOException {
        try  {
            out1.close();
        }
        finally {
            out2.close();
        }
    }
    
    /** Forward to the two actual outputstreams. */   
    @Override
	public void flush() throws IOException {
        try  {
            out1.flush();
        }
        finally {
            out2.flush();
        }
    }

    /** Forward to the two actual outputstreams. */   
    @Override
	public void write(byte[] b) throws IOException {
        out1.write(b);
        out2.write(b);
    }

    /** Forward to the two actual outputstreams. */   
    @Override
	public void write(byte[] b, int off, int len) throws IOException {
        out1.write(b, off, len);
        out2.write(b, off, len);
    }

    /** Forward to the two actual outputstreams. */   
    @Override
	public void write(int b) throws IOException {
        out1.write(b);
        out2.write(b);
    }
}
