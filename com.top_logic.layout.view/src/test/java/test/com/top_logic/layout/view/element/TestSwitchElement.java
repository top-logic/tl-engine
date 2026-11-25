/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.element.FormElement;
import com.top_logic.layout.view.element.SwitchElement;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.model.TLObject;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.util.model.ModelService;

/**
 * Tests parsing and instantiation of {@link SwitchElement} and its {@code <case>} children.
 */
public class TestSwitchElement extends BasicTestCase {

	private ViewElement.Config parseTestView() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestSwitchElement.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestSwitchElement.class, "test-switch.view.xml");

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();
		return config;
	}

	/**
	 * Tests that a view XML with {@code <switch>}, {@code <case>} and {@code <default>} parses.
	 */
	public void testParseSwitchView() throws Exception {
		ViewElement.Config config = parseTestView();

		assertTrue("Content should be SwitchElement config",
			config.getContent() instanceof SwitchElement.Config);
		SwitchElement.Config switchConfig = (SwitchElement.Config) config.getContent();

		assertNotNull("Input should be set", switchConfig.getInput());
		assertEquals("Input channel name", "selectedPart", switchConfig.getInput().getChannel().getChannelName());

		assertEquals("Should have one case", 1, switchConfig.getCases().size());
		SwitchElement.CaseConfig caseConfig = switchConfig.getCases().get(0);
		assertNotNull("Case test should be parsed", caseConfig.getTest());
		assertEquals("Case should have one content element", 1, caseConfig.getContent().size());
		PolymorphicConfiguration<? extends UIElement> caseContent = caseConfig.getContent().get(0);
		assertTrue("Case content should be FormElement config", caseContent instanceof FormElement.Config);

		assertEquals("Should have one default content element", 1, switchConfig.getDefault().size());
		assertTrue("Default content should be FormElement config",
			switchConfig.getDefault().get(0) instanceof FormElement.Config);
	}

	/**
	 * Tests that the parsed configuration instantiates into a UIElement tree (compiles the case
	 * test expression via {@link com.top_logic.model.search.expr.query.QueryExecutor}).
	 */
	public void testInstantiateSwitchElement() throws Exception {
		ViewElement.Config config = parseTestView();

		DefaultInstantiationContext context = new DefaultInstantiationContext(TestSwitchElement.class);
		UIElement element = context.getInstance(config);
		context.checkErrors();
		assertNotNull("UIElement should be instantiated", element);
		assertTrue("Should be a ViewElement", element instanceof ViewElement);
	}

	/**
	 * Tests that a switch showing nothing - no case matches and there is no {@code <default>} - is
	 * hidden, so that it takes no place (and no gap) in its container, and is shown again as soon as
	 * a case matches.
	 */
	public void testHiddenWithoutContent() throws Exception {
		DefaultViewChannel value = new DefaultViewChannel("value");
		ReactControl control = createSwitch("test-switch-without-default.xml", value);
		assertTrue("Without a matching case and without default, the switch is hidden.", control.isHidden());

		value.set("something");
		assertFalse("A matching case is shown.", control.isHidden());

		value.set(null);
		assertTrue("Without a matching case, the switch is hidden again.", control.isHidden());
	}

	/**
	 * Tests that content a switch discards for a new input value does not hear that value anymore,
	 * although it listens to the same channel as the switch and is still pending in the running
	 * notification: a discarded form must not evaluate anything over a value it is not built for.
	 */
	public void testDiscardedContentMissesTheNewValue() throws Exception {
		DefaultViewChannel value = new DefaultViewChannel("value");
		ReactControl control = createSwitch("test-switch-rebuild-form.xml", value);

		TLObject first = TLModelUtil.findType("tl.model:TLClass");
		TLObject second = TLModelUtil.findType("tl.model:TLEnumeration");

		value.set(first);
		FormControl discarded = shownForm(control);
		assertSame(first, discarded.getCurrentObject());

		value.set(second);
		FormControl shown = shownForm(control);
		assertNotSame("The switch builds its content anew for the new value.", discarded, shown);
		assertSame(second, shown.getCurrentObject());
		assertTrue("The discarded form is disposed.", discarded.isDisposed());
		assertSame("The discarded form must not take the value it was discarded for.",
			first, discarded.getCurrentObject());
	}

	private static FormControl shownForm(ReactControl switchControl) {
		List<ReactControl> shown = switchControl.visibleChildren();
		assertEquals("The switch shows exactly its content.", 1, shown.size());
		assertTrue("The content of the switch must be a form, but is " + shown.get(0),
			shown.get(0) instanceof FormControl);
		return (FormControl) shown.get(0);
	}

	/**
	 * Creates the control of the {@code <switch>} declared in the given resource, reading its input
	 * from the given channel registered as {@code value}.
	 */
	private static ReactControl createSwitch(String resource, DefaultViewChannel value) throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestSwitchElement.class);
		ConfigurationReader reader = new ConfigurationReader(context, Collections.singletonMap(
			"switch", TypedConfiguration.getConfigurationDescriptor(SwitchElement.Config.class)));
		reader.setSource(new ClassRelativeBinaryContent(TestSwitchElement.class, resource));
		SwitchElement.Config config = (SwitchElement.Config) reader.read();
		context.checkErrors();
		UIElement element = context.getInstance(config);
		context.checkErrors();

		// The controls are never attached and observe no object, so the window reports no model
		// scope - which spares the test the knowledge base one is built from.
		ViewContext viewContext = new DefaultViewContext(new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test")) {
			@Override
			public ModelScope getModelScope() {
				return null;
			}
		});
		viewContext.registerChannel("value", value);

		return (ReactControl) element.createControl(viewContext);
	}

	/**
	 * Test suite with a knowledge base, in which the case tests are evaluated, and the services a form
	 * resolves its commands with.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestSwitchElement.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE,
				CommandGroupRegistry.Module.INSTANCE));
	}
}
