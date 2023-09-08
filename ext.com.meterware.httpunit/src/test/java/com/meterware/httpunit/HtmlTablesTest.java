package com.meterware.httpunit;
/********************************************************************************************************************
* $Id: HtmlTablesTest.java 107617 2012-01-03 09:51:15Z jst $
*
* Copyright (c) 2000-2008, Russell Gold
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
 * A unit test of the table handling code.
 *
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 * @author <a href="mailto:bx@bigfoot.com">Benoit Xhenseval</a>
 **/
public class HtmlTablesTest extends HttpUnitTest {

    public static void main(String args[]) {
        junit.textui.TestRunner.run( suite() );
    }


    public static TestSuite suite() {
        return new TestSuite( HtmlTablesTest.class );
    }


    public HtmlTablesTest( String name ) {
        super( name );
    }


    public void setUp() throws Exception {
        super.setUp();
        _wc = new WebConversation();

        defineWebPage( "OneTable", "<h2>Interesting data</h2>" +
                                   "<table summary=\"tough luck\">" +
                                   "<tr><th>One</th><td>&nbsp;</td><td>1</td></tr>" +
                                   "<tr><td colspan=3><IMG SRC=\"/images/spacer.gif\" ALT=\"\" WIDTH=1 HEIGHT=1></td></tr>" +
                                   "<tr><th>Two</th><td>&nbsp;</td><td>2</td></tr>" +
                                   "<tr><td colspan=3><IMG SRC=\"/images/spacer.gif\" ALT=\"\" WIDTH=1 HEIGHT=1></td></tr>" +
                                   "<tr><th>Three</th><td>&nbsp;</td><td>3</td></tr>" +
                                   "</table>" );
        defineWebPage( "SpanTable", "<h2>Interesting data</h2>" +
                                    "<table summary=\"tough luck\">" +
                                    "<tr><th colspan=2>Colors</th><th>Names</th></tr>" +
                                    "<tr><td>Red</td><td rowspan=\"2\"><b>gules</b></td><td>rot</td></tr>" +
                                    "<tr><td>Green</td><td><a href=\"nowhere\">vert</a></td></tr>" +
                                    "</table>" );
    }


    public void testFindNoTables() throws Exception {
        defineWebPage( "Default", "This has no tables but it does" +
                                  "have <a href=\"/other.html\">an active link</A>" +
                                  " and <a name=here>an anchor</a>" );

        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebTable[] tables = page.getTables();
        assertNotNull( tables );
        assertEquals( 0, tables.length );
    }


    public void testFindOneTable() throws Exception {
        WebTable[] tables = _wc.getResponse( getHostPath() + "/OneTable.html" ).getTables();
        assertEquals( 1, tables.length );
    }

