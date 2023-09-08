package com.meterware.httpunit.javascript;
/********************************************************************************************************************
 * $Id: DocumentScriptingTest.java 107617 2012-01-03 09:51:15Z jst $
 *
 * Copyright (c) 2002-2008, Russell Gold
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
import junit.textui.TestRunner;
import com.meterware.httpunit.*;


public class DocumentScriptingTest extends HttpUnitTest {

    public static void main( String args[] ) {
        TestRunner.run( suite() );
    }


    public static TestSuite suite() {
        return new TestSuite( DocumentScriptingTest.class );
    }


    public DocumentScriptingTest( String name ) {
        super( name );
    }


    public void testDocumentTitle() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head><title>Amazing!</title></head>" +
                                            "<body onLoad='alert(\"Window title is \" + document.title)'></body>" );
        WebConversation wc = new WebConversation();
        wc.getResponse( getHostPath() + "/OnCommand.html" );
        assertEquals( "Alert message", "Window title is Amazing!", wc.popNextAlert() );
    }


    public void testDocumentFindForms() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head><script language='JavaScript'>" +
                                            "function getFound( object ) {" +
                                            "  return (object == null) ? \"did not find \" : \"found \";" +
                                            "  }" +
                                            "function viewForms() { " +
                                            "  alert( \"found \" + document.forms.length + \" form(s)\" );" +
                                            "  alert( getFound( document.realform ) + \"form 'realform'\" );" +
                                            "  alert( getFound( document.forms[\"realform\"] ) + \"form 'forms[\'realform\']'\" );" +
                                            "  alert( getFound( document.noform ) + \"form 'noform'\" ); }" +
                                            "</script></head>" +
                                            "<body onLoad='viewForms()'>" +
                                            "<form name='realform'></form>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        wc.getResponse( getHostPath() + "/OnCommand.html" );
        assertEquals( "Alert message", "found 1 form(s)", wc.popNextAlert() );
        assertEquals( "Alert message", "found form 'realform'", wc.popNextAlert() );
        assertEquals( "Alert message", "found form 'forms[\'realform\']'", wc.popNextAlert() );
        assertEquals( "Alert message", "did not find form 'noform'", wc.popNextAlert() );
        assertNull( "Alert should have been removed", wc.getNextAlert() );
    }


    public void testDocumentFindLinks() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head><script language='JavaScript'>" +
                                            "function getFound( object ) {" +
                                            "  return (object == null) ? \"did not find \" : \"found \";" +
                                            "  }" +
                                            "function viewLinks() { " +
                                            "  alert( \"found \" + document.links.length + \" link(s)\" );" +
                                            "  alert( getFound( document.reallink ) + \"link 'reallink'\" );" +
                                            "  alert( getFound( document.links[\"reallink\"] ) + \"link 'links[reallink]'\" );" +
                                            "  alert( getFound( document.nolink ) + \"link 'nolink'\" );" +
                                            "}" +
                                            "</script></head>" +
                                            "<body onLoad='viewLinks()'>" +
                                            "<a href='something' name='reallink'>first</a>" +
                                            "<a href='else'>second</a>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        wc.getResponse( getHostPath() + "/OnCommand.html" );
        assertEquals( "Alert message", "found 2 link(s)", wc.popNextAlert() );
        assertEquals( "Alert message", "found link 'reallink'", wc.popNextAlert() );
        assertEquals( "Alert message", "found link 'links[reallink]'", wc.popNextAlert() );
        assertEquals( "Alert message", "did not find link 'nolink'", wc.popNextAlert() );
        assertNull( "Alert should have been removed", wc.getNextAlert() );
    }


    public void testJavaScriptObjectIdentity() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head><script language='JavaScript'>" +
                                            "function compareLinks() { " +
                                            "  if (document.reallink == document.links['reallink']) {" +
                                            "      alert( 'they are the same' );" +
                                            "  } else {" +
                                            "      alert( 'they are different' );" +
                                            "  }" +
                                            "}" +
                                            "</script></head>" +
                                            "<body onLoad='compareLinks()'>" +
                                            "<a href='something' name='reallink'>first</a>" +
                                            "<a href='else'>second</a>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        wc.getResponse( getHostPath() + "/OnCommand.html" );
        assertEquals( "Alert message", "they are the same", wc.popNextAlert() );
    }


    public void testCaseSensitiveNames() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head></head>" +
                                            "<body>" +
                                            "<form name='item' action='run'></form>" +
                                            "<a name='Item' href='sample.html'></a>" +
                                            "<a href='#' name='first' onMouseOver='alert( document.item.action );'>1</a>" +
                                            "<a href='#' name='second' onMouseOver='alert( document.Item.href );'>2</a>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        response.getLinkWithName( "first" ).mouseOver();
        assertEquals( "form action", "run", wc.popNextAlert() );
        response.getLinkWithName( "second" ).mouseOver();
        assertEquals( "link href", getHostPath() + "/sample.html", wc.popNextAlert() );
    }


    public void testLinkMouseOverEvent() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head></head>" +
                                            "<body>" +
                                            "<form name='realform'><input name='color' value='blue'></form>" +
                                            "<a href='#' onMouseOver=\"document.realform.color.value='green';return false;\">green</a>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        WebForm form = response.getFormWithName( "realform" );
        WebLink link = response.getLinks()[0];
        assertEquals( "initial parameter value", "blue", form.getParameterValue( "color" ) );
        link.mouseOver();
        assertEquals( "changed parameter value", "green", form.getParameterValue( "color" ) );
    }


    public void testLinkClickEvent() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head></head>" +
                                            "<body>" +
                                            "<form name='realform'><input name='color' value='blue'></form>" +
                                            "<a href='nothing.html' onClick=\"JavaScript:document.realform.color.value='green';return false;\">green</a>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        WebForm form = response.getFormWithName( "realform" );
        WebLink link = response.getLinks()[0];
        assertEquals( "initial parameter value", "blue", form.getParameterValue( "color" ) );
        link.click();
        assertEquals( "changed parameter value", "green", form.getParameterValue( "color" ) );
    }
    
    /**
     * test a mouse event on a link
     * @throws Exception
     */
    public void testLinkMouseDownEvent() throws Exception {
      defineResource(  "nothing.html",  "<html><head></head><body</body></html>");
      defineResource(  "OnMouseDown.html",  "<html><head></head>" +
                                          "<body>" +
                                          "<form name='realform'><input name='color' value='blue'></form>" +
                                          "<a href='nothing.html' onMouseDown=\"JavaScript:document.realform.color.value='green';return false;\">green</a>" +
                                          "</body></html>" );
      WebConversation wc = new WebConversation();
      WebResponse response = wc.getResponse( getHostPath() + "/OnMouseDown.html" );
      WebForm form = response.getFormWithName( "realform" );
      WebLink link = response.getLinks()[0];
      assertEquals( "initial parameter value", "blue", form.getParameterValue( "color" ) );
      link.click();
      assertEquals( "changed parameter value", "green", form.getParameterValue( "color" ) );
    }


    /**
     * Verifies that a link which simply specifies a 
     * fragment identifier does not cause a new request to be sent to the
     * server, so that the current response is unchanged.
     * @throws Exception
     */
    public void testHashDestinationOnClickEvent() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head></head>" +
                                            "<body>" +
                                            "<form name='realform'><input name='color' value='blue'></form>" +
                                            "<a href='#' onClick=\"document.realform.color.value='green';\">green</a>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        WebForm form = response.getFormWithName( "realform" );
        WebLink link = response.getLinks()[0];
        assertEquals( "initial parameter value", "blue", form.getParameterValue( "color" ) );
        response = link.click();
        assertEquals( "changed parameter value", "green", response.getFormWithName( "realform" ).getParameterValue( "color" ) );
    }

    /**
     * check on MouseDownEvent handling
     * @throws Exception
     */
    public void testHashDestinationOnMouseDownEvent() throws Exception {
        defineResource(  "OnMouseDown.html",  "<html><head></head>" +
                                            "<body>" +
                                            "<form name='realform'><input name='color' value='blue'></form>" +
                                            "<a href='#' onMouseDown=\"document.realform.color.value='green';\">green</a>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnMouseDown.html" );
        WebForm form = response.getFormWithName( "realform" );
        WebLink link = response.getLinks()[0];
        assertEquals( "initial parameter value", "blue", form.getParameterValue( "color" ) );
        response = link.click();
        assertEquals( "changed parameter value", "green", response.getFormWithName( "realform" ).getParameterValue( "color" ) );
    }

    public void testLinkProperties() throws Exception {
        defineResource( "somewhere.html?with=values", "you made it!" );
        defineResource(  "OnCommand.html",  "<html><head></head>" +
                                            "<body>" +
                                            "<a name=target href='nowhere.html'>" +
                                            "<a name=control href='#' onClick=\"document.target.href='somewhere.html?with=values';\">green</a>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        WebLink link = response.getLinkWithName( "target" );
        assertEquals( "initial value", "nowhere.html", link.getURLString() );
        response.getLinkWithName( "control" ).click();
        assertEquals( "changed reference", getHostPath() + "/somewhere.html?with=values", link.getRequest().getURL().toExternalForm() );
        response = link.click();
        assertEquals( "New page", "you made it!", response.getText() );
    }


    public void testLinkIndexes() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head><script language='JavaScript'>" +
                                            "function alertLinks() { " +
                                            "  for (var i=0; i < document.links.length; i++) {" +
                                            "    alert( document.links[i].href );" +
                                            "  }" +
                                            "}" +
                                            "</script></head>" +
                                            "<body onLoad='alertLinks()'>" +
                                            "<a href='demo.html'>green</a>" +
                                            "<map name='map1'>" +
                                            "  <area href='guide.html' alt='Guide' shape='rect' coords='0,0,118,28'>" +
                                            "  <area href='search.html' alt='Search' shape='circle' coords='184,200,60'>" +
                                            "</map>" +
                                            "<a href='sample.html'>green</a>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        wc.getResponse( getHostPath() + "/OnCommand.html" );
        assertEquals( "Alert message", getHostPath() + "/demo.html", wc.popNextAlert() );
        assertEquals( "Alert message", getHostPath() + "/guide.html", wc.popNextAlert() );
        assertEquals( "Alert message", getHostPath() + "/search.html", wc.popNextAlert() );
        assertEquals( "Alert message", getHostPath() + "/sample.html", wc.popNextAlert() );
        assertNull( "Alert should have been removed", wc.getNextAlert() );
    }


    public void testDocumentFindImages() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head><script language='JavaScript'>" +
                                            "function getFound( object ) {\n" +
                                            "  return (object == null) ? \"did not find \" : \"found \";\n" +
                                            "  }\n" +
                                            "function viewImages() { \n" +
                                            "  alert( \"found \" + document.images.length + \" images(s)\" );\n" +
                                            "  alert( getFound( document.realimage ) + \"image 'realimage'\" )\n;" +
                                            "  alert( getFound( document.images['realimage'] ) + \"image 'images[realimage]'\" )\n;" +
                                            "  alert( getFound( document.noimage ) + \"image 'noimage'\" );\n" +
                                            "  alert( document.images[1].name ); }\n" +
                                            "</script></head>\n" +
                                            "<body onLoad='viewImages()'>\n" +
                                            "<img name='realimage' src='pict1.gif'>\n" +
                                            "<img name='2ndimage' src='pict2.gif'>\n" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        wc.getResponse( getHostPath() + "/OnCommand.html" );
        assertEquals( "Alert message", "found 2 images(s)", wc.popNextAlert() );
        assertEquals( "Alert message", "found image 'realimage'", wc.popNextAlert() );
        assertEquals( "Alert message", "found image 'images[realimage]'", wc.popNextAlert() );
        assertEquals( "Alert message", "did not find image 'noimage'", wc.popNextAlert() );
        assertEquals( "Alert message", "2ndimage", wc.popNextAlert() );
        assertNull( "Alert should have been removed", wc.getNextAlert() );
    }


    public void testImageSwap() throws Exception {
        defineResource(  "OnCommand.html",  "<html><head></head>" +
                                            "<body>" +
                                            "<img name='theImage' src='initial.gif'>" +
                                            "<a href='#' onMouseOver=\"document.theImage.src='new.jpg';\">green</a>" +
                                            "</body></html>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        WebImage image = response.getImageWithName( "theImage" );
        WebLink link = response.getLinks()[0];
        assertEquals( "initial image source", "initial.gif", image.getSource() );
        link.mouseOver();
        assertEquals( "changed image source", "new.jpg", image.getSource() );
    }



    public void testWriteToNewDocument() throws Exception {
        defineWebPage( "OnCommand", "<a href='#' onclick=\"window.open( '', 'empty' );w = window.open( '', 'sample' );w.document.open( 'text/plain' ); w.document.write( 'You made it!' );w.document.close()\" >" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/OnCommand.html" );
        WebLink link = response.getLinks()[0];
        link.click();
        WebWindow ww = wc.getOpenWindow( "sample");
        assertEquals( "Generated page", "You made it!", ww.getCurrentPage().getText() );
        assertEquals( "Content Type", "text/plain", ww.getCurrentPage().getContentType() );
        link.click();
        assertEquals( "Generated page", "You made it!", ww.getCurrentPage().getText() );
        assertEquals( "Empty page", "", wc.getOpenWindow("empty").getCurrentPage().getText() );
    }


    public void testSetDocumentReparse() throws Exception {
        defineResource( "index.html",
                        "<html><head>" +
                        "<script language='JavaScript ' >document.title = 'New title';</script>" +
                        "</head><body><form name=\"aForm\"></form>" +
                        "<script language='JavaScript'>alert(\"No of forms: \" + document.forms.length);</script>" +
                        "</body></html>");

        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/index.html" );
        assertEquals("No of forms", response.getForms().length, 1);
        assertEquals("JavaScript no of forms", "No of forms: 1", wc.popNextAlert());
    }


    public void testTagProperty() throws Exception {
        defineResource( "start.html",
                "<html><head><script language='JavaScript'>" +
                "function showFormsCount(oDOM){   " +
                "   var forms = oDOM.getElementsByTagName('form');" +
                "   for( i = 0; i < forms.length; i++) {" +
                "     alert( 'form with number ' + i + ' has ' + forms[i].getElementsByTagName('input').length + ' inputs' );" +
                "   }" +
                "}" +
                "function showAll() {" +
                "    showFormsCount(document);" +
                "}" +
                "</script></head><body onLoad='showAll();'>" +
                "<a href='somewhere' name='there' title=second>here</a>" +
                "<form name='perform1' title=fifth><input type='text' name='input' title='input1'></form>" +
                "<form name='perform2' title=fifth><input type='text' name='input' title='input1'>" +
                "<input type='hidden' name='input' title='input2'><input type='submit' name='doIt' title=sixth></form>" +
                "</body></html>"
                        );
        WebConversation wc = new WebConversation();
        wc.getResponse( getHostPath() + "/start.html" );

        assertElementTags( wc, "0", "1");
        assertElementTags( wc, "1", "3");
    }


    private void assertElementTags( WebConversation wc, String number, final String counts) {
        assertEquals( "form '" + number + "' message", "form with number " + number + " has " + counts +" inputs", wc.popNextAlert() );
    }

}
