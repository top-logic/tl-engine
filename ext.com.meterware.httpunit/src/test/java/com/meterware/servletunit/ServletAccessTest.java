package com.meterware.servletunit;
/********************************************************************************************************************
 * $Id: ServletAccessTest.java 107617 2012-01-03 09:51:15Z jst $
 *
 * Copyright (c) 2001-2004, Russell Gold
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
import com.meterware.httpunit.WebResponse;

import javax.servlet.http.HttpServlet;


/**
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class ServletAccessTest extends ServletTestCase {

    public ServletAccessTest( String name ) {
        super( name );
    }


    public void testServletParameters() throws Exception {
        ServletUnitClient client = newClient();
        InvocationContext ic = client.newInvocation( "http://localhost/SimpleServlet" );
        assertNull( "init parameter 'gender' should be null", ic.getServlet().getServletConfig().getInitParameter( "gender" ) );
        assertEquals( "init parameter via config", "red", ic.getServlet().getServletConfig().getInitParameter( "color" ) );
        assertEquals( "init parameter directly", "12", ((HttpServlet) ic.getServlet()).getInitParameter( "age" ) );
        ic.getServlet().service( ic.getRequest(), ic.getResponse() );

        WebResponse wr = client.getResponse( ic );
        assertEquals( "Servlet response", "the desired content", wr.getText() );
    }


}
