/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.gui.layout;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.SimpleDecoratedTestSetup;
import test.com.top_logic.basic.TestSetupDecorator;
import test.com.top_logic.basic.TestUtils;

import com.top_logic.gui.MultiThemeFactory;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.knowledge.wrap.person.TLPersonManager;
import com.top_logic.util.TLContext;


/**
 * @author    <a href=mailto:jco@top-logic.com>jco</a>
 */
public class TestMultiThemeFactory extends BasicTestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(TestMultiThemeFactory.class);
    }

    /**
     * Constructor for TestMultiThemeFactory.
     */
    public TestMultiThemeFactory(String name) {
        super(name);
    }

    public void testLoadClass (){
        ThemeFactory theFactory = ThemeFactory.getInstance();
        assertNotNull(theFactory);
        assertTrue (theFactory instanceof MultiThemeFactory);
    }
    
    public void testGetCurrentThemeWithoutInitializedContext() {
		TLContext.pushSuperUser();
        try {
            Person root = TLPersonManager.getManager().getRoot();
			TLContext theContext = TLContext.getContext();
            theContext.setCurrentPerson(root);

            PersonalConfiguration config = theContext.getPersonalConfiguration();
			MultiThemeFactory.setPersonalThemeId(config, "default");
            
            ThemeFactory theFactory = ThemeFactory.getInstance();
            Theme theTheme = theFactory.getCurrentTheme();
            
            assertNotNull(theTheme);
        } finally {
        	TLContext.popSuperUser();
        }
    }
    /**
     * The method constructing a test suite for this class.
     *
     * @return    The test to be executed.
     */
    public static Test suite () {
		TestSetupDecorator decorator = CustomPropertiesDecorator.newDecorator(TestMultiThemeFactory.class, true);

		// PersonManager is needed to get root person.
		Test setupPersonManager =
			PersonManagerSetup.createPersonManagerSetup(new TestSuite(TestMultiThemeFactory.class));
		// Install custom configuration before the KB are setup as otherwise the internal switch to
		// a different base is reverted.
		SimpleDecoratedTestSetup test = new SimpleDecoratedTestSetup(decorator, setupPersonManager);
		// Setup configuration to be able to read own configuration file.
		return TestUtils.doNotMerge(TLTestSetup.createTLTestSetup(test));
    }
}
