/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tool.state;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.Logger;
import com.top_logic.tool.state.ElementState;
import com.top_logic.tool.state.StateManager;
import com.top_logic.tool.state.StatefullElement;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class TestStateManager extends BasicTestCase {

	public static String TEST_CONFIG_FILE = "/WEB-INF/xml/state/TestStateConfiguration.xml";

	private static String TEST_EMPTY_CONFIG_FILE = "/WEB-INF/xml/state/TestEmptyStateConfiguration.xml";

	private static String BAD_CONFIG_FILE = "/WEB-INF/xml/state/DoesNotExist.xml";
    
    private static String START_STATE   ="dummy.start";
    private static String INTER_STATE   ="dummy.intermediate";    
    private static String END_STATE     ="dummy.end";
    private static String FINAL_STATE   ="dummy.final"; 
    
    StateManager stateManager;
    ElementState startState ;
    ElementState intermState;        
    ElementState endState;
    
    @Override
	protected void setUp() throws Exception {
        StateManager.resetForTest();
    }
    
    /**
     * Release variables used for testinng to GC.
     */
    @Override
	protected void tearDown() throws Exception {
        stateManager    = null;
        startState      = null;
        intermState     = null;     
        endState        = null;
    }
    
 
    public void testGetManager() throws Exception{
        StateManager.setCONFIG_FILEForTest(TEST_CONFIG_FILE);
        StateManager theManager = StateManager.getManager();
        assertNotNull(theManager);
    }
    
    public void testNotExistingConfigFile() throws Exception{
        try{
            StateManager.setCONFIG_FILEForTest(BAD_CONFIG_FILE);   
            StateManager.getManager();            
            fail("Wrong config file read");
        } catch(Exception e) { /* expected */ }        
    }   
    
    public void testEmptyConfigFile() throws Exception{
        try{
            Logger.configureStdout("FATAL"); // Swallo expected ERROR-Log
            StateManager.setCONFIG_FILEForTest(TEST_EMPTY_CONFIG_FILE);   
            StateManager.getManager();            
            fail("Empty config file read");
        } catch(Exception e){
            // expected
        } finally {
            Logger.configureStdout(); 
        }
    }      
    
    /**
     * checks that only transitions defined in XML-File are possible
     *
     */
    public void testChangeState(){
        doSetUp();
        StatefullElement elt = new MyElement();
        elt.setState(startState);
        stateManager.changeState(elt,INTER_STATE);
        assertEquals(intermState, elt.getState());
        stateManager.changeState(elt,END_STATE);
        assertEquals(endState, elt.getState());        
        stateManager.changeState(elt,INTER_STATE);
        assertEquals(intermState, elt.getState());    
        
        //      not possible
        stateManager.changeState(elt,START_STATE);
        // STATE NOT CHANGED
        assertEquals(intermState, elt.getState());  
    }
    
    public void testSetInitialState(){
        doSetUp();
        StatefullElement elt = new MyElement();
        stateManager.setInitialState(elt);
        stateManager.changeState(elt,START_STATE);       
    }
    
    public void testIsFinished(){
        doSetUp();
        StatefullElement elt = new MyElement();
        elt.setState(startState);
        assertFalse(stateManager.isFinished(elt));
        stateManager.changeState(elt,INTER_STATE);
        assertFalse(stateManager.isFinished(elt));
        stateManager.changeState(elt,END_STATE);
        assertFalse(stateManager.isFinished(elt));
        stateManager.changeState(elt,FINAL_STATE);        
        assertTrue(stateManager.isFinished(elt));                
    }
    
    public void testIsRunning(){
        doSetUp();
        StatefullElement elt = new MyElement();
        elt.setState(startState);
        assertTrue(stateManager.isRunning(elt));
        stateManager.changeState(elt,INTER_STATE);
        assertTrue(stateManager.isRunning(elt));
        stateManager.changeState(elt,END_STATE);
        assertTrue(stateManager.isRunning(elt));
        stateManager.changeState(elt,FINAL_STATE);        
        assertFalse(stateManager.isRunning(elt));
        
    }
    
    
    private void doSetUp() {
        StateManager.setCONFIG_FILEForTest(TEST_CONFIG_FILE);
        stateManager = StateManager.getManager();
        startState = stateManager.getState(START_STATE);
        intermState = stateManager.getState(INTER_STATE);        
        endState = stateManager.getState(END_STATE);
    }

    /*package protected*/ class MyElement implements StatefullElement{
        
        ElementState state = null;

        @Override
		public ElementState getState() {
            return state;
        }

        @Override
		public void setState(ElementState aState) {
            state=aState;
        }
    }

    /**
     * Return the suite of tests to perform. 
     */
    public static Test suite () {
        return TLTestSetup.createTLTestSetup(new TestSuite(TestStateManager.class));
    }

    public static void main(String[] args) {
        // SHOW_TIME             = true;     // for debugging
        // KBSetup.CREATE_TABLES = false;    // for faster debugging
        
        Logger.configureStdout();
        
        junit.textui.TestRunner.run(suite());
    }
    
    
}
