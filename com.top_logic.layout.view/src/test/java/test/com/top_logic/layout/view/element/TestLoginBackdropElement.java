/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.json.JSON.ParseException;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactLoginBackdropControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.element.InsetElement;
import com.top_logic.layout.view.element.LoginBackdropElement;

/**
 * Tests what a {@link LoginBackdropElement} publishes to the client: the logo resources made
 * addressable within the application, the translated description of the logo, and the content.
 */
public class TestLoginBackdropElement extends TestCase {

	/** Context path of the test application, prefixed to a configured resource. */
	private static final String CONTEXT_PATH = "/app";

	/** Without any option, no logo is shown. */
	public void testNoOptions() {
		ReactControl backdrop = createBackdrop(config -> { /* Nothing. */ });

		assertNull(state(backdrop, ReactLoginBackdropControl.LOGO));
		assertNull(state(backdrop, ReactLoginBackdropControl.LOGO_DARK));
		assertNull(state(backdrop, ReactLoginBackdropControl.LOGO_ALT));
	}

	/** A logo resource is addressed within the application. */
	public void testLogoIsAddressedWithinApplication() {
		ReactControl backdrop = createBackdrop(config -> {
			set(config, LoginBackdropElement.Config.LOGO, "/images/a.svg");
			set(config, LoginBackdropElement.Config.LOGO_DARK, "/images/b.svg");
		});

		assertEquals(CONTEXT_PATH + "/images/a.svg", state(backdrop, ReactLoginBackdropControl.LOGO));
		assertEquals(CONTEXT_PATH + "/images/b.svg", state(backdrop, ReactLoginBackdropControl.LOGO_DARK));
	}

	/** The description of the logo is translated. */
	public void testLogoAltIsTranslated() {
		ReactControl backdrop = createBackdrop(config -> set(config, LoginBackdropElement.Config.LOGO_ALT,
			ResKey.text("Company logo")));

		assertEquals("Company logo", state(backdrop, ReactLoginBackdropControl.LOGO_ALT));
	}

	/** A child is the content shown in front of the backdrop. */
	public void testChildIsContent() {
		ReactControl backdrop = createBackdrop(
			config -> config.getChildren().add(TypedConfiguration.newConfigItem(InsetElement.Config.class)));

		assertNotNull("The child control is published.", state(backdrop, ReactLoginBackdropControl.CHILD));
	}

	private static ReactControl createBackdrop(java.util.function.Consumer<LoginBackdropElement.Config> setup) {
		LoginBackdropElement.Config config = TypedConfiguration.newConfigItem(LoginBackdropElement.Config.class);
		setup.accept(config);

		DefaultInstantiationContext instantiationContext =
			new DefaultInstantiationContext(TestLoginBackdropElement.class);
		LoginBackdropElement element = (LoginBackdropElement) instantiationContext.getInstance(config);

		ViewContext context = new DefaultViewContext(new DefaultReactContext(CONTEXT_PATH, "test",
			new SSEUpdateQueue(), new ReactWindowRegistry("test")));
		return (ReactControl) element.createControl(context);
	}

	private static void set(LoginBackdropElement.Config config, String property, Object value) {
		PropertyDescriptor descriptor = config.descriptor().getProperty(property);
		config.update(descriptor, value);
	}

	private static Object state(ReactControl control, String key) {
		String json = control.stateAsJSON();
		try {
			return ((Map<?, ?>) JSON.fromString(json)).get(key);
		} catch (ParseException ex) {
			throw new RuntimeException("Not a state object: " + json, ex);
		}
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module and the {@link ResourcesModule}, which
	 * translates the description of the logo.
	 */
	public static Test suite() throws ModuleException {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestLoginBackdropElement.class, TypeIndex.Module.INSTANCE,
				ResourcesModule.Module.INSTANCE));
	}

}
