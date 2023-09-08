package com.meterware.httpunit;
/********************************************************************************************************************
* $Id: WebFrameTest.java 107617 2012-01-03 09:51:15Z jst $
*
* Copyright (c) 2000-2004,2007 Russell Gold
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
import java.net.HttpURLConnection;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * A test of the web frame functionality.
 **/
public class WebFrameTest extends HttpUnitTest {

    public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
    }


    public static Test suite() {
        return new TestSuite( WebFrameTest.class );
    }


    public WebFrameTest( String name ) {
        super( name );
    }


    public void setUp() throws Exception {
        super.setUp();
        _wc = new WebConversation();

        defineWebPage( "Linker",  "This is a trivial page with <a href=Target.html>one link</a>" );
        defineWebPage( "Target",  "This is another page with <a href=Form.html target=\"_top\">one link</a>" );
        defineWebPage( "Form",    "This is a page with a simple form: " +
                                  "<form action=submit><input name=name><input type=submit></form>" +
                                  "<a href=Linker.html target=red>a link</a>");
        defineResource( "Frames.html",
                        "<HTML><HEAD><TITLE>Initial</TITLE></HEAD>" +
                        "<FRAMESET cols=\"20%,80%\">" +
                        "    <FRAME src=\"Linker.html\" name=\"red\">" +
                        "    <FRAME src=Form.html name=blue>" +
                        "</FRAMESET></HTML>" );
    }


    public void testDefaultFrameNames() throws Exception {
        defineWebPage( "Initial", "This is a trivial page" );
        _wc.getResponse( getHostPath() + "/Initial.html" );
        assertMatchingSet( "Frames defined for the conversation", new String[] { "_top" }, _wc.getFrameNames() );
    }


    public void testDefaultFrameContents() throws Exception {
        WebResponse response = _wc.getResponse( getHostPath() + "/Linker.html" );
        assertTrue( "Default response not the same as default frame contents", response == _wc.getFrameContents( "_top" ) );
        response = _wc.getResponse( response.getLinks()[0].getRequest() );
        assertTrue( "Second response not the same as default frame contents", response == _wc.getFrameContents( "_top" ) );
    }


    public void testFrameNames() throws Exception {
        WebResponse response = _wc.getResponse( getHostPath() + "/Frames.html" );
        assertMatchingSet( "frame set names",
                           new String[] { "red", "blue" },
                           response.getFrameNames() );
    }


    public void testParentTarget() throws Exception {
        defineWebPage( "Target",  "This is another page with <a href=Form.html target='_parent'>one link</a>" );
        _wc.getResponse( getHostPath() + "/Frames.html" );
        WebResponse resp = _wc.getResponse( _wc.getFrameContents( "red" ).getLinks()[0].getRequest() );
        resp = _wc.getResponse( resp.getLinks()[0].getRequest() );
        assertMatchingSet( "Frames after third response", new String[] { "_top" }, _wc.getFrameNames() );
    }


    public void testParentTargetFromTopFrame() throws Exception {
        defineWebPage( "Target",  "This is another page with <a href=Form.html target='_parent'>one link</a>" );
        WebResponse resp = _wc.getResponse( getHostPath() + "/Target.html" );
        resp = _wc.getResponse( resp.getLinks()[0].getRequest() );
        assertMatchingSet( "Frames after second response", new String[] { "_top" }, _wc.getFrameNames() );
    }


    public void testFrameRequests() throws Exception {
        WebResponse response = _wc.getResponse( getHostPath() + "/Frames.html" );
        WebRequest[] requests = response.getFrameRequests();
        assertEquals( "Number of frame requests", 2, requests.length );
        assertEquals( "Target for first request", "red", requests[0].getTarget() );
        assertEquals( "URL for second request", getHostPath() + "/Form.html", requests[1].getURL().toExternalForm() );
    }


    public void testFrameRequestsWithFragments() throws Exception {
        defineResource( "Frames.html",
                        "<HTML><HEAD><TITLE>Initial</TITLE></HEAD>" +
                        "<FRAMESET cols=\"20%,80%\">" +
                        "    <FRAME src='Linker.html' name=\"red\">" +
                        "    <FRAME src='Form.html#middle' name=blue>" +
                        "</FRAMESET></HTML>" );
        WebResponse response = _wc.getResponse( getHostPath() + "/Frames.html" );
        WebRequest[] requests = response.getFrameRequests();
        assertEquals( "URL for second request", getHostPath() + "/Form.html", requests[1].getURL().toExternalForm() );
    }


    public void testFrameLoading() throws Exception {
        _wc.getResponse( getHostPath() + "/Frames.html" );

        assertMatchingSet( "Frames defined for the conversation", new String[] { "_top", "red", "blue" }, _wc.getFrameNames() );
        assertEquals( "Number of links in first frame", 1, _wc.getFrameContents( "red" ).getLinks().length );
        assertEquals( "Number of forms in second frame", 1, _wc.getFrameContents( "blue" ).getForms().length );
    }


    public void testInFrameLinks() throws Exception {
        WebResponse response = _wc.getResponse( getHostPath() + "/Frames.html" );

        response = _wc.getResponse( _wc.getFrameContents( "red" ).getLinks()[0].getRequest() );
        assertTrue( "Second response not the same as source frame contents", response == _wc.getFrameContents( "red" ) );
        assertMatchingSet( "Frames defined for the conversation", new String[] { "_top", "red", "blue" }, _wc.getFrameNames() );
        assertEquals( "URL for second request", getHostPath() + "/Target.html", response.getURL().toExternalForm() );
    }


    public void testFrameURLBase() throws Exception {
        defineWebPage( "Deeper/Linker",  "This is a trivial page with <a href=Target.html>one link</a>" );
        defineWebPage( "Deeper/Target",  "This is another page with <a href=Form.html target=\"_top\">one link</a>" );
        defineWebPage( "Deeper/Form",    "This is a page with a simple form: " +
                                  "<form action=submit><input name=name><input type=submit></form>" +
                                  "<a href=Linker.html target=red>a link</a>");
        defineResource( "Frames.html",
                        "<HTML><HEAD><TITLE>Initial</TITLE>" +
                        "<base href=\"" + getHostPath() + "/Deeper/Frames.html\"></HEAD>" +
                        "<FRAMESET cols=\"20%,80%\">" +
                        "    <FRAME src=\"Linker.html\" name=\"red\">" +
                        "    <FRAME src=Form.html name=blue>" +
                        "</FRAMESET></HTML>" );

        WebResponse response = _wc.getResponse( getHostPath() + "/Frames.html" );

        response = _wc.getResponse( _wc.getFrameContents( "red" ).getLinks()[0].getRequest() );
        assertTrue( "Second response not the same as source frame contents", response == _wc.getFrameContents( "red" ) );
        assertMatchingSet( "Frames defined for the conversation", new String[] { "_top", "red", "blue" }, _wc.getFrameNames() );
        assertEquals( "URL for second request", getHostPath() + "/Deeper/Target.html", response.getURL().toExternalForm() );
    }


    public void testDuplicateFrameNames() throws Exception {
        defineWebPage( "Linker",  "This is a trivial page with <a href=Target.html>one link</a>" );
        defineWebPage( "Target",  "This is another page with <a href=Form.html target=\"_top\">one link</a>" );
        defineWebPage( "Form",    "This is a page with a simple form: " +
                                  "<form action=submit><input name=name><input type=submit></form>" +
                                  "<a href=Linker.html target=red>a link</a>");
        defineResource( "Frames.html",
                        "<HTML><HEAD><TITLE>Initial</TITLE></HEAD>" +
                        "<FRAMESET cols=\"20%,80%\">" +
                        "    <FRAME src='SubFrames.html'>" +
                        "    <FRAME src=Form.html>" +
                        "</FRAMESET></HTML>" );

        defineResource( "SubFrames.html",
                        "<HTML><HEAD><TITLE>Initial</TITLE></HEAD>" +
                        "<FRAMESET cols=\"20%,80%\">" +
                        "    <FRAME src=\"Linker.html\">" +
                        "    <FRAME src=Form.html>" +
                        "</FRAMESET></HTML>" );

        WebResponse response = _wc.getResponse( getHostPath() + "/Frames.html" );
        WebResponse linker = getFrameWithURL( _wc, "Linker" );
        assertNotNull( "Linker not found", linker );

        response = _wc.getResponse( linker.getLinks()[0].getRequest() );
        WebResponse target = getFrameWithURL( _wc, "Target" );
        assertTrue( "Second response not the same as source frame contents", response == target );
    }


    public void testUnnamedFrames() throws Exception {
        defineWebPage( "Linker",  "This is a trivial page with <a href=Target.html>one link</a>" );
        defineWebPage( "Target",  "This is another page with <a href=Form.html target=\"_top\">one link</a>" );
        defineWebPage( "Form",    "This is a page with a simple form: " +
                                  "<form action=submit><input name=name><input type=submit></form>" +
                                  "<a href=Linker.html target=red>a link</a>");
        defineResource( "Frames.html",
                        "<HTML><HEAD><TITLE>Initial</TITLE></HEAD>" +
                        "<FRAMESET cols=\"20%,80%\">" +
                        "    <FRAME src=\"Linker.html\">" +
                        "    <FRAME src=Form.html>" +
                        "</FRAMESET></HTML>" );

        WebResponse response = _wc.getResponse( getHostPath() + "/Frames.html" );
        WebResponse linker = getFrameWithURL( _wc, "Linker" );
        assertNotNull( "Linker not found", linker );

        response = _wc.getResponse( linker.getLinks()[0].getRequest() );
        WebResponse target = getFrameWithURL( _wc, "Target" );
        assertTrue( "Second response not the same as source frame contents", response == target );
    }


    private String getNameOfFrameWithURL( WebConversation wc, String urlString ) {
        String[] names = wc.getFrameNames();
        for (int i = 0; i < names.length; i++) {
            WebResponse candidate = wc.getFrameContents( names[i] );
            if (candidate.getURL().toExternalForm().indexOf( urlString ) >= 0) {
                return names[i];
            }
        }
        return null;
    }


    private WebResponse getFrameWithURL( WebConversation wc, String urlString ) {
        String name = getNameOfFrameWithURL( wc, urlString );
        if (name == null) return null;
        return wc.getFrameContents( name );
    }


    public void testCrossFrameLinks() throws Exception {
        WebResponse response = _wc.getResponse( getHostPath() + "/Frames.html" );

        _wc.getResponse( _wc.getFrameContents( "red" ).getLinks()[0].getRequest() );
        response = _wc.getResponse( _wc.getFrameContents( "blue" ).getLinks()[0].getRequest() );
        assertTrue( "Second response not the same as source frame contents", response == _wc.getFrameContents( "red" ) );
        assertMatchingSet( "Frames defined for the conversation", new String[] { "_top", "red", "blue" }, _wc.getFrameNames() );
        assertEquals( "URL for second request", getHostPath() + "/Linker.html", response.getURL().toExternalForm() );
    }


    public void testGetSubframes() throws Exception {
        WebResponse response = _wc.getResponse( getHostPath() + "/Frames.html" );
        assertEquals( "red subframe", _wc.getFrameContents( "red" ), response.getSubframeContents( "red" ) );
    }
    
    /**
     * test for bug report
     * [ 1535018 ] Sub frame recognition - getSubframeContents
     * by Oliver GL
     * this is how it does not work ...
     */
    public void xtestSubFrameRecognitionOriginal() throws Exception {
    	defineWebPage("frame1","frame1Content");
    	defineWebPage("frame2","frame2Content");
    	defineWebPage("frame3","frame3Content");
	    String html="<html>\n"+
	    "<head>\n"+
	    "</head>\n"+
	    "<frameset name=\"topset\" rows=\"65,*\">\n"+
	    "  <frame src=\"frame1.html\" name=\"Banner\" frameborder=\"0\" noresize scrolling=\"no\">\n"+
	    "  <frameset name=\"subset\" cols=\"180,*\">\n"+
	    "     <frame src=\"frame2.html\" name=\"Menu\"   frameborder=\"0\" noresize scrolling=\"yes\">\n"+
	    "     <frame src=\"frame3.html\" name=\"Action\" frameborder=\"0\" noresize scrolling=\"auto\">\n"+
	    "  </frameset>\n"+
	    "</frameset>\n"+
	    "</html>";
	    // for checking the resulting frame page in real browser ...
	    // System.out.println(html);
	    defineResource("frames.html",html);
      WebResponse topResponse = _wc.getResponse( getHostPath() + "/frames.html" );
      String[] frameNames=topResponse.getFrameNames();
      // System.out.println("found "+frameNames.length+" frames");
      assertTrue(frameNames.length==3);
      String[]expectedNames={"Banner","Menu","Action"};
      for (int i=0;i<frameNames.length;i++) {
      	// System.out.println("frame #"+i+" is '"+frameNames[i]+"'");
      	assertTrue("frame #"+i+" should be '"+expectedNames[i]+"'",frameNames[i].equals(expectedNames[i]));
      }
      WebResponse bannerFrame=_wc.getFrameContents("Banner");
      for (int i=0;i<frameNames.length;i++) {
      	WebResponse subFrame =bannerFrame.getSubframeContents(frameNames[i]);
      	assertNotNull(subFrame);
      }	
    }
    
    /**
     * test for bug report
     * [ 1535018 ] Sub frame recognition - getSubframeContents
     * by Oliver GL
     * and this is how it works
     */
    public void testSubFrameRecognition() throws Exception {
    	String frame1Content=
	    "  <frameset name=\"subset\" cols=\"180,*\">\n"+
	    "     <frame src=\"frame2.html\" name=\"Menu\"   frameborder=\"0\" noresize scrolling=\"yes\">\n"+
	    "     <frame src=\"frame3.html\" name=\"Action\" frameborder=\"0\" noresize scrolling=\"auto\">\n"+
	    "  </frameset>";

    	defineWebPage("frame1",frame1Content);
    	defineWebPage("frame2","frame2Content");
    	defineWebPage("frame3","frame3Content");
	    String html="<html>\n"+
	    "<head>\n"+
	    "</head>\n"+
	    "<frameset name=\"topset\" rows=\"65,*\">\n"+
	    "  <frame src=\"frame1.html\" name=\"Banner\" frameborder=\"0\" noresize scrolling=\"no\">\n"+
	    "</frameset>\n"+
	    "</html>";
	    // for checking the resulting frame page in real browser ...
	    // System.out.println(html);
	    defineResource("frames.html",html);
      WebResponse topResponse = _wc.getResponse( getHostPath() + "/frames.html" );
      String[] frameNames=topResponse.getFrameNames();
      // System.out.println("found "+frameNames.length+" frames");
      String[]expectedTopNames={"Banner"};
      assertTrue(frameNames.length==expectedTopNames.length);
      for (int i=0;i<frameNames.length;i++) {
      	// System.out.println("frame #"+i+" is '"+frameNames[i]+"'");
      	assertTrue("frame #"+i+" should be '"+expectedTopNames[i]+"'",frameNames[i].equals(expectedTopNames[i]));
      }
      WebResponse bannerFrame=_wc.getFrameContents("Banner");
      String[] subFrameNames=bannerFrame.getFrameNames();
      String[] expectedSubNames={"Menu","Action"};
      for (int i=1;i<subFrameNames.length;i++) {
      	WebResponse subFrame =bannerFrame.getSubframeContents(subFrameNames[i]);
      	assertNotNull(subFrame);
      }	
    }


    public void testNestedSubFrames() throws Exception {
        defineResource( "SuperFrames.html",
                        "<HTML><HEAD><TITLE>Initial</TITLE></HEAD>" +
                        "<FRAMESET cols=\"50%,50%\">" +
                        "    <FRAME src=\"Frames.html\" name=\"crimson\">" +
                        "    <FRAME src=\"Form.html\" name=\"blue\">" +
                        "</FRAMESET></HTML>" );
        WebResponse response = _wc.getResponse( getHostPath() + "/SuperFrames.html" );
        WebResponse frameContents = _wc.getFrameContents( "red" );
        WebResponse subframeContents = response.getSubframeContents( "crimson" ).getSubframeContents( "red" );
        assertEquals( "crimson.red subframe", frameContents,
                subframeContents );
    }


    /**
     * Verifies that a link in one subframe can update the contents of a different subframe of the same frame.
     * @throws Exception if any method throws an unexpected exception
     */
    public void testNestedCrossFrameLinks() throws Exception {
        defineResource( "SuperFrames.html",
                        "<HTML><HEAD><TITLE>Initial</TITLE></HEAD>" +
                        "<FRAMESET cols=\"50%,50%\">" +
                        "    <FRAME src=\"Frames.html\" name=\"red\">" +
                        "    <FRAME src=\"Frames.html\" name=\"blue\">" +
                        "</FRAMESET></HTML>" );
        _wc.getResponse( getHostPath() + "/SuperFrames.html" );
        FrameSelector nestedRedFrame  = _wc.getFrameContents( "red" ).getSubframeContents( "red" ).getFrame();
        FrameSelector nestedBlueFrame = _wc.getFrameContents( "red" ).getSubframeContents( "blue" ).getFrame();

        _wc.getResponse( _wc.getFrameContents( nestedRedFrame ).getLinks()[0].getRequest() );
        _wc.getFrameContents( nestedBlueFrame ).getLinks()[0].click();
        assertEquals( "URL for second request", getHostPath() + "/Linker.html", _wc.getFrameContents( nestedRedFrame ).getURL().toExternalForm() );
    }


    /**
     * Verifies that a link in one subframe can update the original subframe or the top-level window.
     * @throws Exception if any method throws an unexpected exception
     */
    public void testCrossLevelLinks() throws Exception {
        defineResource( "SuperFrames.html",
                        "<HTML><HEAD><TITLE>Initial</TITLE></HEAD>" +
                        "<FRAMESET cols=\"50%,50%\">" +
                        "    <FRAME src=\"Frames.html\" name=\"red\">" +
                        "    <FRAME src=\"Frames.html\" name=\"blue\">" +
                        "</FRAMESET></HTML>" );
        _wc.getResponse( getHostPath() + "/SuperFrames.html" );
        FrameSelector nestedRedFrame  = _wc.getFrameContents( "red" ).getSubframeContents( "red" ).getFrame();

        _wc.getFrameContents( nestedRedFrame ).getLinks()[0].click();
        WebResponse frameContent = _wc.getResponse( _wc.getFrameContents( nestedRedFrame ).getLinks()[0].getRequest() );
        assertTrue( "Second response not the same as source frame contents", frameContent == _wc.getFrameContents( "_top" ) );
        assertEquals( "URL for second request", getHostPath() + "/Form.html", frameContent.getURL().toExternalForm() );
        assertEquals( "Number of active frames", 1, _wc.getFrameNames().length );
    }


    public void testLinkToTopFrame() throws Exception {
        WebResponse response = _wc.getResponse( getHostPath() + "/Frames.html" );

        response = _wc.getResponse( _wc.getFrameContents( "red" ).getLinks()[0].getRequest() );
        response = _wc.getResponse( response.getLinks()[0].getRequest() );
        assertTrue( "Second response not the same as source frame contents", response == _wc.getFrameContents( "_top" ) );
        assertEquals( "URL for second request", getHostPath() + "/Form.html", response.getURL().toExternalForm() );
        assertMatchingSet( "Frames defined for the conversation", new String[] { "_top" }, _wc.getFrameNames() );
    }


    public void testEmptyFrame() throws Exception {
        defineResource( "HalfFrames.html",
                        "<HTML><HEAD><TITLE>Initial</TITLE></HEAD>" +
                        "<FRAMESET cols=\"20%,80%\">" +
                        "    <FRAME src=\"Linker.html\" name=\"red\">" +
                        "    <FRAME name=blue>" +
                        "</FRAMESET></HTML>" );
        _wc.getResponse( getHostPath() + "/HalfFrames.html" );
        WebResponse response = _wc.getFrameContents( "blue" );

        assertNotNull( "Loaded nothing for the empty frame", response );
        assertEquals( "Num links", 0, response.getLinks().length );
    }


    public void testSelfTargetLink() throws Exception {
        defineWebPage( "Linker",  "This is a trivial page with <a href=Target.html target=_self>one link</a>" );

        _wc.getResponse( getHostPath() + "/Frames.html" );
        WebResponse response = _wc.getResponse( _wc.getFrameContents( "red" ).getLinks()[0].getRequest() );
        assertMatchingSet( "Frames defined for the conversation", new String[] { "_top", "red", "blue" }, _wc.getFrameNames() );
        assertTrue( "Second response not the same as source frame contents", response == _wc.getFrameContents( "red" ) );
        assertEquals( "URL for second request", getHostPath() + "/Target.html", response.getURL().toExternalForm() );
    }


    public void testSelfTargetForm() throws Exception {
        defineWebPage( "Linker",  "<form action=redirect.html target=_self><input type=text name=sample value=z></form>" );
        defineResource( "redirect.html?sample=z", "", HttpURLConnection.HTTP_MOVED_PERM );
        addResourceHeader( "redirect.html?sample=z", "Location: " + getHostPath() + "/Target.html" );

        _wc.getResponse( getHostPath() + "/Frames.html" );
        WebResponse response = _wc.getResponse( _wc.getFrameContents( "red" ).getForms()[0].getRequest() );
        assertMatchingSet( "Frames defined for the conversation", new String[] { "_top", "red", "blue" }, _wc.getFrameNames() );
        assertTrue( "Second response not the same as source frame contents", response == _wc.getFrameContents( "red" ) );
        assertEquals( "URL for second request", getHostPath() + "/Target.html", response.getURL().toExternalForm() );
    }


    public void testSubFrameRedirect() throws Exception {
        defineResource( "Linker.html", "", HttpURLConnection.HTTP_MOVED_PERM );
        addResourceHeader( "Linker.html", "Location: " + getHostPath() + "/Target.html" );

        _wc.getResponse( getHostPath() + "/Frames.html" );
        assertMatchingSet( "Frames defined for the conversation", new String[] { "_top", "red", "blue" }, _wc.getFrameNames() );
        assertTrue( "Did not redirect", _wc.getFrameContents( "red" ).getURL().toExternalForm().endsWith( "Target.html" ) );

    }


    private void defineNestedFrames() throws Exception {
        defineResource( "Topmost.html",
                        "<HTML><HEAD><TITLE>Topmost</TITLE></HEAD>" +
                        "<FRAMESET cols=\"20%,80%\">" +
                        "    <FRAME src=\"Target.html\" name=\"red\">" +
                        "    <FRAME src=\"Inner.html\" name=\"blue\">" +
                        "</FRAMESET></HTML>" );
        defineResource( "Inner.html",
                        "<HTML><HEAD><TITLE>Inner</TITLE></HEAD>" +
                        "<FRAMESET rows=\"20%,80%\">" +
                        "    <FRAME src=\"Form.html\" name=\"green\">" +
                        "</FRAMESET></HTML>" );
    }


    public void testGetNestedFrameByName() throws Exception {
        defineNestedFrames();
        _wc.getResponse( getHostPath() + "/Topmost.html" );
        _wc.getFrameContents( "green" );
    }


    public void testLinkWithAncestorTarget() throws Exception {
        defineNestedFrames();
        _wc.getResponse( getHostPath() + "/Topmost.html" );
        WebResponse innerResponse = _wc.getFrameContents( "blue" ).getSubframeContents( "green" );
        innerResponse.getLinks()[0].click();
        assertEquals( "Title of 'red' frame", "Linker", _wc.getFrameContents( "red" ).getTitle() );
    }

    /**
     * test I frame detection
     * @throws Exception
     */
    public void testIFrameDetection() throws Exception {
        defineWebPage( "Frame",  "This is a trivial page with <a href='mailto:russgold@httpunit.org'>one link</a>" +
                                 "and <iframe name=center src='Contents.html'><form name=hidden></form></iframe>" );
        defineWebPage( "Contents",  "This is another page with <a href=Form.html>one link</a>" );
        defineWebPage( "Form",    "This is a page with a simple form: " +
                                  "<form action=submit><input name=name><input type=submit></form>");

        WebResponse response = _wc.getResponse( getHostPath() + "/Frame.html" );
        WebRequest[] requests = response.getFrameRequests();
        assertEquals( "Number of links in main frame", 1, response.getLinks().length );
        assertEquals( "Number of forms in main frame", 0, response.getForms().length );
        assertEquals( "Number of frame requests", 1, requests.length );
        assertEquals( "Target for iframe request", "center", requests[0].getTarget() );

        WebResponse contents = getFrameWithURL( _wc, "Contents" );
        assertNotNull( "Contents not found", contents );
        assertEquals( "Number of links in iframe", 1, _wc.getFrameContents( "center" ).getLinks().length );
    }
    

    /**
     * test I Frame with a Form according to mail to mailinglist of 2008-03-25
     * Problems with IFrames by Allwyn D'souza
     * TODO activate test when it's clear how it should work
     * @throws Exception
     */
    public void xtestIFrameForm() throws Exception {
    	String login="//Login.html (main page that is loaded - this page embed the IFrame).\n"+
    	" \n"+
    	"<html>\n"+
    	"<Head>\n"+
    	"<Script>\n"+
    	"<!--\n"+
    	"function SetLoginForm(name, password, Submit) {\n"+
    	"  document.loginForm.name.value = name;\n"+
    	"  document.loginForm.password.value = password;\n"+
    	" \n"+
    	"  document.loginForm.submit();\n"+
    	"}\n"+
    	"-->\n"+
    	"</Script>\n"+
    	"</Head>\n"+
    	"<Body>\n"+
    	"<Form name=\"loginForm\" action=\"/LoginController\" method=\"Post\">\n"+
    	"<input type=\"hidden\" name=\"name\" value=\"\" />\n"+
    	"<input type=\"hidden\" name=\"password\" value=\"\" />\n"+
    	"</Form>\n"+
    	"<Center>\n"+
    	"<IFrame name=\"login\" id=\"login\" src=\"LoginDialog.html\" />\n"+
    	"</Center>\n"+
    	"</Body>\n"+
    	"</html>\n";

    	String loginDialog="// LoginDialog.html - IFrame\n"+
    	" \n"+
    	"<html>\n"+
    	"<Head>\n"+
    	"<Script>\n"+
    	"<!--\n"+
    	"function SubmitToParent(action) {\n"+
    	"  parent.SetLoginForm(document.submit_to_parent.name.value,document.submit_to_parent.password.value);\n"+
    	"}\n"+
    	"-->\n"+
    	"</Script>\n"+
    	"</Head>\n"+
    	"<Body>\n"+
    	"<Form id=f1 name=\"submit_to_parent\" method=\"Post\">\n"+
    	"<input type=\"text\" name=\"name\" value=\"\" />\n"+
    	"<input type=\"text\" name=\"password\" value=\"\" />\n"+
    	"<input type=\"submit\" name=\"Ok\" value=\"login\" onClick=\"SubmitToParent('Submit')\" />\n"+
    	"</Form>\n"+
    	"</Body>\n"+
    	"</html>\n";

    	defineWebPage("Login",login);
    	defineWebPage("LoginDialog",loginDialog);
      WebResponse response = _wc.getResponse( getHostPath() + "/Login.html" );
      WebResponse bottomFrame = _wc.getFrameContents("login"); // load the <Iframe>
    	WebForm form = bottomFrame.getFormWithName("submit_to_parent");
    	form.setParameter("name","aa");
    	form.setParameter("password","xx");
    	boolean oldDebug=	HttpUnitUtils.setEXCEPTION_DEBUG(true);
    	try  {
    		WebResponse submitResponse = form.submit(); // This response contains the same page, does not log the user in. Load the same //page
    	} catch (ScriptException se) {
    		// TODO clarify what should happen here ...
    		String msg=se.getMessage();
    		// Event 'SubmitToParent('Submit')' failed: org.mozilla.javascript.EcmaError: TypeError: Cannot read property "name" from undefined
    		assertTrue(msg.startsWith("Event"));
    		System.err.println(msg);
    		throw se;
    	}
    	HttpUnitUtils.setEXCEPTION_DEBUG(oldDebug);
    }

    /**
     * test I frames that are disabled
     * @throws Exception
     */
    public void testIFrameDisabled() throws Exception {
        defineWebPage( "Frame",  "This is a trivial page with <a href='mailto:russgold@httpunit.org'>one link</a>" +
                                 "and <iframe name=center src='Contents.html'><form name=hidden></form></iframe>" );
        defineWebPage( "Contents",  "This is another page with <a href=Form.html>one link</a>" );

        _wc.getClientProperties().setIframeSupported( false );
        WebResponse response = _wc.getResponse( getHostPath() + "/Frame.html" );
        WebRequest[] requests = response.getFrameRequests();
        assertEquals( "Number of links in main frame", 1, response.getLinks().length );
        assertEquals( "Number of forms in main frame", 1, response.getForms().length );
        assertEquals( "Number of frame requests", 0, requests.length );
    }


    /**
     * Verifies that an open call from a subframe can specify another frame name.
     */
    public void testOpenIntoSubframe() throws Exception {
        defineResource( "Frames.html",
                        "<html><head><frameset>" +
                        "    <frame name='banner'>" +
                        "    <frame src='main.html' name='main'>" +
                        "</frameset></html>" );
        defineResource( "target.txt", "You made it!" );
        defineWebPage( "main", "<button id='button' onclick=\"window.open( 'target.txt', 'banner' )\">" );

        _wc.getResponse( getHostPath() + "/Frames.html" );
        ((Button) _wc.getFrameContents( "main" ).getElementWithID( "button" )).click();
        assertEquals( "Num open windows", 1, _wc.getOpenWindows().length );
        assertEquals( "New banner", "You made it!", _wc.getFrameContents( "banner" ).getText() );
        assertNotNull( "Original button no longer there",  _wc.getFrameContents( "main" ).getElementWithID( "button" ) );
    }



    /**
     * Verifies that an open call from a subframe can specify another frame name.
     */
    public void testSelfOpenFromSubframe() throws Exception {
        defineResource( "Frames.html",
                        "<html><head><frameset>" +
                        "    <frame name='banner' src='banner.html'>" +
                        "    <frame name='main'   src='main.html'>" +
                        "</frameset></html>" );
        defineResource( "target.txt", "You made it!" );
        defineWebPage( "main", "<button id='button2' onclick=\"window.open( 'target.txt', 'banner' )\">" );
        defineWebPage( "banner", "<button id='button1' onclick=\"window.open( 'target.txt', '_self' )\">" );

        _wc.getResponse( getHostPath() + "/Frames.html" );
        ((Button) _wc.getFrameContents( "banner" ).getElementWithID( "button1" )).click();
        assertEquals( "Num open windows", 1, _wc.getOpenWindows().length );
        assertEquals( "New banner", "You made it!", _wc.getFrameContents( "banner" ).getText() );
        assertNotNull( "Second frame no longer there",  _wc.getFrameContents( "main" ).getElementWithID( "button2" ) );
    }



    /**
     * Verifies that an open call from a subframe can specify another frame name.
     */
    public void testFrameWithHashSource() throws Exception {
        defineResource( "Frames.html",
                        "<html><head><frameset>" +
                        "    <frame name='banner' src='#'>" +
                        "    <frame name='main'   src='main.html'>" +
                        "</frameset></html>" );
        defineResource( "target.txt", "You made it!" );
        defineWebPage( "main", "<a id='banner' href='target.txt'>banner</a>" );

        _wc.getResponse( getHostPath() + "/Frames.html" );
        WebLink link = (WebLink) _wc.getFrameContents( "main" ).getElementWithID( "banner" );
        assertNotNull( "No link found",  link );
    }


    private WebConversation _wc;
}
