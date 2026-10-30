/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.accesscontrol.loginmethod.LoginMethod;
import com.top_logic.base.accesscontrol.loginmethod.LoginMethods;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.common.ReactAlertControl;
import com.top_logic.layout.react.control.overlay.ReactSnackbarControl.Variant;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.element.LoginMethodsElement;

/**
 * Tests that a {@link LoginMethodsElement} offers the login methods available when it is rendered,
 * also when a reloaded page renders the control tree its window keeps, and shows a failed external
 * login.
 */
public class TestLoginMethodsElement extends TestCase {

	/**
	 * {@link LoginMethod} whose availability the test switches.
	 */
	private static class SwitchedMethod implements LoginMethod {

		boolean _available;

		@Override
		public String getId() {
			return "switched";
		}

		@Override
		public ResKey getLabel() {
			return ResKey.text("Switched");
		}

		@Override
		public ThemeImage getIcon() {
			return null;
		}

		@Override
		public String getInitiationUrl(String returnToUrl, String windowName) {
			return "/servlet/switched";
		}

		@Override
		public boolean isAvailable() {
			return _available;
		}

	}

	/**
	 * A method that becomes available after the control was created and rendered is offered by the
	 * next rendering of the same control, and withdrawn again when it is no longer available.
	 */
	public void testRenderingFollowsAvailability() throws IOException {
		SwitchedMethod method = new SwitchedMethod();
		LoginMethodsElement element =
			new LoginMethodsElement(null, () -> LoginMethods.available(List.of(method)), () -> null);
		ReactControl control = (ReactControl) element.createControl(context());

		render(control);
		assertEquals("Unavailable method offered.", 0, buttons(control).size());

		method._available = true;
		render(control);
		assertEquals("Method available now is not offered.", 1, buttons(control).size());

		method._available = false;
		render(control);
		assertEquals("Method no longer available still offered.", 0, buttons(control).size());
	}

	/** Rendering a control again whose state has not changed keeps its buttons. */
	public void testUnchangedMethodsKeepTheirButtons() throws IOException {
		SwitchedMethod method = new SwitchedMethod();
		method._available = true;
		LoginMethodsElement element =
			new LoginMethodsElement(null, () -> LoginMethods.available(List.of(method)), () -> null);
		ReactControl control = (ReactControl) element.createControl(context());

		render(control);
		ReactControl button = buttons(control).get(0);
		render(control);
		assertSame(button, buttons(control).get(0));
	}

	/**
	 * The message of a failed external login is shown by the next rendering and stays shown when the
	 * control is written again without a new failure (as it is with every update of its parent); a
	 * new failure shows its message as new content.
	 */
	public void testFailureShown() throws IOException {
		SwitchedMethod method = new SwitchedMethod();
		method._available = true;
		List<ResKey> failures = new ArrayList<>();
		LoginMethodsElement element = new LoginMethodsElement(null, () -> LoginMethods.available(List.of(method)),
			() -> failures.isEmpty() ? null : failures.remove(0));
		ReactControl control = (ReactControl) element.createControl(context());

		render(control);
		assertTrue("Failure shown without one being reported.", alert(control).isHidden());

		failures.add(ResKey.text("Login failed."));
		render(control);
		ReactAlertControl alert = alert(control);
		assertFalse("Reported failure not shown.", alert.isHidden());
		assertEquals("Login failed.", alert.getMessage());
		assertEquals(Variant.ERROR, alert.getVariant());
		assertEquals("Buttons lost while showing the failure.", 1, buttons(control).size());

		int generation = alert.getGeneration();
		render(control);
		assertFalse("Failure hidden by writing the control again.", alert.isHidden());
		assertEquals("Failure shown as new content without a new failure.", generation, alert.getGeneration());

		failures.add(ResKey.text("Login failed again."));
		render(control);
		assertEquals("Login failed again.", alert.getMessage());
		assertTrue("New failure not shown as new content.", alert.getGeneration() > generation);
	}

	private static List<ReactControl> buttons(ReactControl control) {
		return control.displayedChildren().stream().filter(ReactButtonControl.class::isInstance).toList();
	}

	private static ReactAlertControl alert(ReactControl control) {
		return (ReactAlertControl) control.displayedChildren().stream()
			.filter(ReactAlertControl.class::isInstance).findFirst().orElseThrow();
	}

	private static ViewContext context() {
		return new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")));
	}

	private static void render(ReactControl control) throws IOException {
		control.write(new TagWriter());
	}

	/**
	 * Test suite providing the resources the button labels and the failure message are resolved
	 * with, and the theme of the icon of the failure message.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestLoginMethodsElement.class,
				ThreadContextManager.Module.INSTANCE, ThemeFactory.Module.INSTANCE, ResourcesModule.Module.INSTANCE));
	}

}