    /**
     * test for patch [ 1117822 ] Patch for purgeEmptyCells() problem
     * by Glen Stampoultzis
     * @throws Exception
     */
    public void testPurgeEmptyCells()   throws Exception {
			 defineWebPage( "StrangeSpan", "<h2>Interesting data</h2>" +
	         "<table class=\"headerTable\" width=\"97%\" cellspacing=\"2\" cellpadding=\"0\" border=\"0\" id=\"personalTable\">\n" +
	         "        <tr>\n" +
	         "          <th colspan=\"6\"><img src=\"images/curve-left.gif\" align=\"top\" border=\"0\">Notifications:</th>\n" +
	         "        </tr>\n" +
	         "\n" +
	         "<tr> <td width=\"10\">&nbsp;</td>\n" +
	         "          <td colspan=\"5\">None</td>\n" +
	         "\n" +
	         "</tr> <tr>\n" +
	         "          <th colspan=\"6\"><img src=\"images/curve-left.gif\" align=\"top\" border=\"0\">Watches:</th>\n" +
	         "        </tr>\n" +
	         "\n" +
	         "<tr> <td>&nbsp;</td>\n" +
	         "          <td colspan=\"5\">None</td>\n" +
	         "</tr> <tr>\n" +
	         "          <th colspan=\"6\"><img src=\"images/curve-left.gif\" align=\"top\" border=\"0\">Messages:</th>\n" +
	         "\n" +
	         "        </tr>\n" +
	         "\n" +
	         "<tr> <td>&nbsp;</td>\n" +
	         "          <td colspan=\"5\">None</td>\n" +
	         "</tr> <tr>\n" +
	         "          <th colspan=\"6\"><img src=\"images/curve-left.gif\" align=\"top\" border=\"0\">Favourite Documents:</th>\n" +
	         "        </tr>\n" +
	         "\n" +
	         "<tr> <td>&nbsp;</td>\n" +
	         "\n" +
	         "          <td colspan=\"5\">None</td>\n" +
	         "</tr>\t</table>" );			
			WebTable table = _wc.getResponse( getHostPath() + "/StrangeSpan.html" ).getTables()[0];
      assertNotNull( table );
			 
			assertEquals(6, table.getColumnCount());
			assertEquals(8, table.getRowCount());
			table.purgeEmptyCells();
			assertEquals("after purging Cells there should be 2 columns left",2, table.getColumnCount());
			assertEquals(8, table.getRowCount());
			String[][] text = table.asText();
			int row = 0;
			assertEquals("Notifications:", text[row][0]);
			assertEquals("Notifications:", text[row++][1]);
			assertEquals("", text[row][0]);
			assertEquals("None", text[row++][1]);
			assertEquals("Watches:", text[row][0]);
			assertEquals("Watches:", text[row++][1]);
			assertEquals("", text[row][0]);
			assertEquals("None", text[row++][1]);
			assertEquals("Messages:", text[row][0]);
			assertEquals("Messages:", text[row++][1]);
			assertEquals("", text[row][0]);
			assertEquals("None", text[row++][1]);
			assertEquals("Favourite Documents:", text[row][0]);
			assertEquals("Favourite Documents:", text[row++][1]);
			assertEquals("", text[row][0]);
			assertEquals("None", text[row++][1]);
    }
    
  /**
   * test for bug report [ 1295782 ] Method purgeEmptyCells Truncates Table
   * by ahansen 2005-09-19 22:47
   * @throws Exception
   */
  public void testPurgeEmptyCells2() throws Exception {
    defineWebPage( "BrokenSpan", "<h2>Broken Span</h2>" +
        "<table id=\"testTable\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">" +
        "   <tr>" +
        "       <td><img src=\"test.jpg\"/></td>" +
        "       <td colspan=\"2\">h3</td>" +
        "   </tr>" +
        "   <tr>" +
        "       <td colspan=\"2\">a</td>" +
        "       <td>1</td>" +
        "   </tr>" +
        "</table>"
        );          	
    WebResponse page = _wc.getResponse( getHostPath() + "/BrokenSpan.html" );
    WebTable table = page.getTables()[0];
    //String expected="WebTable:\n[0]:   [0]=  [1]=h3  [2]=h3\n[1]:   [0]=a  [1]=a  [2]=1";
    String expected=table.toString();
    table.purgeEmptyCells();
    assertEquals("1st",table.toString(),expected);
    table.purgeEmptyCells();
    assertEquals("2nd",table.toString(),expected);
  }


    /** 
     * test finding the Table Size
     * @throws Exception
     */
    public void testFindTableSize() throws Exception {
        WebTable table = _wc.getResponse( getHostPath() + "/OneTable.html" ).getTables()[0];
        assertEquals( 5, table.getRowCount() );
        assertEquals( 3, table.getColumnCount() );
        try {
            table.getCellAsText( 5, 0 );
            fail( "Should throw out of range exception" );
        } catch (IndexOutOfBoundsException e ) {
        }
        try {
            table.getCellAsText( 0, 3 );
            fail( "Should throw out of range exception" );
        } catch (RuntimeException e ) {
        }
    }


