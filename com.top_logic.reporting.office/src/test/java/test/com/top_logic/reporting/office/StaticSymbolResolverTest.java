/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.office;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.Logger;
import com.top_logic.basic.util.Computation;
import com.top_logic.reporting.office.DefaultStaticSymbolResolver;
import com.top_logic.reporting.office.ExpansionContext;
import com.top_logic.reporting.office.ExpansionObject;
import com.top_logic.reporting.office.StaticSymbolResolver;
import com.top_logic.reporting.office.basic.AliasSymbolResolver;
import com.top_logic.reporting.office.basic.BasicExpansionContext;


/**
 * Test case for several different Resolver
 * 
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public class StaticSymbolResolverTest extends TestCase {

    public StaticSymbolResolverTest (String aName){
        super(aName);
    }
    
    private ExpansionObject eo1;
    private ExpansionObject eo2;
    private ExpansionObject eo3;
    private ExpansionObject eo4;
    private ExpansionContext ec;
    private static final String KEY1 = "EXAMPLE_KEY"; 

	private static final String ALIAS = "EXAMPLE_ALIAS";

    private static final String ENTRY = "entry";
    private static final String SECOND = "secondEntry";
    
    @Override
	protected void setUp() throws Exception {
        
        eo1 = createExpansionObject(KEY1);
        eo2 = createExpansionObject(ALIAS);
        eo3 = createExpansionObject(ENTRY);
        eo4 = createExpansionObject(SECOND);
        
        Map contextMap = new HashMap (1);
        contextMap.put("entry","something");
        contextMap.put("secondEntry","do it again, sam");
        ec = new BasicExpansionContext(contextMap);        
    }
    
    @Override
	protected void tearDown() throws Exception {
        eo1 = null;
        eo2 = null;
        eo3 = null;
        eo4 = null;
        ec = null;
    }
    
    public void testDefaultSymbolResolver() throws Exception{
        StaticSymbolResolver resolver = new DefaultStaticSymbolResolver();
        Object result = resolver.resolveSymbol(ec,eo3);
        assertEquals ("something",result);
        result = resolver.resolveSymbol(ec,eo4);
        assertEquals ("do it again, sam",result);
    }
    
    public void testAliasSymbolResolver() throws Exception {
        StaticSymbolResolver resolver = new AliasSymbolResolver ();
		String expected = "EXAMPLE_ALIAS_VALUE";
        
        Object result = resolver.resolveSymbol(ec,eo2);
        assertTrue (result instanceof String);
        assertEquals (expected,result);
    }
        
    public void testResolverDelegation() throws Exception {
        StaticSymbolResolver resolver = new DelegationSymbolResolverExample ();
        
        Object result = resolver.resolveSymbol(null,eo1);
        assertTrue (result instanceof String);
        assertEquals ("nothing",result);
        
        result = resolver.resolveSymbol (ec,eo1);
        assertEquals ("we have something",result);
        
    }
    
    public void testExceptionHandlingForResolver() throws Exception {
		final StaticSymbolResolver resolver =
			new FaultyDelegationSymbolResolver(FaultyDelegationSymbolResolver.NO_SYMBOLS);
        Object result = resolver.resolveSymbol(null,eo1);
        assertNull(result);
        
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		BasicTestCase.runWithSystemOut(new PrintStream(outputStream), new Computation<Void>() {

			@Override
			public Void run() {
				Logger.configureStdout();

				FaultyDelegationSymbolResolver resolver2 =
					new FaultyDelegationSymbolResolver(FaultyDelegationSymbolResolver.WRONG_METHOD_NAME);
				resolver2.resolveSymbol(null, eo1);
				return null;
			}
		});
        // examine the outputStream
		String logString = outputStream.toString();
        assertNotNull(logString);
		assertTrue("Ticket #26497: Improve the Log4j support after switching to version 2.",
			logString.indexOf("call of unknown method") > -1);
    }
    
    public void testInvocationException () throws Exception {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		BasicTestCase.runWithSystemOut(new PrintStream(outputStream), new Computation<Void>() {

			@Override
			public Void run() {
				Logger.configureStdout();

				StaticSymbolResolver resolver =
					new FaultyDelegationSymbolResolver(FaultyDelegationSymbolResolver.INVOCATION_TARGET);
				resolver.resolveSymbol(null, eo1);
				return null;
			}
		});
        // examine the outputStream
		String logString = outputStream.toString();
        assertNotNull(logString);
		assertTrue("Ticket #26497: Improve the Log4j support after switching to version 2.",
			logString.indexOf("Something in the invoked method") > -1);
        
    }
    
    private ExpansionObject createExpansionObject(String aKey) {
        
        ExpansionObject result = new ExpansionObject (aKey);
        result.setSymbolType(ExpansionObject.STATIC);
        result.setSymbol("<exp type=static>"+aKey+"</exp>");
        result.setSymbolContent(aKey);
        return result;
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    public static Test suite () {
        return OfficeTestSetup.createOfficeTestSetup(new TestSuite (StaticSymbolResolverTest.class));
    }
}
