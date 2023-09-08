/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap;

import java.util.Collection;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.module.ManagedClass.ServiceConfiguration;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.knowledge.wrap.TreeWebfolderFactory;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.knowledge.wrap.WebFolderFactory;

/**
 * Test the {@link WebFolderFactory} and the {@link TreeWebfolderFactory}.
 * 
 * @author    <a href=mailto:fma@top-logic.com>fma</a>
 */
public class TestWebFolderFactory extends BasicTestCase {

  
    public TestWebFolderFactory(String aName) {
        super(aName);
    }
    
    /**
     * Test main aspects of the {@link WebFolderFactory}.
     */
    public void testCreateFolder(WebFolderFactory fac) throws Exception{
         String name=null;
        
        Collection types = fac.getAllowedFolderTypes();
        assertTrue("there should be at least one allowed type", types.size()>0);
        String type = types.iterator().next().toString();
        
        
        //      folder with empty name is not allowed
        try{
            fac.createNewWebFolder(name,type);
            fail("folder with empty name is not allowed");
        }
        catch(IllegalArgumentException e){
            // expected
        }
        name="";
        try{
            fac.createNewWebFolder(name,type);
            fail("folder with empty name is not allowed");
        }
        catch(IllegalArgumentException e){
            // expected
        }       
        
        name="meier";
        String wrongType=null;
        //      folder with empty type is not allowed
        try{
            fac.createNewWebFolder(name,wrongType);
            fail("folder with empty type is not allowed");
        }
        catch(IllegalArgumentException e){
            // expected
        }     
        
        wrongType="";
        try{
            fac.createNewWebFolder(name,wrongType);
            fail("folder with empty type is not allowed");
        }
        catch(IllegalArgumentException e){
            // expected
        }  
        
        wrongType="schulze";
        assertFalse("test needs a not allowed type", fac.isAllowedType(wrongType));
        
        // create folder with wrong type is not allowed
        try{
            fac.createNewWebFolder(name,wrongType);
            fail("folder with wrong type is not allowed");
        }
        catch(IllegalArgumentException e){
            // expected
        } 
        
        assertTrue("type is not set up correct", fac.isAllowedType(type));
        
        WebFolder folder = fac.createNewWebFolder(name,type);
        
        assertNotNull("no folder created", folder); 
        assertTrue(folder.getKnowledgeBase().commit());
    }
    
    public void testCreateTreeFolder() throws Exception {
		ServiceConfiguration<WebFolderFactory> config =
			ApplicationConfig.getInstance().getServiceConfiguration(WebFolderFactory.class);

		testCreateFolder(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config));
    }

    public void testCreateFolder() throws Exception {
		ServiceConfiguration<WebFolderFactory> config =
			ApplicationConfig.getInstance().getServiceConfiguration(WebFolderFactory.class);

		testCreateFolder(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config));
    }

    /** 
     * Test Concurrent Creation of WebFolders (which may fail in a Cluster).
     */
    public void testConcurrent(final WebFolderFactory fac) throws Exception {
        final String type      = CollectionUtil.getFirst(fac.getAllowedFolderTypes()).toString();
        final int    PAR_COUNT = 100;
        
        Execution createFolder1 = new Execution() {
            @Override
			public void run() throws Exception {
                WebFolder wf = fac.createNewWebFolder("testConcurrent", type);
                for (int i=0; i < PAR_COUNT; i++) {
                    fac.createNewWebFolder("testConcurrent", type);
                }
                assertTrue(wf.getKnowledgeBase().commit());

            }
        };
        
		createFolder1.run();

        Execution createFolder2 = new Execution() {
            @Override
			public void run() throws Exception {
                WebFolder wf = fac.createNewWebFolder("testConcurrent", type);
                for (int i=0; i < PAR_COUNT; i++) {
                    fac.createNewWebFolder("testConcurrent", type);
                }
                assertTrue(wf.getKnowledgeBase().commit());
            }
        };

        TestFuture t1 = inParallel(createFolder1);
        TestFuture t2 = inParallel(createFolder2);
        
        t1.check();
        t2.check();
    }

    public void testTreeConcurrent() throws Exception {
		ServiceConfiguration<WebFolderFactory> config =
			ApplicationConfig.getInstance().getServiceConfiguration(WebFolderFactory.class);

		testConcurrent(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config));
    }

	public void testConcurrent() throws Exception {
		ServiceConfiguration<WebFolderFactory> config =
			ApplicationConfig.getInstance().getServiceConfiguration(WebFolderFactory.class);

		testConcurrent(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config));
    }

	/**
	 * Suite of tests.
	 */
	public static Test suite () {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(null, TestWebFolderFactory.class, MimeTypes.Module.INSTANCE));
	}
	
}