    public void testFindTableCell() throws Exception {
        WebTable table = _wc.getResponse( getHostPath() + "/OneTable.html" ).getTables()[0];
        assertEquals( "Two", table.getCellAsText( 2, 0 ) );
        assertEquals( "3",   table.getCellAsText( 4, 2 ) );
    }


    public void testTableAsText() throws Exception {
       WebTable table = _wc.getResponse( getHostPath() + "/OneTable.html" ).getTables()[0];
       table.purgeEmptyCells();
       String[][] text = table.asText();
       assertEquals( "rows with text", 3, text.length );
       assertEquals( "Two", text[1][0] );
       assertEquals( "3", text[2][1] );
       assertEquals( "columns with text", 2, text[0].length );
    }



    public void testNestedTable() throws Exception {
        defineWebPage( "Default", "<h2>Interesting data</h2>" +
                                  "<table summary=\"outer one\">" +
                                  "<tr><td>" +
                                  "Inner Table<br>" +
                                  "<table summary=\"inner one\">" +
                                  "        <tr><td>Red</td><td>1</td></tr>" +
                                  "        <tr><td>Blue</td><td>2</td></tr>" +
                                  "</table></td></tr>" +
                                  "</table>" );

        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebTable[] tables = page.getTables();
        assertEquals( "top level tables count", 1, tables.length );
        assertEquals( "rows", 1, tables[0].getRowCount() );
        assertEquals( "columns", 1, tables[0].getColumnCount() );
        WebTable[] nested = tables[0].getTableCell( 0, 0 ).getTables();
        assertEquals( "nested tables count", 1, nested.length );
        assertEquals( "nested rows", 2, nested[0].getRowCount() );
        assertEquals( "nested columns", 2, nested[0].getColumnCount() );

        String nestedString = tables[0].getCellAsText( 0, 0 );
        assertTrue( "Cannot find 'Red' in string", nestedString.indexOf( "Red" ) >= 0 );
        assertTrue( "Cannot find 'Blue' in string", nestedString.indexOf( "Blue" ) >= 0 );
    }


    public void testColumnSpan() throws Exception {
        WebResponse page = _wc.getResponse( getHostPath() + "/SpanTable.html" );
        WebTable table = page.getTables()[0];
        assertEquals( "Colors", table.getCellAsText( 0, 0 ) );
        assertEquals( "Colors", table.getCellAsText( 0, 1 ) );
        assertEquals( "Names",  table.getCellAsText( 0, 2 ) );
        assertSame( table.getTableCell( 0, 0 ), table.getTableCell( 0, 1 ) );
    }

    public static String htmlForBug1043368="<HTML>\n"+
      "<head>\n"+
      "<title>FormTable Servlet GET</title>\n"+
      "</head>\n<body>\n"+
      "<FORM METHOD=\"POST\" ACTION=\"/some/action\">\n"+
      "<TABLE>\n"+
      "   <TR><TD colspan=\"4\">Test Form:</TD></TR>\n\n"+
      "   <TR>\n"+
      "       <TD>*Contact Name:</TD>\n"+
      "       <TD><input type=\"text\" size=\"21\" name=\"CONTACT_NAME\" value=\"TIMOTHY O'LEARY\"></TD>\n"+
      "       <TD>Building Number:</TD>\n"+
      "       <TD><input type=\"text\" size=\"7\" name=\"BUILDING_NUMBER\" value=\"355\"></TD>\n"+
      "   </TR>\n"+
      "</TABLE>\n"+
      "</FORM>";

    /**
     * test for bug report [ 1043368 ] WebTable has wrong number of columns
     * by AutoTest
     */
    public void testColumnNumberInTable() throws Exception {
        defineWebPage( "Default", htmlForBug1043368 );
        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebTable table = page.getTableStartingWithPrefix( "Test Form" );
        assertNotNull( "didn't find table", table );
        // System.out.println( table.toString() );
        assertFalse( "wrong table", table.getCellAsText( 1, 0 ).indexOf( "Contact Name" ) == -1 );
        assertEquals( "wrong column count", 4, table.getColumnCount() );
    }


