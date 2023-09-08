/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.state;

import java.util.Iterator;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.tool.state.TestStateManager;

import com.top_logic.basic.Logger;
import com.top_logic.layout.state.ViewState;
import com.top_logic.layout.state.ViewStateManager;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.execution.service.ConfiguredCommandApprovalService;
import com.top_logic.tool.state.State;
import com.top_logic.tool.state.StateManager;

/**
 * TODO FMA this class
 * 
 * @author <a href="mailto:fma@top-logic.com">fma</a>
 * 
 * @deprecated Use configuration of {@link ConfiguredCommandApprovalService}.
 */
@Deprecated
public class TestViewStatemanager extends BasicTestCase {

    private static String BAD_CONFIG_FILE = "/DoesNotExist.xml"; 
    
	private static String TEST_CONFIG_FILE = "/WEB-INF/xml/state/TestViewStateConfiguration.xml";
    
    /** test what happens when config does not exist */
    public void testNotExistingConfigFile() throws Exception{
        Logger.configureStdout("FATAL"); // Suppress (correct) Errors
        try{
            ViewStateManager.setCONFIG_FILEForTest(BAD_CONFIG_FILE);   
            ViewStateManager.getManager();            
            fail("Wrong config file read");
        }catch(Exception e){
            // expected
        } 
        finally {
            Logger.configureStdout();
        }
        
    }   
    
    public void testGetManager() throws Exception{
        doSetUp();
        ViewStateManager theManager = ViewStateManager.getManager();
        assertNotNull(theManager);
        StateManager statemanager = StateManager.getManager();
        Iterator iter = statemanager.getStates().iterator();
        while(iter.hasNext()){
            State theState = (State)iter.next();
			ViewState theViewState = theManager.getViewState(theState, ComponentName.newName(""));
            assertNotNull(theViewState);
        }
    }
    
    private void doSetUp() {
        StateManager.setCONFIG_FILEForTest(TestStateManager.TEST_CONFIG_FILE);
        ViewStateManager.setCONFIG_FILEForTest(TEST_CONFIG_FILE);
    }
    
    
    @Override
	protected void setUp() throws Exception {
        super.setUp();
        StateManager.resetForTest();
        ViewStateManager.resetForTest();
    }

    /**
     * Return the suite of tests to perform. 
     */
    public static Test suite () {
        Test innerTest = ServiceTestSetup.createSetup(TestViewStatemanager.class, CommandHandlerFactory.Module.INSTANCE);
		return (TLTestSetup.createTLTestSetup(innerTest));
    }

    public static void main(String[] args) {
        // SHOW_TIME             = true;     // for debugging
        KBSetup.setCreateTables(false);    // for debugging
        
        Logger.configureStdout();
        
        junit.textui.TestRunner.run(suite());
    }
}
