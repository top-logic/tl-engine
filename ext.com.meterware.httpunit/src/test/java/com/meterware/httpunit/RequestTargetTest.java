package com.meterware.httpunit;
/********************************************************************************************************************
* $Id: RequestTargetTest.java 107617 2012-01-03 09:51:15Z jst $
*
* Copyright (c) 2000-2004, Russell Gold
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
import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Tests to ensure the proper handling of the target attribute.
 **/
public class RequestTargetTest extends HttpUnitTest {

    public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
    }
    
    
    public static Test suite() {
        return new TestSuite( RequestTargetTest.class );
    }


    public RequestTargetTest( String name ) {
        super( name );
    }


    public void setUp() throws Exception {
        super.setUp();
        _wc = new WebConversation();
    }


    public void testDefaultLinkTarget() throws Exception {
        defineWebPage( "Initial", "Here is a <a href=\"SimpleLink.html\">simple link</a>." );

        WebRequest request = new GetMethodWebRequest( getHostPath() + "/Initial.html" );
        assertEquals( "new link target", WebRequest.TOP_FRAME, request.getTarget() );
        
        WebResponse response = _wc.getResponse( request );
        assertEquals( "default response target", WebRequest.TOP_FRAME, response.getFrameName() );
        WebLink link = response.getLinks()[0];
        assertEquals( "default link target", WebRequest.TOP_FRAME, link.getTarget() );
    }
	
	
    public void testExplicitLinkTarget() throws Exception {
        defineWebPage( "Initial", "Here is a <a href=\"SimpleLink.html\" target=\"subframe\">simple link</a>." );

        WebLink link = _wc.getResponse( getHostPath() + "/Initial.html" ).getLinks()[0];
        assertEquals( "explicit link target", "subframe", link.getTarget() );
        assertEquals( "request target", "subframe", link.getRequest().getTarget() );
    }
	
	
    public void testInheritedLinkTarget() throws Exception {
        defineResource( "Start.html",
                        "<HTML><HEAD><TITLE>Initial</TITLE></HEAD>" +
                        "<FRAMESET cols=\"50%,50%\">" +
                        "    <FRAME src=\"Initial.html\" name=\"red\">" +
                        "    <FRAME name=\"blue\">" +
                        "</FRAMESET></HTML>" );
        defineWebPage( "Initial", "Here is a <a href=\"SimpleLink.html\" target=\"blue\">simple link</a>." );
        defineWebPage( "SimpleLink", "Here is <a href=\"Initial.html\">another simple link</a>." );

        _wc.getResponse( getHostPath() + "/Start.html" );
        WebLink link = _wc.getFrameContents( "red" ).getLinks()[0];
        assertEquals( "explicit link target", "blue", link.getTarget() );
        assertEquals( "request target", "blue", link.getRequest().getTarget() );

        WebResponse response = _wc.getResponse( link.getRequest() );
        assertEquals( "response target", "blue", response.getFrameName() );
        link = response.getLinks()[0];
        assertEquals( "inherited link target", "blue", link.getTarget() );
    }
	
	
    public void testInheritedLinkTargetInTable() throws Exception {
        defineResource( "Start.html",
                        "<HTML><HEAD><TITLE>Initial</TITLE></HEAD>" +
                        "<FRAMESET cols=\"50%,50%\">" +
                        "    <FRAME src=\"Initial.html\" name=\"red\">" +
                        "    <FRAME name=\"subframe\">" +
                        "</FRAMESET></HTML>" );
        defineWebPage( "Initial", "Here is a <a href=\"SimpleLink.html\" target=\"subframe\">simple link</a>." );
        defineWebPage( "SimpleLink", "Here is <table><tr><td><a href=\"Initial.html\">another simple link</a>.</td></tr></table>" );

        WebLink link = _wc.getResponse( getHostPath() + "/Start.html" ).getSubframeContents( "red" ).getLinks()[0];
        assertEquals( "explicit link target", "subframe", link.getTarget() );
        assertEquals( "request target", "subframe", link.getRequest().getTarget() );

        WebResponse response = _wc.getResponse( link.getRequest() );
        assertEquals( "response target", "subframe", response.getFrameName() );
        WebTable table = response.getTables()[0];
        TableCell cell = table.getTableCell(0,0);
        link = cell.getLinks()[0];
        assertEquals( "inherited link target", "subframe", link.getTarget() );
    }
	
	
    public void testDefaultFormTarget() throws Exception {
        defineWebPage( "Initial", "Here is a simple form: " +
                                  "<form method=POST action = \"/servlet/Login\"><B>" +
                                  "<input type=\"checkbox\" name=first>Disabled" +
                                  "<br><Input type=submit value = \"Log in\">" +
                                  "</form>" );

        WebResponse response = _wc.getResponse( getHostPath() + "/Initial.html" );
        assertEquals( "Num forms in page", 1, response.getForms().length );
        WebForm form = response.getForms()[0];
        assertEquals( "default form target", WebRequest.TOP_FRAME, form.getTarget() );
    }

	
    public void testExplicitPostFormTarget() throws Exception {
        defineWebPage( "Initial", "Here is a simple form: " +
                                  "<form method=POST action = \"/servlet/Login\" target=\"subframe\"><B>" +
                                  "<input type=\"checkbox\" name=first>Disabled" +
                                  "<br><Input type=submit value = \"Log in\">" +
                                  "</form>" );

        WebForm form = _wc.getResponse( getHostPath() + "/Initial.html" ).getForms()[0];
        assertEquals( "explicit form target", "subframe", form.getTarget() );
        assertEquals( "request target", "subframe", form.getRequest().getTarget() );
    }
	
	
    public void testExplicitGetFormTarget() throws Exception {
        defineWebPage( "Initial", "Here is a simple form: " +
                                  "<form method=GET action = \"/servlet/Login\" target=\"subframe\"><B>" +
                                  "<input type=\"checkbox\" name=first>Disabled" +
                                  "<br><Input type=submit value = \"Log in\">" +
                                  "</form>" );

        WebForm form = _wc.getResponse( getHostPath() + "/Initial.html" ).getForms()[0];
        assertEquals( "explicit form target", "subframe", form.getTarget() );
        assertEquals( "request target", "subframe", form.getRequest().getTarget() );
    }

	
    public void testInheritedFormTarget() throws Exception {
        defineResource( "Start.html",
                        "<HTML><HEAD><TITLE>Initial</TITLE></HEAD>" +
                        "<FRAMESET cols=\"50%,50%\">" +
                        "    <FRAME src=\"Initial.html\" name=\"red\">" +
                        "    <FRAME name=\"subframe\">" +
                        "</FRAMESET></HTML>" );
        defineWebPage( "Initial", "Here is a <a href=\"SimpleLink.html\" target=\"subframe\">simple link</a>." );
        defineWebPage( "SimpleLink", "Here is a simple form: " +
                                     "<form method=GET action = \"/servlet/Login\" target=\"subframe\"><B>" +
                                     "<input type=\"checkbox\" name=first>Disabled" +
                                     "<br><Input type=submit value = \"Log in\">" +
                                     "</form>" );

        WebLink link = _wc.getResponse( getHostPath() + "/Start.html" ).getSubframeContents( "red" ).getLinks()[0];
        assertEquals( "explicit link target", "subframe", link.getTarget() );
        assertEquals( "request target", "subframe", link.getRequest().getTarget() );

        WebResponse response = _wc.getResponse( link.getRequest() );
        assertEquals( "response target", "subframe", response.getFrameName() );
        WebForm form = response.getForms()[0];
        assertEquals( "inherited form target", "subframe", form.getTarget() );
    }
	
	
    private WebConversation _wc;
}