    public void testRowSpan() throws Exception {
        WebResponse page = _wc.getResponse( getHostPath() + "/SpanTable.html" );
        WebTable table = page.getTables()[0];
        assertEquals( 3, table.getRowCount() );
        assertEquals( 3, table.getColumnCount() );
        assertEquals( "gules", table.getCellAsText( 1, 1 ) );
        assertEquals( "gules", table.getCellAsText( 2, 1 ) );
        assertEquals( "vert",  table.getCellAsText( 2, 2 ) );
        assertSame( table.getTableCell( 1, 1 ), table.getTableCell( 2, 1 ) );
    }


    public void testMissingColumns() throws Exception {
        defineWebPage( "Default", "<h2>Interesting data</h2>" +
                                  "<table summary=\"tough luck\">" +
                                  "<tr><th colspan=2>Colors</th><th>Names</th></tr>" +
                                  "<tr><td>Red</td><td rowspan=\"2\"><b>gules</b></td></tr>" +
                                  "<tr><td>Green</td><td><a href=\"nowhere\">vert</a></td></tr>" +
                                  "</table>" );

        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebTable table = page.getTables()[0];
        table.purgeEmptyCells();
        assertEquals( 3, table.getRowCount() );
        assertEquals( 3, table.getColumnCount() );
    }


    public void testInnerTableSeek() throws Exception {
        defineWebPage( "Default", "<h2>Interesting data</h2>" +
                                  "<table id=you summary=\"outer one\">" +
                                  "<tr><td>Here we are</td><td>" +
                                  "Inner Table 1<br>" +
                                  "<table id=you summary='inner zero'>" +
                                  "        <tr><td colspan=2>&nbsp;</td></tr>" +
                                  "        <tr><td>\nRed\n</td><td>1</td></tr>" +
                                  "        <tr><td>Blue</td><td>2</td></tr>" +
                                  "</table></td><td>" +
                                  "Inner Table 2<br>" +
                                  "<table id=me summary=\"inner one\">" +
                                  "        <tr><td colspan=2>&nbsp;</td></tr>" +
                                  "        <tr><td>Black</td><td>1</td></tr>" +
                                  "        <tr><td>White</td><td>2</td></tr>" +
                                  "</table></td></tr>" +
                                  "</table>" );

        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebTable wt = page.getTableStartingWith( "Red" );
        assertNotNull( "Did not find table starting with 'Red'", wt );
        wt.purgeEmptyCells();
        String[][] cells = wt.asText();
        assertEquals( "Non-blank rows",    2, cells.length );
        assertEquals( "Non-blank columns", 2, cells[0].length );
        assertEquals( "cell at 1,0",       "Blue", cells[1][0] );

        wt = page.getTableStartingWithPrefix( "Re" );
        assertNotNull( "Did not find table starting with prefix 'Re'", wt );
        cells = wt.asText();
        assertEquals( "Non-blank rows",    2, cells.length );
        assertEquals( "Non-blank columns", 2, cells[0].length );
        assertEquals( "cell at 1,0",       "Blue", cells[1][0] );

        wt = page.getTableWithSummary( "Inner One" );
        assertNotNull( "Did not find table with summary 'Inner One'", wt );
        cells = wt.asText();
        assertEquals( "Total rows",    3, cells.length );
        assertEquals( "Total columns", 2, cells[0].length );
        assertEquals( "cell at 2,0",       "White", cells[2][0] );

        wt = page.getTableWithID( "me" );
        assertNotNull( "Did not find table with id 'me'", wt );
        cells = wt.asText();
        assertEquals( "Total rows",    3, cells.length );
        assertEquals( "Total columns", 2, cells[0].length );
        assertEquals( "cell at 2,0",       "White", cells[2][0] );
    }


