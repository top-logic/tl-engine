/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.tool.boundsec.commandhandler;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.demo.DemoSetup;
import test.com.top_logic.layout.scripting.ApplicationTestSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.form.component.SimpleFormComponent;
import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.runtime.ApplicationSession;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.commandhandlers.GotoHandler;

/**
 * The class {@link TestGotoHandler} test goto into components
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestGotoHandler extends BasicTestCase {

	private ApplicationSession session;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		session = ApplicationTestSetup.getApplication().login(PersonManager.getManager().getRoot(), layoutName());
	}

	private static String layoutName() {
		return TestGotoHandler.class.getName() + ".xml";
	}

	@Override
	protected void tearDown() throws Exception {
		session.invalidate();
		session = null;
		super.tearDown();
	}

	public void testCompToDialogOnComp() {
		checkVisible("tab1");
		checkNotVisible("dialog1");
		goTo("tab1", "dialog1");
		checkVisible("dialog1");
	}

	public void testCompToSameComp() {
		checkVisible("tab1");
		goTo("tab1", "tab1");
		checkVisible("tab1");
	}

	public void testCompToOtherComp() {
		checkVisible("tab1");
		checkNotVisible("tab2");
		goTo("tab1", "tab2");
		checkVisible("tab2");
		checkNotVisible("tab1");
	}

	public void testCompToDialogOnOtherComp() {
		checkVisible("tab1");
		checkNotVisible("tab2", "dialog2");
		goTo("tab1", "dialog2");
		checkVisible("tab2", "dialog2");
		checkNotVisible("tab1");
	}

	public void testDialogToSameDialog() {
		openDialog("tab1", "dialog1");

		goTo("dialog1", "dialog1");
		
		try {
			checkVisible("dialog1");
		} catch (Throwable cause) {
			fail("see #2132", cause);
		}
	}

	public void testDialogToComponent() {
		openDialog("tab1", "dialog1");

		goTo("dialog1", "tab1");
		
		checkNotVisible("dialog1");
		checkVisible("tab1");
	}

	public void testDialogToOtherComponent() {
		openDialog("tab1", "dialog1");

		goTo("dialog1", "tab2");
		
		checkNotVisible("dialog1", "tab1");
		checkVisible("tab2");
	}

	public void testDialogToOtherDialog() {
		openDialog("tab1", "dialog1");

		goTo("dialog1", "dialog2");
		
		checkNotVisible("dialog1", "tab1");
		checkVisible("tab2", "dialog2");
	}

	public void testDialogToInnerDialog() {
		openDialog("tab1", "dialog1");

		goTo("dialog1", "dialog1.1");
		
		checkVisible("tab1");
		try {
			checkVisible("dialog1", "dialog1.1");
		} catch (Throwable cause) {
			fail("see #2132", cause);
		}
	}

	public void testDialogToOuterDialog() {
		openDialog("tab1", "dialog1");
		openDialog("dialog1", "dialog1.1");

		goTo("dialog1.1", "dialog1");
		
		checkVisible("dialog1", "tab1");
		checkNotVisible("dialog1.1");
	}

	public void testInnerDialogToSiblingDialog() {
		openDialog("tab1", "dialog1");
		openDialog("dialog1", "dialog1.1");
		
		goTo("dialog1.1", "dialog1.2");
		
		checkVisible("dialog1", "tab1", "dialog1.2");
		checkNotVisible("dialog1.1");
	}
	
	public void testInnerDialogToOtherInnerDialog() {
		openDialog("tab1", "dialog1");
		openDialog("dialog1", "dialog1.1");
		
		goTo("dialog1.1", "dialog2.1");
		
		checkNotVisible("dialog1", "tab1", "dialog1.1");
		checkVisible("dialog2.1", "dialog2", "tab2");
	}
	
	public void testDialogToOtherChildOfDialogParent() {
		openDialog("tab1", "dialog1");
		
		goTo("dialog1", "tab1.1");
		
		checkNotVisible("dialog1");
		checkVisible("tab1.1");
	}

	private void openDialog(final String parentName, final String dialogName) {
		checkVisible(parentName);
		checkNotVisible(dialogName);
		session.process(ActionFactory.openDialog(newComponentName(parentName), newComponentName(dialogName)));
		checkVisible(dialogName);
	}

	private static ComponentName newComponentName(String localName) {
		return ComponentName.newName(layoutName(), localName);
	}

	private void checkVisible(String... components) {
		for (String component : components) {
			session.process(ActionFactory.checkVisibility(newComponentName(component), true));
		}
	}

	private void checkNotVisible(String... components) {
		for (String component : components) {
			session.process(ActionFactory.checkVisibility(newComponentName(component), false));
		}
	}

	private void goTo(String sourceCompName, String targetCompName) {
		session.process(ActionFactory.commandAction(newComponentName(sourceCompName), "gotoCmd", getGotoArgs(targetCompName)));
	}

	private static Map<String, ?> getGotoArgs(String componentName) {
		Map<String, Object> arguments = new HashMap<>();
		arguments.put(GotoHandler.COMMAND_PARAM_COMPONENT, newComponentName(componentName));
		return arguments;
	}

	public static Test suite() {
		return DemoSetup.createDemoSetup(TestGotoHandler.class);
	}

	/**
	 * A {@link SimpleFormComponent} which accepts each model. Currently necessary to act as target
	 * of a {@link GotoHandler GOTO}.
	 * 
	 * @see GotoHandler#gotoLayout(LayoutComponent, Object, ComponentName)
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	public static final class Component extends SimpleFormComponent {

		public Component(InstantiationContext context, Config atts) throws ConfigurationException {
			super(context, atts);
		}

	}

}
