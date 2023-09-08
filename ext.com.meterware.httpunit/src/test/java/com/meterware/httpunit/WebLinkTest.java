package com.meterware.httpunit;
/********************************************************************************************************************
* $Id: WebLinkTest.java 107617 2012-01-03 09:51:15Z jst $
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
import junit.framework.TestSuite;


/**
 * Tests for the WebLink class.
 *
 * @author <a href="mailto:russgold@httpunit.org>Russell Gold</a>
 * @author <a href="mailto:bx@bigfoot.com>Benoit Xhenseval</a>
 **/
public class WebLinkTest extends HttpUnitTest {

    public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
    }


    public static TestSuite suite() {
        return new TestSuite( WebLinkTest.class );
    }


    public WebLinkTest( String name ) {
        super( name );
    }


    public void setUp() throws Exception {
        super.setUp();
        defineResource( "SimplePage.html",
                        "<html><head><title>A Sample Page</title></head>\n" +
                        "<body>This has no forms but it does\n" +
                        "have <a href='/other.html#middle' id='activeID'>an <b>active</b> link</A>\n" +
                        " and <a name=here>an anchor</a>\n" +
                        "<a href='basic.html' name=\"nextLink\"><IMG SRC=\"/images/arrow.gif\" ALT=\"Next -->\" WIDTH=1 HEIGHT=4></a>\n" +
                        "<a href='another.html' name='myLink'>some text</a>\n" +
                        "</body></html>\n" );

        WebConversation wc = new WebConversation();
        _simplePage = wc.getResponse( getHostPath() + "/SimplePage.html" );
    }


    public void testFindNoLinks() throws Exception {
        defineResource( "NoLinks.html", "<html><head><title>NoLinks</title></head><body>No links at all</body></html>" );
        WebConversation wc = new WebConversation();

        WebLink[] links = wc.getResponse( getHostPath() + "/NoLinks.html" ).getLinks();
        assertNotNull( links );
        assertEquals( 0, links.length );
    }
    
    
    /**
     * test for Bug report 1908117 by firebird74
     * http://www.w3.org/Addressing/URL/url-spec.html
		 * says
     *    Spaces and control characters in URLs must be escaped for transmission in HTTP
     * so %20 ist used for space/blank
     * trying out 
     * <html>
		 * <body>
     * <a href="http://www.bit plan.com">BITlan blank link</a>
     * </body>
     * </html>
     * in different browers gives different results.
     * Firefox 2.0.0.13 will not show a link at all
     * Internet Explorer 6.0 will show a link that has http://www.bit%20plan.com
     * as it's target
     * @throws Exception
     */
    public void testLinkToURLWithBlanks() throws Exception {
      defineWebPage( "urlwithblank", 
      		"<a href='http://bla.fasel.com/a/b/lorem ipsum.pdf'>link with blank</a>"+
      		"<a href='http://bla.fasel.com/a/b/lorem%20ipsum.pdf'>link with blank</a>");
    	WebConversation wc = new WebConversation();
      WebResponse resp = wc.getResponse( getHostPath() + "/urlwithblank.html" );
    	WebLink [] webLinks = resp.getLinks();
    	assertTrue("There should be two link but there are "+webLinks.length,webLinks.length==2);
    	// TODO this is what we expect in fact
    	// String blankLink1="http://bla.fasel.com/a/b/lorem%20ipsum.pdf";
    	String blankLink1="http://bla.fasel.com/a/b/loremipsum.pdf";
    	String blankLink2="http://bla.fasel.com/a/b/lorem%20ipsum.pdf";
    	WebLink link1 = webLinks[0];
    	assertTrue("the blank in the link1 should be converted but we got '"+link1.getURLString()+"'",link1.getURLString().equals(blankLink1));
    	WebLink link2 = webLinks[1];
    	assertTrue("the blank %20 in the link2 should not be converted but we got '"+link2.getURLString()+"'",link2.getURLString().equals(blankLink2));    	
    }


    public void testLinks() throws Exception {
        WebLink[] links = _simplePage.getLinks();
        assertNotNull( "Found no links", links );
        assertEquals( "number of links in page", 3, links.length );
    }


    public void testEmbeddedFontTags() throws Exception {
        defineResource( "FontPage.html",
                        "<html><head><title>A Sample Page</title></head>\n" +
                        "<table><tr><td><a href='/other.html' id='activeID'><font face='Arial'>an <b>active</b> link</font></A></td>\n" +
                        "<td><a href='basic.html' name=\"nextLink\"><IMG SRC=\"/images/arrow.gif\" ALT=\"Next -->\" WIDTH=1 HEIGHT=4></a></td>\n" +
                        "<td><a href='another.html' name='myLink'>some text</a></td>\n" +
                        "</tr></table></body></html>\n" );
        WebConversation wc = new WebConversation();
        WebResponse wr = wc.getResponse( getHostPath() + "/FontPage.html" );
        assertEquals( "Number of links found", 3, wr.getLinks().length );
    }


    public void testLinkRequest() throws Exception {
        WebLink link = _simplePage.getLinks()[0];
        WebRequest request = link.getRequest();
        assertTrue( "Should be a get request", request instanceof GetMethodWebRequest );
        assertEquals( getHostPath() + "/other.html", request.getURL().toExternalForm() );
    }


    public void testLinkReference() throws Exception {
        WebLink link = _simplePage.getLinks()[0];
        assertEquals( "URLString", "/other.html", link.getURLString() );
    }


    public void testGetLinkByText() throws Exception {
        WebLink link = _simplePage.getLinkWith( "no link" );
        assertNull( "Non-existent link should not have been found", link );
        link = _simplePage.getLinkWith( "an active link" );
        assertNotNull( "an active link was not found", link );
        assertEquals( "active link URL", getHostPath() + "/other.html", link.getRequest().getURL().toExternalForm() );

        link = _simplePage.getLinkWithImageText( "Next -->" );
        assertNotNull( "the image link was not found", link );
        assertEquals( "image link URL", getHostPath() + "/basic.html", link.getRequest().getURL().toExternalForm() );

        HttpUnitOptions.setImagesTreatedAsAltText( true );
        link = _simplePage.getLinkWith( "Next -->" );
        assertNotNull( "the image link was not found", link );
        assertEquals( "image link URL", getHostPath() + "/basic.html", link.getRequest().getURL().toExternalForm() );

        HttpUnitOptions.setImagesTreatedAsAltText( false );
        link = _simplePage.getLinkWith( "Next -->" );
        assertNull( "the image link was found based on its hidden alt attribute", link );
    }


    public void testCustomMatching() throws Exception {
        WebLink link = _simplePage.getFirstMatchingLink( WebLink.MATCH_URL_STRING, "nothing" );
        assertNull( "Non-existent link should not have been found", link );

        link = _simplePage.getFirstMatchingLink( WebLink.MATCH_URL_STRING, "/other.html" );
        assertNotNull( "an active link was not found", link );
        assertEquals( "active link text", "an active link", link.getText() );

        link = _simplePage.getFirstMatchingLink( WebLink.MATCH_URL_STRING, "basic" );
        assertNotNull( "the image link was not found", link );
        assertEquals( "image link URL", getHostPath() + "/basic.html", link.getRequest().getURL().toExternalForm() );

        WebLink[] links = _simplePage.getMatchingLinks( WebLink.MATCH_URL_STRING, "other.ht" );
        assertNotNull( "No link array returned", links );
        assertEquals( "Number of links with URL containing 'other.ht'", 2, links.length );
    }


    public void testGetLinkByIDAndName() throws Exception {
        WebLink link = _simplePage.getLinkWithID( "noSuchID" );
        assertNull( "Non-existent link should not have been found", link );

        link = _simplePage.getLinkWithID( "activeID" );
        assertNotNull( "an active link was not found", link );
        assertEquals( "active link URL", getHostPath() + "/other.html", link.getRequest().getURL().toExternalForm() );

        link = _simplePage.getLinkWithName( "nextLink" );
        assertNotNull( "the image link was not found", link );
        assertEquals( "image link URL", getHostPath() + "/basic.html", link.getRequest().getURL().toExternalForm() );
    }


    public void testFragmentIdentifier() throws Exception {
        WebLink link = (WebLink) _simplePage.getElementWithID( "activeID" );
        assertNotNull( "the active link was not found", link );
        assertEquals( "fragment identifier #1", "middle", link.getFragmentIdentifier() );

        assertEquals( "fragment identifier #2", "", _simplePage.getLinks()[1].getFragmentIdentifier() );
    }


    public void testLinkText() throws Exception {
        WebLink link = _simplePage.getLinks()[0];
        assertEquals( "Link text", "an active link", link.getText() );
    }


    public void testLinkImageAsText() throws Exception {
        WebConversation wc = new WebConversation();
        defineWebPage( "HasImage", "<a href='somwhere.html' >\r\n<img src='blah.gif' alt='Blah Blah' >\r\n</a>" );

        WebResponse initialPage = wc.getResponse( getHostPath() + "/HasImage.html" );
        WebLink link = initialPage.getLinks()[0];
        assertEquals( "Link text", "", link.getText().trim() );
        initialPage.getLinkWithImageText("Blah Blah");
    }


    /**
     * @see [ 1156972 ] isWebLink doesn't recognize all anchor tags
     * for different opinion on weblink count.
     * @throws Exception
     */
    public void testLinkFollowing() throws Exception {
        WebConversation wc = new WebConversation();
        defineWebPage( "Initial", "Go to <a href=\"Next.html\">the next page.</a> <a name=\"bottom\">Bottom</a>" );
        defineWebPage( "Next", "And go back to <a href=\"Initial.html#Bottom\">the first page.</a>" );

        WebResponse initialPage = wc.getResponse( getHostPath() + "/Initial.html" );
        assertEquals( "Num links in initial page", 1, initialPage.getLinks().length );
        WebLink link = initialPage.getLinks()[0];

        WebResponse nextPage = wc.getResponse( link.getRequest() );
        assertEquals( "Title of next page", "Next", nextPage.getTitle() );
        assertEquals( "Num links in next page", 1, nextPage.getLinks().length );
        link = nextPage.getLinks()[0];

        link.click();
        assertEquals( "Title of next page", "Initial", wc.getFrameContents( link.getTarget() ).getTitle() );
    }
    
    /**
     * test for bug report
     * [ 1232591 ] getTarget() gives "_top" even if target is not present
     * by Rifi
     */
    public void testGetTarget_top() throws Exception {
      WebConversation wc = new WebConversation();
      defineWebPage( "target", "<a href=\"a.html\">" );
      WebResponse targetPage = wc.getResponse( getHostPath() + "/target.html" );
      assertEquals( "Num links in initial page", 1,targetPage.getLinks().length );
      WebLink link = targetPage.getLinks()[0];
      String target=link.getTarget();
      // the bug report _top is NOT what we expect
      // but for the time being this is how httpunit behaves ...
      String expected="_top";
      //System.err.println(target);
      assertTrue(target.equals(expected));    	
    }



    public void testLinksWithFragmentsAndParameters() throws Exception {
        WebConversation wc = new WebConversation();
        defineResource( "Initial.html?age=3", "<html><head><title>Initial</title></head><body>" +
                                              "Go to <a href=\"Next.html\">the next page.</a> <a name=\"bottom\">Bottom</a>" +
                                              "</body></html>" );
        defineWebPage( "Next", "And go back to <a href=\"Initial.html?age=3#Bottom\">the first page.</a>" );

        WebResponse initialPage = wc.getResponse( getHostPath() + "/Initial.html?age=3" );
        assertEquals( "Num links in initial page", 1, initialPage.getLinks().length );
        WebLink link = initialPage.getLinks()[0];

        WebResponse nextPage = wc.getResponse( link.getRequest() );
        assertEquals( "Title of next page", "Next", nextPage.getTitle() );
        assertEquals( "Num links in next page", 1, nextPage.getLinks().length );
        link = nextPage.getLinks()[0];

        WebResponse thirdPage = wc.getResponse( link.getRequest() );
        assertEquals( "Title of next page", "Initial", thirdPage.getTitle() );
    }


    public void testLinksWithSlashesInQuery() throws Exception {
        WebConversation wc = new WebConversation();
        defineResource( "sample/Initial.html?age=3/5", "<html><head><title>Initial</title></head><body>" +
                                              "Go to <a href=\"Next.html\">the next page.</a>" +
                                              "</body></html>" );
        defineWebPage( "sample/Next", "And go back to <a href=\"Initial.html?age=3/5\">the first page.</a>" );

        WebResponse initialPage = wc.getResponse( getHostPath() + "/sample/Initial.html?age=3/5" );
        assertEquals( "Num links in initial page", 1, initialPage.getLinks().length );
        WebLink link = initialPage.getLinks()[0];

        WebResponse nextPage = wc.getResponse( link.getRequest() );
        assertEquals( "Title of next page", "sample/Next", nextPage.getTitle() );
        assertEquals( "Num links in next page", 1, nextPage.getLinks().length );
    }


    public void testDocumentBase() throws Exception {
        WebConversation wc = new WebConversation();
        defineWebPage( "alternate/Target", "Found me!" );
        defineResource( "Initial.html", "<html><head><title>Test for Base</title>" +
                                        "            <base href='/alternate/'></head>" +
                                        "      <body><a href=\"Target.html\">Go</a></body></html>" );

        WebResponse initialPage = wc.getResponse( getHostPath() + "/Initial.html" );
        assertEquals( "Num links in initial page", 1, initialPage.getLinks().length );
        WebLink link = initialPage.getLinks()[0];

        WebRequest request = link.getRequest();
        assertEquals( "Destination for link", getHostPath() + "/alternate/Target.html", request.getURL().toExternalForm() );
        WebResponse nextPage = wc.getResponse( request );
        assertTrue( "Did not find the target", nextPage.getText().indexOf( "Found" ) >= 0 );
    }


    public void testTargetBase() throws Exception {
        WebConversation wc = new WebConversation();
        defineWebPage( "alternate/Target", "Found me!" );
        defineResource( "Initial.html", "<html><head><title>Test for Base</title>" +
                                        "            <base target=blue></head>" +
                                        "      <body><a href=\"Target.html\">Go</a></body></html>" );

        WebResponse initialPage = wc.getResponse( getHostPath() + "/Initial.html" );
        assertEquals( "Num links in initial page", 1, initialPage.getLinks().length );
        WebLink link = initialPage.getLinks()[0];

        assertEquals( "Target for link", "blue", link.getTarget() );
    }


    public void testParametersOnLinks() throws Exception {
        defineResource( "ParameterLinks.html",
                        "<html><head><title>Param on Link Page</title></head>\n" +
                        "<body>" +
                        "<a href=\"/other.html\">no parameter link</A>\n" +
                        "<a href=\"/other.html?param1=value1\">one parameter link</A>\n" +
                        "<a href=\"/other.html?param1=value1&param2=value2\">two parameters link</A>\n" +
                        "<a href=\"/other.html?param1=value1&param1=value3\">two values link</A>\n" +
                        "<a href=\"/other.html?param1=value1&param2=value2&param1=value3\">two values link</A>\n" +
                        "</body></html>\n" );
        WebConversation wc = new WebConversation();

        WebLink[] links = wc.getResponse( getHostPath() + "/ParameterLinks.html" ).getLinks();
        assertNotNull( links );
        assertEquals( "number of links", 5, links.length );
        WebRequest request;

        // first link should not have any param
        request = links[0].getRequest();
        assertNotNull( request);
        String[] names = request.getRequestParameterNames();
        assertNotNull( names );
        assertEquals( "Num parameters found", 0, names.length );
        assertEquals("Non Existent parameter should be empty","",request.getParameter("nonexistent"));

        // second link should have one parameter
        checkLinkParameters( links[1], new String[] { "param1" }, 
		                     new String[][] { { "value1" } });

        // third link should have 2 parameters.  !! Order of parameters cannot be guaranted.
        checkLinkParameters( links[2], new String[] { "param1", "param2" }, 
		                     new String[][] { { "value1" }, { "value2" } });

        // fourth link should have 1 parameter with 2 values.
        checkLinkParameters( links[3], new String[] { "param1" }, 
		                     new String[][] { { "value1", "value3" } });

        // fifth link should have 2 parameters with one with 2 values.
        checkLinkParameters( links[4], new String[] { "param1", "param2" }, 
		                     new String[][] { { "value1", "value3" }, { "value2" } });
    }


	private void checkLinkParameters( WebLink link, String[] expectedNames, String[][] expectedValues ) {
		WebRequest request = link.getRequest();
		assertNotNull( request );
		assertMatchingSet( "Parameter names", expectedNames, request.getRequestParameterNames() );
		for (int i = 0; i < expectedValues.length; i++) {
			assertMatchingSet( expectedNames[i] + " values", expectedValues[i], request.getParameterValues( expectedNames[i] ));
		}
	}


    public void testEncodedLinkParameters() throws Exception {
        WebConversation wc = new WebConversation();
        defineWebPage( "encodedLinks", "<html><head><title>Encode Test</title></head>" +
                                       "<body>" +
                                       "<a href=\"/request?%24dollar=%25percent&%23hash=%26ampersand\">request</a>" +
                                       "</body></html>" );
        WebResponse mapPage = wc.getResponse( getHostPath() + "/encodedLinks.html" );
        WebLink link = mapPage.getLinks()[0];
        WebRequest wr = link.getRequest();
        assertMatchingSet( "Request parameter names", new String[] { "$dollar", "#hash" }, wr.getRequestParameterNames() );
        assertEquals( "Value of $dollar", "%percent", wr.getParameter( "$dollar" ) );
        assertEquals( "Value of #hash", "&ampersand", wr.getParameter( "#hash" ) );
    }


    public void testValuelessLinkParameters() throws Exception {
        WebConversation wc = new WebConversation();
        defineWebPage( "encodedLinks", "<html><head><title>Encode Test</title></head>" +
                                       "<body>" +
                                       "<a href=\"/request?arg1&valueless=\">request</a>" +
                                       "</body></html>" );
        WebResponse mapPage = wc.getResponse( getHostPath() + "/encodedLinks.html" );
        WebLink link = mapPage.getLinks()[0];
        WebRequest wr = link.getRequest();
        assertMatchingSet( "Request parameter names", new String[] { "arg1", "valueless" }, wr.getRequestParameterNames() );
        assertEquals( "Value of arg1", null, wr.getParameter( "arg1" ) );
    }


    public void testLinkParameterOrder() throws Exception {
        WebConversation wc = new WebConversation();
        defineWebPage( "encodedLinks", "<html><head><title>Encode Test</title></head>" +
                                       "<body>" +
                                       "<a href='/request?arg0=0\n&arg1&arg0=2&valueless='>request</a>" +
                                       "</body></html>" );
        WebResponse mapPage = wc.getResponse( getHostPath() + "/encodedLinks.html" );
        WebLink link = mapPage.getLinks()[0];
        WebRequest wr = link.getRequest();
        assertMatchingSet( "Request parameter names", new String[] { "arg0", "arg1", "valueless" }, wr.getRequestParameterNames() );
        assertMatchingSet( "Value of arg0", new String[] { "0", "2" }, wr.getParameterValues( "arg0" ) );
        assertEquals( "Actual query", "arg0=0&arg1&arg0=2&valueless=", wr.getQueryString() );
    }


    public void testLinkParameterValidation() throws Exception {
        WebConversation wc = new WebConversation();
        defineWebPage( "encodedLinks", "<html><head><title>Encode Test</title></head>" +
                                       "<body>" +
                                       "<a href='/request?arg0=0&arg1&arg0=2&valueless='>request</a>" +
                                       "</body></html>" );
        WebResponse mapPage = wc.getResponse( getHostPath() + "/encodedLinks.html" );
        WebLink link = mapPage.getLinks()[0];
        WebRequest wr = link.getRequest();
        wr.setParameter( "arg0", new String[] { "0", "2" } );
        try {
            wr.setParameter( "arg0", "3" );
            fail( "Did not prevent change to link parameters" );
        } catch (IllegalRequestParameterException e) {}
    }


    public void testImageMapLinks() throws Exception {
        WebConversation wc = new WebConversation();
        defineWebPage( "pageWithMap", "Here is a page with <a href=\"somewhere\">a link</a>" +
                                      " and a map: <IMG src=\"navbar1.gif\" usemap=\"#map1\" alt=\"navigation bar\">" +
                                      "<map name=\"map1\">" +
                                      "  <area href=\"guide.html\" alt=\"Guide\" shape=\"rect\" coords=\"0,0,118,28\">" +
                                      "  <area href=\"search.html\" alt=\"Search\" shape=\"circle\" coords=\"184,200,60\">" +
                                      "</map>" );
        WebResponse mapPage = wc.getResponse( getHostPath() + "/pageWithMap.html" );
        WebLink[] links = mapPage.getLinks();
        assertEquals( "number of links found", 3, links.length );

        WebLink guide = mapPage.getLinkWith( "Guide" );
        assertNotNull( "Did not find the guide area", guide );
        assertEquals( "Relative URL", "guide.html", guide.getURLString() );
    }
    /**
     * test for bug report [ 1035949 ] NullPointerException on Weblink.click
     * by Ute Platzer
     * @throws Exception
     */   
    public void testLinkBug() throws Exception {
      WebConversation wc = new WebConversation();
      String html="\"<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html40/strict.dtd\">\n"+
    	"<html><body>\n"+
    	"	<a target=\"_parent\" class=\"core_button_normal\"  href=\"/test2.html\">test link</a>"+
    	"</body></html>";
      defineWebPage( "test3",html);
      defineWebPage( "test2","test page2");
      HttpUnitOptions.setLoggingHttpHeaders(false);
      HttpUnitOptions.setScriptingEnabled( false );
      WebResponse mapPage = wc.getResponse( getHostPath() + "/test3.html" );
      WebLink link =wc.getCurrentPage().getLinkWith("test link");
      link.click();
      
      String html2=(wc.getCurrentPage().getText());
      assertTrue("click should lead to page 2",html2.indexOf("test page2")>0);
    }
    
    private WebResponse _simplePage;
}