    public void testSpanOverEmptyColumns() throws Exception {
        defineWebPage( "Default", "<h2>Interesting data</h2>" +
                                  "<table summary=little>" +
                                  "<tr><td colspan=2>Title</td><td>Data</td></tr>" +
                                  "<tr><td>Name</td><td>&nbsp;</td><td>Value</td></tr>" +
                                  "<tr><td>Name</td><td>&nbsp;</td><td>Value</td></tr>" +
                                  "</table>" );

        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebTable table = page.getTableStartingWith( "Title" );
        table.purgeEmptyCells();
        String[][] cells = table.asText();
        assertEquals( "Non-blank rows",    3, cells.length );
        assertEquals( "Non-blank columns", 2, cells[0].length );
        assertEquals( "cell at 1,1",       "Value", cells[1][1] );
    }


    public void testSpanOverAllEmptyColumns() throws Exception {
        defineWebPage( "Default", "<h2>Interesting data</h2>" +
                                  "<table summary=little>" +
                                  "<tr><td colspan=2>Title</td><td>Data</td></tr>" +
                                  "<tr><td>&nbsp;</td><td>&nbsp;</td><td>Value</td></tr>" +
                                  "<tr><td>&nbsp;</td><td>&nbsp;</td><td>Value</td></tr>" +
                                  "</table>" );

        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebTable table = page.getTableStartingWith( "Title" );
        table.purgeEmptyCells();
        String[][] cells = table.asText();
        assertEquals( "Non-blank rows",    3, cells.length );
        assertEquals( "Non-blank columns", 2, cells[0].length );
        assertEquals( "cell at 1,1",       "Value", cells[1][1] );
    }


    public void testTableInParagraph() throws Exception {
        defineWebPage( "Default", "<p>" +
                                  "<table summary=little>" +
                                  "<tr><td>a</td><td>b</td><td>Value</td></tr>" +
                                  "<tr><td>c</td><td>d</td><td>Value</td></tr>" +
                                  "</table></p>" );

        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        assertEquals( "Number of tables in paragraph", 1, page.getTextBlocks()[0].getTables().length );
        assertEquals( "Number of tables in page", 1, page.getTables().length );
    }

    /**
     * Get a specific cell with a given id in a WebTable
     **/
    public void testCellsWithID() throws Exception {
        defineWebPage( "Default", "<h2>Interesting data</h2>" +
                                  "<table id=\"table\" summary=little>" +
                                  "<tr><td>Title</td><td>Data</td></tr>" +
                                  "<tr><td id=\"id1\">value1</td><td id=\"id2\">value2</td><td>Value</td></tr>" +
                                  "<tr><td>&nbsp;</td><td>&nbsp;</td><td>Value</td></tr>" +
                                  "</table>" );

        WebResponse page = _wc.getResponse( getHostPath() + "/Default.html" );
        WebTable table = page.getTableWithID("table");
        assertNotNull("there is a table",table);
        TableCell cell = table.getTableCellWithID("id1");
        assertNotNull("cell id1",cell);
        assertEquals("Value of cell id1","value1",cell.getText());
        cell = table.getTableCellWithID("id2");
        assertNotNull("cell id2",cell);
        assertEquals("Value of cell id2","value2",cell.getText());

        // test non existent cell id
        cell = table.getTableCellWithID("nonExistingID");
        assertNull("cell id2",cell);

        cell = (TableCell) page.getElementWithID( "id1" );
        assertEquals( "value of cell found from page", "value1", cell.getText() );
    }

    /**
     * Test that the tag name can be extracted for a cell.
     */
    public void testCellTagName() throws Exception {
        WebTable table = _wc.getResponse( getHostPath() + "/OneTable.html" ).getTables()[0];
        assertEquals("Tag name of header cell", table.getTableCell(0,0).getTagName().toUpperCase(), "TH");
        assertEquals("Tag name of non-header cell", table.getTableCell(0,1).getTagName().toUpperCase(), "TD");
    }

    private WebConversation _wc;
}

