/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.gui.layout.person;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.knowledge.gui.layout.person.EditPersonComponent;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.channel.SelectionChannel;
import com.top_logic.layout.channel.linking.Channel;
import com.top_logic.layout.channel.linking.ref.NamedComponent;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.SecurityObjectProviderManager;
import com.top_logic.util.TLContext;

/** 
 * Tests for {@link com.top_logic.knowledge.gui.layout.person.EditPersonComponent}.
 * Test-methods have to be implemented!
 *
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 * @author    <a href="mailto:hbo@top-logic.com">hbo</a>
 */
public class TestEditPersonComponent extends BasicTestCase {
	
	private static ComponentName name = ComponentName.newName("editUser");

	private static ComponentName master = ComponentName.newName("master");
	private static String page 		= "/jsp/layout/persons/EditPerson.jsp";
    
	/**
	 * Test for some methods from {@link EditPersonComponent}.
	 */
	public void testWriteBody() {
		EditPersonComponent theComponent = this.createComponent(this.createPerson());
		// theComponent = (EditPersonComponent) assertSerializable(theComponent);
		this.writeBody(theComponent);
	}
	
	/**
	 * Create and return an EditUserComponent.
	 * @param aPerson a Person, that is the model of the tested EditPersonComponent
	 * 
	 */
	private EditPersonComponent createComponent(Person aPerson){
		EditPersonComponent.Config config = TypedConfiguration.newConfigItem(EditPersonComponent.Config.class);
		config.setName(name);
		config.setPage(page);

		NamedComponent masterRef = TypedConfiguration.newConfigItem(NamedComponent.class);
		masterRef.setName(master);

		Channel channel = TypedConfiguration.newConfigItem(Channel.class);
		channel.setName(SelectionChannel.NAME);
		channel.setComponentRef(masterRef);

		config.setModelSpec(channel);
		try {
			EditPersonComponent result =
				new EditPersonComponent(new DefaultInstantiationContext(TestEditPersonComponent.class), config);
			result.setModel(aPerson);
			return result;
		} catch (ConfigurationException ex) {
			throw fail("Cannot instantiate component.", ex);
		}
	}
	
   /**
	* Create and return an Person with the configured superusername.
	*/
	private Person createPerson(){
		try {
			TLContext.pushSuperUser();
			PersonManager pm = PersonManager.getManager();
			// pm.initUserMap(); //just in case it wasn't initialized before
			Person root = pm.getRoot();
			assertNotNull("Failed to getRoot()", root);
			return root;
		} finally {
			TLContext.popSuperUser();
		}
		
	}
	
	
	private void writeBody(EditPersonComponent aComponent){
		// TODO KHA do soemthing reasonable here
	}

	/**
	* The method constructing a test suite for this class.
	*
	* @return    The test to be executed.
	*/
	public static Test suite () {
		TestSuite theSuite = new TestSuite (TestEditPersonComponent.class);
		// setting model to component checks for allow which needs BoundHelper
		Test setupBoundHelper = setupBoundHelper(theSuite);
		Test setupSecurityObjectProviders = setupSecurityObjectProviders(setupBoundHelper);
		return PersonManagerSetup.createPersonManagerSetup(setupSecurityObjectProviders);
	}

	private static Test setupBoundHelper(Test test) {
		return ServiceTestSetup.createSetup(test, BoundHelper.Module.INSTANCE);
	}

	private static Test setupSecurityObjectProviders(Test test) {
		return ServiceTestSetup.createSetup(test, SecurityObjectProviderManager.Module.INSTANCE);
	}

	/**
	* Start this test.
	* @param args not used
	*/  
	public static void main(String[] args) {
		SHOW_TIME = true;
		
		Logger.configureStdout();
		TestRunner.run (suite ());
	}
}
