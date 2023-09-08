package com.meterware.httpunit.javascript;
/********************************************************************************************************************
 * $Id: HTMLElementTest.java 107617 2012-01-03 09:51:15Z jst $
 *
 * Copyright (c) 2002-2003, Russell Gold
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
import junit.textui.TestRunner;
import junit.framework.TestSuite;
import com.meterware.httpunit.*;

/**
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
public class HTMLElementTest  extends HttpUnitTest {

    public static void main( String args[] ) {
        TestRunner.run( suite() );
    }


    public static TestSuite suite() {
        return new TestSuite( HTMLElementTest.class );
    }


    public HTMLElementTest( String name ) {
        super( name );
    }


    public void testIDProperty() throws Exception {
        defineResource( "start.html",
                       "<html><head><script language='JavaScript'>" +
                       "function showTitle( id ) {" +
                       "   alert( 'element with id ' + id + ' has title ' + document.getElementById( id ).title );" +
                       "}" +
                       "function showAll() {" +
                       "    showTitle( 'there' ); showTitle( 'perform' ); showTitle( 'doIt' );" +
                       "    showTitle( 'grouping' ); showTitle( 'aCell' ); showTitle( 'myDiv' );\n" +
                       "}</script>" +
                       "</head><body onLoad='showAll();'>" +
                       "<div id=myDiv title=first><a href='somewhere' id='there' title=second>here</a>" +
                       "<table id=grouping title=third><tr><td id='aCell' title=fourth>" +
                       "<form id='perform' title=fifth><input type='submit' id='doIt' title=sixth></form>" +
                       "</td></tr></table></div>" );
        WebConversation wc = new WebConversation();
        wc.getResponse( getHostPath() + "/start.html" );

        assertElementTitle( wc, "id", "there", "second" );
        assertElementTitle( wc, "id", "perform", "fifth" );
        assertElementTitle( wc, "id", "doIt", "sixth" );
        assertElementTitle( wc, "id", "grouping", "third" );
        assertElementTitle( wc, "id", "aCell", "fourth" );
        assertElementTitle( wc, "id", "myDiv", "first" );
    }


    public void testElementByIdReturnsNull() throws Exception {
        defineResource( "start.html",
                       "<html><head><script language='JavaScript'>" +
                       "function showNone() {" +
                       "    alert( 'It returned ' + document.getElementById( 'zork' ) )" +
                       "}</script>" +
                       "</head><body onLoad='showNone();'>" +
                       "<div id=myDiv title=first><a href='somewhere' id='there' title=second>here</a>" +
                       "<table id=grouping title=third><tr><td id='aCell' title=fourth>" +
                       "<form id='perform' title=fifth><input type='submit' id='doIt' title=sixth></form>" +
                       "</td></tr></table></div>" );
        WebConversation wc = new WebConversation();
        wc.getResponse( getHostPath() + "/start.html" );

        assertEquals( "Null test alert", "It returned null", wc.popNextAlert() );
    }


    public void testNameProperty() throws Exception {
        defineResource( "start.html",
                       "<html><head><script language='JavaScript'>" +
                       "function showTitle( name ) {" +
                       "  var elements = document.getElementsByName( name );" +
                       "  for( i = 0; i < elements.length; i++) {" +
                       "   alert( 'element with name ' + name + ' has title ' + elements[i].title );" +
                       "  }" +
                       "}" +
                       "function showAll() {" +
                       "    showTitle( 'there' ); showTitle( 'perform' ); showTitle( 'doIt' );" +
                       "    showTitle( 'input' );" +
                       "}</script>" +
                       "</head><body onLoad='showAll();'>" +
                       "<a href='somewhere' name='there' title=second>here</a>" +
                       "<form name='perform' title=fifth><input type='text' name='input' title='input1'>" +
                       "<input type='hidden' name='input' title='input2'><input type='submit' name='doIt' title=sixth></form>" +
                       "</td></tr></table></div></body></html>" );
        WebConversation wc = new WebConversation();
        wc.getResponse( getHostPath() + "/start.html" );

        assertElementTitle( wc, "name", "there", "second" );
        assertElementTitle( wc, "name", "perform", "fifth" );
        assertElementTitle( wc, "name", "doIt", "sixth" );
        assertElementTitle( wc, "name", "input", "input1" );
        assertElementTitle( wc, "name", "input", "input2" );
    }


    public void testNamePropertyWithIdAttribute() throws Exception {
        defineResource( "start.html",
                       "<html><head><script language='JavaScript'>" +
                       "function showAll() {" +
                       "    alert( 'element with name there has title '   + document.there.title );" +
                       "    alert( 'element with name perform has title ' + document.perform.title );" +
                       "    alert( 'element with name seeme has title '   + document.seeme.title );" +
                       "}</script>" +
                       "</head><body onLoad='showAll();'>" +
                       "<a href='somewhere' id='there' title=second>here</a>" +
                       "<form id='perform' title=fifth></form>" +
                       "<img id='seeme' title='haha' src='haha.jpg'>" +
                       "</td></tr></table></div></body></html>" );
        WebConversation wc = new WebConversation();
        wc.getResponse( getHostPath() + "/start.html" );

        assertElementTitle( wc, "name", "there", "second" );
        assertElementTitle( wc, "name", "perform", "fifth" );
        assertElementTitle( wc, "name", "seeme", "haha" );
    }


    private void assertElementTitle( WebConversation wc, String propertyName, final String id, final String title ) {
        assertEquals( "element '" + id + "' message", "element with " + propertyName + ' ' + id + " has title " + title, wc.popNextAlert() );
    }


    public void testElementProperties() throws Exception {
        defineWebPage( "start",
                       "<form name='perform' title=fifth>" +
                       "  <input name='name' maxlength=20 tabindex='1'>" +
                       "</form>" );
        WebConversation wc = new WebConversation();
        WebResponse response = wc.getResponse( getHostPath() + "/start.html" );

        assertEquals( "tabindex", "1", response.getScriptingHandler().evaluateExpression( "document.perform.name.tabindex").toString() );
        assertEquals( "maxlength", "20", response.getScriptingHandler().evaluateExpression( "document.perform.name.maxlength").toString() );
    }



}
