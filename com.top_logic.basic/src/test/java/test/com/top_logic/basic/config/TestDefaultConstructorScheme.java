/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.DefaultConfigConstructorScheme;
import com.top_logic.basic.config.DefaultConfigConstructorScheme.Factory;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;

/**
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class TestDefaultConstructorScheme extends TestCase {

    public static Test suite() {
        return BasicTestSetup.createBasicTestSetup(new TestSuite(TestDefaultConstructorScheme.class));
    }
    
    public void testGetFactory() throws Exception {
        ConfigurationItem theConfig = TypedConfiguration.newConfigItem(PolymorphicConfiguration.class);
		InstantiationContext context = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;

        // Class A
        // Instantiated via default constructor
        Factory theFactory = DefaultConfigConstructorScheme.getFactory(A.class);
        assertFalse(theFactory.isSharedInstance());
		theFactory.createInstance(context, theConfig);
        
        // Class B
        // Instantiated via INSTANCE field
        theFactory = DefaultConfigConstructorScheme.getFactory(B.class);
        assertTrue(theFactory.isSharedInstance());
        theFactory.createInstance(context, theConfig);
        
        // Class C
        // Instantiated via getInstance()
        theFactory = DefaultConfigConstructorScheme.getFactory(C.class);
        assertTrue(theFactory.isSharedInstance());
        theFactory.createInstance(context, theConfig);
        
        // Class D
        // Instantiated via D(InstantiationContext, ConfigurationItem)
        theFactory = DefaultConfigConstructorScheme.getFactory(D.class);
        assertFalse(theFactory.isSharedInstance());
        theFactory.createInstance(context, theConfig);
        
        // Class E
        // Instantiated via E(InstantiationContext, ConfigurationItem)
        // No public constructor
        try {
            DefaultConfigConstructorScheme.getFactory(E.class);
            fail("Expected ConfigurationException");
        } catch (ConfigurationException c) {
            // expected
        }
    }
    
    public static class A {
        public A() {
        }
    }

    public static class B extends A {
        public static final B INSTANCE = new B(null);

        public B() {
            super();
            throw new UnsupportedOperationException("Unexpected method invocation: B()");
        }

        B(Object arg) {
        }
    }

    public static class C extends B {
        
        public C() {
            super();
            throw new UnsupportedOperationException("Unexpected method invocation: C()");
        }
        
        C(Object arg) {
            super(arg);
            throw new UnsupportedOperationException("Unexpected method invocation: C(Object)");
        }
        
        C(Object arg1, Object arg2) {
            super(arg1);
        }

        public static C getInstance() {
            return new C(null, null);
        }
    }

    public static class D extends C {

        public D() {
            super(null, null);
            throw new UnsupportedOperationException("Unexpected method invocation: D()");
        }

        public D(InstantiationContext context, ConfigurationItem config) {
            super(null, null);
        }
    }
    
    public static class E {

        private E() {}
        
        private E(InstantiationContext context, ConfigurationItem config) {
        }
    }
}
