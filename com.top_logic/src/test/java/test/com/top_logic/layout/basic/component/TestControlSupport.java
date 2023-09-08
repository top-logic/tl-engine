/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.basic.component;

import static test.com.top_logic.ComponentTestUtils.*;

import java.util.HashSet;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.ComponentTestUtils;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.layout.DummyCommandListener;
import test.com.top_logic.layout.DummyUpdateListener;

import com.top_logic.base.services.simpleajax.RequestLockFactory;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.layout.CommandListener;
import com.top_logic.layout.CommandListenerRegistry;
import com.top_logic.layout.SimpleCommandListenerRegistry;
import com.top_logic.layout.basic.component.ControlSupport;
import com.top_logic.mig.html.layout.LayoutComponentScope;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.mig.html.layout.SimpleComponent;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestControlSupport extends BasicTestCase {

	public void testRemoveUpdateListener() {
		ControlSupport support = new ControlSupport(newSimpleComponent("simpleComponent", "noContent"));
		DummyUpdateListener dummyUpdateListener1 = new DummyUpdateListener();
		DummyUpdateListener dummyUpdateListener2 = new DummyUpdateListener();

		support.addUpdateListener(dummyUpdateListener1);
		assertTrue(support.removeUpdateListener(dummyUpdateListener1));
		assertFalse(support.removeUpdateListener(dummyUpdateListener1));
		assertFalse(support.removeUpdateListener(dummyUpdateListener2));
	}

	public void testExecuteCommand() throws ConfigurationException {
		ControlSupport support = new ControlSupportForTest();
		HashSet<String> commands = new HashSet<>();
		commands.add("command1");
		commands.add("command2");
		CommandListener listener1 = new DummyCommandListener("listener1", commands);

		support.getFrameScope().addCommandListener(listener1);
		assertSame(HandlerResult.DEFAULT_RESULT, support.executeCommand(null, "listener1", "command1", null));
	}

	public void testDetachDisplayedControls() throws ConfigurationException {
		ControlSupportForTest support = new ControlSupportForTest();
		DummyUpdateListener dummyUpdateListener = new DummyUpdateListener();

		support.addUpdateListener(dummyUpdateListener);

		support.detachDisplayedControlsForTest();
		assertEquals(0, dummyUpdateListener.numberOfListenedModels());
		assertFalse(support.removeUpdateListener(dummyUpdateListener));
	}

	private static class ControlSupportForTest extends ControlSupport {
		public ControlSupportForTest() throws ConfigurationException {
			super(component());
		}

		protected static SimpleComponent component() throws ConfigurationException {
			MainLayout ml = ComponentTestUtils.newMainLayout();
			SimpleComponent result = new SimpleComponent(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
				newSimpleComponentConfig("simpleComponent",
				"noContent")) {
				
				@Override
				protected LayoutComponentScope createEnclosingFrameScope() {
					return new LayoutComponentScope(this) {
						
						private CommandListenerRegistry registry = new SimpleCommandListenerRegistry();
						
						@Override
						protected CommandListenerRegistry getRegistryImplementation() {
							return registry;
						}
					};
				}
			};
			ml.addComponent(result);
			return result;
		}

		public void detachDisplayedControlsForTest() {
			detachDisplayedControls();
		}
	}

	/**
	 * the suite of Tests to execute
	 */
	static public Test suite() {
		Test plainTest = new TestSuite(TestControlSupport.class);
		Test serviceSetup = ServiceTestSetup.createSetup(plainTest, RequestLockFactory.Module.INSTANCE);
		return KBSetup.getSingleKBTest(serviceSetup);
	}

	/**
	 * main function for direct testing.
	 */
	static public void main(String[] args) {
		Logger.configureStdout();

		junit.textui.TestRunner.run(suite());
	}

}
