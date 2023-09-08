/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mig.html;

import javax.servlet.jsp.JspWriter;

/**
 * A JspWriter that is based on a Stringbuffer suiteable for Testing.
 *
 * @author  <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class BufferedJspWriter extends JspWriter {

    /** The underlying buffer */
    protected StringBuffer buf;
    
    /** Default CTor, initialises the embedded StringBuffer */
    public BufferedJspWriter()  {
        super(0,true);
        buf = new StringBuffer();
    }
        
    /** CTor to initialise the embedded StringBuffer to given size */
    public BufferedJspWriter(int size)  {
        super(0,true);
        buf = new StringBuffer(size);
    }

   @Override
public String toString() {
        return buf.toString();
    }


    // implemenatation of JspWriter
    
    @Override
	public void clear()  {
        buf.setLength(0);
    }

    @Override
	public void clearBuffer()  {
        buf.setLength(0);
    }
          
    @Override
	public void close()  {
        // nothing to do here
    }

    @Override
	public void flush()  {
        // nothing to do here
    }
    
    @Override
	public int getRemaining()  {
        return 0;   // claim we are unbuffered.
    }

    @Override
	public void newLine()  {
        buf.append('\n');
    }

    @Override
	public void print(boolean b) {
        buf.append(b);
    }
    
    @Override
	public void print(char c) {
        buf.append(c);
    }

    @Override
	public void print(char[] s)  {
        buf.append(s);
    }

    @Override
	public void print(double d)  {
        buf.append(d);
    }

    @Override
	public void print(float f)  {
        buf.append(f);
    }

    @Override
	public void print(int i)  {
        buf.append(i);
    }

    @Override
	public void print(long l)  {
        buf.append(l);
    }

    @Override
	public void print(Object obj)  {
        buf.append(obj);
    }

    @Override
	public void print(String s)  {
        buf.append(s);
    }

    @Override
	public void println()  {
        buf.append('\n');
    }

    @Override
	public void println(boolean x) {
        buf.append(x);
        buf.append('\n');
    } 

    @Override
	public void println(char x)  {
        buf.append(x);
        buf.append('\n');
    }

    @Override
	public void println(char[] x)  {
        buf.append(x);
        buf.append('\n');
    }

    @Override
	public void println(double x)  {
        buf.append(x);
        buf.append('\n');
    }

    @Override
	public void println(float x)  {
        buf.append(x);
        buf.append('\n');
    }

    @Override
	public void println(int x)  {
        buf.append(x);
        buf.append('\n');
    }

    @Override
	public void println(long x)  {
        buf.append(x);
        buf.append('\n');
    }

    @Override
	public void println(Object x)  {
        buf.append(x);
        buf.append('\n');
    }

    @Override
	public void println(String x)  {
        buf.append(x);
        buf.append('\n');
    }
    
    @Override
	public void write(char[] cbuf, int off, int len) {
        buf.append(cbuf, off, len);
    }
}
