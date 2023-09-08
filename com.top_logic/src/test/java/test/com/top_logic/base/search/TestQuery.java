/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.search;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.base.search.Query;
import com.top_logic.base.search.nodes.AllNode;
import com.top_logic.base.search.nodes.BinaryFilter;
import com.top_logic.base.search.nodes.BinaryOper;
import com.top_logic.base.search.nodes.ExampleNode;
import com.top_logic.base.search.nodes.LiteralNode;
import com.top_logic.base.search.nodes.UnaryFilter;
import com.top_logic.base.search.nodes.UnaryOper;
import com.top_logic.basic.DateUtil;
import com.top_logic.dob.simple.ExampleDataObject;

/** 
 * Tests cases for the classes found in 
 *  {@link com.top_logic.base.search}
 *<p>
 *  There are no single Test for the classes, instead they are mostly
 *  tested via the QueryParsers and Visitors.
 *</p>
 * 
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class TestQuery extends TestCase {

    /** 
     * Default CTor with name of testfunction to execute.
     */
    public TestQuery (String s) {
        super (s);
    }

    /** 
     * Test direct creation of simple Queries.
     */
    public void testSimpleQuery () throws Exception {
        
        String order[] =  { "Name" , "Age" };
        
        Query q = new Query(AllNode.ALL, order);
        
        assertEquals("A ORDER BY Name,Age"   , q.toString());
        assertEquals("ALL ORDER BY Name, Age", q.toInfix());
        
        q = new Query(AllNode.NONE, null);
    
        assertEquals("N"    ,q.toString());
        assertEquals("NONE" ,q.toInfix());
    }

    /** 
     * Test direct creation of an Eyample Query.
     */
    public void testExampleQuery () throws Exception {
        
        HashMap map = new HashMap(3);
        map.put("Name"      , "Michael");
		map.put("Age", Integer.valueOf(8));
		map.put("Birthday", DateUtil.createDate(1975, Calendar.JANUARY, 19, 7, 5, 35));
        
        ExampleDataObject ex = new ExampleDataObject(map);
        
        ExampleNode enode = new ExampleNode(ex);
        
        Query q = new Query(enode, null);
        
            // The actual order may vary due to the HashMap implementation.
        String qString = q.toString();
        assertTrue(qString.startsWith("E("));
        assertTrue(qString.endsWith  (")"));
        assertTrue(qString.indexOf("Name,\"Michael\"") 			   > 0);
        assertTrue(qString.indexOf("Birthday,1975-01-19 07:05:35") > 0);
        assertTrue(qString.indexOf("Age,8") 					   > 0);
        
        qString = q.toInfix();
        assertTrue(qString.startsWith("EXAMPLE ( "));
        assertTrue(qString.endsWith  (" )"));
        assertTrue(qString.indexOf(" Name , \"Michael\" ") 			   > 0);
        assertTrue(qString.indexOf(" Birthday , 1975-01-19 07:05:35 ") > 0);
        assertTrue(qString.indexOf(" Age , 8") 					       > 0);
    }

    /** 
     * Test direct creation of getFullText Queryies.
     */
    public void testFullTextQuery () throws Exception {
        
        Query query = Query.getFullTextQuery("", false, true);
        assertNull(query);
        
        query = Query.getFullTextQuery("  ", false, true);
        assertNull(query);

		query = Query.getFullTextQuery("   Amen ", false, true);
		assertEquals("$(\"Amen\")", query.toString());

        query = Query.getFullTextQuery("Amen", false, true);
        assertEquals("$(\"Amen\")", query.toString());

        query = Query.getFullTextQuery("Amen", true, true);
        assertEquals("$(\"Amen\")", query.toString());
        
        query = Query.getFullTextQuery("Amen sicher Kirche", true, true);
        assertEquals("&(&($(\"Amen\"),$(\"sicher\")),$(\"Kirche\"))", query.toString());

        query = Query.getFullTextQuery("Amen sicher Kirche", false, true);
        assertEquals("|(|($(\"Amen\"),$(\"sicher\")),$(\"Kirche\"))", query.toString());

		query = Query.getFullTextQuery("Amen ist\"sicher wie in\"\" Kirche\"", true, true);
		assertEquals("&(&(&($(\"Amen\"),$(\"ist\")),$(\"\\\"sicher wie in\\\"\")),$(\"\\\" Kirche\\\"\"))",
			query.toString());
    }

    /** 
     * Test direct creation of unary Queries.
     */
    public void testUnaryQuery () throws Exception {
                
        UnaryOper not = new UnaryOper(UnaryOper.NOT, AllNode.NONE);
        Query q = new Query(not, null);
    
        assertEquals("!(N)"         ,q.toString());
        assertEquals("NOT (NONE)"   ,q.toInfix());

        LiteralNode strLiteral = new LiteralNode(LiteralNode.STRING_LITERAL,"\"xxl\"");
        UnaryFilter id         = new UnaryFilter(UnaryOper.ID, strLiteral);
        q = new Query(id, null);

        assertEquals("@(\"xxl\")"   ,q.toString());
        assertEquals("ID (\"xxl\")" ,q.toInfix());

        LiteralNode dateLiteral = new LiteralNode(LiteralNode.DATE_LITERAL,new Date(999183535500L));
                    id          = new UnaryFilter(UnaryOper.ID, dateLiteral);
        q = new Query(id, null);

        assertEquals("@(2001-08-30)"   ,q.toString());
        assertEquals("ID (2001-08-30)" ,q.toInfix());

              strLiteral = new LiteralNode(LiteralNode.STRING_LITERAL,"\"Person\"");
        UnaryFilter type = new UnaryFilter(UnaryOper.TYPE, strLiteral);
        q = new Query(type, null);

        assertEquals("#(\"Person\")"     ,q.toString());
        assertEquals("TYPE (\"Person\")" ,q.toInfix());
    }

    /** 
     * Test direct creation of binary Queries.
     */
    public void testBinaryQuery () throws Exception {
                
        BinaryOper and = new BinaryOper(BinaryOper.AND, AllNode.ALL, AllNode.NONE);
        Query q = new Query(and, null);

        assertEquals("&(A,N)"           ,q.toString());
        assertEquals("( ALL AND NONE )" ,q.toInfix());

		LiteralNode intLiteral = new LiteralNode(LiteralNode.INTEGER_LITERAL, Integer.valueOf(12));
        BinaryFilter like      = new BinaryFilter(BinaryFilter.LIKE, 
                                 LiteralNode.LITERAL_TRUE, intLiteral);
        q = new Query(like, null);

        assertEquals("L(T,12)"          ,q.toString());
        assertEquals("( TRUE LIKE 12 )" ,q.toInfix());

        LiteralNode ident  = new LiteralNode(LiteralNode.IDENTIFIER,    "name");
        LiteralNode strLit = new LiteralNode(LiteralNode.STRING_LITERAL,"\"Hugo\"");
        BinaryFilter equals = new BinaryFilter(BinaryFilter.EQUALS, 
                                 ident, strLit);
        q = new Query(equals, null);

        assertEquals("=(name,\"Hugo\")"         ,q.toString());
        assertEquals("( name EQUALS \"Hugo\" )" ,q.toInfix());

        LiteralNode st  = new LiteralNode(LiteralNode.IDENTIFIER,        "someTime");
		LiteralNode dt =
			new LiteralNode(LiteralNode.DATE_TIME_LITERAL, DateUtil.createDate(2001, Calendar.AUGUST, 30, 16, 58, 55));
        BinaryFilter lt = new BinaryFilter(BinaryFilter.LT, st, dt);
        q = new Query(lt, null);

        assertEquals("<(someTime,2001-08-30 16:58:55)"       ,q.toString());
        assertEquals("( someTime LESS 2001-08-30 16:58:55 )" ,q.toInfix());
    }

    /** 
     * Test Queries with ORDER BY parts,
     */
    public void testOrderBy () throws Exception {
        Query q = Query.parse("A ORDER BY A,b,C");
        String order[] = q.getOrderBy();
        assertEquals("A", order[0]);
        assertEquals("b", order[1]);
        assertEquals("C", order[2]);

        q = Query.parseInfix(" NONE ORDER BY Me , Myself,And");
        order = q.getOrderBy();
        assertEquals("Me",      order[0]);
        assertEquals("Myself",  order[1]);
        assertEquals("And",     order[2]);
    }
    
    /** 
     * Test creation of Queries via the Prefix Parser.
     */
    public void testPrefix () throws Exception {
        
        String tests[] = {
            // Input        // Output
                "  A  "  ,  "A",     
                "ALL"    ,  "A",
                "N"      ,  "N",
                " NONE " ,  "N",
             // Example Data Objects, wont work with JKD1.4 any more
             // "E (Name, \"Michael\",Birthday,1975-01-19,Age, 010)",
             //     "E(Name,\"Michael\",Birthday,1975-01-19 00:00:00,Age,8)",
             // "EXAMPLE ( Name, \"Michael\",Birthday,11:11:11,Age, 011)",
             //     "E(Name,\"Michael\",Birthday,1970-01-01 11:11:11,Age,9)",
             // Unary Operator
                "!(ALL)" ,      "!(A)",
                "NOT (NONE)" ,  "!(N)",
             // Unary Filter
                "@(1965-07-14)"             , "@(1965-07-14)",
                "ID (11:22:13)"             , "@(11:22:13)",
                "$( 'Mammi' )"              , "$(\"Mammi\")",
                "TEXT (\"Pappi\")"          , "$(\"Pappi\")",
                "#(\"KnowledgeBlurb\")"     , "#(\"KnowledgeBlurb\")",  
                "TYPE (\"Schneudelkreuf\")" , "#(\"Schneudelkreuf\")", 
             // Binary Operator
                "&(NONE, ALL )"         , "&(N,A)", 
                "AND ( ALL, NONE)"      , "&(A,N)", 
                "|(N, ALL )"            , "|(N,A)", 
                "OR ( ALL, NONE)"       , "|(A,N)", 
                "M(NONE, ALL )"         , "M(N,A)",   
                "MATCHES ( ALL, N)"     , "M(A,N)",   
                "->(NONE, ALL )"        , "<-(N,A)",  
                "ISDEST( ALL, NONE)"    , "<-(A,N)",  
                "<-(N, ALL )"           , "->(N,A)",  
                "ISSOURCE( ALL, NONE)"  , "->(A,N)",  
                 // Binary Filter
                "=(Siebzehn, 4 )"           , "=(Siebzehn,4)",
                "EQUALS (Mathias,\"Maul\")" , "=(Mathias,\"Maul\")",
                "<(Age, 18 )"               , "<(Age,18)", 
                "LESS ( Hundert, 99.99f)"   , "<(Hundert,99.99)", 
                ">(Nothing, 0xFFFF )"       , ">(Nothing,65535)", 
                "GREATER ( Eleven, 021)"    , ">(Eleven,17)", 
                "<=(ok, TRUE )"             , "<=(ok,T)", 
                "LESSOREQUAL ( bad, 0D)"    , "<=(bad,0.0d)", 
                ">=(name, \"Samson\" )"     , ">=(name,\"Samson\")", 
                "GREATEROREQUAL(x,F)"       , ">=(x,F)", 
                "c=(anything, TRUE )"       , "c=(anything,T)", 
                "ISIN ( Ape, \"Banana\")"   , "c=(Ape,\"Banana\")", 
                "L(name, \"Fr?d*\" )"       , "L(name,\"Fr?d*\")", 
                "LIKE (time,2001-8-2 6:3:0)", "L(time,2001-08-02 06:03:00)" 
        };
        
        int len = tests.length;
        for (int i=0; i < len; i+= 2) {
            String expr     = tests[i];
            String expected = tests[i+1];
            // System.out.println("Checking :" + expr);
            Query q = Query.parse(expr);
            assertEquals(expected ,q.toString());
        }
    }        

    /** 
     * Test creation of Queries via the Infix Parser.
     */
    public void testInfix() throws Exception {
        
        String tests[] = {
            // Input        // Output
            "  A   "  ,  "ALL",     
            "ALL"     ,  "ALL",
            "NONE"    ,  "NONE",
            " N "     ,  "NONE",
        // Example Data Objects, wont work with JKD1.4 any more
        // "EXAMPLE(Name, \"BrocoliMA\",Birthday,1975-01-11,Age, 0xbl)",
        //    "EXAMPLE ( Name , \"BrocoliMA\" , Birthday , 1975-01-11 00:00:00 , Age , 11l )",
        // "E ( Name, \"BrokoliMA\",Birthday,  11:11:11,Age, 0xc)",
        //    "EXAMPLE ( Name , \"BrokoliMA\" , Birthday , 1970-01-01 11:11:11 , Age , 12 )",
        // Unary Operator
            "!(ALL)" ,   "NOT (ALL)",
            "NOT (N)" ,  "NOT (NONE)",
        // Unary Filter
            "@(TRUE)" ,  "ID (TRUE)",
            "ID (F)"  ,  "ID (FALSE)",
            "$(TRUE)" ,  "TEXT (TRUE)",
            "TEXT (F)",  "TEXT (FALSE)",
            "#(TRUE)" ,  "TYPE (TRUE)",  // will not work in later releases
            "TYPE (F)",  "TYPE (FALSE)", // will not work in later releases
        // Binary Operator
           "(N AND ALL )"           , "( NONE AND ALL )", 
           "( ALL AND  N)"          , "( ALL AND NONE )", 
           "(N OR ALL )"            , "( NONE OR ALL )", 
           " ( ALL OR N)"           , "( ALL OR NONE )", 
           "(NONE M ALL )"          , "( NONE MATCHES ALL )", 
           "( A MATCHES N)"         , "( ALL MATCHES NONE )",
           "(N -> A )"              , "( NONE ISDEST ALL )",
           "( ALL ISDEST NONE)"     , "( ALL ISDEST NONE )",  
           "(N<-ALL)"               , "( NONE ISSOURCE ALL )",  
           "( ALL ISSOURCE NONE)"   , "( ALL ISSOURCE NONE )",
        // Binary Filter
           "(Hello = TRUE )"        , "( Hello EQUALS TRUE )",
           "(sieben EQUALS \"12\")" , "( sieben EQUALS \"12\" )",
           "( Wrong < TRUE )"       , "( Wrong LESS TRUE )", 
           "( Easy LESS  0123)"     , "( Easy LESS 83 )", 
           "( Lazy > 0L )"          , "( Lazy GREATER 0l )", 
           "( strange GREATER 0)"   , "( strange GREATER 0 )", 
           "( x <= 1967-2-6 3:4:5 )", "( x LESSOREQUAL 1967-02-06 03:04:05 )", 
           "(y LESSOREQUAL 076l)"   , "( y LESSOREQUAL 62l )", 
           "( f >= 0X0L )"          , "( f GREATEROREQUAL 0l )", 
                            // This is a feature of java.text.SimpleDateFormat ...
           "(ttt GREATEROREQUAL 23:59:62)" , "( ttt GREATEROREQUAL 00:00:02 )", 
           "( nothing c= TRUE )"    , "( nothing ISIN TRUE )", 
           "( all ISIN  F)"         , "( all ISIN FALSE )", 
           "(anything L TRUE )"     , "( anything LIKE TRUE )", 
           "( name LIKE F)"         , "( name LIKE FALSE )" 
        };
        
        int len = tests.length;
        for (int i=0; i < len; i+= 2) {
            String expr     = tests[i];
            String expected = tests[i+1];
            // System.out.println("Checking :" + expr);
            Query q = Query.parseInfix(expr);
            assertEquals("Checking: " + expr, expected  ,q.toInfix());
        }
    }        
        
    /**
     * the suite of tests to execute.
     */
    static public Test suite () {
        return new TestSuite (TestQuery.class);
        // return new TestQuery ("testFullTextQuery");
    }

    /**
     * Main method for direct testing.
     */
    static public void main (String[] argv) {
        junit.textui.TestRunner.run (suite ());
    }

}
