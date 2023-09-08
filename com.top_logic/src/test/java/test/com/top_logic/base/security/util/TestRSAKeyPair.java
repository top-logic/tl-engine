/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.security.util;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;

import com.top_logic.base.security.util.KeyStore;
import com.top_logic.base.security.util.RSAKeyPair;
import com.top_logic.basic.FileManager;

/**
 * @author    <a href=mailto:kha@top-logic.com>kha</a>
 */
public class TestRSAKeyPair extends TestCase {
    
    protected RSAKeyPair rkp1;
    protected RSAKeyPair rkp2;

    /** 
     * Cleanup the keyfile after each test.
     * 
     */
    @Override
	protected void tearDown() throws Exception {
        super.tearDown();
        
		File theFile = FileManager.getInstance().getIDEFileOrNull(KeyStore.KEY_PATH + "/keyPair_Testing.key");
        if (theFile != null)
            theFile.delete();
        rkp1 = rkp2 = null;
    }

    /**
     * Test method for 'com.top_logic.base.security.util.RSAKeyPair.RSAKeyPair(String)'
     */
    public void testRSAKeyPairString() throws Exception {
        
        try {
            new RSAKeyPair(null);
            fail("Expected IllegalArgumentException ");
        } catch (IllegalArgumentException expetcted) { /* expected */ }

        rkp1 = new RSAKeyPair("Testing");
        rkp2 = new RSAKeyPair("Testing");
        
        assertEquals("Testing", rkp1.getUserID());
        
        assertEquals(rkp1.getPublicKey() , rkp2.getPublicKey());
        assertEquals(rkp1.getPrivateKey(), rkp1.getPrivateKey());

    }
    
    /**
     * Test that reading the dafult keys always works
     */
    public void testDefaultsKeys() throws Exception {
        
        new RSAKeyPair("top-logic");
        new RSAKeyPair("signature");
    }

    /**
     * Reading (and writing) of the key must bes synchronzied. 
     */
    public void testMultiThreaded() throws InterruptedException {

        Thread t1 = new Thread() {
            @Override
			public void run() {
               try {
                   rkp1 = new RSAKeyPair("Testing");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };

        Thread t2 = new Thread() {
            @Override
			public void run() {
                try {
                    rkp2 = new RSAKeyPair("Testing");
                 } catch (Exception ex) {
                     ex.printStackTrace();
                 }
            }
        };
        
        t1.start(); t2.start();
        t1.join();  t2.join();
        
        assertEquals(rkp1, rkp2);
        
        t1 = new Thread() {
            @Override
			public void run() {
                try {
                    rkp1 = new RSAKeyPair("Testing");
                 } catch (Exception ex) {
                     ex.printStackTrace();
                 }
            }
        };

        t2 = new Thread() {
            @Override
			public void run() {
                try {
                    rkp2 = new RSAKeyPair("Testing");
                 } catch (Exception ex) {
                     ex.printStackTrace();
                 }
            }
        };
        
        t1.start(); t2.start();
        t1.join();  t2.join();
        assertEquals(rkp1, rkp2);
    }

    /**
     * Used for framework.
     *
     * @return    The test suite for this class.
     */
    public static Test suite () {
        return TLTestSetup.createTLTestSetup(new TestSuite (TestRSAKeyPair.class));
    }

    /**
     * Main class to start test without UI.
     *
     * @param    args    Will be ignored.
     */
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite ());
    }

}
