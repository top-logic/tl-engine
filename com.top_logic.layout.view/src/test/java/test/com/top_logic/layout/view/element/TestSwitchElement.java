/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.Collections;
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
import com.top_logic.model.listen.ModelScope;
import com.top_logic.model.search.expr.config.SearchBuilder;
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
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestSwitchElement.class);
		ConfigurationReader reader = new ConfigurationReader(context, Collections.singletonMap(
			"switch", TypedConfiguration.getConfigurationDescriptor(SwitchElement.Config.class)));
		reader.setSource(new ClassRelativeBinaryContent(TestSwitchElement.class, "test-switch-without-default.xml"));
		SwitchElement.Config config = (SwitchElement.Config) reader.read();
		context.checkErrors();
		UIElement element = context.getInstance(config);
		context.checkErrors();

		// No object is observed, so the window reports no model scope - which spares the test the
		// knowledge base one is built from.
		ViewContext viewContext = new DefaultViewContext(new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test")) {
			@Override
			public ModelScope getModelScope() {
				return null;
			}
		});
		DefaultViewChannel value = new DefaultViewChannel("value");
		viewContext.registerChannel("value", value);

		ReactControl control = (ReactControl) element.createControl(viewContext);
		assertTrue("Without a matching case and without default, the switch is hidden.", control.isHidden());

		value.set("something");
		assertFalse("A matching case is shown.", control.isHidden());

		value.set(null);
		assertTrue("Without a matching case, the switch is hidden again.", control.isHidden());
	}

	/**
	 * Test suite with a knowledge base, in which the case tests are evaluated.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestSwitchElement.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE));
	}
}
