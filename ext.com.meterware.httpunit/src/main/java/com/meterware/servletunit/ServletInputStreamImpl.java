package com.meterware.servletunit;
/********************************************************************************************************************
 * $Id: ServletInputStreamImpl.java 252573 2018-04-12 09:31:24Z bhu $
 *
 * Copyright (c) 2002, Russell Gold
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 *******************************************************************************************************************/
import java.io.ByteArrayInputStream;
import java.io.IOException;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;


/**
 * An implementation of the standard servlet input stream
 *
 * @author <a href="mailto:russgold@acm.org">Russell Gold</a>
 **/
class ServletInputStreamImpl extends ServletInputStream {

    private ByteArrayInputStream _baseStream;


    public ServletInputStreamImpl( byte[] messageBody ) {
        _baseStream = new ByteArrayInputStream( messageBody );
    }


    public int read() throws IOException {
        return _baseStream.read();
    }


	@Override
	public boolean isFinished() {
		return false;
	}


	@Override
	public boolean isReady() {
		return true;
	}


	@Override
	public void setReadListener(ReadListener readListener) {
		// Ignore.
	}

}
