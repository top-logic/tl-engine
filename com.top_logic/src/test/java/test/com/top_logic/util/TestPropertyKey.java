/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.Logger;
import com.top_logic.util.PropertyKey;

/**
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class TestPropertyKey extends TestCase {

    public TestPropertyKey(String aName) {
        super(aName);
    }

    public void testCreate() throws Exception {
        PropertyKey theKey = new PropertyKey("myKey;param1=a;param2=b;param3=c");

        Assert.assertTrue("toString() doesn't contain main key", theKey.toString().indexOf("myKey") > 0);
        Assert.assertEquals("myKey", theKey.getKey());
        Assert.assertEquals("a", theKey.getProperty("param1"));
        Assert.assertEquals("b", theKey.getProperty("param2"));
        Assert.assertEquals("c", theKey.getProperty("param3"));
        Assert.assertEquals(null, theKey.getProperty("param4"));
    }

    public void testEquals() throws Exception {
        PropertyKey theKey1 = new PropertyKey("myKey;param1=a;param2=b;param3=c");
        PropertyKey theKey2 = new PropertyKey("myKey;param1=a;param2=b;param3=c");
        PropertyKey theKey3 = new PropertyKey("myKey;param1=d;param2=e;param3=f");
        PropertyKey theKey4 = new PropertyKey("myKey;param1=d;param2=e;param3=g");
        PropertyKey theKey5 = new PropertyKey("myKey;param1=d;param2=e");

        Assert.assertEquals(theKey1, theKey2);
        Assert.assertTrue("Key 1 must not be equals to key 3", !theKey1.equals(theKey3));
        Assert.assertTrue("Key 3 must not be equals to key 4", !theKey3.equals(theKey4));
        Assert.assertTrue("Key 4 must not be equals to key 5", !theKey4.equals(theKey5));
    }

    public void testSimpleEquals() throws Exception {
        PropertyKey theKey1 = new PropertyKey("myKey");
        PropertyKey theKey2 = new PropertyKey("myKey");
        PropertyKey theKey3 = new PropertyKey("notMyKey");

        Assert.assertEquals(theKey1, theKey2);
        Assert.assertTrue("Key 1 must not be equals to key 3", !theKey1.equals(theKey3));
        Assert.assertEquals(null, theKey1.getProperty("param1"));
    }

    public static Test suite() {
        TestSuite theSuite = new TestSuite(TestPropertyKey.class);
        
        return (theSuite);
    }

    public static void main (String[] args) {
        
        Logger.configureStdout();
        junit.textui.TestRunner.run (suite ());
    }
}
