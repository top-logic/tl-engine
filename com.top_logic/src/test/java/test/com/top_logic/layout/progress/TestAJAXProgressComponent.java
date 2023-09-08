/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.progress;

import junit.framework.Test;

import org.xml.sax.Attributes;

import test.com.top_logic.ComponentTestUtils;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.services.simpleajax.RequestLockFactory;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.progress.AJAXProgressComponent;
import com.top_logic.layout.progress.AJAXProgressComponent.Config;
import com.top_logic.layout.progress.DefaultProgressInfo;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.SecurityObjectProviderManager;
import com.top_logic.tool.boundsec.wrap.SecurityComponentCache;


/**
 * Test the {@link AJAXProgressComponent}
 * 
 * @author     <a href="mailto:cca@top-logic.com>cca</a>
 */
public class TestAJAXProgressComponent  extends BasicTestCase  {

    Attributes attr;
    
    public TestAJAXProgressComponent(String someName) {
        super(someName);
    }
    
    /**
     * drop {@link #attr} when done,
     */
    @Override
	protected void tearDown() throws Exception {
        super.tearDown();
        attr = null;
    }
    
	public void testGetMessageDelta() throws ConfigurationException {
		TestedAJAXProgressComponent myTestClass = newTestComponent();
        
        assertEquals("ABC", myTestClass.getMessageDelta("ABC"));
        assertEquals(null , myTestClass.getMessageDelta("ABC"));
        assertEquals(null , myTestClass.getMessageDelta("ABC"));
        assertEquals("DEF", myTestClass.getMessageDelta("ABCDEF"));
        assertEquals(null , myTestClass.getMessageDelta(null));       
        assertEquals("GHI", myTestClass.getMessageDelta("GHI"));
        assertEquals("JKL", myTestClass.getMessageDelta("GHIJKL"));
        assertEquals(null , myTestClass.getMessageDelta(null));       
        assertEquals("GHI", myTestClass.getMessageDelta("GHI"));   
        assertEquals("JKLMNO", myTestClass.getMessageDelta("GHIJKLMNO"));   
        assertEquals(null , myTestClass.getMessageDelta("GHIJKLMNO"));
        
        startTime();
        final int N = 100;
        for (int i=0; i <N; i++) {
            String longTestMessage = StringServices.getRandomString(1024);
            assertEquals(longTestMessage, myTestClass.getMessageDelta(longTestMessage));
            assertEquals("ABC"          , myTestClass.getMessageDelta(longTestMessage + "ABC"));
            assertEquals(longTestMessage, myTestClass.getMessageDelta(longTestMessage + "ABC" + longTestMessage));
        }
        logTime(N + " x getMessageDelta");
    }
   
    /**
     * Test the State-handling in AJAXProgressComponent
     */
    public void testCheckState() throws Exception{
		TestedAJAXProgressComponent testComp = newTestComponent();
        
        assertEquals(0, testComp.getState());
        
        
        MyProgressInfo myInfo = new MyProgressInfo();
        testComp.setProgressInfo(myInfo);
        
        assertEquals(0, testComp.getState());
        
        testComp.checkState();
        assertEquals(1, testComp.getState());
        
        myInfo.setAtWork();
        testComp.checkState();
        assertEquals(1, testComp.getState());      
        
        myInfo.setFinished();
        
        testComp.checkState();
        assertEquals(2, testComp.getState());
        
        testComp.resetState();
        testComp.resetLocalVariables();
        assertEquals(0, testComp.getState());
        
        myInfo = new MyProgressInfo();
        testComp.setProgressInfo(myInfo);
        assertEquals(0, testComp.getState());
        
        myInfo.setFinished();
        testComp.checkState();
        assertEquals(2, testComp.getState());
    }

	private TestedAJAXProgressComponent newTestComponent() throws ConfigurationException {
		return newTestComponent(newConfig());
	}

	private TestedAJAXProgressComponent newTestComponent(Config config) throws ConfigurationException {
		return new TestedAJAXProgressComponent(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
	}

	private Config newConfig() {
		return TypedConfiguration.newConfigItem(TestedAJAXProgressComponent.Config.class);
	}
    
    public void testAttributes() throws Exception {
		Config config = newConfig();
		config.setScrollUp(true);
		config.setCloseOnFinish(true);
		MainLayout ml = ComponentTestUtils.newMainLayout();
		TestedAJAXProgressComponent myTestClass = newTestComponent(config);
		ml.addComponent(myTestClass);
		myTestClass.accessibleResolveComponents(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY);

        // TODO need more assertions here.
        assertNotNull(myTestClass);
    }

	public static Test suite() {
		Test innerTest = ServiceTestSetup.createSetup(TestAJAXProgressComponent.class,
			CommandHandlerFactory.Module.INSTANCE,
			BoundHelper.Module.INSTANCE,
			SecurityObjectProviderManager.Module.INSTANCE,
			RequestLockFactory.Module.INSTANCE,
			SecurityComponentCache.Module.INSTANCE);
		return KBSetup.getSingleKBTest(innerTest);
	}
    
	static class MyProgressInfo extends DefaultProgressInfo {
        
		/**
		 * Resets the progress info.
		 */
		@Override
		public void reset() {
			super.reset();
			setExpected(200);
        }

        public void setAtWork() {
			setCurrent(50);
            
        }

        public void setFinished() {
			setCurrent(200);
			super.setFinished(true);
        }
        
    }

}
